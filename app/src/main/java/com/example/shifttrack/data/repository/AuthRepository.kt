package com.example.shifttrack.data.repository

import com.example.shifttrack.data.api.ApiClient
import com.example.shifttrack.data.api.AppConfig
import com.example.shifttrack.data.local.SessionManager
import com.example.shifttrack.data.model.LoginRequest
import com.example.shifttrack.data.model.UserDto
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val isLoggedIn: StateFlow<Boolean>
    val currentUser: StateFlow<UserDto?>
    suspend fun login(identifier: String, password: String): Result<UserDto>
    suspend fun logout(): Result<Unit>
    suspend fun getProfile(): Result<UserDto>
    suspend fun register(fullName: String, email: String, password: String): Result<Unit>
}

class AuthRepositoryImpl(
    private val apiClient: ApiClient,
    private val sessionManager: SessionManager,
    private val mockDataStore: MockDataStore
) : AuthRepository {

    override val isLoggedIn: StateFlow<Boolean> = sessionManager.isLoggedIn
    override val currentUser: StateFlow<UserDto?> = sessionManager.currentUser

    override suspend fun login(identifier: String, password: String): Result<UserDto> {
        if (AppConfig.USE_MOCK_DATA) {
            return mockDataStore.login(identifier, password)
        }

        return try {
            val response = apiClient.getService().login(
                LoginRequest(identifier = identifier, password = password, deviceToken = sessionManager.getDeviceToken())
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    sessionManager.saveSession(data.accessToken, data.refreshToken, data.user)
                    Result.success(data.user)
                } else {
                    Result.failure(Exception("Invalid server response"))
                }
            } else {
                val errorMsg = response.body()?.error ?: response.body()?.message ?: "Login failed (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        if (AppConfig.USE_MOCK_DATA) {
            return mockDataStore.logout()
        }

        return try {
            apiClient.getService().logout()
            sessionManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            // Even if network fails, always clear local session
            sessionManager.clearSession()
            Result.success(Unit)
        }
    }

    override suspend fun getProfile(): Result<UserDto> {
        if (AppConfig.USE_MOCK_DATA) {
            return mockDataStore.getProfile()
        }

        return try {
            val response = apiClient.getService().getProfile()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val user = response.body()!!.data!!
                sessionManager.saveSession(sessionManager.getAccessToken() ?: "", sessionManager.getRefreshToken(), user)
                Result.success(user)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(fullName: String, email: String, password: String): Result<Unit> {
        if (AppConfig.USE_MOCK_DATA) {
            return mockDataStore.register(fullName, email, password)
        }
        // Backend registration endpoint not yet implemented.
        // Return a clear error so the UI can surface a helpful message.
        return Result.failure(
            Exception("Self-registration is not yet available. Please contact your administrator to create your account.")
        )
    }
}
