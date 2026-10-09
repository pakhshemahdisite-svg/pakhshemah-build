package com.pakhshmahdi.app.feature.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pakhshmahdi.app.BuildConfig
import com.pakhshmahdi.app.core.AppConfig
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.components.PMSelectableCard
import com.pakhshmahdi.app.ui.theme.PMTheme
import com.pakhshmahdi.app.ui.theme.PMThemeController
import com.pakhshmahdi.app.ui.theme.PMThemeMode
import androidx.core.content.ContextCompat

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val c = PMTheme.colors
    val context = LocalContext.current
    var notificationGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationGranted = granted
    }

    Column(
        Modifier.fillMaxSize().background(c.background)
    ) {
        PMScreenTopBar(
            title = "تنظیمات",
            subtitle = "ظاهر و تنظیمات برنامه",
            onBack = onBack
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                "حالت نمایش",
                style = MaterialTheme.typography.titleLarge,
                color = c.textPrimary
            )
            Spacer(Modifier.height(10.dp))

            PMSelectableCard(
                selected = PMThemeController.mode == PMThemeMode.SYSTEM,
                title = "مطابق تنظیمات گوشی",
                subtitle = "تم روشن یا تیره بر اساس تنظیمات سیستم",
                onClick = { PMThemeController.updateMode(PMThemeMode.SYSTEM) },
                leading = { Icon(Icons.Outlined.PhoneAndroid, null, tint = c.primary) }
            )

            Spacer(Modifier.height(8.dp))

            PMSelectableCard(
                selected = PMThemeController.mode == PMThemeMode.LIGHT,
                title = "حالت روشن",
                subtitle = "سفید، مشکی و Accent طلایی",
                onClick = { PMThemeController.updateMode(PMThemeMode.LIGHT) },
                leading = { Icon(Icons.Outlined.LightMode, null, tint = c.primary) }
            )

            Spacer(Modifier.height(8.dp))

            PMSelectableCard(
                selected = PMThemeController.mode == PMThemeMode.DARK,
                title = "حالت تیره",
                subtitle = "مشکی عمیق با جزئیات طلایی",
                onClick = { PMThemeController.updateMode(PMThemeMode.DARK) },
                leading = { Icon(Icons.Outlined.DarkMode, null, tint = c.primary) }
            )

            Spacer(Modifier.height(20.dp))

            if (AppConfig.FEATURE_NOTIFICATIONS) {
                Text(
                    "اعلان‌های سفارش",
                    style = MaterialTheme.typography.titleLarge,
                    color = c.textPrimary
                )
                Spacer(Modifier.height(10.dp))
    
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = PMTheme.shapes.card,
                    color = c.surface,
                    border = BorderStroke(1.dp, c.border)
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.NotificationsActive,
                            null,
                            tint = if (notificationGranted) c.success else c.primary
                        )
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (notificationGranted) "اعلان تغییر وضعیت فعال است" else "مجوز اعلان غیرفعال است",
                                color = c.textPrimary,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "برنامه تغییر وضعیت سفارش‌ها را در پس‌زمینه بررسی می‌کند.",
                                color = c.textSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (!notificationGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            TextButton(
                                onClick = {
                                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            ) {
                                Text("فعال‌سازی", color = c.primary)
                            }
                        }
                    }
                }
    
    
            }

            Spacer(Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = PMTheme.shapes.card,
                color = c.surface,
                border = BorderStroke(1.dp, c.border)
            ) {
                Column(Modifier.padding(15.dp)) {
                    Text(
                        "${AppConfig.APP_NAME} • ${AppConfig.APP_SUBTITLE}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textSecondary
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "نسخه ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textMuted
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "توسعه دهنده امیر بخشی 09359492927",
                style = MaterialTheme.typography.labelLarge,
                color = c.textSecondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
