package com.pakhshmahdi.app.feature.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pakhshmahdi.app.ui.components.PMEmptyState
import com.pakhshmahdi.app.ui.components.PMErrorState
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.components.ProductImage
import com.pakhshmahdi.app.ui.theme.PMGoldDeep
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun CategoriesScreen(
    onCategory: (Long) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val c = PMTheme.colors
    val vm: CategoriesViewModel = viewModel()
    val state by vm.state.collectAsState()

    Column(Modifier.fillMaxSize().background(c.background)) {
        PMScreenTopBar(
            title = "دسته‌بندی‌ها",
            subtitle = "انتخاب گروه کالایی",
            onBack = onBack
        )

        when {
            state.loading && state.categories.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PMGoldDeep)
                }
            }

            state.error != null && state.categories.isEmpty() -> {
                PMErrorState(
                    message = state.error ?: "دریافت دسته‌بندی‌ها انجام نشد.",
                    onRetry = vm::refresh,
                    modifier = Modifier.fillMaxSize()
                )
            }

            state.categories.isEmpty() -> {
                PMEmptyState(
                    icon = Icons.Outlined.Category,
                    title = "دسته‌بندی‌ای پیدا نشد",
                    message = "در حال حاضر دسته‌بندی قابل نمایشی از فروشگاه دریافت نشده است.",
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.categories, key = { it.id }) { category ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategory(category.id) },
                            shape = RoundedCornerShape(20.dp),
                            color = c.surface,
                            border = BorderStroke(1.dp, c.border),
                            shadowElevation = 3.dp
                        ) {
                            Column {
                                ProductImage(
                                    category.image,
                                    Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1.35f)
                                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(34.dp),
                                        shape = CircleShape,
                                        color = c.accentGold.copy(alpha = .22f),
                                        border = BorderStroke(1.dp, PMGoldDeep.copy(alpha = .18f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.ChevronLeft,
                                                contentDescription = "مشاهده دسته‌بندی",
                                                tint = PMGoldDeep,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(8.dp))

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            category.name,
                                            color = c.textPrimary,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.End,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (category.count > 0) {
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                "${category.count} محصول",
                                                color = c.textMuted,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
