package com.pakhshmahdi.app.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pakhshmahdi.app.data.auth.AuthStore
import com.pakhshmahdi.app.data.auth.OrderSummary
import com.pakhshmahdi.app.data.local.LocalStore
import com.pakhshmahdi.app.ui.components.PMEmptyState
import com.pakhshmahdi.app.ui.components.PMErrorState
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOrder: (Long) -> Unit,
    onLogin: () -> Unit
) {
    val c = PMTheme.colors
    val vm: ProfileViewModel = viewModel()
    val state by vm.state.collectAsState()
    val user = AuthStore.user
    var changedOrderIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val previousStatuses = remember(user?.id) { LocalStore.loadOrderStatuses() }

    LaunchedEffect(user?.id) {
        if (user != null) vm.refresh()
    }

    LaunchedEffect(state.orders) {
        if (state.orders.isNotEmpty()) {
            changedOrderIds = state.orders
                .filter { order ->
                    val previous = previousStatuses[order.id]
                    when {
                        previous == null && previousStatuses.isNotEmpty() -> true
                        previous != null && previous != order.status -> true
                        else -> false
                    }
                }
                .map { it.id }
                .toSet()

            LocalStore.saveOrderStatuses(
                state.orders.associate { it.id to it.status }
            )
        }
    }

    Column(
        Modifier.fillMaxSize().background(c.background)
    ) {
        PMScreenTopBar(
            title = "اطلاعیه‌ها",
            subtitle = "آخرین وضعیت سفارش‌های شما",
            onBack = onBack,
            trailing = {
                IconButton(
                    onClick = vm::refresh,
                    enabled = user != null && !state.loading
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "بروزرسانی", tint = c.primary)
                }
            }
        )

        when {
            user == null -> {
                PMEmptyState(
                    icon = Icons.Outlined.NotificationsNone,
                    title = "برای دیدن اطلاعیه‌ها وارد شوید",
                    message = "وضعیت سفارش‌ها پس از ورود به حساب کاربری در این بخش نمایش داده می‌شود.",
                    action = "ورود به حساب",
                    onAction = onLogin,
                    modifier = Modifier.fillMaxSize()
                )
            }

            state.loading && state.orders.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = c.primary)
                }
            }

            state.error != null && state.orders.isEmpty() -> {
                PMErrorState(
                    message = state.error ?: "دریافت اطلاعیه‌ها انجام نشد.",
                    onRetry = vm::refresh,
                    modifier = Modifier.fillMaxSize()
                )
            }

            state.orders.isEmpty() -> {
                PMEmptyState(
                    icon = Icons.Outlined.NotificationsNone,
                    title = "اطلاعیه جدیدی ندارید",
                    message = "با ثبت سفارش، تغییر وضعیت آن اینجا نمایش داده می‌شود.",
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    items(state.orders.take(20), key = { it.id }) { order ->
                        OrderNotificationCard(
                            order = order,
                            isNew = order.id in changedOrderIds,
                            onClick = { onOrder(order.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderNotificationCard(
    order: OrderSummary,
    isNew: Boolean,
    onClick: () -> Unit
) {
    val c = PMTheme.colors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
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
                shape = PMTheme.shapes.medium,
                color = notificationColor(order.status).copy(alpha = .10f)
            ) {
                Icon(
                    Icons.Outlined.LocalShipping,
                    null,
                    tint = notificationColor(order.status),
                    modifier = Modifier.padding(10.dp).size(21.dp)
                )
            }

            Spacer(Modifier.width(11.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        notificationTitle(order),
                        color = c.textPrimary,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f)
                    )
                    if (isNew) {
                        Surface(
                            shape = PMTheme.shapes.pill,
                            color = c.primary.copy(alpha = .12f)
                        ) {
                            Text(
                                "جدید",
                                color = c.primary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    notificationMessage(order),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary
                )
                if (order.date.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        order.date.take(16),
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textMuted
                    )
                }
            }
        }
    }
}

private fun notificationTitle(order: OrderSummary): String = when (order.status) {
    "pending" -> "سفارش #${order.id} در انتظار پرداخت است"
    "processing" -> "سفارش #${order.id} در حال پردازش است"
    "on-hold" -> "سفارش #${order.id} در انتظار بررسی است"
    "completed" -> "سفارش #${order.id} تکمیل شده است"
    "cancelled" -> "سفارش #${order.id} لغو شده است"
    "refunded" -> "سفارش #${order.id} مرجوع شده است"
    "failed" -> "پرداخت سفارش #${order.id} ناموفق بوده است"
    else -> "وضعیت سفارش #${order.id} بروزرسانی شد"
}

private fun notificationMessage(order: OrderSummary): String = when (order.status) {
    "pending" -> "برای تکمیل خرید، وضعیت پرداخت سفارش را بررسی کنید."
    "processing" -> "سفارش شما ثبت شده و در حال آماده‌سازی است."
    "on-hold" -> "سفارش نیاز به بررسی فروشگاه دارد."
    "completed" -> "فرآیند این سفارش تکمیل شده است."
    "cancelled" -> "این سفارش لغو شده است."
    "refunded" -> "وضعیت سفارش به مرجوع‌شده تغییر کرده است."
    "failed" -> "پرداخت انجام نشده یا ناموفق بوده است."
    else -> "برای مشاهده جزئیات، روی این اطلاعیه بزنید."
}

@Composable
private fun notificationColor(status: String) = when (status) {
    "completed" -> PMTheme.colors.success
    "processing" -> PMTheme.colors.primary
    "pending", "on-hold" -> PMTheme.colors.warning
    "cancelled", "failed", "refunded" -> PMTheme.colors.error
    else -> PMTheme.colors.textSecondary
}
