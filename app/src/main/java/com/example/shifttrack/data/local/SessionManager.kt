package com.example.shifttrack.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.shifttrack.data.model.UserDto
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    private val securePrefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            SECURE_PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (t: Throwable) {
        try {
            Log.w("SessionManager", "Keystore initialization failed. Using standard storage fallback: ${t.message}")
        } catch (_: Throwable) {}
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private val plainPrefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        migratePlaintextStorageIfNeeded()
    }

    private fun migratePlaintextStorageIfNeeded() {
        try {
            if (plainPrefs != securePrefs && (plainPrefs.contains(KEY_ACCESS_TOKEN) || plainPrefs.contains(KEY_USER_DATA))) {
                val oldToken = plainPrefs.getString(KEY_ACCESS_TOKEN, null)
                val oldRefreshToken = plainPrefs.getString(KEY_REFRESH_TOKEN, null)
                val oldUserData = plainPrefs.getString(KEY_USER_DATA, null)

                securePrefs.edit {
                    if (oldToken != null) putString(KEY_ACCESS_TOKEN, oldToken)
                    if (oldRefreshToken != null) putString(KEY_REFRESH_TOKEN, oldRefreshToken)
                    if (oldUserData != null) putString(KEY_USER_DATA, oldUserData)
                }

                plainPrefs.edit {
                    remove(KEY_ACCESS_TOKEN)
                    remove(KEY_REFRESH_TOKEN)
                    remove(KEY_USER_DATA)
                }
                try {
                    Log.d("SessionManager", "Successfully migrated plaintext tokens to EncryptedSharedPreferences")
                } catch (_: Throwable) {}
            }
        } catch (t: Throwable) {
            try {
                Log.e("SessionManager", "Session migration failed: ${t.message}")
            } catch (_: Throwable) {}
        }
    }

    private val _isLoggedIn = MutableStateFlow(hasValidToken())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow(loadUser())
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

    private val _serverUrl = MutableStateFlow(getServerUrl())
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _themePref = MutableStateFlow(getThemePref())
    val themePref: StateFlow<String> = _themePref.asStateFlow()

    fun saveSession(token: String, refreshToken: String?, user: UserDto) {
        securePrefs.edit {
            putString(KEY_ACCESS_TOKEN, token)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putString(KEY_USER_DATA, gson.toJson(user))
        }

        _currentUser.value = user
        _isLoggedIn.value = true
    }

    fun getAccessToken(): String? = securePrefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = securePrefs.getString(KEY_REFRESH_TOKEN, null)

    fun hasValidToken(): Boolean = !getAccessToken().isNullOrBlank()

    fun clearSession() {
        securePrefs.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_DATA)
        }
        plainPrefs.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_DATA)
        }

        _currentUser.value = null
        _isLoggedIn.value = false
    }

    fun sanitizeServerUrl(rawUrl: String): String {
        var clean = rawUrl.trim()
        if (clean.isBlank()) return DEFAULT_URL
        if (!clean.startsWith("http://", ignoreCase = true) && !clean.startsWith("https://", ignoreCase = true)) {
            clean = "http://$clean"
        }
        if (!clean.endsWith("/")) {
            clean = "$clean/"
        }
        return clean
    }

    fun setServerUrl(url: String) {
        val cleanUrl = sanitizeServerUrl(url)
        plainPrefs.edit { putString(KEY_SERVER_URL, cleanUrl) }
        _serverUrl.value = cleanUrl
    }

    fun getServerUrl(): String {
        val stored = plainPrefs.getString(KEY_SERVER_URL, DEFAULT_URL) ?: DEFAULT_URL
        return sanitizeServerUrl(stored)
    }

    fun saveDeviceToken(token: String) {
        plainPrefs.edit { putString(KEY_DEVICE_TOKEN, token) }
    }

    fun getDeviceToken(): String? = plainPrefs.getString(KEY_DEVICE_TOKEN, null)

    /** Returns "SYSTEM", "LIGHT", or "DARK". Defaults to "SYSTEM" on first launch. */
    fun getThemePref(): String = plainPrefs.getString(KEY_THEME_PREF, THEME_SYSTEM) ?: THEME_SYSTEM

    /** Persists the user's theme choice and updates the reactive flow immediately. */
    fun setThemePref(pref: String) {
        plainPrefs.edit { putString(KEY_THEME_PREF, pref) }
        _themePref.value = pref
    }

    private fun loadUser(): UserDto? {
        val json = securePrefs.getString(KEY_USER_DATA, null) 
            ?: plainPrefs.getString(KEY_USER_DATA, null)
            ?: return null
        return try {
            gson.fromJson(json, UserDto::class.java)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val SECURE_PREF_NAME = "shifttrack_encrypted_session"
        private const val PREF_NAME = "shifttrack_secure_session"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_DEVICE_TOKEN = "fcm_device_token"
        private const val KEY_THEME_PREF = "theme_preference"
        const val DEFAULT_URL = "http://10.0.2.2:5000/"

        // Theme preference constants
        const val THEME_SYSTEM = "SYSTEM"
        const val THEME_LIGHT = "LIGHT"
        const val THEME_DARK = "DARK"
    }
}
