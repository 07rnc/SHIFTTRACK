package com.example.shifttrack.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.shifttrack.data.model.ClockState
import com.example.shifttrack.ui.common.*
import com.example.shifttrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateGpsAttendance: () -> Unit,
    onNavigateQrAttendance: () -> Unit,
    onNavigateAttendanceHistory: () -> Unit,
    onNavigateLeaveRequest: () -> Unit,
    onNavigateLeaveHistory: () -> Unit,
    onNavigateNotifications: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Automatically refresh on resume so attendance status is always up-to-date
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.loadData(isRefresh = true)
    }

    val currentDate = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ShiftTrack",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateNotifications) {
                        BadgedBox(
                            badge = {
                                if (uiState.unreadNotifsCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(uiState.unreadNotifsCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.loadData(isRefresh = true) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
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
                .padding(Spacing.screen)
        ) {
            // Employee Identity Card
            val user = uiState.user
            ShiftTrackCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.fullName?.split(" ")?.mapNotNull { it.firstOrNull()?.toString() }?.take(2)?.joinToString("") ?: "ST",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(Spacing.md))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.fullName ?: "Employee",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Spacing.xxs))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Radius.xs))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = Spacing.xs, vertical = 2.dp)
                            ) {
                                Text(
                                    text = user?.employeeCode ?: "ID",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = user?.department ?: "Operations",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Action Feedback / Error Banner
            if (uiState.actionFeedback != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.sm),
                    colors = CardDefaults.cardColors(containerColor = ShiftTrackTheme.statusColors.success.container)
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ShiftTrackTheme.statusColors.success.content)
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = uiState.actionFeedback!!,
                            color = ShiftTrackTheme.statusColors.success.content,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearFeedback() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = ShiftTrackTheme.statusColors.success.content)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.md))
            }

            if (uiState.errorMessage != null) {
                ErrorBanner(
                    message = uiState.errorMessage!!,
                    onRetry = { viewModel.loadData(isRefresh = true) }
                )
                Spacer(modifier = Modifier.height(Spacing.md))
            }

            // Today's Assigned Shift Card
            val shift = uiState.currentShift
            ShiftTrackCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = "Today's Assigned Shift",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    StatusBadge(status = shift?.status ?: "SCHEDULED")
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(
                    text = shift?.title ?: "Standard Day Shift",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "${shift?.startTime ?: "09:00"} - ${shift?.endTime ?: "17:00"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = shift?.locationName ?: "Office Headquarters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Central Attendance Action Card
            val attState = uiState.attendanceState
            val isClockedIn = attState?.state == ClockState.CLOCKED_IN.name
            val isClockedOut = attState?.state == ClockState.CLOCKED_OUT.name

            ShiftTrackCard(
                elevation = 2.dp,
                containerColor = if (isClockedIn) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ATTENDANCE STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    StatusBadge(status = attState?.state ?: "NOT_CLOCKED_IN")

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    val statusDetail = when {
                        isClockedIn -> "Clocked In at " + (attState?.currentRecord?.clockInTime ?: "") + " via " + (attState?.currentRecord?.method ?: "GPS")
                        isClockedOut -> "Shift Completed at " + (attState?.currentRecord?.clockOutTime ?: "")
                        else -> "Ready to record today's attendance"
                    }

                    Text(
                        text = statusDetail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    // Dynamic Action Buttons
                    if (isClockedIn) {
                        ShiftTrackDestructiveButton(
                            text = "Clock Out Now",
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            isLoading = uiState.actionLoading,
                            onClick = { viewModel.quickClockOut() }
                        )
                    } else if (isClockedOut) {
                        ShiftTrackOutlinedButton(
                            text = "View Today's Attendance Log",
                            icon = Icons.Default.History,
                            onClick = onNavigateAttendanceHistory
                        )
                    } else {
                        ShiftTrackButton(
                            text = "Clock In with GPS",
                            icon = Icons.Default.MyLocation,
                            onClick = onNavigateGpsAttendance
                        )

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        ShiftTrackOutlinedButton(
                            text = "Scan Office QR (Fallback)",
                            icon = Icons.Default.QrCodeScanner,
                            onClick = onNavigateQrAttendance
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            // Quick Actions Section
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(modifier = Modifier.fillMaxWidth()) {
                QuickActionCard(
                    title = "Request Leave",
                    subtitle = "Apply time-off",
                    icon = Icons.Default.EventBusy,
                    iconTint = ShiftTrackTheme.statusColors.warning.content,
                    iconBg = ShiftTrackTheme.statusColors.warning.container,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateLeaveRequest
                )
                Spacer(modifier = Modifier.width(Spacing.md))
                QuickActionCard(
                    title = "Leave Status",
                    subtitle = "Track requests",
                    icon = Icons.Default.PendingActions,
                    iconTint = MaterialTheme.colorScheme.primary,
                    iconBg = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateLeaveHistory
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(modifier = Modifier.fillMaxWidth()) {
                QuickActionCard(
                    title = "Attendance Log",
                    subtitle = "History & audits",
                    icon = Icons.Default.History,
                    iconTint = ShiftTrackTheme.statusColors.success.content,
                    iconBg = ShiftTrackTheme.statusColors.success.container,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateAttendanceHistory
                )
                Spacer(modifier = Modifier.width(Spacing.md))
                QuickActionCard(
                    title = "QR Scanner",
                    subtitle = "Office QR check-in",
                    icon = Icons.Default.QrCodeScanner,
                    iconTint = MaterialTheme.colorScheme.primary,
                    iconBg = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateQrAttendance
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ShiftTrackCard(
        modifier = modifier,
        onClick = onClick,
        elevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(Radius.sm))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
