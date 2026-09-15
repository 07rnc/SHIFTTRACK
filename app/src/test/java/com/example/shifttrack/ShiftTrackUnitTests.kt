package com.example.shifttrack

import com.example.shifttrack.data.model.*
import com.example.shifttrack.data.repository.MockDataStore
import com.example.shifttrack.data.local.SessionManager
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ShiftTrackUnitTests {

    private lateinit var sessionManager: SessionManager
    private lateinit var mockDataStore: MockDataStore

    @Before
    fun setup() {
        sessionManager = mockk(relaxed = true)
        val testUser = UserDto(
            id = "emp_101",
            email = "alex.chen@shifttrack.com",
            fullName = "Alex Chen",
            employeeCode = "ST-0104",
            role = "EMPLOYEE"
        )
        every { sessionManager.currentUser } returns kotlinx.coroutines.flow.MutableStateFlow(testUser)
        mockDataStore = MockDataStore(sessionManager)
    }

    // ==================== AUTH TESTS ====================

    @Test
    fun testLogin_Success() = runBlocking {
        val result = mockDataStore.login("alex.chen@shifttrack.com", "validPass123")
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals("Alex Chen", user?.fullName)
        assertEquals("ST-0104", user?.employeeCode)
        assertEquals("EMPLOYEE", user?.role)
        verify { sessionManager.saveSession(any(), any(), any()) }
    }

    @Test
    fun testLogin_BlankCredentials_Fails() = runBlocking {
        val result = mockDataStore.login("", "")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("required", ignoreCase = true) == true)
    }

    @Test
    fun testLogout_ClearsSession() = runBlocking {
        val result = mockDataStore.logout()
        assertTrue(result.isSuccess)
        verify { sessionManager.clearSession() }
    }

    // ==================== SHIFT TESTS ====================

    @Test
    fun testGetCurrentShift_ReturnsAssignedShift() = runBlocking {
        val result = mockDataStore.getCurrentShift()
        assertTrue(result.isSuccess)
        val shift = result.getOrNull()
        assertNotNull(shift)
        assertEquals("09:00", shift?.startTime)
        assertEquals("17:00", shift?.endTime)
        assertTrue(shift?.geofenceRadiusMeters ?: 0.0 > 0.0)
    }

    // ==================== ATTENDANCE TESTS ====================

    @Test
    fun testClockIn_GPS_Success() = runBlocking {
        // Clock in near office headquarters
        val result = mockDataStore.clockInGps(28.6139, 77.2090, 10.0f)
        assertTrue(result.isSuccess)
        val record = result.getOrNull()
        assertNotNull(record)
        assertEquals("GPS", record?.method)
        assertNotNull(record?.clockInTime)

        // Verify state is now CLOCKED_IN
        val stateRes = mockDataStore.getAttendanceState()
        assertEquals(ClockState.CLOCKED_IN.name, stateRes.getOrNull()?.state)
    }

    @Test
    fun testClockIn_DuplicatePrevention_RejectsSecondClockIn() = runBlocking {
        // First clock-in
        val first = mockDataStore.clockInGps(28.6139, 77.2090, 10.0f)
        assertTrue(first.isSuccess)

        // Attempt second clock-in while already clocked in
        val second = mockDataStore.clockInGps(28.6139, 77.2090, 10.0f)
        assertTrue(second.isFailure)
        assertTrue(second.exceptionOrNull()?.message?.contains("Already clocked in", ignoreCase = true) == true)
    }

    @Test
    fun testClockIn_QR_ValidCode_Success() = runBlocking {
        val result = mockDataStore.clockInQr("SHIFTTRACK-OFFICE-MAIN-ENTRANCE-101")
        assertTrue(result.isSuccess)
        assertEquals("QR", result.getOrNull()?.method)
    }

    @Test
    fun testClockIn_QR_InvalidCode_RejectsAuthoritatively() = runBlocking {
        val result = mockDataStore.clockInQr("RANDOM_EXTERNAL_BARCODE_XYZ")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Invalid QR code", ignoreCase = true) == true)
    }

    @Test
    fun testClockOut_Success() = runBlocking {
        // Must clock in first
        mockDataStore.clockInGps(28.6139, 77.2090, 10.0f)

        val outRes = mockDataStore.clockOut(28.6139, 77.2090)
        assertTrue(outRes.isSuccess)
        val record = outRes.getOrNull()
        assertNotNull(record?.clockOutTime)

        // State is now CLOCKED_OUT
        val stateRes = mockDataStore.getAttendanceState()
        assertEquals(ClockState.CLOCKED_OUT.name, stateRes.getOrNull()?.state)
    }

    @Test
    fun testAttendanceHistory_ReturnsRecords() = runBlocking {
        val historyRes = mockDataStore.getAttendanceHistory()
        assertTrue(historyRes.isSuccess)
        val history = historyRes.getOrNull()
        assertNotNull(history)
        assertTrue(history!!.isNotEmpty())
    }

    // ==================== LEAVE TESTS ====================

    @Test
    fun testLeaveSubmission_Success_PreservesBackendPendingStatus() = runBlocking {
        val req = LeaveRequestDto(
            leaveType = "SICK",
            startDate = "2026-10-01",
            endDate = "2026-10-02",
            reason = "Medical consultation and rest",
            isHalfDay = false
        )
        val result = mockDataStore.submitLeave(req)
        assertTrue(result.isSuccess)
        val leave = result.getOrNull()
        assertNotNull(leave)
        // EXACT backend status check
        assertEquals(BackendLeaveStatus.PENDING, leave?.status)
        assertEquals("SICK", leave?.leaveType)
    }

    @Test
    fun testLeaveSubmission_InvalidDateRange_Rejects() = runBlocking {
        val req = LeaveRequestDto(
            leaveType = "CASUAL",
            startDate = "2026-10-10",
            endDate = "2026-10-05", // End date before start date
            reason = "Vacation"
        )
        val result = mockDataStore.submitLeave(req)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("cannot be after", ignoreCase = true) == true)
    }

    @Test
    fun testLeaveSubmission_EmptyReason_Rejects() = runBlocking {
        val req = LeaveRequestDto(
            leaveType = "CASUAL",
            startDate = "2026-10-01",
            endDate = "2026-10-02",
            reason = ""
        )
        val result = mockDataStore.submitLeave(req)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("reason", ignoreCase = true) == true)
    }

    @Test
    fun testLeaveHistory_PreservesExactBackendStatuses() = runBlocking {
        val historyRes = mockDataStore.getLeaveHistory()
        assertTrue(historyRes.isSuccess)
        val history = historyRes.getOrNull()
        assertNotNull(history)
        assertTrue(history!!.isNotEmpty())

        val statuses = history.map { it.status }
        assertTrue(statuses.contains(BackendLeaveStatus.PENDING))
        assertTrue(statuses.contains(BackendLeaveStatus.APPROVED))
        assertTrue(statuses.contains(BackendLeaveStatus.REJECTED))
    }

    // ==================== NOTIFICATIONS TESTS ====================

    @Test
    fun testNotifications_FetchAndMarkAsRead() = runBlocking {
        val notifRes = mockDataStore.getNotifications()
        assertTrue(notifRes.isSuccess)
        val list = notifRes.getOrNull()
        assertNotNull(list)
        assertTrue(list!!.isNotEmpty())

        val firstNotif = list.first()
        val markRes = mockDataStore.markNotificationRead(firstNotif.id)
        assertTrue(markRes.isSuccess)

        val updatedList = mockDataStore.getNotifications().getOrNull()
        assertTrue(updatedList?.find { it.id == firstNotif.id }?.isRead == true)
    }

    // ==================== EDGE CASES & DOMAIN VALIDATION TESTS ====================

    @Test
    fun testLeaveSubmission_SingleDay_CalculatesOneDay() = runBlocking {
        val req = LeaveRequestDto(
            leaveType = "CASUAL",
            startDate = "2026-10-05",
            endDate = "2026-10-05",
            reason = "Personal work",
            isHalfDay = false
        )
        val result = mockDataStore.submitLeave(req)
        assertTrue(result.isSuccess)
        assertEquals(1.0, result.getOrNull()?.daysCount ?: 0.0, 0.001)
    }

    @Test
    fun testLeaveSubmission_MultiDay_CalculatesAccurateDays() = runBlocking {
        val req = LeaveRequestDto(
            leaveType = "ANNUAL",
            startDate = "2026-10-01",
            endDate = "2026-10-04", // 4 days inclusive
            reason = "Family vacation trip",
            isHalfDay = false
        )
        val result = mockDataStore.submitLeave(req)
        assertTrue(result.isSuccess)
        assertEquals(4.0, result.getOrNull()?.daysCount ?: 0.0, 0.001)
    }

    @Test
    fun testLeaveSubmission_HalfDay_CalculatesHalfDay() = runBlocking {
        val req = LeaveRequestDto(
            leaveType = "CASUAL",
            startDate = "2026-10-05",
            endDate = "2026-10-05",
            reason = "Afternoon appointment",
            isHalfDay = true
        )
        val result = mockDataStore.submitLeave(req)
        assertTrue(result.isSuccess)
        assertEquals(0.5, result.getOrNull()?.daysCount ?: 0.0, 0.001)
    }

    @Test
    fun testLeaveSubmission_HalfDay_MultiDate_Rejects() = runBlocking {
        val req = LeaveRequestDto(
            leaveType = "CASUAL",
            startDate = "2026-10-01",
            endDate = "2026-10-03",
            reason = "Multi day half day conflict",
            isHalfDay = true
        )
        val result = mockDataStore.submitLeave(req)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Half-day leave must be for a single date", ignoreCase = true) == true)
    }

    @Test
    fun testServerUrlSanitization() {
        val context = mockk<android.content.Context>(relaxed = true)
        val prefs = mockk<android.content.SharedPreferences>(relaxed = true)
        every { context.getSharedPreferences(any(), any()) } returns prefs

        val manager = SessionManager(context)
        assertEquals("http://10.0.2.2:5000/", manager.sanitizeServerUrl("10.0.2.2:5000"))
        assertEquals("http://10.0.2.2:5000/", manager.sanitizeServerUrl("http://10.0.2.2:5000"))
        assertEquals("https://api.shifttrack.com/", manager.sanitizeServerUrl("https://api.shifttrack.com"))
        assertEquals("http://10.0.2.2:5000/", manager.sanitizeServerUrl("   "))
    }
}
