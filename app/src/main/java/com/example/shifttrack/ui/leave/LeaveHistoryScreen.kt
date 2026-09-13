package com.example.shifttrack.ui.leave

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shifttrack.data.model.LeaveApplicationDto
import com.example.shifttrack.ui.common.EmptyStateView
import com.example.shifttrack.ui.common.StatusBadge
import com.example.shifttrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveHistoryScreen(
    viewModel: LeaveViewModel,
    onNavigateApplyLeave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val history by viewModel.history.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLeaveHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave Status & History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadLeaveHistory() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateApplyLeave,
                containerColor = BrandBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply Leave", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Slate50
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading && history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandBlue)
                }
            } else if (history.isEmpty()) {
                EmptyStateView(
                    title = "No Leave Requests",
                    message = "You haven't submitted any leave requests yet.",
                    icon = Icons.Default.EventBusy
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(history, key = { it.id }) { item ->
                        LeaveItemCard(item)
                    }
                }
            }
        }
    }
}

@Composable
fun LeaveItemCard(item: LeaveApplicationDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.leaveType} Leave (${item.daysCount} days)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                // Authoritative backend status badge
                StatusBadge(status = item.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = Slate500, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${item.startDate} to ${item.endDate}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate700
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Reason: ${item.reason}",
                fontSize = 13.sp,
                color = Slate700
            )

            if (!item.reviewerRemarks.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate100)
                ) {
                    Text(
                        text = "Manager Note: ${item.reviewerRemarks}",
                        fontSize = 12.sp,
                        color = Slate700,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Applied on ${item.appliedAt}",
                fontSize = 11.sp,
                color = Slate500
            )
        }
    }
}
