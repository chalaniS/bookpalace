package com.bookpalace.app.repositories

import com.bookpalace.app.model.User
import com.bookpalace.app.model.UserRole
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class representing the content and recipients of a notification.
 * @property title The title of the notification.
 * @property message The body/content of the notification message.
 * @property recipients The list of users who should receive the notification.
 */
data class NotificationPayload(
    val title: String,
    val message: String,
    val recipients: List<User>
)

/**
 * Sealed class representing the result of a notification sending operation.
 */
sealed class NotificationResult {
    /** Indicates all notifications were sent successfully. */
    data class Success(val sentCount: Int) : NotificationResult()
    /** Indicates some notifications were sent successfully, while others failed. */
    data class PartialSuccess(val sentCount: Int, val failedCount: Int) : NotificationResult()
    /** Indicates the entire operation failed. */
    data class Failure(val error: String) : NotificationResult()
}

/**
 * Repository responsible for managing notification operations, including fetching recipients,
 * retrieving FCM tokens, sending notifications via Cloud Functions, and logging results.
 */
@Singleton
class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
    private val realtimeDb: FirebaseDatabase
) {

    /**
     * Fetches all active users from the Realtime Database who are eligible to receive notifications.
     * This includes both Students and Librarians who have their 'isActive' flag set to true.
     * 
     * @return A Result containing a list of [User] objects on success.
     */
    suspend fun fetchAllRecipients(): Result<List<User>> = runCatching {
        val snapshot = realtimeDb.getReference("users")
            .get()
            .await()

        val users = mutableListOf<User>()
        for (child in snapshot.children) {
            val user = child.getValue(User::class.java)
            // Ensure both STUDENTS and LIBRARIANS are included
            if (user != null && user.isActive) {
                users.add(user.copy(id = child.key ?: ""))
            }
        }
        println("NotificationRepository: Fetched ${users.size} users: $users")
        users
    }

    /**
     * Orchestrates the process of sending a push notification.
     * It first retrieves FCM tokens for the recipients, then calls a Firebase Cloud Function
     * to perform the actual delivery, and finally logs the result to Firestore.
     * 
     * @param payload The notification details and recipient list.
     * @return A [NotificationResult] indicating success, partial success, or failure.
     */
    suspend fun sendNotification(payload: NotificationPayload): NotificationResult {
        return try {
            // 1. Collect FCM tokens for selected users
            val tokens = fetchFcmTokens(payload.recipients)
            if (tokens.isEmpty()) {
                return NotificationResult.Failure("No valid FCM tokens found for selected recipients.")
            }

            // 2. Call Firebase Cloud Function "sendNotification"
            // This function handles the actual communication with FCM backend.
            val data = hashMapOf(
                "title" to payload.title,
                "message" to payload.message,
                "fcmtokens" to tokens
            )

            val result = functions
                .getHttpsCallable("sendNotification")
                .call(data)
                .await()

            // 3. Parse response from Cloud Function
            @Suppress("UNCHECKED_CAST")
            val responseData = result.getData() as? Map<String, Any>
            val sentCount = (responseData?.get("successCount") as? Long)?.toInt() ?: tokens.size
            val failureCount = (responseData?.get("failureCount") as? Long)?.toInt() ?: 0

            // 4. Save notification log to Firestore for audit purposes
            saveNotificationLog(payload, tokens.size, sentCount, failureCount)

            if (failureCount == 0) {
                NotificationResult.Success(sentCount)
            } else {
                NotificationResult.PartialSuccess(sentCount, failureCount)
            }
        } catch (e: Exception) {
            // Handle cases where the function might not be found or connection fails
            NotificationResult.Failure(e.message ?: "Function not found or execution failed.")
        }
    }

    /**
     * Retrieves FCM tokens from Firestore for a given list of users.
     * Tokens are fetched in chunks to stay within Firestore query limits.
     * 
     * @param users The list of users to get tokens for.
     * @return A list of valid FCM token strings.
     */
    private suspend fun fetchFcmTokens(users: List<User>): List<String> {
        if (users.isEmpty()) return emptyList()

        val userIds = users.map { it.id }
        val tokens = mutableListOf<String>()

        // Using chunked(10) to accommodate Firestore's 'whereIn' limitation (max 30 usually, 10 is safe)
        userIds.chunked(10).forEach { chunk ->
            val snapshot = firestore.collection("user_tokens")
                .whereIn("userId", chunk)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.getString("fcmToken")?.let { token ->
                    if (token.isNotBlank()) tokens.add(token)
                }
            }
        }
        println("NotificationRepository: Fetched ${tokens.size} FCM tokens: $tokens")
        return tokens
    }

    /**
     * Persists a record of a sent notification into Firestore.
     * This allows admins to review notification history and delivery success rates.
     * 
     * @param payload The original notification payload.
     * @param totalRecipients The number of tokens we attempted to send to.
     * @param sentCount Number of successfully delivered notifications.
     * @param failedCount Number of failed deliveries.
     */
    private suspend fun saveNotificationLog(
        payload: NotificationPayload,
        totalRecipients: Int,
        sentCount: Int,
        failedCount: Int
    ) {
        val log = hashMapOf(
            "title" to payload.title,
            "message" to payload.message,
            "recipientIds" to payload.recipients.map { it.id },
            "totalRecipients" to totalRecipients,
            "sentCount" to sentCount,
            "failedCount" to failedCount,
            "sentAt" to Timestamp.now()
        )
        firestore.collection("notification_logs")
            .add(log)
            .await()
    }
}
