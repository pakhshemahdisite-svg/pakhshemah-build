package com.pakhshmahdi.app.feature.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pakhshmahdi.app.data.cart.CartLine
import com.pakhshmahdi.app.data.cart.CartStore
import com.pakhshmahdi.app.data.store.moneyLineTotal
import com.pakhshmahdi.app.ui.components.PMEmptyState
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.components.ProductImage
import com.pakhshmahdi.app.ui.components.toman
import com.pakhshmahdi.app.ui.theme.PMGoldDeep
import com.pakhshmahdi.app.ui.theme.PMGoldSoft
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun CartScreen(onCheckout: () -> Unit) {
    val c = PMTheme.colors
    val lines = CartStore.lines
    var showClearCartDialog by remember { mutableStateOf(false) }
    val subtotalLong = lines.sumOf { line -> moneyLineTotal(line.unitPrice, line.quantity) }
    val subtotal = subtotalLong.toString()
    val totalQty = lines.sumOf { it.quantity }

    Column(
        Modifier.fillMaxSize().background(c.background)
    ) {
        PMScreenTopBar(
            title = "سبد خرید",
            subtitle = if (lines.isEmpty()) "سبد شما خالی است" else "$totalQty کالا • ${lines.size} قلم",
            trailing = {
                if (lines.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = c.surface,
                        border = BorderStroke(1.dp, c.border)
                    ) {
                        IconButton(onClick = { showClearCartDialog = true }) {
                            Icon(Icons.Outlined.DeleteSweep, "خالی کردن سبد", tint = c.textPrimary)
                        }
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
                    TextButton(onClick = {
                        CartStore.clear()
                        showClearCartDialog = false
                    }) { Text("پاک کردن", color = c.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showClearCartDialog = false }) { Text("انصراف") }
                }
            )
        }

        if (lines.isEmpty()) {
            PMEmptyState(
                icon = Icons.Outlined.ShoppingCart,
                title = "سبد خرید شما خالی است",
                message = "محصولات موردنظر را از فروشگاه به سبد خرید اضافه کنید.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { WholesaleRulesCard() }

                items(lines, key = { it.key }) { line ->
                    PremiumCartLineCard(line)
                }

                item {
                    PremiumOrderSummary(
                        totalQty = totalQty,
                        lineCount = lines.size,
                        subtotal = subtotal
                    )
                }
            }

            Surface(
                color = c.surface,
                border = BorderStroke(1.dp, c.border),
                shadowElevation = 12.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(12.dp)
                        .height(58.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(PMGoldSoft, PMGoldDeep)
                            )
                        )
                        .clickable(onClick = onCheckout),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Outlined.ShoppingCart,
                            null,
                            tint = Color(0xFF111111)
                        )
                        Spacer(Modifier.width(9.dp))
                        Text(
                            "ادامه خرید و ثبت سفارش",
                            color = Color(0xFF111111),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WholesaleRulesCard() {
    val c = PMTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
        shape = RoundedCornerShape(24.dp),
        color = c.surfaceElevated,
        border = BorderStroke(1.2.dp, PMGoldDeep.copy(alpha = .55f)),
        shadowElevation = 4.dp
    ) {
        Column(Modifier.padding(15.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    color = c.accentGold.copy(alpha = .68f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.ShoppingCart,
                            null,
                            tint = Color(0xFF111111),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(Modifier.width(11.dp))
                Text(
                    "شرایط خرید عمده",
                    modifier = Modifier.weight(1f),
                    color = c.textPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.End
                )
            }

            Spacer(Modifier.height(12.dp))
            RuleRow(
                icon = { Icon(Icons.Outlined.Inventory2, null, tint = PMGoldDeep) },
                label = "حداقل خرید هر کالا",
                value = "۶ عدد"
            )
            Spacer(Modifier.height(7.dp))
            RuleRow(
                icon = { Icon(Icons.Outlined.Payments, null, tint = PMGoldDeep) },
                label = "حداقل مبلغ سفارش",
                value = "۱۵,۰۰۰,۰۰۰ تومان"
            )
            Spacer(Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(13.dp),
                color = c.accentGold.copy(alpha = .18f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        null,
                        tint = PMGoldDeep,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        "برای ادامه خرید، این شرایط باید در سبد رعایت شود. کنترل نهایی توسط فروشگاه انجام می‌شود.",
                        modifier = Modifier.weight(1f),
                        color = c.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    val c = PMTheme.colors
    Surface(
        shape = RoundedCornerShape(15.dp),
        color = c.surface,
        border = BorderStroke(1.dp, c.border)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(25.dp), contentAlignment = Alignment.Center) { icon() }
            Spacer(Modifier.width(8.dp))
            Text(label, color = c.textSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text(value, color = PMGoldDeep, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun PremiumCartLineCard(line: CartLine) {
    val c = PMTheme.colors
    val maxStock = line.variation?.stockQuantity
        ?: line.product.stockQuantity.takeIf { line.variation == null }
    val canIncrement = maxStock == null || line.quantity < maxStock
    val lineTotal = moneyLineTotal(line.unitPrice, line.quantity).toString()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = c.surface,
        border = BorderStroke(1.dp, c.border),
        shadowElevation = 2.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImage(
                line.variation?.image ?: line.product.image,
                Modifier
                    .size(94.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(Modifier.width(11.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    line.product.name,
                    color = c.textPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    textAlign = TextAlign.End
                )

                val variationText = line.variation?.attributes?.values
                    ?.filter { it.isNotBlank() }
                    ?.joinToString(" • ")
                    .orEmpty()
                if (variationText.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        variationText,
                        color = c.textMuted,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.End
                    )
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    toman(line.unitPrice),
                    color = PMGoldDeep,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = c.accentGold.copy(alpha = .34f),
                        border = BorderStroke(1.dp, PMGoldDeep.copy(alpha = .30f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { CartStore.decrement(line.key) },
                                modifier = Modifier.size(34.dp)
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
                                modifier = Modifier.size(34.dp)
                            ) {
                                Text("+", color = c.textPrimary, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        color = c.surfaceElevated,
                        border = BorderStroke(1.dp, c.border)
                    ) {
                        IconButton(onClick = { CartStore.remove(line.key) }) {
                            Icon(Icons.Outlined.DeleteOutline, "حذف", tint = PMGoldDeep)
                        }
                    }
                }

                Spacer(Modifier.height(5.dp))
                Text(
                    "جمع: ${toman(lineTotal)}",
                    color = c.textSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun PremiumOrderSummary(
    totalQty: Int,
    lineCount: Int,
    subtotal: String
) {
    val c = PMTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 5.dp),
        shape = RoundedCornerShape(22.dp),
        color = c.surfaceElevated,
        border = BorderStroke(1.dp, c.accentGold.copy(alpha = .35f))
    ) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("مجموع کل", color = c.textSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        toman(subtotal),
                        color = PMGoldDeep,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("تعداد کل کالاها   $totalQty عدد", color = c.textPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text("تعداد اقلام مختلف   $lineCount قلم", color = c.textSecondary)
                }
            }
        }
    }
}
