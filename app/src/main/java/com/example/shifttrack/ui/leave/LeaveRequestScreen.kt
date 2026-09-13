package com.example.shifttrack.ui.leave

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.shifttrack.ui.common.ErrorBanner
import com.example.shifttrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveRequestScreen(
    viewModel: LeaveViewModel,
    onNavigateBack: () -> Unit,
    onSubmissionComplete: () -> Unit
) {
    val submitState by viewModel.submitState.collectAsState()
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var leaveType by remember { mutableStateOf("CASUAL") }
    var startDate by remember { mutableStateOf(sdf.format(Date())) }
    var endDate by remember { mutableStateOf(sdf.format(Date())) }
    var isHalfDay by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    val leaveTypes = listOf("CASUAL", "SICK", "ANNUAL", "UNPAID")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Leave", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "New Leave Application",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Submit a request to your manager for attendance approval",
                        fontSize = 13.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (submitState is LeaveSubmitState.Error) {
                        ErrorBanner(
                            message = (submitState as LeaveSubmitState.Error).message,
                            onRetry = { viewModel.resetSubmitState() }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Leave Type Selector
                    Text("Leave Type", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                    Spacer(modifier = Modifier.height(6.dp))
                    ExposedDropdownMenuBox(
                        expanded = typeDropdownExpanded,
                        onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = "$leaveType Leave",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = typeDropdownExpanded,
                            onDismissRequest = { typeDropdownExpanded = false }
                        ) {
                            leaveTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text("$type Leave") },
                                    onClick = {
                                        leaveType = type
                                        typeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Start & End Date Inputs
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Start Date", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                placeholder = { Text("YYYY-MM-DD") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("End Date", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = { endDate = it },
                                placeholder = { Text("YYYY-MM-DD") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Half-Day Option
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isHalfDay,
                            onCheckedChange = { isHalfDay = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Apply as Half-Day leave (0.5 working day)",
                            fontSize = 14.sp,
                            color = Slate700
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reason Text Area
                    Text("Reason for Leave", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        placeholder = { Text("Please explain the reason for your leave request...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            viewModel.submitRequest(
                                leaveType = leaveType,
                                startDate = startDate,
                                endDate = endDate,
                                reason = reason,
                                isHalfDay = isHalfDay
                            )
                        },
                        enabled = submitState !is LeaveSubmitState.Submitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        if (submitState is LeaveSubmitState.Submitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit Leave Request", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Success Dialog
            if (submitState is LeaveSubmitState.Success) {
                val app = (submitState as LeaveSubmitState.Success).application
                AlertDialog(
                    onDismissRequest = {
                        viewModel.resetSubmitState()
                        onSubmissionComplete()
                    },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald600, modifier = Modifier.size(36.dp)) },
                    title = { Text("Leave Request Submitted") },
                    text = {
                        Text("Your leave request for ${app.startDate} to ${app.endDate} has been submitted with authoritative status: ${app.status}. Your manager will review it shortly.")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.resetSubmitState()
                                onSubmissionComplete()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                        ) {
                            Text("View Leave History")
                        }
                    }
                )
            }
        }
    }
}
