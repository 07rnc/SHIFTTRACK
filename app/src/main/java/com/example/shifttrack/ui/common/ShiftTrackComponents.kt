package com.example.shifttrack.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shifttrack.data.model.BackendLeaveStatus
import com.example.shifttrack.ui.theme.*

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "ON_TIME", "PRESENT", "COMPLETED", BackendLeaveStatus.APPROVED -> 
            Triple(Emerald100, Emerald600, if (status == BackendLeaveStatus.APPROVED) "Approved" else if (status == "ON_TIME") "On Time" else "Completed")
        "LATE", BackendLeaveStatus.PENDING, "SCHEDULED" -> 
            Triple(Amber100, Amber600, if (status == BackendLeaveStatus.PENDING) "Pending Approval" else if (status == "LATE") "Late" else "Scheduled")
        "ABSENT", "EARLY_DEPARTURE", BackendLeaveStatus.REJECTED, BackendLeaveStatus.DENIED -> 
            Triple(Rose100, Rose600, if (status == BackendLeaveStatus.REJECTED || status == BackendLeaveStatus.DENIED) "Rejected" else "Early Departure")
        "CLOCKED_IN", "IN_PROGRESS" -> 
            Triple(BrandBlueLight, BrandBlue, if (status == "CLOCKED_IN") "Clocked In" else "In Progress")
        "CLOCKED_OUT" -> 
            Triple(Slate200, Slate700, "Clocked Out")
        else -> 
            Triple(Slate200, Slate700, status.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EmptyStateView(
    title: String,
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Slate500
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = Slate500,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorBanner(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Rose100),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Rose600.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Rose600,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                color = Rose600,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onRetry) {
                    Text("Retry", color = Rose600, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
