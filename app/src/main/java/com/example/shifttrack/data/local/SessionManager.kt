package com.example.shifttrack.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.shifttrack.data.model.UserDto
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _isLoggedIn = MutableStateFlow(hasValidToken())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow(loadUser())
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

    private val _serverUrl = MutableStateFlow(getServerUrl())
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    fun saveSession(token: String, refreshToken: String?, user: UserDto) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USER_DATA, gson.toJson(user))
            .apply()

        _currentUser.value = user
        _isLoggedIn.value = true
    }

    fun updateAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun hasValidToken(): Boolean = !getAccessToken().isNullOrBlank()

    fun clearSession() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_DATA)
            .apply()

        _currentUser.value = null
        _isLoggedIn.value = false
    }

    fun setServerUrl(url: String) {
        val cleanUrl = if (!url.endsWith("/")) "$url/" else url
        prefs.edit().putString(KEY_SERVER_URL, cleanUrl).apply()
        _serverUrl.value = cleanUrl
    }

    fun getServerUrl(): String {
        return prefs.getString(KEY_SERVER_URL, DEFAULT_URL) ?: DEFAULT_URL
    }

    fun saveDeviceToken(token: String) {
        prefs.edit().putString(KEY_DEVICE_TOKEN, token).apply()
    }

    fun getDeviceToken(): String? = prefs.getString(KEY_DEVICE_TOKEN, null)

    private fun loadUser(): UserDto? {
        val json = prefs.getString(KEY_USER_DATA, null) ?: return null
        return try {
            gson.fromJson(json, UserDto::class.java)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val PREF_NAME = "shifttrack_secure_session"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_DEVICE_TOKEN = "fcm_device_token"
        const val DEFAULT_URL = "http://10.0.2.2:5000/"
    }
}
