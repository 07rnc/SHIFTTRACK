package com.example.shifttrack.ui.attendance

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.shifttrack.qr.QrCodeAnalyzer
import com.example.shifttrack.ui.common.ShiftTrackButton
import com.example.shifttrack.ui.theme.*
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrAttendanceScreen(
    viewModel: AttendanceViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val actionState by viewModel.actionState.collectAsState()

    val analyzer = remember {
        QrCodeAnalyzer { qrData ->
            viewModel.performQrClockIn(qrData)
        }
    }

    LaunchedEffect(actionState) {
        if (actionState is AttendanceActionState.Idle) {
            analyzer.resumeScanning()
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    fun handleBack() {
        analyzer.pauseScanning()
        viewModel.resetActionState()
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "QR Scanner Attendance",
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
                actions = {
                    // Demo Mock QR scan trigger for easy testing on emulators without camera
                    IconButton(onClick = { viewModel.performQrClockIn("SHIFTTRACK-OFFICE-ROOM-101-FALLBACK") }) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Scan Demo QR",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (hasCameraPermission) {
                // CameraX Viewfinder
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        val executor = ContextCompat.getMainExecutor(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(Executors.newSingleThreadExecutor(), analyzer)
                                }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, executor)

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Visual Reticle Overlay
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(260.dp)
                                .border(BorderStroke(3.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(Radius.lg))
                                .background(Color.Transparent)
                        )
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.sm))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                        ) {
                            Text(
                                text = "Point camera at office QR code",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                // Camera Permission Denied View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = "To scan the fallback office QR code, ShiftTrack needs access to your device camera.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Spacing.xl))
                    ShiftTrackButton(
                        text = "Grant Camera Permission",
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                    )
                }
            }

            // Result State Overlays
            when (val state = actionState) {
                is AttendanceActionState.Submitting -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(Radius.lg),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(Spacing.xxl),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(Spacing.md))
                                Text(
                                    text = "Validating QR Code with Server...",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                is AttendanceActionState.Success -> {
                    AlertDialog(
                        onDismissRequest = { handleBack() },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ShiftTrackTheme.statusColors.success.content,
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        title = {
                            Text("QR Attendance Verified", style = MaterialTheme.typography.titleMedium)
                        },
                        text = {
                            Text(state.message, style = MaterialTheme.typography.bodyMedium)
                        },
                        confirmButton = {
                            Button(
                                onClick = { handleBack() },
                                colors = ButtonDefaults.buttonColors(containerColor = ShiftTrackTheme.statusColors.success.content)
                            ) {
                                Text("Done", color = MaterialTheme.colorScheme.surface)
                            }
                        }
                    )
                }
                is AttendanceActionState.Error -> {
                    AlertDialog(
                        onDismissRequest = {
                            viewModel.resetActionState()
                            analyzer.resumeScanning()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        title = {
                            Text("Attendance Rejected", style = MaterialTheme.typography.titleMedium)
                        },
                        text = {
                            Text(state.message, style = MaterialTheme.typography.bodyMedium)
                        },
                        confirmButton = {
                            Button(onClick = {
                                viewModel.resetActionState()
                                analyzer.resumeScanning()
                            }) {
                                Text("Scan Again")
                            }
                        }
                    )
                }
                else -> {}
            }
        }
    }
}
