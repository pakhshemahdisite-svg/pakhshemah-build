package com.pakhshmahdi.app.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pakhshmahdi.app.data.model.Category
import com.pakhshmahdi.app.data.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CategoriesState(
    val loading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val error: String? = null
)

class CategoriesViewModel : ViewModel() {
    private val repository = CatalogRepository()
    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state

    init {
        refresh()
    }

    fun refresh() {
        if (_state.value.loading && _state.value.categories.isNotEmpty()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { repository.categories() }
                .onSuccess { categories ->
                    _state.value = CategoriesState(
                        loading = false,
                        categories = categories
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        loading = false,
                        error = "دریافت دسته‌بندی‌ها از سایت انجام نشد."
                    )
                }
        }
    }
}
