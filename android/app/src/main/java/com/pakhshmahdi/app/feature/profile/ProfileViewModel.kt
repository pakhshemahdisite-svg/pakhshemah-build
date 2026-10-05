package com.pakhshmahdi.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pakhshmahdi.app.data.auth.AuthRepository
import com.pakhshmahdi.app.data.auth.AuthStore
import com.pakhshmahdi.app.data.auth.OrderSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileState(
    val loading: Boolean = false,
    val orders: List<OrderSummary> = emptyList(),
    val error: String? = null
)

class ProfileViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    fun refresh() {
        if (!AuthStore.isLoggedIn || _state.value.loading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                val user = repository.refreshMe()
                val orders = repository.orders()
                user to orders
            }.onSuccess { (user, orders) ->
                AuthStore.updateUser(user)
                _state.value = ProfileState(loading = false, orders = orders)
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = repository.readableError(error, "دریافت اطلاعات حساب انجام نشد.")
                )
            }
        }
    }

    fun logout() {
        AuthStore.logout()
        _state.value = ProfileState()
    }
}
