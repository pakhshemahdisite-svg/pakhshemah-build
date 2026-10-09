package com.pakhshmahdi.app.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.pakhshmahdi.app.core.AppConfig

private const val BRAND_LOGO_URL =
    "https://pakhshemahdi.com/wp-content/uploads/2026/06/pakshemahdi-main-logo.webp"

@Composable
fun BrandLogo(modifier: Modifier = Modifier) {
    val fallbackPainter = remember {
        val payload = GOLD_LOGO_B64_1 + GOLD_LOGO_B64_2 + GOLD_LOGO_B64_3
        val bytes = Base64.decode(payload, Base64.DEFAULT)
        BitmapPainter(BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap())
    }

    AsyncImage(
        model = BRAND_LOGO_URL,
        contentDescription = "لوگوی ${AppConfig.APP_NAME}",
        modifier = modifier,
        placeholder = fallbackPainter,
        error = fallbackPainter,
        fallback = fallbackPainter,
        contentScale = ContentScale.Fit
    )
}
