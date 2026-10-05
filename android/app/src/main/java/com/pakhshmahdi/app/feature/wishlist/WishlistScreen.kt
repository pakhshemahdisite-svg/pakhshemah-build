package com.pakhshmahdi.app.feature.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pakhshmahdi.app.data.cart.CartStore
import com.pakhshmahdi.app.data.repository.CatalogRepository
import com.pakhshmahdi.app.data.wishlist.WishlistStore
import com.pakhshmahdi.app.ui.components.PMEmptyState
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.components.ProductCard
import com.pakhshmahdi.app.ui.theme.PMTheme
import kotlinx.coroutines.launch

@Composable
fun WishlistScreen(onProduct: (Long) -> Unit) {
    val c = PMTheme.colors
    val products = WishlistStore.items
    val repository = remember { CatalogRepository() }
    val scope = rememberCoroutineScope()
    var filter by remember { mutableStateOf("all") }
    var refreshing by remember { mutableStateOf(false) }

    suspend fun refreshWishlist() {
        if (refreshing || products.isEmpty()) return
        refreshing = true
        val snapshot = products.toList()
        val refreshed = snapshot.map { stored ->
            runCatching { repository.product(stored.id) }.getOrElse { stored }
        }
        WishlistStore.replaceAll(refreshed)
        refreshing = false
    }

    LaunchedEffect(Unit) {
        refreshWishlist()
    }

    val visible = when (filter) {
        "stock" -> products.filter { it.isInStock }
        "sale" -> products.filter { it.hasSale }
        else -> products.toList()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        PMScreenTopBar(
            title = "علاقه‌مندی‌ها",
            subtitle = if (products.isEmpty()) "لیست شما خالی است" else "${products.size} محصول ذخیره‌شده",
            trailing = {
                if (products.isNotEmpty()) {
                    IconButton(
                        onClick = { scope.launch { refreshWishlist() } },
                        enabled = !refreshing
                    ) {
                        if (refreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = c.primary
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Refresh,
                                contentDescription = "بروزرسانی",
                                tint = c.primary
                            )
                        }
                    }
                }
            }
        )

        if (products.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filter == "all",
                        onClick = { filter = "all" },
                        label = { Text("همه") },
                        shape = PMTheme.shapes.pill,
                        colors = wishFilterColors()
                    )
                }
                item {
                    FilterChip(
                        selected = filter == "stock",
                        onClick = { filter = "stock" },
                        label = { Text("موجود") },
                        shape = PMTheme.shapes.pill,
                        colors = wishFilterColors()
                    )
                }
                item {
                    FilterChip(
                        selected = filter == "sale",
                        onClick = { filter = "sale" },
                        label = { Text("تخفیف‌دار") },
                        shape = PMTheme.shapes.pill,
                        colors = wishFilterColors()
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        if (products.isEmpty()) {
            PMEmptyState(
                icon = Icons.Outlined.FavoriteBorder,
                title = "هنوز محصولی ذخیره نکرده‌اید",
                message = "با لمس آیکن قلب، محصولات دلخواه شما اینجا نمایش داده می‌شوند.",
                modifier = Modifier.fillMaxSize()
            )
        } else if (visible.isEmpty()) {
            PMEmptyState(
                icon = Icons.Outlined.FavoriteBorder,
                title = if (filter == "sale") "محصول تخفیف‌داری ندارید" else "محصول موجودی در این فیلتر نیست",
                message = "فیلتر «همه» را انتخاب کنید یا محصولات دیگری به علاقه‌مندی‌ها اضافه کنید.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(visible, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onOpen = { onProduct(product.id) },
                        onAdd = { CartStore.add(product) }
                    )
                }
            }
        }
    }
}

@Composable
private fun wishFilterColors() = FilterChipDefaults.filterChipColors(
    containerColor = PMTheme.colors.surface,
    labelColor = PMTheme.colors.textSecondary,
    selectedContainerColor = PMTheme.colors.primary,
    selectedLabelColor = if (PMTheme.colors.primary == PMTheme.colors.accentGold) PMTheme.colors.primaryDark else androidx.compose.ui.graphics.Color.White
)
