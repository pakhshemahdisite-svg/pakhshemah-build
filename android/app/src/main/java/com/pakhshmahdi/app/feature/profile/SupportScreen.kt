package com.pakhshmahdi.app.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pakhshmahdi.app.core.AppConfig
import com.pakhshmahdi.app.ui.components.PMPrimaryButton
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun SupportScreen(onBack: () -> Unit) {
    val c = PMTheme.colors
    val uriHandler = LocalUriHandler.current

    Column(
        Modifier.fillMaxSize().background(c.background)
    ) {
        PMScreenTopBar(
            title = "پشتیبانی",
            subtitle = "پاسخ‌گویی به سوالات و مشکلات",
            onBack = onBack
        )

        Column(
            Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(92.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = c.primary.copy(alpha = .10f)
            ) {
                Icon(
                    Icons.Outlined.HeadsetMic,
                    null,
                    tint = c.primary,
                    modifier = Modifier.padding(22.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "چطور می‌توانیم کمک کنیم؟",
                style = MaterialTheme.typography.headlineSmall,
                color = c.textPrimary,
                fontWeight = FontWeight.Black
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "برای پیگیری سفارش یا سوالات خرید از پشتیبانی رسمی ${AppConfig.APP_NAME} استفاده کنید.",
                color = c.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(24.dp))

            PMPrimaryButton(
                text = "ثبت و پیگیری تیکت",
                onClick = {
                    uriHandler.openUri(AppConfig.SUPPORT_URL)
                },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Outlined.ChatBubbleOutline
            )

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    uriHandler.openUri(AppConfig.CONTACT_URL)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = PMTheme.shapes.medium,
                border = BorderStroke(1.dp, c.border)
            ) {
                Icon(Icons.Outlined.Phone, null, tint = c.primary)
                Spacer(Modifier.width(8.dp))
                Text("اطلاعات تماس", color = c.textPrimary)
            }
        }
    }
}
