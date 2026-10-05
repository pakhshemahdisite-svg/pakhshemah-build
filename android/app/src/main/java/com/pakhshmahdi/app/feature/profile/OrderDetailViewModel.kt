package com.pakhshmahdi.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pakhshmahdi.app.data.auth.AuthRepository
import com.pakhshmahdi.app.data.auth.OrderDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class OrderDetailState(
    val loading: Boolean = true,
    val detail: OrderDetail? = null,
    val error: String? = null
)

class OrderDetailViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val _state = MutableStateFlow(OrderDetailState())
    val state: StateFlow<OrderDetailState> = _state

    fun load(id: Long, force: Boolean = false) {
        if (!force && _state.value.detail?.id == id) return
        viewModelScope.launch {
            _state.value = OrderDetailState(loading = true)
            runCatching { repository.orderDetail(id) }
                .onSuccess { _state.value = OrderDetailState(loading = false, detail = it) }
                .onFailure { error ->
                    _state.value = OrderDetailState(
                        loading = false,
                        error = repository.readableError(error, "دریافت جزئیات سفارش انجام نشد.")
                    )
                }
        }
    }
}
