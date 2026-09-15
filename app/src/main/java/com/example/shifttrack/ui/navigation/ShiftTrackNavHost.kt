package com.example.shifttrack.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shifttrack.ShiftTrackApp
import com.example.shifttrack.location.LocationClient
import com.example.shifttrack.ui.attendance.*
import com.example.shifttrack.ui.auth.*
import com.example.shifttrack.ui.home.*
import com.example.shifttrack.ui.leave.*
import com.example.shifttrack.ui.notifications.*
import com.example.shifttrack.ui.profile.*
import com.example.shifttrack.ui.theme.*

@Composable
fun ShiftTrackNavHost(
    app: ShiftTrackApp,
    initialRoute: String? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authViewModel = remember { AuthViewModel(app.authRepository, app.sessionManager) }
    val homeViewModel = remember {
        HomeViewModel(
            app.authRepository,
            app.shiftRepository,
            app.attendanceRepository,
            app.notificationRepository,
            app.webSocketManager
        )
    }
    val attendanceViewModel = remember {
        AttendanceViewModel(
            app.attendanceRepository,
            LocationClient(app)
        )
    }
    val leaveViewModel = remember { LeaveViewModel(app.leaveRepository) }
    val notificationViewModel = remember { NotificationViewModel(app.notificationRepository) }

    val isAuthenticated = app.sessionManager.hasValidToken()
    val validAuthenticatedRoutes = listOf(
        Screen.Home.route,
        Screen.GpsAttendance.route,
        Screen.QrAttendance.route,
        Screen.AttendanceHistory.route,
        Screen.LeaveRequest.route,
        Screen.LeaveHistory.route,
        Screen.NotificationHistory.route,
        Screen.Profile.route
    )

    val resolvedStartDestination = if (!isAuthenticated) {
        Screen.Login.route
    } else if (!initialRoute.isNullOrBlank() && initialRoute in validAuthenticatedRoutes) {
        initialRoute
    } else {
        Screen.Home.route
    }

    val bottomBarScreens = listOf(
        Screen.Home.route,
        Screen.AttendanceHistory.route,
        Screen.LeaveHistory.route,
        Screen.Profile.route
    )

    val showBottomBar = currentRoute in bottomBarScreens

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Home.route,
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Home") },
                        label = { Text("Home", style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.AttendanceHistory.route,
                        onClick = {
                            navController.navigate(Screen.AttendanceHistory.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.History, contentDescription = "Attendance") },
                        label = { Text("Attendance", style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.LeaveHistory.route,
                        onClick = {
                            navController.navigate(Screen.LeaveHistory.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.EventBusy, contentDescription = "Leave") },
                        label = { Text("Leave", style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Profile.route,
                        onClick = {
                            navController.navigate(Screen.Profile.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                        label = { Text("Profile", style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = resolvedStartDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        homeViewModel.loadData(isRefresh = true)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateGpsAttendance = { navController.navigate(Screen.GpsAttendance.route) },
                    onNavigateQrAttendance = { navController.navigate(Screen.QrAttendance.route) },
                    onNavigateAttendanceHistory = { navController.navigate(Screen.AttendanceHistory.route) },
                    onNavigateLeaveRequest = { navController.navigate(Screen.LeaveRequest.route) },
                    onNavigateLeaveHistory = { navController.navigate(Screen.LeaveHistory.route) },
                    onNavigateNotifications = { navController.navigate(Screen.NotificationHistory.route) }
                )
            }

            composable(Screen.GpsAttendance.route) {
                GpsAttendanceScreen(
                    viewModel = attendanceViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateQrFallback = {
                        navController.popBackStack()
                        navController.navigate(Screen.QrAttendance.route)
                    }
                )
            }

            composable(Screen.QrAttendance.route) {
                QrAttendanceScreen(
                    viewModel = attendanceViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AttendanceHistory.route) {
                AttendanceHistoryScreen(
                    viewModel = attendanceViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.LeaveRequest.route) {
                LeaveRequestScreen(
                    viewModel = leaveViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSubmissionComplete = {
                        navController.navigate(Screen.LeaveHistory.route) {
                            popUpTo(Screen.LeaveRequest.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.LeaveHistory.route) {
                LeaveHistoryScreen(
                    viewModel = leaveViewModel,
                    onNavigateApplyLeave = { navController.navigate(Screen.LeaveRequest.route) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.NotificationHistory.route) {
                NotificationHistoryScreen(
                    viewModel = notificationViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNotificationClick = { route ->
                        when (route) {
                            Screen.LeaveHistory.route, "leave_history" -> navController.navigate(Screen.LeaveHistory.route)
                            Screen.AttendanceHistory.route, "attendance_history" -> navController.navigate(Screen.AttendanceHistory.route)
                            Screen.LeaveRequest.route, "leave_request" -> navController.navigate(Screen.LeaveRequest.route)
                            Screen.GpsAttendance.route, "gps_attendance" -> navController.navigate(Screen.GpsAttendance.route)
                            Screen.QrAttendance.route, "qr_attendance" -> navController.navigate(Screen.QrAttendance.route)
                            Screen.Profile.route, "profile" -> navController.navigate(Screen.Profile.route)
                            Screen.NotificationHistory.route, "notifications", "notification_history" -> navController.navigate(Screen.NotificationHistory.route)
                            Screen.Home.route, "home" -> navController.navigate(Screen.Home.route)
                            else -> {}
                        }
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    authRepository = app.authRepository,
                    sessionManager = app.sessionManager,
                    onLogoutSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
