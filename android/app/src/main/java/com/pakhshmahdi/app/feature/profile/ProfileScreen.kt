package com.pakhshmahdi.app.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pakhshmahdi.app.core.AppConfig
import com.pakhshmahdi.app.data.auth.AuthStore
import com.pakhshmahdi.app.ui.components.BrandLogo
import com.pakhshmahdi.app.ui.components.PMPrimaryButton
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun ProfileScreen(
    onLogin: () -> Unit,
    onEdit: () -> Unit,
    onOrders: () -> Unit,
    onNotifications: () -> Unit,
    onSupport: () -> Unit,
    onSettings: () -> Unit
) {
    val c = PMTheme.colors
    val vm: ProfileViewModel = viewModel()
    val state by vm.state.collectAsState()
    val user = AuthStore.user

    LaunchedEffect(user?.id) {
        if (user != null) vm.refresh()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        PMScreenTopBar(
            title = "پروفایل من",
            subtitle = if (user == null) "حساب مهمان" else "مدیریت حساب و سفارش‌ها"
        )

        if (user == null) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = PMTheme.shapes.largeCard,
                color = c.surface,
                border = BorderStroke(1.dp, c.border)
            ) {
                Column(
                    Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BrandLogo(Modifier.width(150.dp).height(110.dp))
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "به حساب ${AppConfig.APP_NAME} وارد شوید",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = c.textPrimary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "برای مشاهده سفارش‌ها، آدرس‌ها و پیگیری خرید وارد حساب شوید.",
                        color = c.textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(18.dp))
                    PMPrimaryButton(
                        text = "ورود یا عضویت",
                        onClick = onLogin,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Outlined.Login
                    )
                }
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = PMTheme.shapes.largeCard,
                color = c.surface,
                border = BorderStroke(1.dp, c.border),
                shadowElevation = 1.dp
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(70.dp),
                        shape = CircleShape,
                        color = c.surfaceElevated,
                        border = BorderStroke(1.dp, c.border)
                    ) {
                        BrandLogo(Modifier.padding(9.dp))
                    }

                    Spacer(Modifier.width(13.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            user.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = c.textPrimary
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            user.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.primary
                        )
                        if (user.email.isNotBlank()) {
                            Text(
                                user.email,
                                style = MaterialTheme.typography.labelMedium,
                                color = c.textSecondary
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.size(40.dp).clickable(onClick = onEdit),
                        shape = CircleShape,
                        color = c.accentGold.copy(alpha = .11f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Edit, null, tint = c.accentGold, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileMetric(
                    value = if (state.loading) "…" else state.orders.size.toString(),
                    label = "سفارش",
                    modifier = Modifier.weight(1f)
                )
                ProfileMetric(
                    value = if (user.billing.address1.isBlank()) "۰" else "۱",
                    label = "آدرس",
                    modifier = Modifier.weight(1f)
                )
                ProfileMetric(
                    value = "فعال",
                    label = "حساب",
                    modifier = Modifier.weight(1f)
                )
            }

            if (!state.error.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = PMTheme.shapes.medium,
                    color = c.error.copy(alpha = .08f),
                    border = BorderStroke(1.dp, c.error.copy(alpha = .22f))
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            state.error ?: "دریافت اطلاعات حساب انجام نشد.",
                            color = c.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = vm::refresh) {
                            Text("تلاش دوباره", color = c.primary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            ProfileMenuItem(
                icon = Icons.Outlined.ReceiptLong,
                title = "سفارش‌های من",
                subtitle = "مشاهده و پیگیری سفارش‌ها",
                onClick = onOrders
            )
            ProfileMenuItem(
                icon = Icons.Outlined.LocationOn,
                title = "اطلاعات و آدرس‌ها",
                subtitle = "ویرایش مشخصات و آدرس پیش‌فرض",
                onClick = onEdit
            )
            if (AppConfig.FEATURE_NOTIFICATIONS) {
                ProfileMenuItem(
                    icon = Icons.Outlined.NotificationsNone,
                    title = "اطلاعیه‌ها",
                    subtitle = "پیام‌ها و وضعیت سفارش",
                    onClick = onNotifications
                )
            }
            if (AppConfig.FEATURE_SUPPORT) {
                ProfileMenuItem(
                    icon = Icons.Outlined.HeadsetMic,
                    title = "پشتیبانی",
                    subtitle = "ارتباط با پشتیبانی ${AppConfig.APP_NAME}",
                    onClick = onSupport
                )
            }
            ProfileMenuItem(
                icon = Icons.Outlined.Settings,
                title = "تنظیمات",
                subtitle = "ظاهر، تم و تنظیمات برنامه",
                onClick = onSettings
            )

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = vm::logout,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = PMTheme.shapes.medium,
                border = BorderStroke(1.dp, c.error.copy(alpha = .5f))
            ) {
                Icon(Icons.Outlined.Logout, null, tint = c.error)
                Spacer(Modifier.width(8.dp))
                Text("خروج از حساب", color = c.error)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileMetric(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val c = PMTheme.colors
    Surface(
        modifier = modifier,
        shape = PMTheme.shapes.card,
        color = c.surface,
        border = BorderStroke(1.dp, c.border)
    ) {
        Column(
            Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = c.primary, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(3.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = c.textMuted)
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val c = PMTheme.colors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable(onClick = onClick),
        shape = PMTheme.shapes.card,
        color = c.surface,
        border = BorderStroke(1.dp, c.border)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = PMTheme.shapes.medium,
                color = c.primary.copy(alpha = .09f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = c.accentGold, modifier = Modifier.size(21.dp))
                }
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = c.textPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
            }
            Icon(Icons.Outlined.ChevronLeft, null, tint = c.textMuted)
        }
    }
}
