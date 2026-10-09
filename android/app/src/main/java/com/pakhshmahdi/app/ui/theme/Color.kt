package com.pakhshmahdi.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.pakhshmahdi.app.BuildConfig

private fun clientColor(value: String, fallback: Color): Color =
    runCatching { Color(android.graphics.Color.parseColor(value)) }
        .getOrDefault(fallback)

data class PMColors(
    val primary: Color,
    val primaryDark: Color,
    val accentGold: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val scrim: Color
)

val PMLightColors = PMColors(
    primary = Color(0xFF111111),
    primaryDark = Color(0xFF000000),
    accentGold = Color(0xFFD4AF37),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF7F7F5),
    textPrimary = Color(0xFF121212),
    textSecondary = Color(0xFF5F5F5F),
    textMuted = Color(0xFF8A8A86),
    border = Color(0xFFE8E6E0),
    success = Color(0xFF26865A),
    warning = Color(0xFFC4932F),
    error = Color(0xFFC83D3D),
    scrim = Color(0x66000000)
)

val PMDarkColors = PMColors(
    primary = Color(0xFFD4AF37),
    primaryDark = Color(0xFF000000),
    accentGold = Color(0xFFD4AF37),
    background = Color(0xFF000000),
    surface = Color(0xFF0B0B0B),
    surfaceElevated = Color(0xFF151515),
    textPrimary = Color(0xFFF8F8F5),
    textSecondary = Color(0xFFC8C6C0),
    textMuted = Color(0xFF8D8B85),
    border = Color(0xFF2A2720),
    success = Color(0xFF5FC28B),
    warning = Color(0xFFD4AF37),
    error = Color(0xFFFF6B6B),
    scrim = Color(0xB3000000)
)

// Compatibility aliases. New UI code should read colors through PMTheme.colors.
val PMPrimary = Color(0xFF111111)
val PMSecondary = Color(0xFF000000)
val PMNavySoft = Color(0xFF1A1A1A)
val PMWhite = Color(0xFFFFFFFF)
val PMBorder = Color(0xFFE5E2DA)
val PMBackground = Color(0xFFFFFFFF)
val PMSurfaceSoft = Color(0xFFF7F7F5)
val PMSuccess = Color(0xFF26865A)
val PMWarning = Color(0xFFC4932F)
val PMError = Color(0xFFC83D3D)
val PMText = Color(0xFF121212)
val PMMuted = Color(0xFF8A8A86)
