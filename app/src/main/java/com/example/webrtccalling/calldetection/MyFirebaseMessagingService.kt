package com.example.webrtccalling.calldetection

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.webrtccalling.R
import com.example.webrtccalling.ui.CallRingingActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FirebaseMessagingService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if the message contains data
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            val type = remoteMessage.data["type"] ?: ""
            val sender = remoteMessage.data["sender"] ?: ""
            val isVideoCall = remoteMessage.data["isVideoCall"]?.toBoolean() ?: false

            if (type == "incoming_call") {
                val userRef = FirebaseDatabase.getInstance().getReference().child("User").child(sender).child("name")

                userRef.get().addOnSuccessListener { dataSnapshot ->
                    val sendername = dataSnapshot.getValue(String::class.java) // Assuming the value is a String
                    if (sendername != null) {
                        showCallNotification(sendername, isVideoCall)
                        // startCallRingingActivity(sender, isVideoCall) // Uncomment if needed
                    } else {
                        // If sender name is not found, use their UID
                        showCallNotification(sender, isVideoCall)
                    }
                }.addOnFailureListener { exception ->
                    // If failed to retrieve sender name, use their UID
                    Toast.makeText(this, "Failed to retrieve sender's name: ${exception.message}", Toast.LENGTH_SHORT).show()
                    showCallNotification(sender, isVideoCall)
                }
            }

        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New token: $token")
        // You can send the token to your server here
    }

    private fun showCallNotification(sender: String, isVideoCall: Boolean) {
        val notificationTitle = if (isVideoCall) "Incoming Video Call" else "Incoming Audio Call"
        val notificationBody = "Call from $sender"

        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "call_notifications"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Call Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_call)
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(ringtoneUri)

        notificationManager.notify(1, notificationBuilder.build())
    }

    private fun startCallRingingActivity(sender: String, isVideoCall: Boolean) {
        val intent = Intent(this, CallRingingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("sender", sender)
            putExtra("isVideoCall", isVideoCall)
        }
        startActivity(intent)

    }
}
