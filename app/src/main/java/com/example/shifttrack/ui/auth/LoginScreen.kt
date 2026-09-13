package com.example.shifttrack.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shifttrack.ui.common.ErrorBanner
import com.example.shifttrack.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val focusManager = LocalFocusManager.current

    var identifier by remember { mutableStateOf("alex.chen@shifttrack.com") }
    var password by remember { mutableStateOf("ShiftTrackPass123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showServerConfig by remember { mutableStateOf(false) }
    var editableServerUrl by remember { mutableStateOf(serverUrl) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Slate50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand Logo & Header
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BrandBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ShiftTrack",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Slate900
            )

            Text(
                text = "Employee Attendance & Leave System",
                fontSize = 14.sp,
                color = Slate500,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Enter your work credentials to access your shift",
                        fontSize = 13.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (uiState is AuthUiState.Error) {
                        ErrorBanner(
                            message = (uiState as AuthUiState.Error).message,
                            onRetry = { viewModel.clearError() }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Email / Employee ID Field
                    OutlinedTextField(
                        value = identifier,
                        onValueChange = { identifier = it },
                        label = { Text("Employee Email or ID") },
                        placeholder = { Text("e.g. ST-0104 or user@shifttrack.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Slate500)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Slate500)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = Slate500
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.login(identifier, password)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sign In Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.login(identifier, password)
                        },
                        enabled = uiState !is AuthUiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Demo Fill Chip
                    TextButton(
                        onClick = {
                            identifier = "alex.chen@shifttrack.com"
                            password = "ShiftTrackPass123"
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Use Demo Employee Account (Alex Chen)",
                            fontSize = 12.sp,
                            color = BrandBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Self-Hosted Server Configuration Expandable
            TextButton(
                onClick = { showServerConfig = !showServerConfig }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Slate500
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showServerConfig) "Hide Server Settings" else "Server Connection Settings",
                    fontSize = 13.sp,
                    color = Slate500
                )
            }

            AnimatedVisibility(visible = showServerConfig) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate100)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Self-Hosted API Server URL",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editableServerUrl,
                            onValueChange = { editableServerUrl = it },
                            placeholder = { Text("http://10.0.2.2:5000/") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.updateServerUrl(editableServerUrl)
                                showServerConfig = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save Server URL", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
