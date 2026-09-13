package com.example.shifttrack.data.api

object AppConfig {
    const val DEFAULT_BASE_URL = "http://10.0.2.2:5000/"

    // In accordance with Section 19 of requirements:
    // If backend endpoints are not live, Mock Mode provides reliable,
    // backend-faithful local simulation. Toggled in Profile/Settings screen.
    var USE_MOCK_DATA: Boolean = true

    // Standard geofence headquarters coordinates for demo and testing
    const val DEMO_OFFICE_LAT = 28.613939
    const val DEMO_OFFICE_LNG = 77.209021
    const val DEMO_OFFICE_RADIUS_METERS = 200.0
    const val DEMO_OFFICE_NAME = "ShiftTrack Headquarters - Central Office"
    const val DEMO_VALID_QR_CODE = "SHIFTTRACK-HQ-OFFICE-CLOCKIN-V1"
}
