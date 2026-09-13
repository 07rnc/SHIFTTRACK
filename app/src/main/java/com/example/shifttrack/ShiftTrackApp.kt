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

        // Initialize Firebase if available
        try {
            FirebaseApp.initializeApp(this)
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.isNullOrBlank()) {
                    val token = task.result
                    sessionManager.saveDeviceToken(token)
                }
            }
        } catch (e: Exception) {
            // Handled gracefully in mock / local environments
        }
    }

    companion object {
        lateinit var instance: ShiftTrackApp
            private set
    }
}
