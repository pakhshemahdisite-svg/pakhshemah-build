package com.pakhshmahdi.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pakhshmahdi.app.data.model.HomePayload
import com.pakhshmahdi.app.data.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeState(
    val loading: Boolean = true,
    val data: HomePayload = HomePayload(),
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val repository = CatalogRepository()
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        runCatching { repository.home() }
            .onSuccess { payload ->
                _state.value = HomeState(loading = false, data = payload)
            }
            .onFailure {
                _state.value = _state.value.copy(
                    loading = false,
                    error = "اتصال به فروشگاه برقرار نشد. دوباره تلاش کنید."
                )
            }
    }
}
