package com.example.shifttrack.notifications

import android.util.Log
import com.example.shifttrack.ShiftTrackApp
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShiftTrackFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService", "FCM Device Token Refreshed: $token")
        val app = application as? ShiftTrackApp ?: return
        app.sessionManager.saveDeviceToken(token)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.notificationRepository.registerDeviceToken(token)
            } catch (e: Exception) {
                Log.e("FCMService", "Error syncing device token: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "ShiftTrack Alert"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["message"]
            ?: "New update received"

        val route = remoteMessage.data["actionRoute"] ?: remoteMessage.data["route"]

        NotificationHelper.showNotification(applicationContext, title, body, route)
    }
}
