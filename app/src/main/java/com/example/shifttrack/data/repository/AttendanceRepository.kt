package com.example.shifttrack.data.repository

import com.example.shifttrack.data.api.ApiClient
import com.example.shifttrack.data.api.AppConfig
import com.example.shifttrack.data.model.*

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AttendanceRepository {
    val currentAttendanceState: StateFlow<AttendanceStateResponse?>
    suspend fun getAttendanceState(): Result<AttendanceStateResponse>
    suspend fun clockInGps(latitude: Double, longitude: Double, accuracy: Float): Result<AttendanceDto>
    suspend fun clockInQr(qrData: String): Result<AttendanceDto>
    suspend fun clockOut(latitude: Double?, longitude: Double?): Result<AttendanceDto>
    suspend fun getAttendanceHistory(): Result<List<AttendanceDto>>
}

class AttendanceRepositoryImpl(
    private val apiClient: ApiClient,
    private val mockDataStore: MockDataStore
) : AttendanceRepository {

    private val _currentAttendanceState = MutableStateFlow<AttendanceStateResponse?>(null)
    override val currentAttendanceState: StateFlow<AttendanceStateResponse?> = _currentAttendanceState.asStateFlow()

    override suspend fun getAttendanceState(): Result<AttendanceStateResponse> {
        val result = if (AppConfig.USE_MOCK_DATA) {
            mockDataStore.getAttendanceState()
        } else {
            try {
                val response = apiClient.getService().getAttendanceState()
                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    Result.success(response.body()!!.data!!)
                } else {
                    Result.failure(Exception(response.body()?.error ?: "Failed to get attendance status"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        result.onSuccess { state ->
            _currentAttendanceState.value = state
        }
        return result
    }

    override suspend fun clockInGps(latitude: Double, longitude: Double, accuracy: Float): Result<AttendanceDto> {
        val result = if (AppConfig.USE_MOCK_DATA) {
            mockDataStore.clockInGps(latitude, longitude, accuracy)
        } else {
            try {
                val request = ClockInRequest(
                    method = AttendanceMethod.GPS.name,
                    latitude = latitude,
                    longitude = longitude,
                    accuracy = accuracy
                )
                val response = apiClient.getService().clockIn(request)
                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    Result.success(response.body()!!.data!!)
                } else {
                    val errorMsg = response.body()?.error ?: response.body()?.message ?: "Clock-in rejected by server"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        result.onSuccess { record ->
            _currentAttendanceState.value = AttendanceStateResponse(
                state = ClockState.CLOCKED_IN.name,
                currentRecord = record,
                shift = _currentAttendanceState.value?.shift
            )
        }
        return result
    }

    override suspend fun clockInQr(qrData: String): Result<AttendanceDto> {
        val result = if (AppConfig.USE_MOCK_DATA) {
            mockDataStore.clockInQr(qrData)
        } else {
            try {
                val request = ClockInRequest(
                    method = AttendanceMethod.QR.name,
                    qrData = qrData
                )
                val response = apiClient.getService().clockIn(request)
                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    Result.success(response.body()!!.data!!)
                } else {
                    val errorMsg = response.body()?.error ?: response.body()?.message ?: "QR verification failed"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        result.onSuccess { record ->
            _currentAttendanceState.value = AttendanceStateResponse(
                state = ClockState.CLOCKED_IN.name,
                currentRecord = record,
                shift = _currentAttendanceState.value?.shift
            )
        }
        return result
    }

    override suspend fun clockOut(latitude: Double?, longitude: Double?): Result<AttendanceDto> {
        val result = if (AppConfig.USE_MOCK_DATA) {
            mockDataStore.clockOut(latitude, longitude)
        } else {
            try {
                val request = ClockOutRequest(latitude = latitude, longitude = longitude)
                val response = apiClient.getService().clockOut(request)
                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    Result.success(response.body()!!.data!!)
                } else {
                    val errorMsg = response.body()?.error ?: response.body()?.message ?: "Clock-out failed"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        result.onSuccess { record ->
            _currentAttendanceState.value = AttendanceStateResponse(
                state = ClockState.CLOCKED_OUT.name,
                currentRecord = record,
                shift = _currentAttendanceState.value?.shift
            )
        }
        return result
    }

    override suspend fun getAttendanceHistory(): Result<List<AttendanceDto>> {
        if (AppConfig.USE_MOCK_DATA) {
            return mockDataStore.getAttendanceHistory()
        }

        return try {
            val response = apiClient.getService().getAttendanceHistory()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to fetch attendance history"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
