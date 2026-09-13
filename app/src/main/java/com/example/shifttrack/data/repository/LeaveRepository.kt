package com.example.shifttrack.data.repository

import com.example.shifttrack.data.api.ApiClient
import com.example.shifttrack.data.api.AppConfig
import com.example.shifttrack.data.model.LeaveApplicationDto
import com.example.shifttrack.data.model.LeaveRequestDto

interface LeaveRepository {
    suspend fun submitLeaveRequest(request: LeaveRequestDto): Result<LeaveApplicationDto>
    suspend fun getLeaveHistory(): Result<List<LeaveApplicationDto>>
}

class LeaveRepositoryImpl(
    private val apiClient: ApiClient,
    private val mockDataStore: MockDataStore
) : LeaveRepository {

    override suspend fun submitLeaveRequest(request: LeaveRequestDto): Result<LeaveApplicationDto> {
        if (AppConfig.USE_MOCK_DATA) {
            return mockDataStore.submitLeave(request)
        }

        return try {
            val response = apiClient.getService().submitLeave(request)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val error = response.body()?.error ?: response.body()?.message ?: "Failed to submit leave request"
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLeaveHistory(): Result<List<LeaveApplicationDto>> {
        if (AppConfig.USE_MOCK_DATA) {
            return mockDataStore.getLeaveHistory()
        }

        return try {
            val response = apiClient.getService().getLeaveHistory()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to fetch leave history"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
