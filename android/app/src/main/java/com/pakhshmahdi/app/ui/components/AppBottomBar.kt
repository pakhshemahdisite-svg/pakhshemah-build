package com.pakhshmahdi.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pakhshmahdi.app.core.AppConfig
import com.pakhshmahdi.app.data.cart.CartStore
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun AppBottomBar(
    selected: String,
    onHome: () -> Unit,
    onCatalog: () -> Unit,
    onWishlist: () -> Unit,
    onCart: () -> Unit,
    onProfile: () -> Unit
) {
    val c = PMTheme.colors
    val cartCount = CartStore.totalQuantity

    Surface(
        color = c.surface,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, c.border)
    ) {
        NavigationBar(
            containerColor = c.surface,
            tonalElevation = 0.dp,
            modifier = Modifier.navigationBarsPadding()
        ) {
            NavigationBarItem(
                selected = selected == "home",
                onClick = onHome,
                icon = { Icon(Icons.Outlined.Home, null) },
                label = { Text("خانه") },
                colors = navColors()
            )
            NavigationBarItem(
                selected = selected == "catalog",
                onClick = onCatalog,
                icon = { Icon(Icons.Outlined.GridView, null) },
                label = { Text("دسته‌بندی") },
                colors = navColors()
            )
            NavigationBarItem(
                selected = selected == "cart",
                onClick = onCart,
                icon = {
                    if (cartCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = c.accentGold, contentColor = c.primaryDark) {
                                    Text(cartCount.coerceAtMost(99).toString())
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.ShoppingBag, null)
                        }
                    } else {
                        Icon(Icons.Outlined.ShoppingBag, null)
                    }
                },
                label = { Text("سبد خرید") },
                colors = navColors()
            )
            if (AppConfig.FEATURE_WISHLIST) {
                NavigationBarItem(
                    selected = selected == "wishlist",
                    onClick = onWishlist,
                    icon = { Icon(Icons.Outlined.FavoriteBorder, null) },
                    label = { Text("علاقه‌مندی") },
                    colors = navColors()
                )
            }
            NavigationBarItem(
                selected = selected == "profile",
                onClick = onProfile,
                icon = { Icon(Icons.Outlined.PersonOutline, null) },
                label = { Text("پروفایل") },
                colors = navColors()
            )
        }
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = PMTheme.colors.primary,
    selectedTextColor = PMTheme.colors.primary,
    indicatorColor = PMTheme.colors.primary.copy(alpha = .12f),
    unselectedIconColor = PMTheme.colors.textMuted,
    unselectedTextColor = PMTheme.colors.textMuted
)
