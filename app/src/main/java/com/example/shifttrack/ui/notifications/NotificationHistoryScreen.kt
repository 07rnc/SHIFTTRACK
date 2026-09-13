package com.example.shifttrack.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shifttrack.data.model.AppNotificationDto
import com.example.shifttrack.ui.common.EmptyStateView
import com.example.shifttrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistoryScreen(
    viewModel: NotificationViewModel,
    onNavigateBack: () -> Unit,
    onNotificationClick: (String?) -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadNotifications() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Slate50
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading && notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandBlue)
                }
            } else if (notifications.isEmpty()) {
                EmptyStateView(
                    title = "No Notifications",
                    message = "You don't have any notifications at this moment.",
                    icon = Icons.Default.NotificationsNone
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        NotificationItemCard(
                            item = notif,
                            onClick = {
                                viewModel.markAsRead(notif.id)
                                onNotificationClick(notif.actionRoute)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemCard(
    item: AppNotificationDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (item.isRead) Color.White else BrandBlueLight.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            val (icon, iconTint) = when (item.type.uppercase()) {
                "LEAVE" -> Pair(Icons.Default.EventBusy, Amber600)
                "SHIFT" -> Pair(Icons.Default.Schedule, BrandBlue)
                else -> Pair(Icons.Default.Notifications, Slate700)
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = if (item.isRead) FontWeight.SemiBold else FontWeight.Bold,
                        color = Slate900
                    )
                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BrandBlue)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.message,
                    fontSize = 13.sp,
                    color = Slate700
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.timestamp,
                    fontSize = 11.sp,
                    color = Slate500
                )
            }
        }
    }
}
