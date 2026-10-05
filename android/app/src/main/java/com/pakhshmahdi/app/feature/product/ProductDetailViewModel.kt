package com.pakhshmahdi.app.feature.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pakhshmahdi.app.data.model.Product
import com.pakhshmahdi.app.data.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProductDetailState(
    val loading: Boolean = true,
    val product: Product? = null,
    val error: String? = null
)

class ProductDetailViewModel : ViewModel() {
    private val repository = CatalogRepository()
    private val _state = MutableStateFlow(ProductDetailState())
    val state: StateFlow<ProductDetailState> = _state

    fun load(id: Long) {
        if (_state.value.product?.id == id) return
        viewModelScope.launch {
            _state.value = ProductDetailState(loading = true)
            runCatching { repository.product(id) }
                .onSuccess { _state.value = ProductDetailState(loading = false, product = it) }
                .onFailure {
                    _state.value = ProductDetailState(
                        loading = false,
                        error = "اطلاعات محصول دریافت نشد."
                    )
                }
        }
    }
}
