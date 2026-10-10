package com.pakhshmahdi.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.pakhshmahdi.app.R
import com.pakhshmahdi.app.core.AppConfig

@Composable
fun BrandLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.brand_logo),
        contentDescription = "لوگوی ${AppConfig.APP_NAME}",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
