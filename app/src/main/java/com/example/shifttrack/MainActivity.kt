package com.example.shifttrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.shifttrack.ui.navigation.ShiftTrackNavHost
import com.example.shifttrack.ui.theme.SHIFTTRACKTheme

class MainActivity : ComponentActivity() {

    private var activeRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ShiftTrackApp
        activeRoute = intent.getStringExtra("NAV_ROUTE")

        setContent {
            SHIFTTRACKTheme {
                ShiftTrackNavHost(
                    app = app,
                    initialRoute = activeRoute
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val newRoute = intent.getStringExtra("NAV_ROUTE")
        if (!newRoute.isNullOrBlank()) {
            activeRoute = newRoute
        }
    }
}
