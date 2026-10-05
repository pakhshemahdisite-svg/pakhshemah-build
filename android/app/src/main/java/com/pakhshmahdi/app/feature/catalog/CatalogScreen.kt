package com.pakhshmahdi.app.feature.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as rowItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pakhshmahdi.app.data.cart.CartStore
import com.pakhshmahdi.app.ui.components.PMErrorState
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.components.PMSearchField
import com.pakhshmahdi.app.ui.components.ProductCard
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun CatalogScreen(
    initialCategoryId: Long? = null,
    onProduct: (Long) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val c = PMTheme.colors
    val vm: CatalogViewModel = viewModel()
    val state by vm.state.collectAsState()
    val gridState = rememberLazyGridState()

    LaunchedEffect(initialCategoryId) {
        if (initialCategoryId != null) vm.selectCategory(initialCategoryId)
    }

    val selectedCategory = state.categories.firstOrNull { it.id == state.selectedCategoryId }

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
            title = selectedCategory?.name ?: "محصولات",
            subtitle = if (state.total > 0) "${state.total} محصول" else "فروشگاه پخش مهدی",
            onBack = onBack
        )

        PMSearchField(
            value = state.query,
            onValueChange = vm::setQuery,
            placeholder = "جستجو در محصولات...",
            onSearch = vm::search,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = state.selectedCategoryId == null,
                    onClick = { vm.selectCategory(null) },
                    label = { Text("همه") },
                    shape = PMTheme.shapes.pill,
                    colors = filterColors()
                )
            }
            rowItems(state.categories, key = { it.id }) { category ->
                FilterChip(
                    selected = state.selectedCategoryId == category.id,
                    onClick = { vm.selectCategory(category.id) },
                    label = { Text(category.name) },
                    shape = PMTheme.shapes.pill,
                    colors = filterColors()
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortChip(
                label = "جدیدترین",
                selected = state.sort == CatalogSort.NEWEST,
                onClick = { vm.setSort(CatalogSort.NEWEST) },
                modifier = Modifier.weight(1f)
            )
            SortChip(
                label = "ارزان‌ترین",
                selected = state.sort == CatalogSort.PRICE_ASC,
                onClick = { vm.setSort(CatalogSort.PRICE_ASC) },
                modifier = Modifier.weight(1f)
            )
            SortChip(
                label = "گران‌ترین",
                selected = state.sort == CatalogSort.PRICE_DESC,
                onClick = { vm.setSort(CatalogSort.PRICE_DESC) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(10.dp))

        when {
            state.loading && state.products.isEmpty() -> CatalogSkeleton()

            state.error != null && state.products.isEmpty() -> {
                Box(Modifier.fillMaxSize()) {
                    PMErrorState(
                        message = state.error ?: "محصولات دریافت نشد.",
                        onRetry = vm::retry,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            state.products.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("محصولی پیدا نشد.", color = c.textSecondary)
                }
            }

            else -> {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 24.dp),
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

                    if (state.loadingMore) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().height(54.dp),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = c.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SortChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = PMTheme.colors
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 1) },
        leadingIcon = if (selected) {
            { Icon(Icons.Outlined.Tune, null, modifier = Modifier.size(16.dp)) }
        } else null,
        modifier = modifier,
        shape = PMTheme.shapes.pill,
        colors = filterColors()
    )
}

@Composable
private fun filterColors() = FilterChipDefaults.filterChipColors(
    containerColor = PMTheme.colors.surface,
    labelColor = PMTheme.colors.textSecondary,
    selectedContainerColor = PMTheme.colors.primary,
    selectedLabelColor = if (PMTheme.colors.primary == PMTheme.colors.accentGold) PMTheme.colors.primaryDark else androidx.compose.ui.graphics.Color.White,
    iconColor = PMTheme.colors.textSecondary,
    selectedLeadingIconColor = if (PMTheme.colors.primary == PMTheme.colors.accentGold) PMTheme.colors.primaryDark else androidx.compose.ui.graphics.Color.White
)

@Composable
private fun CatalogSkeleton() {
    val c = PMTheme.colors
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(8) {
            Surface(
                modifier = Modifier.height(250.dp),
                shape = PMTheme.shapes.card,
                color = c.surface,
                border = BorderStroke(1.dp, c.border)
            ) {
                Column(Modifier.padding(9.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().aspectRatio(1.05f),
                        shape = PMTheme.shapes.medium,
                        color = c.surfaceElevated
                    ) {}
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        Modifier.fillMaxWidth(.82f).height(13.dp),
                        shape = PMTheme.shapes.pill,
                        color = c.surfaceElevated
                    ) {}
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        Modifier.fillMaxWidth(.55f).height(12.dp),
                        shape = PMTheme.shapes.pill,
                        color = c.surfaceElevated
                    ) {}
                }
            }
        }
    }
}
