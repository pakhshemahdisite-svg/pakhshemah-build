package com.pakhshmahdi.app.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.pakhshmahdi.app.core.AppConfig

@Composable
fun BrandLogo(modifier: Modifier = Modifier) {
    val bitmap = remember {
        val payload = GOLD_LOGO_B64_1 + GOLD_LOGO_B64_2 + GOLD_LOGO_B64_3
        val bytes = Base64.decode(payload, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
    }

    Image(
        bitmap = bitmap,
        contentDescription = "لوگوی ${AppConfig.APP_NAME}",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
