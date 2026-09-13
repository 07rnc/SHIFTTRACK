package com.example.shifttrack.ui.attendance

import androidx.compose.foundation.background
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
import com.example.shifttrack.data.model.AttendanceDto
import com.example.shifttrack.ui.common.EmptyStateView
import com.example.shifttrack.ui.common.StatusBadge
import com.example.shifttrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHistoryScreen(
    viewModel: AttendanceViewModel,
    onNavigateBack: () -> Unit
) {
    val history by viewModel.history.collectAsState()
    val isLoading by viewModel.historyLoading.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadHistory() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All Records") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = selectedFilter == "GPS",
                    onClick = { selectedFilter = "GPS" },
                    label = { Text("GPS Only") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = selectedFilter == "QR",
                    onClick = { selectedFilter = "QR" },
                    label = { Text("QR Only") }
                )
            }

            val filteredList = when (selectedFilter) {
                "GPS" -> history.filter { it.method.equals("GPS", ignoreCase = true) }
                "QR" -> history.filter { it.method.equals("QR", ignoreCase = true) }
                else -> history
            }

            if (isLoading && history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandBlue)
                }
            } else if (filteredList.isEmpty()) {
                EmptyStateView(
                    title = "No Attendance Records",
                    message = "You don't have any attendance entries matching this filter yet.",
                    icon = Icons.Default.EventAvailable
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        AttendanceItemCard(item)
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceItemCard(item: AttendanceDto) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Slate500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.date,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }

                StatusBadge(status = item.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timings Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Clock In", fontSize = 11.sp, color = Slate500)
                    Text(
                        text = item.clockInTime ?: "--:--",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                }

                Column {
                    Text("Clock Out", fontSize = 11.sp, color = Slate500)
                    Text(
                        text = item.clockOutTime ?: "Active / In-progress",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (item.clockOutTime != null) Slate900 else Emerald600
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Method", fontSize = 11.sp, color = Slate500)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (item.method == "GPS") BrandBlueLight else Slate100)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.method,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.method == "GPS") BrandBlue else Slate700
                        )
                    }
                }
            }

            if (!item.locationNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Slate500,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.locationNote,
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }
        }
    }
}
