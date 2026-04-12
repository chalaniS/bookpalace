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

data class NotificationPayload(
    val title: String,
    val message: String,
    val recipients: List<User>
)

sealed class NotificationResult {
    data class Success(val sentCount: Int) : NotificationResult()
    data class PartialSuccess(val sentCount: Int, val failedCount: Int) : NotificationResult()
    data class Failure(val error: String) : NotificationResult()
}

@Singleton
class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
    private val realtimeDb: FirebaseDatabase
) {

    /**
     * Fetch all active students from Realtime Database (matching SignupActivity).
     */
    suspend fun fetchAllStudents(): Result<List<User>> = runCatching {
        val snapshot = realtimeDb.getReference("users")
            .get()
            .await()

        val students = mutableListOf<User>()
        for (child in snapshot.children) {
            val user = child.getValue(User::class.java)
            if (user != null && user.role == UserRole.STUDENT && user.isActive) {
                students.add(user.copy(id = child.key ?: ""))
            }
        }
        students
    }

    /**
     * Send FCM push notification to a list of students via Firebase Cloud Functions.
     */
    suspend fun sendNotification(payload: NotificationPayload): NotificationResult {
        return try {
            // 1. Collect FCM tokens for selected students
            val tokens = fetchFcmTokens(payload.recipients)
            if (tokens.isEmpty()) {
                return NotificationResult.Failure("No valid FCM tokens found for selected students.")
            }

            // 2. Call Firebase Cloud Function
            val data = hashMapOf(
                "title" to payload.title,
                "message" to payload.message,
                "tokens" to tokens
            )

            val result = functions
                .getHttpsCallable("sendNotification")
                .call(data)
                .await()

            // 3. Parse response
            @Suppress("UNCHECKED_CAST")
            val responseData = result.getData() as? Map<String, Any>
            val sentCount = (responseData?.get("successCount") as? Long)?.toInt() ?: tokens.size
            val failureCount = (responseData?.get("failureCount") as? Long)?.toInt() ?: 0

            // 4. Save notification log to Firestore
            saveNotificationLog(payload, tokens.size, sentCount, failureCount)

            if (failureCount == 0) {
                NotificationResult.Success(sentCount)
            } else {
                NotificationResult.PartialSuccess(sentCount, failureCount)
            }
        } catch (e: Exception) {
            NotificationResult.Failure(e.message ?: "Unknown error occurred.")
        }
    }

    /**
     * Retrieve FCM tokens for the given users from Firestore "user_tokens" collection.
     */
    private suspend fun fetchFcmTokens(users: List<User>): List<String> {
        if (users.isEmpty()) return emptyList()

        val userIds = users.map { it.id }
        val tokens = mutableListOf<String>()

        // Firestore "in" query supports max 10 items — chunk if needed
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

        return tokens
    }

    /**
     * Persist a notification record to Firestore for audit/history.
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
