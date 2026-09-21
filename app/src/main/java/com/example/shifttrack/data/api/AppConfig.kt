package com.example.shifttrack.data.api

import com.example.shifttrack.BuildConfig

object AppConfig {
    const val DEFAULT_BASE_URL = "http://10.0.2.2:5000/"

    // In accordance with security requirements:
    // Mock mode is strictly development-only and defaults to false in Release builds.
    var USE_MOCK_DATA: Boolean = BuildConfig.DEBUG

    // Standard demo credentials for debug/offline mode
    const val DEMO_PASSWORD = "ShiftTrackPass123"

    // Standard geofence headquarters coordinates for demo and testing
    const val DEMO_OFFICE_LAT = 28.613939
    const val DEMO_OFFICE_LNG = 77.209021
    const val DEMO_OFFICE_RADIUS_METERS = 200.0
    const val DEMO_OFFICE_NAME = "ShiftTrack Headquarters - Central Office"
    const val DEMO_VALID_QR_CODE = "SHIFTTRACK-HQ-OFFICE-CLOCKIN-V1"
}
