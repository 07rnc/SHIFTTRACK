package com.example.shifttrack.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shifttrack.data.api.AppConfig
import com.example.shifttrack.data.local.SessionManager
import com.example.shifttrack.data.repository.AuthRepository
import com.example.shifttrack.ui.common.ShiftTrackCard
import com.example.shifttrack.ui.common.ShiftTrackDestructiveButton
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
                title = {
                    Text(
                        text = "Employee Account",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
                .padding(Spacing.screen)
        ) {
            // Profile Card
            ShiftTrackCard(elevation = 1.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.fullName?.split(" ")?.mapNotNull { it.firstOrNull()?.toString() }?.take(2)?.joinToString("") ?: "ST",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))

                    Text(
                        text = user?.fullName ?: "Employee",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(Spacing.xxs))

                    Text(
                        text = user?.employeeCode ?: "ST-0104",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(Spacing.xxs))

                    Text(
                        text = user?.email ?: "employee@shifttrack.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    ProfileDetailRow("Department", user?.department ?: "Engineering & Operations")
                    ProfileDetailRow("Role", user?.role ?: "EMPLOYEE")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Server & Architecture Settings Card
            ShiftTrackCard(elevation = 1.dp) {
                Text(
                    text = "System & Server Connection",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(
                    text = "Backend API Endpoint",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = serverUrl,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                // Mock mode toggle (per Section 19 of requirements)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Standalone Mock Mode",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Simulate backend offline while server is being deployed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = useMockMode,
                        onCheckedChange = {
                            useMockMode = it
                            AppConfig.USE_MOCK_DATA = it
                        }
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                val deviceToken = sessionManager.getDeviceToken()
                Text(
                    text = "FCM Device Push Token",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (!deviceToken.isNullOrBlank()) deviceToken.take(32) + "..." else "Generating device token...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            // Logout Button
            ShiftTrackDestructiveButton(
                text = "Sign Out of ShiftTrack",
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                onClick = { showLogoutDialog = true }
            )

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Confirm Logout", style = MaterialTheme.typography.titleMedium) },
                    text = { Text("Are you sure you want to end your current session on this device?", style = MaterialTheme.typography.bodyMedium) },
                    confirmButton = {
                        Button(
                            onClick = {
                                showLogoutDialog = false
                                coroutineScope.launch {
                                    authRepository.logout()
                                    onLogoutSuccess()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Sign Out", color = MaterialTheme.colorScheme.onError)
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
            .padding(vertical = Spacing.xxs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
