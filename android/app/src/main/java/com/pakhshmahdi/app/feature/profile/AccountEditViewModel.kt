package com.pakhshmahdi.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pakhshmahdi.app.data.auth.AuthRepository
import com.pakhshmahdi.app.data.auth.AuthStore
import com.pakhshmahdi.app.data.auth.BillingProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AccountEditState(
    val saving: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class AccountEditViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val _state = MutableStateFlow(AccountEditState())
    val state: StateFlow<AccountEditState> = _state

    fun save(
        firstName: String,
        lastName: String,
        email: String,
        billing: BillingProfile
    ) {
        if (_state.value.saving) return
        viewModelScope.launch {
            _state.value = AccountEditState(saving = true)
            runCatching {
                repository.updateProfile(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    billing = billing
                )
            }.onSuccess { user ->
                AuthStore.updateUser(user)
                _state.value = AccountEditState(success = true)
            }.onFailure { error ->
                _state.value = AccountEditState(
                    error = repository.readableError(error, "ذخیره اطلاعات حساب انجام نشد.")
                )
            }
        }
    }
}
