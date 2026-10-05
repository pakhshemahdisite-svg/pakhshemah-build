package com.pakhshmahdi.app.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pakhshmahdi.app.data.model.Product
import com.pakhshmahdi.app.data.repository.CatalogRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SearchState(
    val query: String = "",
    val products: List<Product> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val error: String? = null
) {
    val hasMore: Boolean get() = currentPage < totalPages
}

class SearchViewModel : ViewModel() {
    private val repository = CatalogRepository()
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state

    private var debounceJob: Job? = null
    private var requestJob: Job? = null

    fun setQuery(value: String) {
        val normalized = value.take(80)
        _state.value = _state.value.copy(query = normalized, error = null)

        debounceJob?.cancel()

        if (normalized.isBlank()) {
            requestJob?.cancel()
            _state.value = SearchState()
            return
        }

        debounceJob = viewModelScope.launch {
            delay(450)
            if (_state.value.query == normalized) {
                load(reset = true)
            }
        }
    }

    fun search() {
        debounceJob?.cancel()
        if (_state.value.query.isBlank()) {
            _state.value = SearchState()
            return
        }
        load(reset = true)
    }

    fun loadMore() {
        val current = _state.value
        if (
            current.query.isBlank() ||
            current.loading ||
            current.loadingMore ||
            !current.hasMore
        ) {
            return
        }
        load(reset = false)
    }

    private fun load(reset: Boolean) {
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            val current = _state.value
            val query = current.query.trim()
            if (query.isEmpty()) return@launch

            val page = if (reset) 1 else current.currentPage + 1
            _state.value = if (reset) {
                current.copy(
                    loading = true,
                    loadingMore = false,
                    products = emptyList(),
                    currentPage = 1,
                    totalPages = 1,
                    error = null
                )
            } else {
                current.copy(loadingMore = true, error = null)
            }

            runCatching {
                repository.productsPage(
                    page = page,
                    perPage = 20,
                    search = query
                )
            }.onSuccess { payload ->
                val latest = _state.value
                if (latest.query.trim() != query) return@onSuccess

                val merged = if (reset) {
                    payload.items
                } else {
                    (current.products + payload.items).distinctBy { it.id }
                }

                _state.value = latest.copy(
                    loading = false,
                    loadingMore = false,
                    products = merged,
                    currentPage = payload.page,
                    totalPages = payload.totalPages.coerceAtLeast(1),
                    error = null
                )
            }.onFailure {
                val latest = _state.value
                if (latest.query.trim() != query) return@onFailure

                _state.value = latest.copy(
                    loading = false,
                    loadingMore = false,
                    products = if (reset) emptyList() else current.products,
                    error = if (reset) "جستجوی محصولات انجام نشد." else "بارگذاری نتایج بیشتر انجام نشد."
                )
            }
        }
    }
}
