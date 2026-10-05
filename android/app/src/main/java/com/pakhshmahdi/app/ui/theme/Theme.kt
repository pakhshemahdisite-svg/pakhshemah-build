package com.pakhshmahdi.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.pakhshmahdi.app.R

object PMTheme {
    val colors: PMColors
        @Composable get() = LocalPMColors.current

    val dimensions: PMDimensions
        @Composable get() = LocalPMDimensions.current

    val shapes: PMShapes
        @Composable get() = LocalPMShapes.current
}

private val YekanBakhFaNum = FontFamily(
    Font(R.font.yekan_bakh_thin, FontWeight.Thin),
    Font(R.font.yekan_bakh_light, FontWeight.Light),
    Font(R.font.yekan_bakh_regular, FontWeight.Normal),
    Font(R.font.yekan_bakh_semibold, FontWeight.Medium),
    Font(R.font.yekan_bakh_semibold, FontWeight.SemiBold),
    Font(R.font.yekan_bakh_bold, FontWeight.Bold),
    Font(R.font.yekan_bakh_extrabold, FontWeight.ExtraBold),
    Font(R.font.yekan_bakh_black, FontWeight.Black)
)

private fun pmTextStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight
) = TextStyle(
    fontFamily = YekanBakhFaNum,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight
)

private fun lightScheme(c: PMColors) = lightColorScheme(
    primary = c.primary,
    onPrimary = Color.White,
    primaryContainer = c.surfaceElevated,
    onPrimaryContainer = c.textPrimary,
    secondary = c.accentGold,
    onSecondary = c.primaryDark,
    background = c.background,
    onBackground = c.textPrimary,
    surface = c.surface,
    onSurface = c.textPrimary,
    surfaceVariant = c.surfaceElevated,
    onSurfaceVariant = c.textSecondary,
    outline = c.border,
    error = c.error
)

private fun darkScheme(c: PMColors) = darkColorScheme(
    primary = c.accentGold,
    onPrimary = c.primaryDark,
    primaryContainer = c.surfaceElevated,
    onPrimaryContainer = c.textPrimary,
    secondary = c.accentGold,
    onSecondary = c.primaryDark,
    background = c.background,
    onBackground = c.textPrimary,
    surface = c.surface,
    onSurface = c.textPrimary,
    surfaceVariant = c.surfaceElevated,
    onSurfaceVariant = c.textSecondary,
    outline = c.border,
    error = c.error
)

private val PakhshMahdiTypography = Typography(
    displayLarge = pmTextStyle(42, 52, FontWeight.Black),
    displayMedium = pmTextStyle(38, 48, FontWeight.ExtraBold),
    displaySmall = pmTextStyle(34, 44, FontWeight.Black),
    headlineLarge = pmTextStyle(28, 38, FontWeight.Black),
    headlineMedium = pmTextStyle(23, 32, FontWeight.ExtraBold),
    headlineSmall = pmTextStyle(20, 29, FontWeight.Bold),
    titleLarge = pmTextStyle(18, 27, FontWeight.Bold),
    titleMedium = pmTextStyle(16, 24, FontWeight.SemiBold),
    titleSmall = pmTextStyle(14, 21, FontWeight.SemiBold),
    bodyLarge = pmTextStyle(15, 25, FontWeight.Normal),
    bodyMedium = pmTextStyle(14, 22, FontWeight.Normal),
    bodySmall = pmTextStyle(12, 19, FontWeight.Light),
    labelLarge = pmTextStyle(14, 20, FontWeight.Bold),
    labelMedium = pmTextStyle(12, 18, FontWeight.Medium),
    labelSmall = pmTextStyle(11, 16, FontWeight.Medium)
)

@Composable
fun PakhshMahdiTheme(content: @Composable () -> Unit) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (PMThemeController.mode) {
        PMThemeMode.SYSTEM -> systemDark
        PMThemeMode.LIGHT -> false
        PMThemeMode.DARK -> true
    }
    val palette = if (dark) PMDarkColors else PMLightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !dark
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = palette.background.toArgb()
        }
    }

    CompositionLocalProvider(
        LocalPMColors provides palette,
        LocalPMDimensions provides PMDimensions(),
        LocalPMShapes provides PMShapes(),
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(
            colorScheme = if (dark) darkScheme(palette) else lightScheme(palette),
            typography = PakhshMahdiTypography
        ) {
            ProvideTextStyle(PakhshMahdiTypography.bodyLarge) {
                content()
            }
        }
    }
}
