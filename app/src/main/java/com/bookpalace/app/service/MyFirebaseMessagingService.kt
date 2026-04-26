package com.bookpalace.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bookpalace.app.R
import com.bookpalace.app.ui.DashboardActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token generated: $token")
        saveTokenToFirestore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "From: ${remoteMessage.from}")

        // Check if message contains a notification payload
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
        val message = remoteMessage.notification?.body ?: remoteMessage.data["message"]

        if (title != null && message != null) {
            showNotification(title, message)
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "book_palace_notifications"
        val notificationId = System.currentTimeMillis().toInt()

        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo) // Updated to use the app logo
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Set to HIGH for better visibility
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message)) // Support for long messages

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Book Palace Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "General notifications for Book Palace"
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, builder.build())
    }

    private fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        val tokenData = hashMapOf(
            "userId" to userId,
            "fcmToken" to token,
            "lastUpdated" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("user_tokens")
            .document(userId)
            .set(tokenData)
            .addOnSuccessListener {
                Log.d("FCM", "Token successfully saved for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.w("FCM", "Error saving token", e)
            }
    }
}
