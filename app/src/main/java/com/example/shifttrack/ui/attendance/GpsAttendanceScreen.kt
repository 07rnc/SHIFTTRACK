package com.example.shifttrack.ui.attendance

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.shifttrack.data.api.AppConfig
import com.example.shifttrack.ui.common.ErrorBanner
import com.example.shifttrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsAttendanceScreen(
    viewModel: AttendanceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateQrFallback: () -> Unit
) {
    val context = LocalContext.current
    val actionState by viewModel.actionState.collectAsState()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasLocationPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GPS Attendance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Slate50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Permission Rationale Card if missing
            if (!hasLocationPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Amber100),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOff, contentDescription = null, tint = Amber600)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Location Permission Required",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Amber600
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ShiftTrack uses GPS to verify you are present at the configured workplace geofence. The backend performs authoritative validation of your coordinates. Permission is required to clock in.",
                            fontSize = 13.sp,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Amber600),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Grant Location Permission")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Central Geofence Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(BrandBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = BrandBlue,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Authoritative GPS Clock-In",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Device coordinates are sent directly to the backend for geofence validation against office boundaries.",
                        fontSize = 13.sp,
                        color = Slate500,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Office Geofence Target Info
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate100)
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Business, contentDescription = null, tint = Slate700, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = AppConfig.DEMO_OFFICE_NAME,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Allowed Radius: 200 meters around office site",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Dynamic Result / Status Cards
                    when (val state = actionState) {
                        is AttendanceActionState.AcquiringLocation -> {
                            CircularProgressIndicator(color = BrandBlue)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Acquiring GPS fix...", fontSize = 14.sp, color = Slate700)
                        }
                        is AttendanceActionState.Submitting -> {
                            CircularProgressIndicator(color = BrandBlue)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Authorizing attendance with server...", fontSize = 14.sp, color = Slate700)
                        }
                        is AttendanceActionState.Success -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Emerald100),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald600, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Clock-In Successful!", fontWeight = FontWeight.Bold, color = Emerald600, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(state.message, color = Slate700, fontSize = 13.sp, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            viewModel.resetActionState()
                                            onNavigateBack()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                                    ) {
                                        Text("Return to Dashboard")
                                    }
                                }
                            }
                        }
                        is AttendanceActionState.OutsideGeofence -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Amber100),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Amber600, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Outside Authorized Geofence", fontWeight = FontWeight.Bold, color = Amber600, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(state.message, color = Slate700, fontSize = 13.sp, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row {
                                        OutlinedButton(onClick = { viewModel.performGpsClockIn() }) {
                                            Text("Retry GPS")
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = onNavigateQrFallback,
                                            colors = ButtonDefaults.buttonColors(containerColor = Amber600)
                                        ) {
                                            Text("Use QR Fallback")
                                        }
                                    }
                                }
                            }
                        }
                        is AttendanceActionState.Error -> {
                            ErrorBanner(
                                message = state.message,
                                onRetry = { viewModel.performGpsClockIn() }
                            )
                        }
                        is AttendanceActionState.Idle -> {
                            Button(
                                onClick = {
                                    if (hasLocationPermission) {
                                        viewModel.performGpsClockIn()
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Record GPS Clock-In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = onNavigateQrFallback,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Switch to QR Scanner Fallback")
                            }
                        }
                    }
                }
            }
        }
    }
}
