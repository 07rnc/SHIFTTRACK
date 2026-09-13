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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.shifttrack.qr.QrCodeAnalyzer
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

    var isTorchOn by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Scanner Attendance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Demo Mock QR scan trigger for easy testing on emulators without camera
                    IconButton(onClick = { viewModel.performQrClockIn("SHIFTTRACK-OFFICE-ROOM-101-FALLBACK") }) {
                        Icon(Icons.Default.QrCode, contentDescription = "Scan Demo QR", tint = BrandBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Slate900
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

                            val analyzer = QrCodeAnalyzer { qrData ->
                                viewModel.performQrClockIn(qrData)
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
                                .border(BorderStroke(3.dp, BrandBlue), RoundedCornerShape(16.dp))
                                .background(Color.Transparent)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Point camera at office QR code",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                // Camera Permission Denied Card
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera Permission Required",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To scan the fallback office QR code, ShiftTrack needs access to your device camera.",
                        color = Slate200,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text("Grant Camera Permission")
                    }
                }
            }

            // Result Bottom Sheet or Dialog
            when (val state = actionState) {
                is AttendanceActionState.Submitting -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = BrandBlue)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Validating QR Code with Server...", fontWeight = FontWeight.Bold, color = Slate900)
                            }
                        }
                    }
                }
                is AttendanceActionState.Success -> {
                    AlertDialog(
                        onDismissRequest = {
                            viewModel.resetActionState()
                            onNavigateBack()
                        },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald600, modifier = Modifier.size(36.dp)) },
                        title = { Text("QR Attendance Verified", fontWeight = FontWeight.Bold) },
                        text = { Text(state.message) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.resetActionState()
                                    onNavigateBack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                            ) {
                                Text("Done")
                            }
                        }
                    )
                }
                is AttendanceActionState.Error -> {
                    AlertDialog(
                        onDismissRequest = { viewModel.resetActionState() },
                        icon = { Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Rose600, modifier = Modifier.size(36.dp)) },
                        title = { Text("Attendance Rejected", fontWeight = FontWeight.Bold) },
                        text = { Text(state.message) },
                        confirmButton = {
                            Button(onClick = { viewModel.resetActionState() }) {
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
