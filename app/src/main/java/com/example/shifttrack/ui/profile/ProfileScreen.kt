package com.example.shifttrack.ui.profile

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shifttrack.data.api.AppConfig
import com.example.shifttrack.data.local.SessionManager
import com.example.shifttrack.data.repository.AuthRepository
import com.example.shifttrack.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    sessionManager: SessionManager,
    onLogoutSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val user by authRepository.currentUser.collectAsState()
    val serverUrl by sessionManager.serverUrl.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var useMockMode by remember { mutableStateOf(AppConfig.USE_MOCK_DATA) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Employee Account", fontWeight = FontWeight.Bold) },
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
                .padding(20.dp)
        ) {
            // Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(BrandBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.fullName?.split(" ")?.mapNotNull { it.firstOrNull()?.toString() }?.take(2)?.joinToString("") ?: "ST",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = user?.fullName ?: "Employee",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = user?.employeeCode ?: "ST-0104",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandBlue
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = user?.email ?: "employee@shifttrack.com",
                        fontSize = 13.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = Slate200)

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileDetailRow("Department", user?.department ?: "Engineering & Operations")
                    ProfileDetailRow("Role", user?.role ?: "EMPLOYEE")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Server & Architecture Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "System & Server Connection",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Backend API Endpoint", fontSize = 12.sp, color = Slate500)
                    Text(serverUrl, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Slate900)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mock mode toggle (Clearly isolated per Section 19 of requirements)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Standalone Mock Mode", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            Text("Simulate backend offline while server is being deployed", fontSize = 12.sp, color = Slate500)
                        }
                        Switch(
                            checked = useMockMode,
                            onCheckedChange = {
                                useMockMode = it
                                AppConfig.USE_MOCK_DATA = it
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val deviceToken = sessionManager.getDeviceToken()
                    Text("FCM Device Push Token", fontSize = 12.sp, color = Slate500)
                    Text(
                        text = if (!deviceToken.isNullOrBlank()) deviceToken.take(32) + "..." else "Generating device token...",
                        fontSize = 12.sp,
                        color = Slate700
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Rose100),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Rose600)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of ShiftTrack", color = Rose600, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Confirm Logout") },
                    text = { Text("Are you sure you want to end your current session on this device?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showLogoutDialog = false
                                coroutineScope.launch {
                                    authRepository.logout()
                                    onLogoutSuccess()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Rose600)
                        ) {
                            Text("Sign Out")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Slate500)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
    }
}
