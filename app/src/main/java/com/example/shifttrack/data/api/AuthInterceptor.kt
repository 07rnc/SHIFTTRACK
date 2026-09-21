package com.example.shifttrack.data.api

import com.example.shifttrack.data.local.SessionManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class AuthInterceptor(
    private val sessionManager: SessionManager,
    private val onUnauthorized: () -> Unit
) : Interceptor {

    private val gson = Gson()

    companion object {
        private val lock = Any()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = sessionManager.getAccessToken()

        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        requestBuilder.header("Accept", "application/json")
        requestBuilder.header("Content-Type", "application/json")

        val response = chain.proceed(requestBuilder.build())

        val requestPath = originalRequest.url.encodedPath
        val isAuthEndpoint = requestPath.contains("/auth/login") ||
                             requestPath.contains("/auth/register") ||
                             requestPath.contains("/auth/refresh")

        if (response.code == 401 && !isAuthEndpoint) {
            val refreshToken = sessionManager.getRefreshToken()
            if (!refreshToken.isNullOrBlank()) {
                synchronized(lock) {
                    val currentToken = sessionManager.getAccessToken()
                    // If another thread already completed token refresh, retry immediately
                    if (currentToken != null && currentToken != token) {
                        response.close()
                        val retryRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer $currentToken")
                            .build()
                        return chain.proceed(retryRequest)
                    }

                    // Attempt refresh
                    val refreshUrl = originalRequest.url.newBuilder()
                        .encodedPath("/api/auth/refresh")
                        .query(null)
                        .build()

                    val jsonBody = JsonObject().apply {
                        addProperty("refreshToken", refreshToken)
                    }.toString()

                    val refreshReq = Request.Builder()
                        .url(refreshUrl)
                        .post(jsonBody.toRequestBody("application/json".toMediaType()))
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .build()

                    var refreshSuccessful = false
                    try {
                        val refreshResponse = chain.proceed(refreshReq)
                        if (refreshResponse.isSuccessful) {
                            val bodyStr = refreshResponse.body?.string()
                            refreshResponse.close()

                            if (!bodyStr.isNullOrBlank()) {
                                val json = gson.fromJson(bodyStr, JsonObject::class.java)
                                val dataObj = json.getAsJsonObject("data")
                                val newAccessToken = dataObj?.get("accessToken")?.asString
                                val newRefreshToken = dataObj?.get("refreshToken")?.asString ?: refreshToken

                                if (!newAccessToken.isNullOrBlank()) {
                                    val currentUser = sessionManager.currentUser.value
                                    if (currentUser != null) {
                                        sessionManager.saveSession(
                                            token = newAccessToken,
                                            refreshToken = newRefreshToken,
                                            user = currentUser
                                        )
                                    }

                                    refreshSuccessful = true
                                    response.close()
                                    val retryRequest = originalRequest.newBuilder()
                                        .header("Authorization", "Bearer $newAccessToken")
                                        .build()
                                    return chain.proceed(retryRequest)
                                }
                            }
                        } else {
                            refreshResponse.close()
                        }
                    } catch (_: Exception) {
                        // Refresh request failed
                    }

                    if (!refreshSuccessful) {
                        sessionManager.clearSession()
                        onUnauthorized()
                    }
                }
            } else {
                sessionManager.clearSession()
                onUnauthorized()
            }
        }

        return response
    }
}
