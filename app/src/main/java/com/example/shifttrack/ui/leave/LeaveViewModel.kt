package com.example.shifttrack.ui.leave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shifttrack.data.model.LeaveApplicationDto
import com.example.shifttrack.data.model.LeaveRequestDto
import com.example.shifttrack.data.repository.LeaveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LeaveSubmitState {
    object Idle : LeaveSubmitState()
    object Submitting : LeaveSubmitState()
    data class Success(val application: LeaveApplicationDto) : LeaveSubmitState()
    data class Error(val message: String) : LeaveSubmitState()
}

class LeaveViewModel(
    private val leaveRepository: LeaveRepository
) : ViewModel() {

    private val _submitState = MutableStateFlow<LeaveSubmitState>(LeaveSubmitState.Idle)
    val submitState: StateFlow<LeaveSubmitState> = _submitState.asStateFlow()

    private val _history = MutableStateFlow<List<LeaveApplicationDto>>(emptyList())
    val history: StateFlow<List<LeaveApplicationDto>> = _history.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadLeaveHistory()
    }

    fun loadLeaveHistory() {
        _isLoading.value = true
        viewModelScope.launch {
            leaveRepository.getLeaveHistory()
                .onSuccess {
                    _history.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _isLoading.value = false
                }
        }
    }

    fun submitRequest(
        leaveType: String,
        startDate: String,
        endDate: String,
        reason: String,
        isHalfDay: Boolean
    ) {
        if (startDate.isBlank() || endDate.isBlank()) {
            _submitState.value = LeaveSubmitState.Error("Please select both start and end dates")
            return
        }
        if (startDate > endDate) {
            _submitState.value = LeaveSubmitState.Error("Start date cannot be after end date")
            return
        }
        if (isHalfDay && startDate != endDate) {
            _submitState.value = LeaveSubmitState.Error("Half-day leave must be for a single date")
            return
        }
        if (reason.trim().length < 5) {
            _submitState.value = LeaveSubmitState.Error("Please provide a detailed reason (at least 5 characters)")
            return
        }

        _submitState.value = LeaveSubmitState.Submitting
        viewModelScope.launch {
            val req = LeaveRequestDto(
                leaveType = leaveType,
                startDate = startDate,
                endDate = endDate,
                reason = reason.trim(),
                isHalfDay = isHalfDay
            )
            leaveRepository.submitLeaveRequest(req)
                .onSuccess { app ->
                    _submitState.value = LeaveSubmitState.Success(app)
                    loadLeaveHistory()
                }
                .onFailure { err ->
                    _submitState.value = LeaveSubmitState.Error(err.message ?: "Failed to submit leave request")
                }
        }
    }

    fun resetSubmitState() {
        _submitState.value = LeaveSubmitState.Idle
    }
}
