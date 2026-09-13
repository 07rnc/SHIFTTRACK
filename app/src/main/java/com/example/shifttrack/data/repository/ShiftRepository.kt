package com.example.shifttrack.data.repository

import com.example.shifttrack.data.api.ApiClient
import com.example.shifttrack.data.api.AppConfig
import com.example.shifttrack.data.model.ShiftDto

interface ShiftRepository {
    suspend fun getCurrentShift(): Result<ShiftDto?>
}

class ShiftRepositoryImpl(
    private val apiClient: ApiClient,
    private val mockDataStore: MockDataStore
) : ShiftRepository {

    override suspend fun getCurrentShift(): Result<ShiftDto?> {
        if (AppConfig.USE_MOCK_DATA) {
            return mockDataStore.getCurrentShift()
        }

        return try {
            val response = apiClient.getService().getCurrentShift()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to fetch shift"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
