package com.pakhshmahdi.app.feature.product

import android.text.Html
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pakhshmahdi.app.core.AppConfig
import com.pakhshmahdi.app.data.cart.CartStore
import com.pakhshmahdi.app.data.model.ProductVariation
import com.pakhshmahdi.app.data.recent.RecentlyViewedStore
import com.pakhshmahdi.app.data.wishlist.WishlistStore
import com.pakhshmahdi.app.ui.components.PMErrorState
import com.pakhshmahdi.app.ui.components.PMPrimaryButton
import com.pakhshmahdi.app.ui.components.ProductImage
import com.pakhshmahdi.app.ui.components.toman
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun ProductDetailScreen(
    id: Long,
    onBack: () -> Unit,
    onBuy: () -> Unit
) {
    val c = PMTheme.colors
    val vm: ProductDetailViewModel = viewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(id) { vm.load(id) }

    if (state.loading) {
        Box(
            Modifier.fillMaxSize().background(c.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = c.primary)
        }
        return
    }

    val product = state.product
    if (product == null) {
        Box(
            Modifier.fillMaxSize().background(c.background),
            contentAlignment = Alignment.Center
        ) {
            PMErrorState(
                message = state.error ?: "محصول پیدا نشد.",
                onRetry = { vm.load(id) }
            )
        }
        return
    }

    LaunchedEffect(product.id) {
        RecentlyViewedStore.add(product)
    }

    var selectedVariationId by remember(product.id) {
        mutableStateOf(
            product.variations.firstOrNull { it.stockStatus == "instock" }?.id
                ?: product.variations.firstOrNull()?.id
        )
    }
    val selectedVariation = product.variations.firstOrNull { it.id == selectedVariationId }
    var quantity by remember(product.id, selectedVariationId) { mutableIntStateOf(1) }
    var selectedImage by remember(product.id) { mutableStateOf(product.image) }

    LaunchedEffect(selectedVariationId) {
        selectedVariation?.image?.takeIf { it.isNotBlank() }?.let { selectedImage = it }
    }

    val images = remember(product.id) {
        buildList {
            product.image?.let { add(it) }
            addAll(product.gallery.filterNot { it == product.image })
        }
    }

    val price = selectedVariation?.price?.takeIf { it.isNotBlank() } ?: product.price
    val regularPrice = selectedVariation?.regularPrice?.takeIf { it.isNotBlank() } ?: product.regularPrice
    val salePrice = selectedVariation?.salePrice ?: product.salePrice
    val maxQuantity = selectedVariation?.stockQuantity
        ?: product.stockQuantity.takeIf { selectedVariation == null }
    val inStock = selectedVariation?.stockStatus?.let { status ->
        status == "instock" && (maxQuantity == null || maxQuantity > 0)
    } ?: (
        product.isInStock &&
            (maxQuantity == null || maxQuantity > 0)
    )
    val canIncreaseQuantity = maxQuantity == null || quantity < maxQuantity
    val favorite = AppConfig.FEATURE_WISHLIST && WishlistStore.contains(product.id)

    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = c.surface,
                border = BorderStroke(1.dp, c.border)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, "بازگشت", tint = c.textPrimary)
                }
            }

            Spacer(Modifier.weight(1f))

            if (AppConfig.FEATURE_WISHLIST) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = c.surface,
                    border = BorderStroke(1.dp, c.border)
                ) {
                    IconButton(onClick = { WishlistStore.toggle(product) }) {
                        Icon(
                            if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            "علاقه‌مندی",
                            tint = if (favorite) c.error else c.textPrimary
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = PMTheme.shapes.largeCard,
                color = c.surface,
                border = BorderStroke(1.dp, c.border)
            ) {
                ProductImage(
                    selectedImage,
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(8.dp)
                        .clip(PMTheme.shapes.largeCard),
                    contentScale = ContentScale.Fit
                )
            }

            if (images.size > 1) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(images) { image ->
                        Surface(
                            modifier = Modifier
                                .size(68.dp)
                                .clickable { selectedImage = image },
                            shape = PMTheme.shapes.medium,
                            color = c.surface,
                            border = BorderStroke(
                                if (selectedImage == image) 1.5.dp else 1.dp,
                                if (selectedImage == image) c.primary else c.border
                            )
                        ) {
                            ProductImage(
                                image,
                                Modifier.fillMaxSize().padding(4.dp).clip(PMTheme.shapes.small)
                            )
                        }
                    }
                }
            }

            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                if (product.categories.isNotEmpty()) {
                    Text(
                        product.categories.take(2).joinToString(" • ") { it.name },
                        style = MaterialTheme.typography.labelMedium,
                        color = c.primary
                    )
                    Spacer(Modifier.height(6.dp))
                }

                Text(
                    product.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = c.textPrimary,
                    fontWeight = FontWeight.Black
                )

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = PMTheme.shapes.pill,
                        color = if (inStock) c.success.copy(alpha = .12f) else c.error.copy(alpha = .12f)
                    ) {
                        Text(
                            if (inStock) "موجود در انبار" else "ناموجود",
                            color = if (inStock) c.success else c.error,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    if (product.ratingCount > 0) {
                        Spacer(Modifier.width(10.dp))
                        Icon(
                            Icons.Outlined.Star,
                            null,
                            tint = c.accentGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${product.averageRating} (${product.ratingCount})",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textSecondary
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = PMTheme.shapes.card,
                    color = c.surface,
                    border = BorderStroke(1.dp, c.border)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "قیمت عمده",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        if (salePrice.isNotBlank() && regularPrice.isNotBlank()) {
                            Text(
                                toman(regularPrice),
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textMuted,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                        Text(
                            toman(price),
                            style = MaterialTheme.typography.headlineSmall,
                            color = c.primary,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                if (product.variations.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "انتخاب مدل",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = c.textPrimary
                    )
                    Spacer(Modifier.height(10.dp))
                    product.variations.forEachIndexed { index, variation ->
                        VariationOption(
                            variation = variation,
                            index = index,
                            selected = variation.id == selectedVariationId,
                            onClick = { selectedVariationId = variation.id }
                        )
                    }
                }

                if (product.attributes.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "مشخصات محصول",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = c.textPrimary
                    )
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = PMTheme.shapes.card,
                        color = c.surface,
                        border = BorderStroke(1.dp, c.border)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            product.attributes.forEachIndexed { index, attribute ->
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        attribute.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = c.textSecondary,
                                        modifier = Modifier.weight(.38f)
                                    )
                                    Text(
                                        attribute.options.joinToString("، "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = c.textPrimary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(.62f)
                                    )
                                }
                                if (index != product.attributes.lastIndex) {
                                    HorizontalDivider(color = c.border)
                                }
                            }
                        }
                    }
                }

                val descriptionHtml = product.shortDescription.ifBlank { product.description }
                val description = remember(descriptionHtml) {
                    Html.fromHtml(descriptionHtml, Html.FROM_HTML_MODE_LEGACY)
                        .toString()
                        .trim()
                }
                if (description.isNotBlank()) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "معرفی محصول",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = c.textPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = c.textSecondary
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "تعداد",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = c.textPrimary
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = PMTheme.shapes.medium,
                    color = c.surface,
                    border = BorderStroke(1.dp, c.border)
                ) {
                    Row(
                        Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = c.surfaceElevated,
                                contentColor = c.textPrimary
                            )
                        ) {
                            Text("−", style = MaterialTheme.typography.titleLarge)
                        }
                        Text(
                            quantity.toString(),
                            modifier = Modifier.padding(horizontal = 24.dp),
                            color = c.textPrimary,
                            fontWeight = FontWeight.Black
                        )
                        FilledTonalIconButton(
                            onClick = { if (canIncreaseQuantity) quantity++ },
                            enabled = canIncreaseQuantity,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = c.surfaceElevated,
                                contentColor = c.textPrimary
                            )
                        ) {
                            Text("+", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }

                if (maxQuantity != null && maxQuantity > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "موجودی قابل سفارش: $maxQuantity عدد",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textMuted
                    )
                }

                Spacer(Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = PMTheme.shapes.medium,
                    color = c.accentGold.copy(alpha = .08f),
                    border = BorderStroke(1.dp, c.accentGold.copy(alpha = .28f))
                ) {
                    Text(
                        "شرایط خرید و حداقل‌های سفارش مستقیماً از سایت پخش مهدی بررسی می‌شود و در مرحله سبد/تسویه به‌روز نمایش داده خواهد شد.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        Surface(
            color = c.surface,
            border = BorderStroke(1.dp, c.border),
            shadowElevation = 10.dp
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        CartStore.add(product, selectedVariation, quantity)
                        onBuy()
                    },
                    enabled = inStock && product.purchasable &&
                        (product.variations.isEmpty() || selectedVariation != null),
                    modifier = Modifier.weight(.92f).height(54.dp),
                    shape = PMTheme.shapes.medium,
                    border = BorderStroke(1.dp, c.primary)
                ) {
                    Text("خرید فوری", color = c.primary, fontWeight = FontWeight.Black)
                }

                Spacer(Modifier.width(8.dp))

                PMPrimaryButton(
                    text = if (inStock) "افزودن به سبد" else "ناموجود",
                    onClick = { CartStore.add(product, selectedVariation, quantity) },
                    modifier = Modifier.weight(1.35f),
                    enabled = inStock && product.purchasable &&
                        (product.variations.isEmpty() || selectedVariation != null),
                    icon = Icons.Outlined.ShoppingBag
                )
            }
        }
    }
}

@Composable
private fun VariationOption(
    variation: ProductVariation,
    index: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val c = PMTheme.colors
    val label = variation.attributes.values
        .filter { it.isNotBlank() }
        .joinToString(" • ")
        .ifBlank { "گزینه ${index + 1}" }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable(onClick = onClick),
        shape = PMTheme.shapes.medium,
        color = if (selected) c.primary.copy(alpha = .10f) else c.surface,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) c.primary else c.border)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = c.primary)
            )
            Spacer(Modifier.width(6.dp))
            Column(Modifier.weight(1f)) {
                Text(label, color = c.textPrimary, fontWeight = FontWeight.Bold)
                val variationInStock = variation.stockStatus == "instock" &&
                    (variation.stockQuantity == null ||
                        variation.stockQuantity > 0)
                Text(
                    when {
                        !variationInStock -> "ناموجود"
                        variation.stockQuantity != null -> "موجودی: ${variation.stockQuantity} عدد"
                        else -> "موجود"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (variationInStock) c.success else c.error
                )
            }
            Text(
                toman(variation.price),
                color = c.primary,
                fontWeight = FontWeight.Black
            )
        }
    }
}
