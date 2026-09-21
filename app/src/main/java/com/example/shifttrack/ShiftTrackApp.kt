package com.example.shifttrack

import android.app.Application
import com.example.shifttrack.data.api.ApiClient
import com.example.shifttrack.data.local.SessionManager
import com.example.shifttrack.data.repository.*
import com.example.shifttrack.notifications.NotificationHelper
import com.example.shifttrack.websocket.WebSocketManager
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class ShiftTrackApp : Application() {

    lateinit var sessionManager: SessionManager
        private set

    lateinit var apiClient: ApiClient
        private set

    lateinit var mockDataStore: MockDataStore
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var shiftRepository: ShiftRepository
        private set

    lateinit var attendanceRepository: AttendanceRepository
        private set

    lateinit var leaveRepository: LeaveRepository
        private set

    lateinit var notificationRepository: NotificationRepository
        private set

    lateinit var webSocketManager: WebSocketManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        sessionManager = SessionManager(this)
        apiClient = ApiClient(sessionManager) {
            sessionManager.clearSession()
        }

        mockDataStore = MockDataStore(sessionManager)

        authRepository = AuthRepositoryImpl(apiClient, sessionManager, mockDataStore)
        shiftRepository = ShiftRepositoryImpl(apiClient, mockDataStore)
        attendanceRepository = AttendanceRepositoryImpl(apiClient, mockDataStore)
        leaveRepository = LeaveRepositoryImpl(apiClient, mockDataStore)
        notificationRepository = NotificationRepositoryImpl(apiClient, mockDataStore)

        webSocketManager = WebSocketManager()

        // Create notification channels
        NotificationHelper.createNotificationChannel(this)

        // Initialize Firebase if configured; fail gracefully if google-services.json is absent
        try {
            val app = FirebaseApp.initializeApp(this)
            if (app != null) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful && !task.result.isNullOrBlank()) {
                        val token = task.result
                        sessionManager.saveDeviceToken(token)
                    } else {
                        android.util.Log.i("ShiftTrackApp", "FCM token registration unavailable: ${task.exception?.message ?: "not returned"}")
                    }
                }
            } else {
                android.util.Log.w("ShiftTrackApp", "Firebase not initialized: Default FirebaseApp options not found (google-services.json is not present). Push notifications will be disabled; local and in-app alerts remain active.")
            }
        } catch (e: IllegalStateException) {
            android.util.Log.w("ShiftTrackApp", "Firebase configuration required for push notifications: google-services.json is missing. The app is running normally with local in-app alerts.")
        } catch (e: Exception) {
            android.util.Log.w("ShiftTrackApp", "FCM initialization skipped: ${e.message ?: "Unknown error"}. App continues in standalone notification mode.")
        }
    }

    companion object {
        lateinit var instance: ShiftTrackApp
            private set
    }
}
