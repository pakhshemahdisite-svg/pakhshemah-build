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
    primary = clientColor(BuildConfig.BRAND_PRIMARY, Color(0xFF0B2A57)),
    primaryDark = clientColor(BuildConfig.BRAND_PRIMARY_DARK, Color(0xFF071C3C)),
    accentGold = clientColor(BuildConfig.BRAND_ACCENT, Color(0xFFD7A84B)),
    background = Color(0xFFF5F8FC),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF0F4F9),
    textPrimary = Color(0xFF0B2344),
    textSecondary = Color(0xFF5D6A7D),
    textMuted = Color(0xFF8A96A8),
    border = Color(0xFFDCE4EE),
    success = Color(0xFF2F9E66),
    warning = Color(0xFFF0A638),
    error = Color(0xFFD94343),
    scrim = Color(0x6607182F)
)

val PMDarkColors = PMColors(
    primary = clientColor(BuildConfig.BRAND_ACCENT, Color(0xFFE4B85E)),
    primaryDark = clientColor(BuildConfig.BRAND_PRIMARY_DARK, Color(0xFF071526)),
    accentGold = clientColor(BuildConfig.BRAND_ACCENT, Color(0xFFE6B75D)),
    background = clientColor(BuildConfig.BRAND_DARK_BACKGROUND, Color(0xFF04111E)),
    surface = Color(0xFF0A1A2B),
    surfaceElevated = Color(0xFF102437),
    textPrimary = Color(0xFFF7F8FA),
    textSecondary = Color(0xFFC5CCD6),
    textMuted = Color(0xFF7E8A99),
    border = Color(0xFF26394A),
    success = Color(0xFF65C98B),
    warning = Color(0xFFE8B44F),
    error = Color(0xFFFF6B6B),
    scrim = Color(0x99000000)
)

// Legacy aliases kept only for binary/source compatibility while the UI layer is migrated.
// New UI code must use PMTheme.colors.
val PMPrimary = Color(0xFF0B2A57)
val PMSecondary = Color(0xFF071C3C)
val PMNavySoft = Color(0xFF17385F)
val PMWhite = Color(0xFFFFFFFF)
val PMBorder = Color(0xFFDCE4EE)
val PMBackground = Color(0xFFF5F8FC)
val PMSurfaceSoft = Color(0xFFF0F4F9)
val PMSuccess = Color(0xFF2F9E66)
val PMWarning = Color(0xFFF0A638)
val PMError = Color(0xFFD94343)
val PMText = Color(0xFF0B2344)
val PMMuted = Color(0xFF8A96A8)
