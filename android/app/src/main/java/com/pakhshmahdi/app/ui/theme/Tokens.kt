package com.pakhshmahdi.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class PMDimensions(
    val space2: Dp = 2.dp,
    val space4: Dp = 4.dp,
    val space6: Dp = 6.dp,
    val space8: Dp = 8.dp,
    val space10: Dp = 10.dp,
    val space12: Dp = 12.dp,
    val space16: Dp = 16.dp,
    val space20: Dp = 20.dp,
    val space24: Dp = 24.dp,
    val space32: Dp = 32.dp,
    val screenPadding: Dp = 16.dp,
    val bottomBarHeight: Dp = 72.dp,
    val buttonHeight: Dp = 56.dp,
    val cardRadius: Dp = 20.dp,
    val largeCardRadius: Dp = 28.dp,
    val buttonRadius: Dp = 16.dp,
    val fieldRadius: Dp = 16.dp
)

data class PMShapes(
    val small: RoundedCornerShape = RoundedCornerShape(12.dp),
    val medium: RoundedCornerShape = RoundedCornerShape(16.dp),
    val card: RoundedCornerShape = RoundedCornerShape(20.dp),
    val largeCard: RoundedCornerShape = RoundedCornerShape(28.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(50)
)

val LocalPMColors = staticCompositionLocalOf { PMLightColors }
val LocalPMDimensions = staticCompositionLocalOf { PMDimensions() }
val LocalPMShapes = staticCompositionLocalOf { PMShapes() }
