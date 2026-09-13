package com.example.shifttrack.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shifttrack.data.model.ClockState
import com.example.shifttrack.ui.common.ErrorBanner
import com.example.shifttrack.ui.common.StatusBadge
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

    // Live formatted time and date
    val currentTime = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()) }
    val currentDate = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ShiftTrack",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = currentDate,
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                },
                actions = {
                    // Notification Icon with Badge
                    IconButton(onClick = onNavigateNotifications) {
                        BadgedBox(
                            badge = {
                                if (uiState.unreadNotifsCount > 0) {
                                    Badge(containerColor = Rose600) {
                                        Text(uiState.unreadNotifsCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Slate700
                            )
                        }
                    }
                    // Manual Refresh
                    IconButton(onClick = { viewModel.loadData(isRefresh = true) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Slate700
                        )
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
                .padding(16.dp)
        ) {
            // Employee Profile Header Card
            val user = uiState.user
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(BrandBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.fullName?.split(" ")?.mapNotNull { it.firstOrNull()?.toString() }?.take(2)?.joinToString("") ?: "ST",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.fullName ?: "Employee",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Slate100)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = user?.employeeCode ?: "ID",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate700
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = user?.department ?: "General Staff",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action feedback or error banner
            if (uiState.actionFeedback != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald100)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald600)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.actionFeedback!!,
                            color = Emerald600,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearFeedback() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Emerald600)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.errorMessage != null) {
                ErrorBanner(
                    message = uiState.errorMessage!!,
                    onRetry = { viewModel.loadData(isRefresh = true) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Current Shift Card
            val shift = uiState.currentShift
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.EventNote,
                                contentDescription = null,
                                tint = BrandBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Today's Assigned Shift",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                        StatusBadge(status = shift?.status ?: "SCHEDULED")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = shift?.title ?: "Standard Day Shift",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${shift?.startTime ?: "09:00"} - ${shift?.endTime ?: "17:00"}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate700
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = shift?.locationName ?: "Office Headquarters",
                            fontSize = 13.sp,
                            color = Slate500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real-Time Attendance Action Card
            val attState = uiState.attendanceState
            val isClockedIn = attState?.state == ClockState.CLOCKED_IN.name
            val isClockedOut = attState?.state == ClockState.CLOCKED_OUT.name

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isClockedIn) Emerald100.copy(alpha = 0.4f) else Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ATTENDANCE STATUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    StatusBadge(
                        status = attState?.state ?: "NOT_CLOCKED_IN",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val statusDetail = when {
                        isClockedIn -> "Clocked In at " + (attState?.currentRecord?.clockInTime ?: "") + " via " + (attState?.currentRecord?.method ?: "GPS")
                        isClockedOut -> "Shift Completed at " + (attState?.currentRecord?.clockOutTime ?: "")
                        else -> "Ready to record today's attendance"
                    }

                    Text(
                        text = statusDetail,
                        fontSize = 14.sp,
                        color = Slate700,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Action Button
                    if (isClockedIn) {
                        Button(
                            onClick = { viewModel.quickClockOut() },
                            enabled = !uiState.actionLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Rose600)
                        ) {
                            if (uiState.actionLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clock Out Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (isClockedOut) {
                        OutlinedButton(
                            onClick = onNavigateAttendanceHistory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("View Today's Attendance Log", fontSize = 14.sp)
                        }
                    } else {
                        // Clock In with GPS
                        Button(
                            onClick = onNavigateGpsAttendance,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clock In with GPS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // QR Fallback Button
                        OutlinedButton(
                            onClick = onNavigateQrAttendance,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = BrandBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan Office QR (Fallback)", fontSize = 14.sp, color = BrandBlue)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Access Grid
            Text(
                text = "Quick Actions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                QuickActionCard(
                    title = "Request Leave",
                    subtitle = "Apply time-off",
                    icon = Icons.Default.EventBusy,
                    iconTint = Amber600,
                    iconBg = Amber100,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateLeaveRequest
                )
                Spacer(modifier = Modifier.width(12.dp))
                QuickActionCard(
                    title = "Leave Status",
                    subtitle = "Track requests",
                    icon = Icons.Default.PendingActions,
                    iconTint = Indigo600,
                    iconBg = Indigo100,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateLeaveHistory
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                QuickActionCard(
                    title = "Attendance Log",
                    subtitle = "History & audits",
                    icon = Icons.Default.History,
                    iconTint = Emerald600,
                    iconBg = Emerald100,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateAttendanceHistory
                )
                Spacer(modifier = Modifier.width(12.dp))
                QuickActionCard(
                    title = "QR Scanner",
                    subtitle = "Office QR check-in",
                    icon = Icons.Default.QrCodeScanner,
                    iconTint = BrandBlue,
                    iconBg = BrandBlueLight,
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
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text(text = subtitle, fontSize = 11.sp, color = Slate500)
        }
    }
}
