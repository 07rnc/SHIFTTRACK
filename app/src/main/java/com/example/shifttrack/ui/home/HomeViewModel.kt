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
    private val webSocketManager: WebSocketManager,
    private val sessionManager: com.example.shifttrack.data.local.SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Do NOT call loadData() here — HomeScreen's LifecycleEventEffect(ON_RESUME)
        // triggers loadData() immediately when the screen enters composition.
        // Calling it here would cause a duplicate API call on every first visit.
        listenToLiveEvents()
        if (sessionManager != null && !com.example.shifttrack.data.api.AppConfig.USE_MOCK_DATA && sessionManager.hasValidToken()) {
            webSocketManager.connect(sessionManager.getServerUrl(), sessionManager.getAccessToken())
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
    }

    fun loadData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            }

            var user = authRepository.currentUser.value
            if (user == null) {
                user = authRepository.getProfile().getOrNull()
            }
            val shiftRes = shiftRepository.getCurrentShift()
            val attRes = attendanceRepository.getAttendanceState()
            val notifRes = notificationRepository.getNotifications()

            val shift = shiftRes.getOrNull()
            val attState = attRes.getOrNull()
            val unreadCount = notifRes.getOrNull()?.count { !it.isRead } ?: 0

            val error = if (attState == null && attRes.isFailure) {
                attRes.exceptionOrNull()?.message ?: "Unable to load attendance status"
            } else if (shift == null && shiftRes.isFailure) {
                shiftRes.exceptionOrNull()?.message
            } else null

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                user = user,
                currentShift = shift,
                attendanceState = attState,
                unreadNotifsCount = unreadCount,
                errorMessage = error
            )
        }
    }

    private fun listenToLiveEvents() {
        viewModelScope.launch {
            attendanceRepository.currentAttendanceState.collect { attState ->
                if (attState != null) {
                    _uiState.value = _uiState.value.copy(attendanceState = attState)
                }
            }
        }
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
