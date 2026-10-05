package com.pakhshmahdi.app.feature.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pakhshmahdi.app.ui.components.PMEmptyState
import com.pakhshmahdi.app.ui.components.PMErrorState
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.components.ProductImage
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun CategoriesScreen(
    onCategory: (Long) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val c = PMTheme.colors
    val vm: CategoriesViewModel = viewModel()
    val state by vm.state.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        PMScreenTopBar(
            title = "دسته‌بندی‌ها",
            subtitle = "انتخاب گروه کالایی",
            onBack = onBack
        )

        when {
            state.loading && state.categories.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator(color = c.primary)
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.categories, key = { it.id }) { category ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategory(category.id) },
                        shape = PMTheme.shapes.card,
                        color = c.surface,
                        border = BorderStroke(1.dp, c.border),
                        shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ProductImage(
                                category.image,
                                Modifier
                                    .size(104.dp)
                                    .clip(PMTheme.shapes.medium)
                            )
                            Spacer(Modifier.height(9.dp))
                            Text(
                                category.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = c.textPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                            if (category.count > 0) {
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    "${category.count} محصول",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c.textSecondary,
                                    textAlign = TextAlign.Center
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
