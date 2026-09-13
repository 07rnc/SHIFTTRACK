package com.example.shifttrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.shifttrack.ui.navigation.ShiftTrackNavHost
import com.example.shifttrack.ui.theme.SHIFTTRACKTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ShiftTrackApp
        val notificationRoute = intent.getStringExtra("NAV_ROUTE")

        setContent {
            SHIFTTRACKTheme {
                ShiftTrackNavHost(
                    app = app,
                    initialRoute = notificationRoute
                )
            }
        }
    }
}
