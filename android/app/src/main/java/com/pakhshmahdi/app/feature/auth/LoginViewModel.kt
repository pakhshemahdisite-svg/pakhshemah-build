package com.pakhshmahdi.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pakhshmahdi.app.data.auth.AuthRepository
import com.pakhshmahdi.app.data.auth.AuthStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginState(
    val phone: String = "",
    val code: String = "",
    val otpSent: Boolean = false,
    val loading: Boolean = false,
    val resendAvailableAt: Long = 0L,
    val success: Boolean = false,
    val error: String? = null
)

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun setPhone(value: String) {
        _state.value = _state.value.copy(
            phone = value.filter { it.isDigit() }.take(11),
            error = null
        )
    }

    fun setCode(value: String) {
        _state.value = _state.value.copy(
            code = value.filter { it.isDigit() }.take(6),
            error = null
        )
    }

    fun editPhone() {
        _state.value = _state.value.copy(
            otpSent = false,
            code = "",
            error = null
        )
    }

    fun requestOtp() {
        val phone = _state.value.phone
        if (phone.length != 11 || !phone.startsWith("09")) {
            _state.value = _state.value.copy(error = "شماره موبایل معتبر وارد کنید.")
            return
        }
        if (_state.value.loading) return

        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { repository.requestOtp(phone) }
                .onSuccess { response ->
                    _state.value = _state.value.copy(
                        loading = false,
                        otpSent = response.success,
                        resendAvailableAt = System.currentTimeMillis() + response.resendAfter * 1000L,
                        code = "",
                        error = if (response.success) null else "ارسال کد تایید انجام نشد."
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = repository.readableError(error, "ارسال کد تایید انجام نشد.")
                    )
                }
        }
    }

    fun verifyOtp() {
        val current = _state.value
        if (current.code.length !in 4..6) {
            _state.value = current.copy(error = "کد تایید را کامل وارد کنید.")
            return
        }
        if (current.loading) return

        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { repository.verifyOtp(current.phone, current.code) }
                .onSuccess { response ->
                    AuthStore.login(response.token, response.user)
                    _state.value = _state.value.copy(
                        loading = false,
                        success = true,
                        error = null
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = repository.readableError(error, "ورود به حساب کاربری انجام نشد.")
                    )
                }
        }
    }
}
