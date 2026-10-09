package com.pakhshmahdi.app.ui.theme

import androidx.compose.ui.graphics.Color

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

val PMGoldSoft = Color(0xFFEFD083)
val PMGoldDeep = Color(0xFFC99738)

val PMLightColors = PMColors(
    primary = Color(0xFF111111),
    primaryDark = Color(0xFF000000),
    accentGold = PMGoldSoft,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFFFBF2),
    textPrimary = Color(0xFF101010),
    textSecondary = Color(0xFF555555),
    textMuted = Color(0xFF8A8A86),
    border = Color(0xFFE9E1D1),
    success = Color(0xFF26865A),
    warning = PMGoldDeep,
    error = Color(0xFFC83D3D),
    scrim = Color(0x66000000)
)

val PMDarkColors = PMColors(
    primary = PMGoldSoft,
    primaryDark = Color(0xFF000000),
    accentGold = PMGoldSoft,
    background = Color(0xFF000000),
    surface = Color(0xFF0B0B0B),
    surfaceElevated = Color(0xFF15130F),
    textPrimary = Color(0xFFF8F8F5),
    textSecondary = Color(0xFFC8C6C0),
    textMuted = Color(0xFF8D8B85),
    border = Color(0xFF302A20),
    success = Color(0xFF5FC28B),
    warning = PMGoldSoft,
    error = Color(0xFFFF6B6B),
    scrim = Color(0xB3000000)
)

// Compatibility aliases.
val PMPrimary = Color(0xFF111111)
val PMSecondary = Color(0xFF000000)
val PMNavySoft = Color(0xFF1A1A1A)
val PMWhite = Color(0xFFFFFFFF)
val PMBorder = Color(0xFFE9E1D1)
val PMBackground = Color(0xFFFFFFFF)
val PMSurfaceSoft = Color(0xFFFFFBF2)
val PMSuccess = Color(0xFF26865A)
val PMWarning = PMGoldDeep
val PMError = Color(0xFFC83D3D)
val PMText = Color(0xFF101010)
val PMMuted = Color(0xFF8A8A86)
