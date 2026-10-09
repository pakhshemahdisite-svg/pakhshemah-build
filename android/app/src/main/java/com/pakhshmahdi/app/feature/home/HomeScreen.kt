package com.pakhshmahdi.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pakhshmahdi.app.core.AppConfig
import com.pakhshmahdi.app.data.cart.CartStore
import com.pakhshmahdi.app.data.model.Category
import com.pakhshmahdi.app.data.model.Product
import com.pakhshmahdi.app.data.recent.RecentlyViewedStore
import com.pakhshmahdi.app.ui.components.BrandLogo
import com.pakhshmahdi.app.ui.components.PMErrorState
import com.pakhshmahdi.app.ui.components.PMSectionHeader
import com.pakhshmahdi.app.ui.components.ProductCard
import com.pakhshmahdi.app.ui.components.ProductImage
import com.pakhshmahdi.app.ui.components.toman
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun HomeScreen(
    onProduct: (Long) -> Unit,
    onCatalog: () -> Unit,
    onCategory: (Long) -> Unit,
    onSearch: () -> Unit,
    onCategories: () -> Unit,
    onNotifications: () -> Unit,
    onCart: () -> Unit
) {
    val c = PMTheme.colors
    val vm: HomeViewModel = viewModel()
    val state by vm.state.collectAsState()
    val recent = RecentlyViewedStore.items

    if (state.loading && state.data.latestProducts.isEmpty()) {
        HomeSkeleton()
        return
    }

    if (state.error != null && state.data.latestProducts.isEmpty()) {
        Box(
            Modifier.fillMaxSize().background(c.background),
            contentAlignment = Alignment.Center
        ) {
            PMErrorState(
                message = state.error ?: "دریافت اطلاعات فروشگاه انجام نشد.",
                onRetry = vm::refresh
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background),
        contentPadding = PaddingValues(bottom = 26.dp)
    ) {
        item {
            HomeHeader(
                onSearch = onSearch,
                onNotifications = onNotifications,
                onCart = onCart
            )
        }

        item {
            HeroBanner(
                product = state.data.featuredProducts.firstOrNull()
                    ?: state.data.latestProducts.firstOrNull(),
                onCatalog = onCatalog
            )
        }

        if (state.data.categories.isNotEmpty()) {
            item {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    PMSectionHeader("دسته‌بندی‌ها", "مشاهده همه", onCategories)
                }
            }
            item {
                CategoryRow(state.data.categories, onCategory)
            }
        }

        if (state.data.featuredProducts.isNotEmpty()) {
            item {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    PMSectionHeader("پرفروش و منتخب", "مشاهده همه", onCatalog)
                }
            }
            item { ProductRow(state.data.featuredProducts, onProduct) }
        }

        if (state.data.onSaleProducts.isNotEmpty()) {
            item {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    PMSectionHeader("پیشنهادهای ویژه", "مشاهده همه", onCatalog)
                }
            }
            item { ProductRow(state.data.onSaleProducts, onProduct) }
        }

        item {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                PMSectionHeader("جدیدترین محصولات", "مشاهده همه", onCatalog)
            }
        }
        item { ProductRow(state.data.latestProducts, onProduct) }

        if (recent.isNotEmpty()) {
            item {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    PMSectionHeader("بازدیدهای اخیر", "فروشگاه", onCatalog)
                }
            }
            item { ProductRow(recent, onProduct) }
        }
    }
}

@Composable
private fun HomeHeader(
    onSearch: () -> Unit,
    onNotifications: () -> Unit,
    onCart: () -> Unit
) {
    val c = PMTheme.colors
    val cartCount = CartStore.totalQuantity

    Column(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.width(96.dp).height(46.dp),
                shape = PMTheme.shapes.medium,
                color = c.surface,
                border = BorderStroke(1.dp, c.border)
            ) {
                BrandLogo(Modifier.padding(horizontal = 7.dp, vertical = 5.dp))
            }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    AppConfig.APP_NAME,
                    style = MaterialTheme.typography.titleLarge,
                    color = c.textPrimary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    AppConfig.APP_SUBTITLE,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary
                )
            }

            if (AppConfig.FEATURE_NOTIFICATIONS) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = c.surface,
                    border = BorderStroke(1.dp, c.border)
                ) {
                    IconButton(onClick = onNotifications) {
                        Icon(Icons.Outlined.NotificationsNone, null, tint = c.textPrimary)
                    }
                }

                Spacer(Modifier.width(7.dp))
            }

            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = c.surface,
                border = BorderStroke(1.dp, c.border)
            ) {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge(
                                containerColor = c.accentGold,
                                contentColor = c.primaryDark
                            ) { Text(cartCount.coerceAtMost(99).toString()) }
                        }
                    }
                ) {
                    IconButton(onClick = onCart) {
                        Icon(Icons.Outlined.ShoppingBag, null, tint = c.textPrimary)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSearch),
            shape = PMTheme.shapes.medium,
            color = c.surfaceElevated,
            border = BorderStroke(1.dp, c.border)
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, null, tint = c.textMuted)
                Spacer(Modifier.width(9.dp))
                Text(
                    "جستجوی محصولات، دسته‌ها و برندها...",
                    color = c.textMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun HeroBanner(
    product: Product?,
    onCatalog: () -> Unit
) {
    val c = PMTheme.colors

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable(onClick = onCatalog),
        shape = PMTheme.shapes.largeCard,
        color = c.primaryDark,
        border = BorderStroke(1.dp, c.accentGold.copy(alpha = .34f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 178.dp)
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = PMTheme.shapes.pill,
                    color = c.accentGold.copy(alpha = .14f),
                    border = BorderStroke(1.dp, c.accentGold.copy(alpha = .34f))
                ) {
                    Text(
                        "WHOLESALE · PAKHSH MAHDI",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.accentGold,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    product?.name ?: AppConfig.APP_SUBTITLE,
                    style = MaterialTheme.typography.headlineSmall,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (product != null && product.price.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        toman(product.price),
                        style = MaterialTheme.typography.titleLarge,
                        color = c.accentGold,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "مشاهده محصولات  ←",
                    style = MaterialTheme.typography.labelLarge,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = .84f),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(14.dp))

            Surface(
                modifier = Modifier.size(134.dp),
                shape = PMTheme.shapes.largeCard,
                color = androidx.compose.ui.graphics.Color.White,
                border = BorderStroke(1.dp, c.accentGold.copy(alpha = .30f))
            ) {
                ProductImage(
                    product?.image,
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clip(PMTheme.shapes.medium)
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    categories: List<Category>,
    onCategory: (Long) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories.take(10), key = { it.id }) { category ->
            CategoryItem(category = category, onClick = { onCategory(category.id) })
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    onClick: () -> Unit
) {
    val c = PMTheme.colors
    Column(
        modifier = Modifier
            .width(82.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(70.dp),
            shape = CircleShape,
            color = c.surface,
            border = BorderStroke(1.dp, c.border),
            shadowElevation = 1.dp
        ) {
            ProductImage(
                category.image,
                Modifier.fillMaxSize().padding(5.dp).clip(CircleShape)
            )
        }
        Spacer(Modifier.height(7.dp))
        Text(
            category.name,
            style = MaterialTheme.typography.labelMedium,
            color = c.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProductRow(
    products: List<Product>,
    onProduct: (Long) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(products, key = { it.id }) { product ->
            ProductCard(
                product = product,
                onOpen = { onProduct(product.id) },
                onAdd = { CartStore.add(product) },
                modifier = Modifier.width(178.dp)
            )
        }
    }
}

@Composable
private fun HomeSkeleton() {
    val c = PMTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = PMTheme.shapes.medium,
                color = c.surfaceElevated
            ) {}
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Surface(
                    modifier = Modifier.width(120.dp).height(18.dp),
                    shape = PMTheme.shapes.pill,
                    color = c.surfaceElevated
                ) {}
                Spacer(Modifier.height(7.dp))
                Surface(
                    modifier = Modifier.width(190.dp).height(11.dp),
                    shape = PMTheme.shapes.pill,
                    color = c.surfaceElevated
                ) {}
            }
        }
        Spacer(Modifier.height(16.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = PMTheme.shapes.medium,
            color = c.surfaceElevated
        ) {}
        Spacer(Modifier.height(14.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().height(190.dp),
            shape = PMTheme.shapes.largeCard,
            color = c.surfaceElevated
        ) {}
        Spacer(Modifier.height(24.dp))
        repeat(4) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(72.dp).padding(bottom = 10.dp),
                shape = PMTheme.shapes.card,
                color = c.surfaceElevated
            ) {}
        }
    }
}
