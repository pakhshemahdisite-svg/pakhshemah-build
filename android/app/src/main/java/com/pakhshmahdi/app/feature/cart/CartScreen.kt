package com.pakhshmahdi.app.feature.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pakhshmahdi.app.data.cart.CartLine
import com.pakhshmahdi.app.data.cart.CartStore
import com.pakhshmahdi.app.data.cart.WholesaleOrderPolicy
import com.pakhshmahdi.app.data.store.moneyLineTotal
import com.pakhshmahdi.app.ui.components.PMEmptyState
import com.pakhshmahdi.app.ui.components.PMPrimaryButton
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.components.ProductImage
import com.pakhshmahdi.app.ui.components.toman
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun CartScreen(onCheckout: () -> Unit) {
    val c = PMTheme.colors
    val lines = CartStore.lines
    var showClearCartDialog by remember { mutableStateOf(false) }
    val subtotalLong = WholesaleOrderPolicy.subtotal(lines)
    val subtotal = subtotalLong.toString()
    val wholesaleMessage = WholesaleOrderPolicy.message(lines)

    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        PMScreenTopBar(
            title = "سبد خرید",
            subtitle = if (lines.isEmpty()) "سبد شما خالی است" else "${lines.sumOf { it.quantity }} کالا",
            trailing = {
                if (lines.isNotEmpty()) {
                    IconButton(onClick = { showClearCartDialog = true }) {
                        Icon(Icons.Outlined.DeleteSweep, contentDescription = "پاک کردن سبد", tint = c.error)
                    }
                }
            }
        )

        if (showClearCartDialog) {
            AlertDialog(
                onDismissRequest = { showClearCartDialog = false },
                title = { Text("پاک کردن سبد خرید") },
                text = { Text("همه محصولات از سبد خرید حذف شوند؟") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            CartStore.clear()
                            showClearCartDialog = false
                        }
                    ) {
                        Text("پاک کردن", color = c.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearCartDialog = false }) {
                        Text("انصراف")
                    }
                }
            )
        }

        if (lines.isEmpty()) {
            PMEmptyState(
                icon = Icons.Outlined.ShoppingBag,
                title = "سبد خرید شما خالی است",
                message = "محصولات موردنظر را از فروشگاه به سبد خرید اضافه کنید.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(lines, key = { it.key }) { line ->
                    CartLineCard(line)
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        shape = PMTheme.shapes.card,
                        color = c.surface,
                        border = BorderStroke(1.dp, c.border)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "خلاصه سفارش",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = c.textPrimary
                            )
                            Spacer(Modifier.height(12.dp))
                            SummaryRow("جمع کالاها", toman(subtotal))
                            SummaryRow("تخفیف", "در مرحله پرداخت محاسبه می‌شود", secondary = true)
                            SummaryRow("هزینه ارسال", "پس از انتخاب آدرس", secondary = true)
                            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = c.border)
                            SummaryRow("مبلغ فعلی", toman(subtotal), bold = true)
                        }
                    }
                }
            }

            Surface(
                color = c.surface,
                border = BorderStroke(1.dp, c.border),
                shadowElevation = 10.dp
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = PMTheme.shapes.medium,
                        color = c.surfaceElevated,
                        border = BorderStroke(1.dp, c.border)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(8.dp),
                                shape = CircleShape,
                                color = c.accentGold
                            ) {}
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "قانون خرید عمده: حداقل ۶ عدد از هر کالا • حداقل سفارش ۱۵ میلیون تومان",
                                color = c.textSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    if (wholesaleMessage != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = PMTheme.shapes.medium,
                            color = c.accentGold.copy(alpha = .10f),
                            border = BorderStroke(1.dp, c.accentGold.copy(alpha = .34f))
                        ) {
                            Text(
                                wholesaleMessage,
                                color = c.textSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(11.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    PMPrimaryButton(
                        text = "ادامه و تکمیل سفارش",
                        onClick = onCheckout,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = wholesaleMessage == null,
                        icon = Icons.Outlined.ShoppingBag
                    )
                }
            }
        }
    }
}

@Composable
private fun CartLineCard(line: CartLine) {
    val c = PMTheme.colors
    val maxStock = line.variation?.stockQuantity
        ?: line.product.stockQuantity.takeIf { line.variation == null }
    val canIncrement = maxStock == null || line.quantity < maxStock
    val lineTotal = moneyLineTotal(line.unitPrice, line.quantity).toString()

    Surface(
        shape = PMTheme.shapes.card,
        color = c.surface,
        border = BorderStroke(1.dp, c.border),
        shadowElevation = 1.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImage(
                line.variation?.image ?: line.product.image,
                Modifier
                    .size(82.dp)
                    .clip(PMTheme.shapes.medium)
            )

            Spacer(Modifier.width(11.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    line.product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = c.textPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                if (line.variation != null) {
                    val variationText = line.variation.attributes.values
                        .filter { it.isNotBlank() }
                        .joinToString(" • ")
                    if (variationText.isNotBlank()) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            variationText,
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textMuted
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    if (line.quantity > 1) "${toman(lineTotal)} • ${toman(line.unitPrice)} هر عدد" else toman(line.unitPrice),
                    color = c.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black
                )

                Spacer(Modifier.height(8.dp))

                Surface(
                    shape = PMTheme.shapes.pill,
                    color = c.surfaceElevated,
                    border = BorderStroke(1.dp, c.border)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { CartStore.decrement(line.key) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Text("−", color = c.textPrimary, fontWeight = FontWeight.Black)
                        }
                        Text(
                            line.quantity.toString(),
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = c.textPrimary,
                            fontWeight = FontWeight.Black
                        )
                        IconButton(
                            onClick = { CartStore.increment(line.key) },
                            enabled = canIncrement,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Text("+", color = c.textPrimary, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = c.error.copy(alpha = .08f)
            ) {
                IconButton(onClick = { CartStore.remove(line.key) }) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        "حذف",
                        tint = c.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    bold: Boolean = false,
    secondary: Boolean = false
) {
    val c = PMTheme.colors
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            color = if (bold) c.textPrimary else c.textSecondary,
            fontWeight = if (bold) FontWeight.Black else FontWeight.Normal
        )
        Text(
            value,
            color = if (secondary) c.textMuted else c.primary,
            fontWeight = if (bold) FontWeight.Black else FontWeight.SemiBold,
            style = if (secondary) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium
        )
    }
}
