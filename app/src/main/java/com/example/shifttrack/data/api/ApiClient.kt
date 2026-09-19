package com.example.shifttrack.data.api

import com.example.shifttrack.BuildConfig
import com.example.shifttrack.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(
    private val sessionManager: SessionManager,
    private val onUnauthorized: () -> Unit = {}
) {
    private var currentBaseUrl = sessionManager.getServerUrl()
    private var cachedService: ShiftTrackApiService? = null

    fun getService(): ShiftTrackApiService {
        val activeUrl = sessionManager.getServerUrl()
        if (cachedService == null || activeUrl != currentBaseUrl) {
            currentBaseUrl = activeUrl
            cachedService = createService(activeUrl)
        }
        return cachedService!!
    }

    private fun createService(baseUrl: String): ShiftTrackApiService {
        val logging = HttpLoggingInterceptor().apply {
            // BODY level in debug only — prevents auth token leakage to Logcat in production
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager, onUnauthorized))
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        val safeUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(safeUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ShiftTrackApiService::class.java)
    }
}
