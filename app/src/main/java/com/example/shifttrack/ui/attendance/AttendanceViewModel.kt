package com.example.shifttrack.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shifttrack.data.model.AttendanceDto
import com.example.shifttrack.data.model.AttendanceStateResponse
import com.example.shifttrack.data.repository.AttendanceRepository
import com.example.shifttrack.location.LocationClient
import com.example.shifttrack.location.LocationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AttendanceActionState {
    object Idle : AttendanceActionState()
    object AcquiringLocation : AttendanceActionState()
    object Submitting : AttendanceActionState()
    data class Success(val message: String, val record: AttendanceDto) : AttendanceActionState()
    data class OutsideGeofence(val message: String) : AttendanceActionState()
    data class Error(val message: String) : AttendanceActionState()
}

class AttendanceViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val locationClient: LocationClient
) : ViewModel() {

    private val _actionState = MutableStateFlow<AttendanceActionState>(AttendanceActionState.Idle)
    val actionState: StateFlow<AttendanceActionState> = _actionState.asStateFlow()

    private val _history = MutableStateFlow<List<AttendanceDto>>(emptyList())
    val history: StateFlow<List<AttendanceDto>> = _history.asStateFlow()

    private val _historyLoading = MutableStateFlow(false)
    val historyLoading: StateFlow<Boolean> = _historyLoading.asStateFlow()

    private val _currentState = MutableStateFlow<AttendanceStateResponse?>(null)
    val currentState: StateFlow<AttendanceStateResponse?> = _currentState.asStateFlow()

    init {
        loadAttendanceState()
        loadHistory()
    }

    fun loadAttendanceState() {
        viewModelScope.launch {
            attendanceRepository.getAttendanceState().onSuccess {
                _currentState.value = it
            }
        }
    }

    fun loadHistory() {
        _historyLoading.value = true
        viewModelScope.launch {
            attendanceRepository.getAttendanceHistory()
                .onSuccess {
                    _history.value = it
                    _historyLoading.value = false
                }
                .onFailure {
                    _historyLoading.value = false
                }
        }
    }

    fun performGpsClockIn() {
        if (_actionState.value is AttendanceActionState.Submitting ||
            _actionState.value is AttendanceActionState.AcquiringLocation) return

        _actionState.value = AttendanceActionState.AcquiringLocation
        viewModelScope.launch {
            when (val locResult = locationClient.getCurrentLocation()) {
                is LocationResult.Success -> {
                    _actionState.value = AttendanceActionState.Submitting
                    val res = attendanceRepository.clockInGps(
                        locResult.latitude,
                        locResult.longitude,
                        locResult.accuracy
                    )
                    res.onSuccess { record ->
                        _actionState.value = AttendanceActionState.Success(
                            "Authoritative Clock-In Confirmed by Server: Shift marked at " + (record.clockInTime ?: ""),
                            record
                        )
                        loadAttendanceState()
                        loadHistory()
                    }.onFailure { err ->
                        val msg = err.message ?: "Clock-in rejected by server"
                        if (msg.contains("geofence", ignoreCase = true) || msg.contains("outside", ignoreCase = true)) {
                            _actionState.value = AttendanceActionState.OutsideGeofence(msg)
                        } else {
                            _actionState.value = AttendanceActionState.Error(msg)
                        }
                    }
                }
                is LocationResult.GpsDisabled -> {
                    _actionState.value = AttendanceActionState.Error("Device GPS/Location services are disabled. Please turn on Location in Settings.")
                }
                is LocationResult.NoPermission -> {
                    _actionState.value = AttendanceActionState.Error("Location permission is required for GPS attendance.")
                }
                is LocationResult.Error -> {
                    _actionState.value = AttendanceActionState.Error(locResult.message)
                }
            }
        }
    }

    fun performQrClockIn(qrCode: String) {
        if (_actionState.value is AttendanceActionState.Submitting) return

        _actionState.value = AttendanceActionState.Submitting
        viewModelScope.launch {
            val res = attendanceRepository.clockInQr(qrCode)
            res.onSuccess { record ->
                _actionState.value = AttendanceActionState.Success(
                    "QR Clock-In Verified by Server: Clocked in at " + (record.clockInTime ?: ""),
                    record
                )
                loadAttendanceState()
                loadHistory()
            }.onFailure { err ->
                _actionState.value = AttendanceActionState.Error(err.message ?: "QR code validation failed")
            }
        }
    }

    fun performClockOut() {
        if (_actionState.value is AttendanceActionState.Submitting) return

        _actionState.value = AttendanceActionState.Submitting
        viewModelScope.launch {
            val res = attendanceRepository.clockOut(null, null)
            res.onSuccess { record ->
                _actionState.value = AttendanceActionState.Success(
                    "Clock-out confirmed: Completed shift at " + (record.clockOutTime ?: ""),
                    record
                )
                loadAttendanceState()
                loadHistory()
            }.onFailure { err ->
                _actionState.value = AttendanceActionState.Error(err.message ?: "Failed to clock out")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = AttendanceActionState.Idle
    }
}
