package com.example.shifttrack.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shifttrack.data.local.SessionManager
import com.example.shifttrack.data.model.UserDto
import com.example.shifttrack.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: UserDto) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val serverUrl: StateFlow<String> = sessionManager.serverUrl

    fun updateServerUrl(url: String) {
        sessionManager.setServerUrl(url)
    }

    fun login(identifier: String, pass: String) {
        if (identifier.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your employee ID/email and password")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.login(identifier.trim(), pass)
            result.onSuccess { user ->
                _uiState.value = AuthUiState.Success(user)
            }.onFailure { err ->
                _uiState.value = AuthUiState.Error(err.message ?: "Authentication failed. Check your network or credentials.")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}
