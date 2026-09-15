package com.example.shifttrack.ui.leave

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.shifttrack.ui.common.*
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
    val context = LocalContext.current
    val submitState by viewModel.submitState.collectAsState()
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var leaveType by remember { mutableStateOf("CASUAL") }
    var startDate by remember { mutableStateOf(sdf.format(Date())) }
    var endDate by remember { mutableStateOf(sdf.format(Date())) }
    var isHalfDay by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    val leaveTypes = listOf("CASUAL", "SICK", "ANNUAL", "UNPAID")

    fun showDatePicker(currentDateStr: String, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        try {
            val parsed = sdf.parse(currentDateStr)
            if (parsed != null) calendar.time = parsed
        } catch (_: Exception) {}

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, dayOfMonth)
                onDateSelected(sdf.format(selectedCal.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Request Leave",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
            ShiftTrackCard(elevation = 2.dp) {
                Text(
                    text = "New Leave Application",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Submit a request to your manager for attendance approval",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Spacing.xl))

                if (submitState is LeaveSubmitState.Error) {
                    ErrorBanner(
                        message = (submitState as LeaveSubmitState.Error).message,
                        onRetry = { viewModel.resetSubmitState() }
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                }

                // Leave Type Selector
                Text(
                    text = "Leave Type",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
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
                        shape = RoundedCornerShape(Radius.md)
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

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Start & End Date Inputs with Calendar Pickers
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Start Date",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        showDatePicker(startDate) { newStart ->
                                            startDate = newStart
                                            if (isHalfDay || endDate < newStart) {
                                                endDate = newStart
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Start Date")
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Radius.md)
                            )
                            // Transparent clickable overlay so tapping anywhere triggers the picker
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        showDatePicker(startDate) { newStart ->
                                            startDate = newStart
                                            if (isHalfDay || endDate < newStart) {
                                                endDate = newStart
                                            }
                                        }
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "End Date",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isHalfDay,
                                trailingIcon = {
                                    if (!isHalfDay) {
                                        IconButton(onClick = {
                                            showDatePicker(endDate) { newEnd ->
                                                endDate = newEnd
                                                if (newEnd < startDate) {
                                                    startDate = newEnd
                                                }
                                            }
                                        }) {
                                            Icon(Icons.Default.CalendarMonth, contentDescription = "Pick End Date")
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Radius.md)
                            )
                            if (!isHalfDay) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable {
                                            showDatePicker(endDate) { newEnd ->
                                                endDate = newEnd
                                                if (newEnd < startDate) {
                                                    startDate = newEnd
                                                }
                                            }
                                        }
                                    )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Half-Day Option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isHalfDay,
                        onCheckedChange = { checked ->
                            isHalfDay = checked
                            if (checked) {
                                endDate = startDate
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "Apply as Half-Day leave (0.5 working day)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Reason Text Area
                Text(
                    text = "Reason for Leave",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("Please explain the reason for your leave request...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(Radius.md)
                )

                Spacer(modifier = Modifier.height(Spacing.xxl))

                // Submit Button
                ShiftTrackButton(
                    text = "Submit Leave Request",
                    icon = Icons.AutoMirrored.Filled.Send,
                    isLoading = submitState is LeaveSubmitState.Submitting,
                    onClick = {
                        viewModel.submitRequest(
                            leaveType = leaveType,
                            startDate = startDate,
                            endDate = endDate,
                            reason = reason,
                            isHalfDay = isHalfDay
                        )
                    }
                )
            }

            // Success Dialog
            if (submitState is LeaveSubmitState.Success) {
                val app = (submitState as LeaveSubmitState.Success).application
                AlertDialog(
                    onDismissRequest = {
                        viewModel.resetSubmitState()
                        onSubmissionComplete()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ShiftTrackTheme.statusColors.success.content,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text("Leave Request Submitted", style = MaterialTheme.typography.titleMedium)
                    },
                    text = {
                        Text(
                            text = "Your leave request for ${app.startDate} to ${app.endDate} has been submitted with authoritative status: ${app.status}. Your manager will review it shortly.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.resetSubmitState()
                                onSubmissionComplete()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ShiftTrackTheme.statusColors.success.content)
                        ) {
                            Text("View Leave History", color = MaterialTheme.colorScheme.surface)
                        }
                    }
                )
            }
        }
    }
}
