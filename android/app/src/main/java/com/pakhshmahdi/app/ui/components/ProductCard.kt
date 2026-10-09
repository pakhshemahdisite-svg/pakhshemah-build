package com.pakhshmahdi.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pakhshmahdi.app.core.AppConfig
import com.pakhshmahdi.app.data.cart.WholesaleOrderPolicy
import com.pakhshmahdi.app.data.model.Product
import com.pakhshmahdi.app.data.wishlist.WishlistStore
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun ProductCard(
    product: Product,
    onOpen: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = PMTheme.colors
    val favorite = AppConfig.FEATURE_WISHLIST && WishlistStore.contains(product.id)
    val inStock = product.isInStock &&
        (product.stockQuantity == null ||
            product.stockQuantity >= WholesaleOrderPolicy.MINIMUM_PER_PRODUCT)

    Surface(
        modifier = modifier.clickable(onClick = onOpen),
        shape = PMTheme.shapes.card,
        color = c.surface,
        border = BorderStroke(1.dp, c.border),
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(8.dp)) {
            Box {
                ProductImage(
                    product.image,
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(PMTheme.shapes.medium)
                )

                if (product.hasSale) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(7.dp),
                        shape = PMTheme.shapes.pill,
                        color = c.accentGold
                    ) {
                        Text(
                            "تخفیف",
                            color = c.primaryDark,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (AppConfig.FEATURE_WISHLIST) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(7.dp),
                        shape = CircleShape,
                        color = c.surface.copy(alpha = .94f),
                        border = BorderStroke(1.dp, c.border)
                    ) {
                        IconButton(
                            onClick = { WishlistStore.toggle(product) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "علاقه‌مندی",
                                tint = if (favorite) c.error else c.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(9.dp))

            Text(
                product.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = c.textPrimary,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(6.dp),
                    shape = CircleShape,
                    color = if (inStock) c.success else c.error
                ) {}
                Spacer(Modifier.width(5.dp))
                Text(
                    if (inStock) "موجود" else "ناموجود",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textMuted
                )
            }

            Spacer(Modifier.height(8.dp))

            if (product.hasSale && product.regularPrice.isNotBlank()) {
                Text(
                    toman(product.regularPrice),
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textMuted,
                    textDecoration = TextDecoration.LineThrough
                )
            } else {
                Spacer(Modifier.height(16.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    toman(product.price),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = c.primary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                val requiresSelection = product.type == "variable" || product.variations.isNotEmpty()

                FilledIconButton(
                    onClick = if (requiresSelection) onOpen else onAdd,
                    enabled = inStock && product.purchasable,
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = c.primary,
                        contentColor = if (c.primary == c.accentGold) c.primaryDark else androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    Icon(
                        if (requiresSelection) Icons.Outlined.ChevronLeft else Icons.Outlined.Add,
                        contentDescription = if (requiresSelection) "انتخاب گزینه" else "افزودن به سبد",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
