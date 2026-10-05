package com.pakhshmahdi.app.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pakhshmahdi.app.data.model.Category
import com.pakhshmahdi.app.data.model.Product
import com.pakhshmahdi.app.data.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class CatalogSort {
    NEWEST,
    PRICE_ASC,
    PRICE_DESC
}

data class CatalogState(
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val categoriesLoading: Boolean = true,
    val categoriesError: String? = null,
    val query: String = "",
    val selectedCategoryId: Long? = null,
    val sort: CatalogSort = CatalogSort.NEWEST,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val total: Int = 0,
    val error: String? = null
) {
    val hasMore: Boolean get() = currentPage < totalPages
}

class CatalogViewModel : ViewModel() {
    private val repository = CatalogRepository()
    private val _state = MutableStateFlow(CatalogState())
    val state: StateFlow<CatalogState> = _state
    private var searchJob: Job? = null
    private var productsJob: Job? = null

    init {
        refreshCategories()
        loadProducts(reset = true)
    }

    fun refreshCategories() {
        viewModelScope.launch {
            _state.value = _state.value.copy(categoriesLoading = true, categoriesError = null)
            runCatching { repository.categories() }
                .onSuccess { categories ->
                    _state.value = _state.value.copy(
                        categories = categories,
                        categoriesLoading = false,
                        categoriesError = null
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        categoriesLoading = false,
                        categoriesError = "دریافت دسته‌بندی‌ها از سایت انجام نشد."
                    )
                }
        }
    }

    fun setQuery(value: String) {
        _state.value = _state.value.copy(query = value)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(450)
            if (_state.value.query == value) {
                loadProducts(reset = true)
            }
        }
    }

    fun search() {
        searchJob?.cancel()
        loadProducts(reset = true)
    }

    fun selectCategory(id: Long?) {
        if (_state.value.selectedCategoryId == id) return
        _state.value = _state.value.copy(selectedCategoryId = id)
        loadProducts(reset = true)
    }

    fun setSort(sort: CatalogSort) {
        if (_state.value.sort == sort) return
        _state.value = _state.value.copy(sort = sort)
        loadProducts(reset = true)
    }

    fun retry() = loadProducts(reset = true)

    fun loadMore() {
        val current = _state.value
        if (current.loading || current.loadingMore || !current.hasMore) return
        loadProducts(reset = false)
    }

    private fun loadProducts(reset: Boolean) {
        productsJob?.cancel()
        productsJob = viewModelScope.launch {
            val current = _state.value
            val nextPage = if (reset) 1 else current.currentPage + 1

            _state.value = if (reset) {
                current.copy(loading = true, loadingMore = false, error = null)
            } else {
                current.copy(loadingMore = true, error = null)
            }

            val orderBy = when (current.sort) {
                CatalogSort.NEWEST -> "date"
                CatalogSort.PRICE_ASC, CatalogSort.PRICE_DESC -> "price"
            }
            val order = when (current.sort) {
                CatalogSort.PRICE_ASC -> "ASC"
                CatalogSort.NEWEST, CatalogSort.PRICE_DESC -> "DESC"
            }

            runCatching {
                repository.productsPage(
                    page = nextPage,
                    perPage = 20,
                    category = current.selectedCategoryId,
                    search = current.query.trim().takeIf { it.isNotEmpty() },
                    orderBy = orderBy,
                    order = order
                )
            }.onSuccess { payload ->
                val merged = if (reset) payload.items else {
                    (current.products + payload.items).distinctBy { it.id }
                }
                _state.value = _state.value.copy(
                    loading = false,
                    loadingMore = false,
                    products = merged,
                    currentPage = payload.page,
                    totalPages = payload.totalPages.coerceAtLeast(1),
                    total = payload.total,
                    error = null
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    loading = false,
                    loadingMore = false,
                    products = if (reset) emptyList() else current.products,
                    error = if (reset) "دریافت محصولات از سایت با خطا مواجه شد." else "بارگذاری محصولات بیشتر انجام نشد."
                )
            }
        }
    }
}
