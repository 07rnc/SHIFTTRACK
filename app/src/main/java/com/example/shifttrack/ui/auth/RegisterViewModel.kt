package com.example.shifttrack.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shifttrack.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        // Client-side validation — in order of importance
        if (fullName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _uiState.value = RegisterUiState.Error("All fields are required")
            return
        }
        if (!email.contains('@') || !email.contains('.')) {
            _uiState.value = RegisterUiState.Error("Please enter a valid email address")
            return
        }
        if (password.length < 8) {
            _uiState.value = RegisterUiState.Error("Password must be at least 8 characters")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = RegisterUiState.Error("Passwords do not match")
            return
        }

        _uiState.value = RegisterUiState.Loading
        viewModelScope.launch {
            val result = authRepository.register(fullName.trim(), email.trim(), password)
            result.onSuccess {
                _uiState.value = RegisterUiState.Success
            }.onFailure { err ->
                _uiState.value = RegisterUiState.Error(
                    err.message ?: "Registration failed. Please try again."
                )
            }
        }
    }

    fun clearError() {
        if (_uiState.value is RegisterUiState.Error) {
            _uiState.value = RegisterUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}
