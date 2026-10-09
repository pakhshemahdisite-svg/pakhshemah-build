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
    primary = clientColor(BuildConfig.BRAND_PRIMARY, Color(0xFF111111)),
    primaryDark = clientColor(BuildConfig.BRAND_PRIMARY_DARK, Color(0xFF000000)),
    accentGold = clientColor(BuildConfig.BRAND_ACCENT, Color(0xFFD4AF37)),
    background = Color(0xFFF8F8F6),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF2F1ED),
    textPrimary = Color(0xFF121212),
    textSecondary = Color(0xFF5F5F5F),
    textMuted = Color(0xFF8A8A86),
    border = Color(0xFFE5E2DA),
    success = Color(0xFF26865A),
    warning = Color(0xFFC4932F),
    error = Color(0xFFC83D3D),
    scrim = Color(0x66000000)
)

val PMDarkColors = PMColors(
    primary = clientColor(BuildConfig.BRAND_ACCENT, Color(0xFFD4AF37)),
    primaryDark = clientColor(BuildConfig.BRAND_PRIMARY_DARK, Color(0xFF000000)),
    accentGold = clientColor(BuildConfig.BRAND_ACCENT, Color(0xFFD4AF37)),
    background = clientColor(BuildConfig.BRAND_DARK_BACKGROUND, Color(0xFF050505)),
    surface = Color(0xFF101010),
    surfaceElevated = Color(0xFF191919),
    textPrimary = Color(0xFFF8F8F5),
    textSecondary = Color(0xFFC8C6C0),
    textMuted = Color(0xFF8D8B85),
    border = Color(0xFF2B2924),
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
val PMBackground = Color(0xFFF8F8F6)
val PMSurfaceSoft = Color(0xFFF2F1ED)
val PMSuccess = Color(0xFF26865A)
val PMWarning = Color(0xFFC4932F)
val PMError = Color(0xFFC83D3D)
val PMText = Color(0xFF121212)
val PMMuted = Color(0xFF8A8A86)
