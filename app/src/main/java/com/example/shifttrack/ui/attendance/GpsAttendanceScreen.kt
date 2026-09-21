package com.example.shifttrack.ui.attendance

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.shifttrack.data.api.AppConfig
import com.example.shifttrack.ui.common.*
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

    fun handleBack() {
        viewModel.resetActionState()
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GPS Attendance",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { handleBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.screen),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Permission Rationale Card if missing
            if (!hasLocationPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = CardDefaults.cardColors(containerColor = ShiftTrackTheme.statusColors.warning.container)
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOff,
                                contentDescription = null,
                                tint = ShiftTrackTheme.statusColors.warning.content
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = "Location Permission Required",
                                style = MaterialTheme.typography.titleMedium,
                                color = ShiftTrackTheme.statusColors.warning.content
                            )
                        }
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = "ShiftTrack uses GPS to verify you are present at the workplace geofence. The backend performs authoritative validation of your coordinates. Permission is required to record attendance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ShiftTrackTheme.statusColors.warning.content
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ShiftTrackTheme.statusColors.warning.content),
                            shape = RoundedCornerShape(Radius.sm)
                        ) {
                            Text(
                                text = "Grant Location Permission",
                                color = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.lg))
            }

            // Central Geofence Status Card
            ShiftTrackCard(elevation = 2.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    Text(
                        text = "Authoritative GPS Clock-In",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Text(
                        text = "Device coordinates are sent directly to the backend for geofence validation against office boundaries.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    // Office Geofence Target Info
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(Spacing.cardPadding)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(Spacing.xs))
                                Text(
                                    text = AppConfig.DEMO_OFFICE_NAME,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(Spacing.xxs))
                            Text(
                                text = "Allowed Radius: ${AppConfig.DEMO_OFFICE_RADIUS_METERS.toInt()} meters around office site",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.xxl))

                    // Dynamic Result / Status Views
                    when (val state = actionState) {
                        is AttendanceActionState.AcquiringLocation -> {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Text(
                                text = "Acquiring GPS fix...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        is AttendanceActionState.Submitting -> {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Text(
                                text = "Authorizing attendance with server...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        is AttendanceActionState.Success -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = ShiftTrackTheme.statusColors.success.container),
                                shape = RoundedCornerShape(Radius.md)
                            ) {
                                Column(
                                    modifier = Modifier.padding(Spacing.lg),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ShiftTrackTheme.statusColors.success.content,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(Spacing.sm))
                                    Text(
                                        text = "Clock-In Successful!",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = ShiftTrackTheme.statusColors.success.content
                                    )
                                    Spacer(modifier = Modifier.height(Spacing.xs))
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ShiftTrackTheme.statusColors.success.content,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(Spacing.md))
                                    ShiftTrackButton(
                                        text = "Return to Dashboard",
                                        onClick = { handleBack() }
                                    )
                                }
                            }
                        }
                        is AttendanceActionState.OutsideGeofence -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = ShiftTrackTheme.statusColors.warning.container),
                                shape = RoundedCornerShape(Radius.md)
                            ) {
                                Column(
                                    modifier = Modifier.padding(Spacing.lg),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = ShiftTrackTheme.statusColors.warning.content,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(Spacing.sm))
                                    Text(
                                        text = "Outside Authorized Geofence",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = ShiftTrackTheme.statusColors.warning.content
                                    )
                                    Spacer(modifier = Modifier.height(Spacing.xs))
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ShiftTrackTheme.statusColors.warning.content,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(Spacing.md))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        ShiftTrackOutlinedButton(
                                            text = "Retry GPS",
                                            modifier = Modifier.weight(1f),
                                            onClick = { viewModel.performGpsClockIn() }
                                        )
                                        Spacer(modifier = Modifier.width(Spacing.sm))
                                        ShiftTrackButton(
                                            text = "Use QR Fallback",
                                            modifier = Modifier.weight(1f),
                                            onClick = onNavigateQrFallback
                                        )
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
                            ShiftTrackButton(
                                text = "Record GPS Clock-In",
                                icon = Icons.Default.MyLocation,
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
                                }
                            )

                            Spacer(modifier = Modifier.height(Spacing.sm))

                            ShiftTrackOutlinedButton(
                                text = "Switch to QR Scanner Fallback",
                                icon = Icons.Default.QrCodeScanner,
                                onClick = onNavigateQrFallback
                            )
                        }
                    }
                }
            }
        }
    }
}
