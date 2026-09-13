package com.example.shifttrack.data.repository

import com.example.shifttrack.data.api.ApiClient
import com.example.shifttrack.data.api.AppConfig
import com.example.shifttrack.data.model.AppNotificationDto
import com.example.shifttrack.data.model.DeviceTokenRequest

interface NotificationRepository {
    suspend fun getNotifications(): Result<List<AppNotificationDto>>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun registerDeviceToken(token: String): Result<Unit>
}

class NotificationRepositoryImpl(
    private val apiClient: ApiClient,
    private val mockDataStore: MockDataStore
) : NotificationRepository {

    override suspend fun getNotifications(): Result<List<AppNotificationDto>> {
        if (AppConfig.USE_MOCK_DATA) {
            return mockDataStore.getNotifications()
        }

        return try {
            val response = apiClient.getService().getNotifications()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to fetch notifications"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        if (AppConfig.USE_MOCK_DATA) {
            return mockDataStore.markNotificationRead(notificationId)
        }

        return try {
            val response = apiClient.getService().markNotificationRead(notificationId)
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Failed to update status"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerDeviceToken(token: String): Result<Unit> {
        if (AppConfig.USE_MOCK_DATA) {
            return mockDataStore.registerDeviceToken(token)
        }

        return try {
            val response = apiClient.getService().registerDeviceToken(DeviceTokenRequest(token))
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Device registration failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
