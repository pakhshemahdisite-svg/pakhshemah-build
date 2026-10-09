package com.pakhshmahdi.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pakhshmahdi.app.data.cart.CartStore
import com.pakhshmahdi.app.ui.theme.PMGoldDeep
import com.pakhshmahdi.app.ui.theme.PMTheme

private data class BottomItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun AppBottomBar(
    selected: String,
    onHome: () -> Unit,
    onCatalog: () -> Unit,
    onCart: () -> Unit,
    onProfile: () -> Unit
) {
    val c = PMTheme.colors
    val cartCount = CartStore.totalQuantity
    val items = listOf(
        BottomItem("home", "خانه", Icons.Outlined.Home, onHome),
        BottomItem("categories", "دسته‌بندی‌ها", Icons.Outlined.GridView, onCatalog),
        BottomItem("cart", "سبد خرید", Icons.Outlined.ShoppingCart, onCart),
        BottomItem("profile", "حساب کاربری", Icons.Outlined.PersonOutline, onProfile)
    )

    Surface(
        color = c.surface,
        shadowElevation = 20.dp,
        border = BorderStroke(1.dp, c.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val active = selected == item.key
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .then(
                            if (active) Modifier.shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(18.dp),
                                ambientColor = c.accentGold,
                                spotColor = c.accentGold
                            ) else Modifier
                        )
                        .clickable(onClick = item.onClick),
                    shape = RoundedCornerShape(18.dp),
                    color = if (active) c.primaryDark else c.surface,
                    border = BorderStroke(
                        if (active) 1.dp else 0.dp,
                        if (active) c.accentGold else c.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(vertical = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (active) c.accentGold else c.textPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            if (item.key == "cart" && cartCount > 0) {
                                Surface(
                                    modifier = Modifier
                                        .offset(x = 8.dp, y = (-5).dp)
                                        .size(19.dp),
                                    shape = CircleShape,
                                    color = PMGoldDeep
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            cartCount.coerceAtMost(99).toString(),
                                            color = c.primaryDark,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            item.label,
                            color = if (active) c.accentGold else c.textSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (active) FontWeight.Black else FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
