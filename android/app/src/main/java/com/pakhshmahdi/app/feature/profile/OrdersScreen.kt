package com.pakhshmahdi.app.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
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
import com.pakhshmahdi.app.ui.components.PMEmptyState
import com.pakhshmahdi.app.ui.components.PMErrorState
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.components.toman
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun OrdersScreen(
    onBack: () -> Unit,
    onOrder: (Long) -> Unit,
    onLogin: () -> Unit
) {
    val c = PMTheme.colors
    val vm: ProfileViewModel = viewModel()
    val state by vm.state.collectAsState()
    var filter by remember { mutableStateOf("all") }

    LaunchedEffect(AuthStore.user?.id) {
        if (AuthStore.user != null) vm.refresh()
    }

    val visible = state.orders.filter { order ->
        when (filter) {
            "processing" -> order.status in listOf("processing", "on-hold")
            "completed" -> order.status == "completed"
            "cancelled" -> order.status in listOf("cancelled", "failed", "refunded")
            "pending" -> order.status == "pending"
            else -> true
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        PMScreenTopBar(
            title = "سفارش‌های من",
            subtitle = if (state.loading) "در حال بروزرسانی..." else "${state.orders.size} سفارش",
            onBack = onBack,
            trailing = {
                IconButton(
                    onClick = vm::refresh,
                    enabled = AuthStore.isLoggedIn && !state.loading
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "بروزرسانی", tint = c.primary)
                }
            }
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { OrderFilter("همه", "all", filter) { filter = it } }
            item { OrderFilter("در انتظار", "pending", filter) { filter = it } }
            item { OrderFilter("در حال پردازش", "processing", filter) { filter = it } }
            item { OrderFilter("تحویل شده", "completed", filter) { filter = it } }
            item { OrderFilter("لغو شده", "cancelled", filter) { filter = it } }
        }

        Spacer(Modifier.height(10.dp))

        when {
            !AuthStore.isLoggedIn -> {
                PMEmptyState(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "برای مشاهده سفارش‌ها وارد شوید",
                    message = "سفارش‌های حساب کاربری شما پس از ورود در این بخش نمایش داده می‌شوند.",
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
                    message = state.error ?: "دریافت سفارش‌ها انجام نشد.",
                    onRetry = vm::refresh,
                    modifier = Modifier.fillMaxSize()
                )
            }
            visible.isEmpty() -> {
                PMEmptyState(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "سفارشی در این بخش نیست",
                    message = "سفارش‌های شما پس از ثبت در این قسمت نمایش داده می‌شوند.",
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(visible, key = { it.id }) { order ->
                        OrderSummaryCard(order, onClick = { onOrder(order.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderFilter(
    title: String,
    value: String,
    current: String,
    onSelect: (String) -> Unit
) {
    val c = PMTheme.colors
    FilterChip(
        selected = current == value,
        onClick = { onSelect(value) },
        label = { Text(title) },
        shape = PMTheme.shapes.pill,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = c.surface,
            labelColor = c.textSecondary,
            selectedContainerColor = c.primary,
            selectedLabelColor = if (c.primary == c.accentGold) c.primaryDark else androidx.compose.ui.graphics.Color.White
        )
    )
}

@Composable
private fun OrderSummaryCard(
    order: OrderSummary,
    onClick: () -> Unit
) {
    val c = PMTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = PMTheme.shapes.card,
        color = c.surface,
        border = BorderStroke(1.dp, c.border)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "سفارش #${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = c.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = PMTheme.shapes.pill,
                    color = statusColor(order.status).copy(alpha = .12f)
                ) {
                    Text(
                        statusLabel(order.status),
                        color = statusColor(order.status),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row {
                Text(
                    if (order.date.isBlank()) "—" else order.date.take(10),
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textMuted,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${order.itemCount} کالا",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textSecondary
                )
            }

            Spacer(Modifier.height(9.dp))
            HorizontalDivider(color = c.border)
            Spacer(Modifier.height(9.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("مبلغ کل", color = c.textSecondary, modifier = Modifier.weight(1f))
                Text(
                    toman(order.total),
                    color = c.primary,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

private fun statusLabel(status: String): String = when (status) {
    "pending" -> "در انتظار پرداخت"
    "processing" -> "در حال پردازش"
    "on-hold" -> "در انتظار بررسی"
    "completed" -> "تحویل شده"
    "cancelled" -> "لغو شده"
    "refunded" -> "مرجوع شده"
    "failed" -> "ناموفق"
    else -> status
}

@Composable
private fun statusColor(status: String) = when (status) {
    "completed" -> PMTheme.colors.success
    "processing" -> PMTheme.colors.primary
    "pending", "on-hold" -> PMTheme.colors.warning
    "cancelled", "failed", "refunded" -> PMTheme.colors.error
    else -> PMTheme.colors.textSecondary
}
