package com.example.shifttrack.data.api

import com.example.shifttrack.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionManager: SessionManager,
    private val onUnauthorized: () -> Unit
) : Interceptor {
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

        if (response.code == 401) {
            // Session expired or invalid token
            onUnauthorized()
        }

        return response
    }
}
