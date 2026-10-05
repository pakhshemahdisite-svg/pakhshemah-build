package com.pakhshmahdi.app.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pakhshmahdi.app.data.auth.AuthStore
import com.pakhshmahdi.app.ui.components.PMEmptyState
import com.pakhshmahdi.app.ui.components.PMErrorState
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun OrderTrackingScreen(
    orderId: Long,
    onBack: () -> Unit,
    onLogin: () -> Unit
) {
    val c = PMTheme.colors
    val vm: OrderDetailViewModel = viewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(orderId) { vm.load(orderId) }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        PMScreenTopBar(
            title = "پیگیری سفارش",
            subtitle = "سفارش #$orderId",
            onBack = onBack,
            trailing = {
                IconButton(
                    onClick = { vm.load(orderId, force = true) },
                    enabled = !state.loading
                ) {
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = "بروزرسانی وضعیت",
                        tint = c.primary
                    )
                }
            }
        )

        when {
            !AuthStore.isLoggedIn -> {
                PMEmptyState(
                    icon = Icons.Outlined.LocalShipping,
                    title = "برای پیگیری سفارش وارد شوید",
                    message = "نشست حساب کاربری شما فعال نیست.",
                    action = "ورود به حساب",
                    onAction = onLogin,
                    modifier = Modifier.fillMaxSize()
                )
            }

            state.loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = c.primary)
                }
            }
            state.detail == null -> {
                PMErrorState(
                    message = state.error ?: "اطلاعات پیگیری دریافت نشد.",
                    onRetry = { vm.load(orderId, force = true) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                val detail = state.detail!!
                val currentStep = statusStep(detail.status)

                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = PMTheme.shapes.largeCard,
                        color = c.surface,
                        border = BorderStroke(1.dp, c.border)
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Icon(
                                Icons.Outlined.LocalShipping,
                                null,
                                tint = c.primary,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                statusTitle(detail.status),
                                style = MaterialTheme.typography.titleLarge,
                                color = c.textPrimary,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                if (detail.shippingMethod.isBlank()) "روش ارسال پس از پردازش سفارش مشخص می‌شود."
                                else "روش ارسال: ${detail.shippingMethod}",
                                color = c.textSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        "وضعیت سفارش",
                        style = MaterialTheme.typography.titleLarge,
                        color = c.textPrimary,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(12.dp))

                    TrackingStep(
                        index = 0,
                        activeStep = currentStep,
                        title = "ثبت سفارش",
                        subtitle = "سفارش با موفقیت در سیستم ثبت شده است.",
                        icon = Icons.Outlined.Check
                    )
                    TrackingStep(
                        index = 1,
                        activeStep = currentStep,
                        title = "در حال پردازش",
                        subtitle = "سفارش توسط پخش مهدی در حال آماده‌سازی است.",
                        icon = Icons.Outlined.Store
                    )
                    TrackingStep(
                        index = 2,
                        activeStep = currentStep,
                        title = "ارسال سفارش",
                        subtitle = "سفارش به واحد ارسال تحویل داده شده است.",
                        icon = Icons.Outlined.LocalShipping
                    )
                    TrackingStep(
                        index = 3,
                        activeStep = currentStep,
                        title = "تحویل شده",
                        subtitle = "سفارش به مقصد تحویل شده است.",
                        icon = Icons.Outlined.Verified,
                        showLine = false
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun TrackingStep(
    index: Int,
    activeStep: Int,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    showLine: Boolean = true
) {
    val c = PMTheme.colors
    val complete = index <= activeStep

    Row(Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = if (complete) c.primary else c.surfaceElevated,
                border = BorderStroke(1.dp, if (complete) c.primary else c.border)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        null,
                        tint = if (complete && c.primary == c.accentGold) c.primaryDark
                        else if (complete) androidx.compose.ui.graphics.Color.White
                        else c.textMuted,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            if (showLine) {
                Box(
                    Modifier
                        .width(2.dp)
                        .height(48.dp)
                        .background(if (index < activeStep) c.primary else c.border)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.padding(top = 4.dp).weight(1f)) {
            Text(
                title,
                color = if (complete) c.textPrimary else c.textMuted,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(3.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary
            )
        }
    }
}

internal fun normalizedOrderStatus(status: String): String =
    status.trim().lowercase().removePrefix("wc-")

internal fun statusStep(status: String): Int = when (normalizedOrderStatus(status)) {
    "completed", "delivered" -> 3
    "shipped", "out-for-delivery", "out_for_delivery" -> 2
    "processing" -> 1
    "on-hold", "pending" -> 0
    else -> 0
}

internal fun statusTitle(status: String): String = when (val normalized = normalizedOrderStatus(status)) {
    "completed", "delivered" -> "سفارش تحویل شده است"
    "shipped" -> "سفارش ارسال شده است"
    "out-for-delivery", "out_for_delivery" -> "سفارش در مسیر تحویل است"
    "processing" -> "سفارش در حال پردازش است"
    "on-hold" -> "سفارش در انتظار بررسی است"
    "pending" -> "در انتظار پرداخت"
    "cancelled" -> "سفارش لغو شده است"
    "refunded" -> "سفارش مرجوع شده است"
    "failed" -> "پرداخت یا سفارش ناموفق بوده است"
    else -> "وضعیت سفارش: $normalized"
}
