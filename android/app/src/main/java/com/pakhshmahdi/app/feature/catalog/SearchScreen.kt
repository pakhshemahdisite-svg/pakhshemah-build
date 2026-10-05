package com.pakhshmahdi.app.feature.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pakhshmahdi.app.data.cart.CartStore
import com.pakhshmahdi.app.ui.components.PMEmptyState
import com.pakhshmahdi.app.ui.components.PMErrorState
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.components.PMSearchField
import com.pakhshmahdi.app.ui.components.ProductCard
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun SearchScreen(
    onProduct: (Long) -> Unit,
    onBack: () -> Unit
) {
    val c = PMTheme.colors
    val vm: SearchViewModel = viewModel()
    val state by vm.state.collectAsState()
    val gridState = rememberLazyGridState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            state.products.isNotEmpty() &&
                lastVisible >= (state.products.lastIndex - 4).coerceAtLeast(0) &&
                state.hasMore &&
                !state.loading &&
                !state.loadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) vm.loadMore()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        PMScreenTopBar(
            title = "جستجو",
            subtitle = "در میان محصولات پخش مهدی",
            onBack = onBack
        )

        PMSearchField(
            value = state.query,
            onValueChange = vm::setQuery,
            placeholder = "نام محصول را وارد کنید...",
            onSearch = vm::search,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        when {
            state.query.isBlank() -> {
                PMEmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = "چه محصولی می‌خواهید؟",
                    message = "نام محصول را بنویسید تا جستجو در فروشگاه شروع شود.",
                    modifier = Modifier.fillMaxSize()
                )
            }
            state.loading -> {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = c.primary,
                    trackColor = c.surfaceElevated
                )
            }
            state.error != null && state.products.isEmpty() -> {
                PMErrorState(
                    message = state.error ?: "جستجو انجام نشد.",
                    onRetry = vm::search,
                    modifier = Modifier.fillMaxSize()
                )
            }
            state.query.isNotBlank() && state.products.isEmpty() -> {
                PMEmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = "نتیجه‌ای پیدا نشد",
                    message = "عبارت دیگری را امتحان کنید.",
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.products, key = { it.id }) { product ->
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
}
