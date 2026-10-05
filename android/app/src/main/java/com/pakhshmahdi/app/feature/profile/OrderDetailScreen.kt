package com.pakhshmahdi.app.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pakhshmahdi.app.data.auth.AuthStore
import com.pakhshmahdi.app.data.auth.OrderLineItem
import com.pakhshmahdi.app.ui.components.PMEmptyState
import com.pakhshmahdi.app.ui.components.PMPrimaryButton
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.components.ProductImage
import com.pakhshmahdi.app.ui.components.toman
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun OrderDetailScreen(
    orderId: Long,
    onBack: () -> Unit,
    onTrack: () -> Unit,
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
            title = "جزئیات سفارش",
            subtitle = "سفارش #$orderId",
            onBack = onBack
        )

        when {
            !AuthStore.isLoggedIn -> {
                PMEmptyState(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "نشست حساب کاربری فعال نیست",
                    message = "برای مشاهده جزئیات این سفارش دوباره وارد حساب شوید.",
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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error ?: "اطلاعات سفارش دریافت نشد.", color = c.textSecondary)
                }
            }

            else -> {
                val detail = state.detail!!

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Surface(
                            shape = PMTheme.shapes.card,
                            color = c.surface,
                            border = BorderStroke(1.dp, c.border)
                        ) {
                            Column(Modifier.padding(15.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.ReceiptLong, null, tint = c.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "سفارش #${detail.id}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = c.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = PMTheme.shapes.pill,
                                        color = detailStatusColor(detail.status).copy(alpha = .12f)
                                    ) {
                                        Text(
                                            detailStatusLabel(detail.status),
                                            color = detailStatusColor(detail.status),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                if (detail.date.isNotBlank()) {
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        "تاریخ: ${detail.date.take(16)}",
                                        color = c.textSecondary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                if (detail.paymentMethod.isNotBlank()) {
                                    Spacer(Modifier.height(5.dp))
                                    Text(
                                        "روش پرداخت: ${detail.paymentMethod}",
                                        color = c.textSecondary
                                    )
                                }

                                if (detail.shippingMethod.isNotBlank()) {
                                    Spacer(Modifier.height(5.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.LocalShipping,
                                            null,
                                            tint = c.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(detail.shippingMethod, color = c.textPrimary)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            "محصولات سفارش",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = c.textPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(detail.items, key = { it.id }) { line ->
                        DetailLineCard(line)
                    }

                    item {
                        Surface(
                            shape = PMTheme.shapes.card,
                            color = c.surface,
                            border = BorderStroke(1.dp, c.border)
                        ) {
                            Column(Modifier.padding(15.dp)) {
                                DetailTotalRow("تخفیف", toman(detail.discountTotal))
                                DetailTotalRow("هزینه ارسال", toman(detail.shippingTotal))
                                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = c.border)
                                DetailTotalRow("مبلغ نهایی", toman(detail.total), bold = true)
                            }
                        }
                    }
                }

                Surface(
                    color = c.surface,
                    border = BorderStroke(1.dp, c.border),
                    shadowElevation = 8.dp
                ) {
                    PMPrimaryButton(
                        text = "پیگیری سفارش",
                        onClick = onTrack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(12.dp),
                        icon = Icons.Outlined.LocalShipping
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailLineCard(line: OrderLineItem) {
    val c = PMTheme.colors
    Surface(
        shape = PMTheme.shapes.card,
        color = c.surface,
        border = BorderStroke(1.dp, c.border)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImage(
                line.image,
                Modifier.size(76.dp).clip(PMTheme.shapes.medium)
            )
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    line.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    "تعداد: ${line.quantity}",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textSecondary
                )
            }
            Text(
                toman(line.total),
                color = c.primary,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun DetailTotalRow(
    label: String,
    value: String,
    bold: Boolean = false
) {
    val c = PMTheme.colors
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            color = if (bold) c.textPrimary else c.textSecondary,
            fontWeight = if (bold) FontWeight.Black else FontWeight.Normal
        )
        Text(
            value,
            color = c.primary,
            fontWeight = if (bold) FontWeight.Black else FontWeight.Bold
        )
    }
}

private fun detailStatusLabel(status: String): String = when (status) {
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
private fun detailStatusColor(status: String) = when (status) {
    "completed" -> PMTheme.colors.success
    "processing" -> PMTheme.colors.primary
    "pending", "on-hold" -> PMTheme.colors.warning
    "cancelled", "failed", "refunded" -> PMTheme.colors.error
    else -> PMTheme.colors.textSecondary
}
