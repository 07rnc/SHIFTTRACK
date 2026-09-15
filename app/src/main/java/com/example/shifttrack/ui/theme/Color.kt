package com.example.shifttrack.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Primary Brand Palette
val BrandBlue = Color(0xFF1D4ED8)
val BrandBlueDark = Color(0xFF1E3A8A)
val BrandBlueLight = Color(0xFFDBEAFE)
val BrandBlueAccent = Color(0xFF60A5FA)

// Neutral Palette
val Slate950 = Color(0xFF0B0F19)
val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate300 = Color(0xFFCBD5E1)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)

// Semantic Status Colors Data Model
data class StatusColor(
    val container: Color,
    val content: Color,
    val border: Color
)

data class ShiftTrackStatusColors(
    val success: StatusColor,
    val warning: StatusColor,
    val error: StatusColor,
    val info: StatusColor,
    val neutral: StatusColor
)

val LightStatusColors = ShiftTrackStatusColors(
    success = StatusColor(
        container = Color(0xFFDCFCE7),
        content = Color(0xFF15803D),
        border = Color(0xFF86EFAC)
    ),
    warning = StatusColor(
        container = Color(0xFFFEF3C7),
        content = Color(0xFFB45309),
        border = Color(0xFFFCD34D)
    ),
    error = StatusColor(
        container = Color(0xFFFEE2E2),
        content = Color(0xFFB91C1C),
        border = Color(0xFFFCA5A5)
    ),
    info = StatusColor(
        container = Color(0xFFDBEAFE),
        content = Color(0xFF1D4ED8),
        border = Color(0xFF93C5FD)
    ),
    neutral = StatusColor(
        container = Color(0xFFF1F5F9),
        content = Color(0xFF475569),
        border = Color(0xFFCBD5E1)
    )
)

val DarkStatusColors = ShiftTrackStatusColors(
    success = StatusColor(
        container = Color(0xFF064E3B),
        content = Color(0xFF6EE7B7),
        border = Color(0xFF047857)
    ),
    warning = StatusColor(
        container = Color(0xFF78350F),
        content = Color(0xFFFDE68A),
        border = Color(0xFFB45309)
    ),
    error = StatusColor(
        container = Color(0xFF7F1D1D),
        content = Color(0xFFFCA5A5),
        border = Color(0xFF991B1B)
    ),
    info = StatusColor(
        container = Color(0xFF1E3A8A),
        content = Color(0xFF93C5FD),
        border = Color(0xFF2563EB)
    ),
    neutral = StatusColor(
        container = Color(0xFF334155),
        content = Color(0xFFCBD5E1),
        border = Color(0xFF475569)
    )
)

val LocalStatusColors = staticCompositionLocalOf { LightStatusColors }
