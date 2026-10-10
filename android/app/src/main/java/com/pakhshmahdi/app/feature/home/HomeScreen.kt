package com.pakhshmahdi.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
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
import com.pakhshmahdi.app.ui.theme.PMGoldDeep
import com.pakhshmahdi.app.ui.theme.PMTheme
import kotlinx.coroutines.delay

private val approvedBanners = listOf(
    "https://raw.githubusercontent.com/pakhshemahdisite-svg/pakhshemah-build/main/assets/app-ui/home-banner-1.jpg",
    "https://raw.githubusercontent.com/pakhshemahdisite-svg/pakhshemah-build/main/assets/app-ui/home-banner-2.jpg",
    "https://raw.githubusercontent.com/pakhshemahdisite-svg/pakhshemah-build/main/assets/app-ui/home-banner-3.jpg"
)

@Composable
fun HomeScreen(
    onProduct: (Long) -> Unit,
    onCatalog: () -> Unit,
    onCategory: (Long) -> Unit,
    onSearch: () -> Unit,
    onCategories: () -> Unit,
    onNotifications: () -> Unit,
    onWishlist: () -> Unit,
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
        modifier = Modifier.fillMaxSize().background(c.background),
        contentPadding = PaddingValues(bottom = 26.dp)
    ) {
        item {
            PremiumHomeHeader(
                onSearch = onSearch,
                onNotifications = onNotifications,
                onWishlist = onWishlist,
                onCart = onCart
            )
        }

        item { ApprovedHeroSlider(onCatalog = onCatalog) }

        if (state.data.categories.isNotEmpty()) {
            item {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    PMSectionHeader("دسته‌بندی‌ها", "مشاهده همه", onCategories)
                }
            }
            item { PremiumCategoryRow(state.data.categories, onCategory, onCategories) }
        }

        if (state.data.featuredProducts.isNotEmpty()) {
            item {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    PMSectionHeader("پیشنهاد ویژه", "مشاهده همه", onCatalog)
                }
            }
            item { ProductRow(state.data.featuredProducts, onProduct) }
        }

        if (state.data.onSaleProducts.isNotEmpty()) {
            item {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    PMSectionHeader("تخفیف‌های منتخب", "مشاهده همه", onCatalog)
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
private fun PremiumHomeHeader(
    onSearch: () -> Unit,
    onNotifications: () -> Unit,
    onWishlist: () -> Unit,
    onCart: () -> Unit
) {
    val c = PMTheme.colors
    val cartCount = CartStore.totalQuantity

    Column(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(94.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BrandLogo(Modifier.width(104.dp).height(64.dp))
                Text(
                    "پخش مهدی",
                    color = c.textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
            }

            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (AppConfig.FEATURE_NOTIFICATIONS) {
                    HeaderCircleButton(onClick = onNotifications) {
                        Icon(Icons.Outlined.NotificationsNone, "اطلاعیه‌ها", tint = c.textPrimary)
                    }
                }
                if (AppConfig.FEATURE_WISHLIST) {
                    HeaderCircleButton(onClick = onWishlist) {
                        Icon(Icons.Outlined.FavoriteBorder, "علاقه‌مندی", tint = c.textPrimary)
                    }
                }
            }

            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderCircleButton(onClick = onCart) {
                    BadgedBox(badge = {
                        if (cartCount > 0) Badge(containerColor = c.accentGold) {
                            Text(cartCount.coerceAtMost(99).toString(), color = c.primaryDark)
                        }
                    }) {
                        Icon(Icons.Outlined.ShoppingBag, "سبد خرید", tint = c.textPrimary)
                    }
                }
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Outlined.LocationOn, null, tint = c.textPrimary, modifier = Modifier.size(21.dp))
                Spacer(Modifier.width(3.dp))
                Text("تهران", color = c.textPrimary, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(5.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onSearch),
            shape = RoundedCornerShape(18.dp),
            color = c.surface,
            border = BorderStroke(1.2.dp, PMGoldDeep)
        ) {
            Row(
                Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, null, tint = c.textPrimary)
                Spacer(Modifier.width(10.dp))
                Text(
                    "جستجوی محصول، برند، دسته‌بندی ...",
                    color = c.textMuted,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )

            }
        }
    }
}

@Composable
private fun HeaderCircleButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val c = PMTheme.colors
    Surface(
        modifier = Modifier.size(39.dp),
        shape = CircleShape,
        color = c.surface,
        border = BorderStroke(1.dp, c.border)
    ) {
        IconButton(onClick = onClick, content = content)
    }
}

@Composable
private fun ApprovedHeroSlider(onCatalog: () -> Unit) {
    val c = PMTheme.colors
    val pagerState = rememberPagerState(pageCount = { approvedBanners.size })

    LaunchedEffect(pagerState.currentPage) {
        delay(4500)
        pagerState.animateScrollToPage((pagerState.currentPage + 1) % approvedBanners.size)
    }

    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 9.dp
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(218.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(onClick = onCatalog)
            ) {
                AsyncImage(
                    model = approvedBanners[page],
                    contentDescription = "بنر پخش مهدی ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Black.copy(alpha = .36f),
                                    Color.Black.copy(alpha = .08f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(190.dp)
                        .padding(start = 18.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "کیفیت در\nهر آشپزخانه",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        lineHeight = MaterialTheme.typography.headlineMedium.lineHeight
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "انتخاب حرفه‌ای‌ها\nبا پخش مهدی",
                        color = Color.White.copy(alpha = .98f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = c.accentGold,
                        border = BorderStroke(1.dp, PMGoldDeep.copy(alpha = .65f))
                    ) {
                        Text(
                            "مشاهده محصولات  ‹",
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                            color = c.primaryDark,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            approvedBanners.indices.forEach { index ->
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .width(if (pagerState.currentPage == index) 22.dp else 8.dp)
                        .height(7.dp),
                    shape = CircleShape,
                    color = if (pagerState.currentPage == index) PMGoldDeep else c.border
                ) {}
            }
        }
    }
}

@Composable
private fun PremiumCategoryRow(
    categories: List<Category>,
    onCategory: (Long) -> Unit,
    onAll: () -> Unit
) {
    val c = PMTheme.colors
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier.width(78.dp).clickable(onClick = onAll),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(66.dp),
                    shape = CircleShape,
                    color = c.accentGold.copy(alpha = .22f),
                    border = BorderStroke(1.dp, PMGoldDeep.copy(alpha = .28f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("▦", color = c.textPrimary, fontSize = MaterialTheme.typography.headlineSmall.fontSize)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "همه دسته‌بندی‌ها",
                    color = c.textPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }

        items(categories.take(8), key = { it.id }) { category ->
            Column(
                modifier = Modifier.width(78.dp).clickable { onCategory(category.id) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(66.dp),
                    shape = CircleShape,
                    color = c.surfaceElevated,
                    border = BorderStroke(1.dp, c.border)
                ) {
                    ProductImage(
                        category.image,
                        Modifier.fillMaxSize().padding(4.dp).clip(CircleShape)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    category.name,
                    color = c.textPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
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
        Surface(
            modifier = Modifier.fillMaxWidth().height(105.dp),
            shape = PMTheme.shapes.card,
            color = c.surfaceElevated
        ) {}
        Spacer(Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = PMTheme.shapes.medium,
            color = c.surfaceElevated
        ) {}
        Spacer(Modifier.height(14.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().height(218.dp),
            shape = PMTheme.shapes.largeCard,
            color = c.surfaceElevated
        ) {}
        Spacer(Modifier.height(22.dp))
        repeat(3) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(76.dp).padding(bottom = 10.dp),
                shape = PMTheme.shapes.card,
                color = c.surfaceElevated
            ) {}
        }
    }
}
