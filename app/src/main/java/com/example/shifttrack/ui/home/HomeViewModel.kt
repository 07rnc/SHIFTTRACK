package com.example.shifttrack.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shifttrack.data.model.*
import com.example.shifttrack.data.repository.*
import com.example.shifttrack.websocket.WebSocketEvent
import com.example.shifttrack.websocket.WebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val user: UserDto? = null,
    val currentShift: ShiftDto? = null,
    val attendanceState: AttendanceStateResponse? = null,
    val unreadNotifsCount: Int = 0,
    val errorMessage: String? = null,
    val actionLoading: Boolean = false,
    val actionFeedback: String? = null
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val shiftRepository: ShiftRepository,
    private val attendanceRepository: AttendanceRepository,
    private val notificationRepository: NotificationRepository,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        listenToLiveEvents()
    }

    fun loadData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            }

            val user = authRepository.currentUser.value
            val shiftRes = shiftRepository.getCurrentShift()
            val attRes = attendanceRepository.getAttendanceState()
            val notifRes = notificationRepository.getNotifications()

            val shift = shiftRes.getOrNull()
            val attState = attRes.getOrNull()
            val unreadCount = notifRes.getOrNull()?.count { !it.isRead } ?: 0

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                user = user,
                currentShift = shift,
                attendanceState = attState,
                unreadNotifsCount = unreadCount
            )
        }
    }

    private fun listenToLiveEvents() {
        viewModelScope.launch {
            webSocketManager.events.collect { event ->
                when (event) {
                    is WebSocketEvent.AttendanceUpdate,
                    is WebSocketEvent.ShiftUpdate -> {
                        loadData(isRefresh = true)
                    }
                    else -> {}
                }
            }
        }
    }

    fun quickClockOut() {
        if (_uiState.value.actionLoading) return

        _uiState.value = _uiState.value.copy(actionLoading = true, actionFeedback = null)
        viewModelScope.launch {
            val result = attendanceRepository.clockOut(null, null)
            result.onSuccess { record ->
                _uiState.value = _uiState.value.copy(
                    actionLoading = false,
                    actionFeedback = "Clocked out successfully at " + (record.clockOutTime ?: "")
                )
                loadData(isRefresh = true)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    actionLoading = false,
                    errorMessage = err.message ?: "Failed to clock out"
                )
            }
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(actionFeedback = null, errorMessage = null)
    }
}
