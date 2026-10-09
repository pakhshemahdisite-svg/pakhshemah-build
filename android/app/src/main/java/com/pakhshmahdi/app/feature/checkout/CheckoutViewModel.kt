package com.pakhshmahdi.app.feature.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pakhshmahdi.app.data.auth.AuthRepository
import com.pakhshmahdi.app.data.auth.AuthStore
import com.pakhshmahdi.app.data.cart.CartLine
import com.pakhshmahdi.app.data.cart.CartStore
import com.pakhshmahdi.app.data.local.LocalStore
import com.pakhshmahdi.app.data.store.CheckoutRepository
import com.pakhshmahdi.app.data.store.PendingPayment
import com.pakhshmahdi.app.data.store.PaymentStatusPolicy
import com.pakhshmahdi.app.data.store.PaymentTerminalAction
import com.pakhshmahdi.app.data.store.StoreAddress
import com.pakhshmahdi.app.data.store.StoreApiException
import com.pakhshmahdi.app.data.store.StoreCart
import com.pakhshmahdi.app.data.store.moneyToLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CheckoutState(
    val loading: Boolean = true,
    val couponBusy: Boolean = false,
    val cart: StoreCart? = null,
    val addressCalculated: Boolean = false,
    val selectedPaymentMethod: String = "",
    val submitting: Boolean = false,
    val orderId: Long? = null,
    val redirectUrl: String? = null,
    val paymentUrl: String? = null,
    val paymentStatus: String? = null,
    val paymentStatusBusy: Boolean = false,
    val paymentStatusError: String? = null,
    val error: String? = null
)

class CheckoutViewModel : ViewModel() {
    private val repository = CheckoutRepository()
    private val authRepository = AuthRepository()
    private val _state = MutableStateFlow(CheckoutState())
    val state: StateFlow<CheckoutState> = _state
    private var prepared = false

    init {
        LocalStore.loadPendingPayment()?.let { pending ->
            _state.value = CheckoutState(
                loading = false,
                orderId = pending.orderId,
                paymentUrl = pending.paymentUrl,
                paymentStatus = pending.status
            )

            if (AuthStore.isLoggedIn) {
                refreshPaymentStatus()
            }
        }
    }

    fun prepare(lines: List<CartLine>) {
        if (prepared || _state.value.orderId != null) return
        prepared = true
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                val refreshedLines = repository.refreshLines(lines)
                CartStore.replaceAll(refreshedLines)

                repository.prepareCart(refreshedLines)
            }.onSuccess { cart ->
                    _state.value = _state.value.copy(
                        loading = false,
                        cart = cart,
                        selectedPaymentMethod = cart.paymentMethods.firstOrNull().orEmpty(),
                        error = cart.errors.firstOrNull()?.message
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = friendlyError(error, "آماده‌سازی پرداخت انجام نشد.")
                    )
                }
        }
    }

    fun applyCoupon(code: String) {
        if (code.isBlank() || _state.value.couponBusy) return
        viewModelScope.launch {
            _state.value = _state.value.copy(couponBusy = true, error = null)
            runCatching { repository.applyCoupon(code) }
                .onSuccess { cart ->
                    _state.value = _state.value.copy(
                        couponBusy = false,
                        cart = cart,
                        error = cart.errors.firstOrNull()?.message
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        couponBusy = false,
                        error = friendlyError(error, "کد تخفیف اعمال نشد.")
                    )
                }
        }
    }

    fun removeCoupon(code: String) {
        if (code.isBlank() || _state.value.couponBusy) return
        viewModelScope.launch {
            _state.value = _state.value.copy(couponBusy = true, error = null)
            runCatching { repository.removeCoupon(code) }
                .onSuccess { cart ->
                    _state.value = _state.value.copy(
                        couponBusy = false,
                        cart = cart,
                        error = cart.errors.firstOrNull()?.message
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        couponBusy = false,
                        error = friendlyError(error, "حذف کد تخفیف انجام نشد.")
                    )
                }
        }
    }

    fun calculateAddress(address: StoreAddress) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                error = null,
                addressCalculated = false
            )
            runCatching { repository.updateAddress(address) }
                .onSuccess { cart ->
                    _state.value = _state.value.copy(
                        loading = false,
                        cart = cart,
                        addressCalculated = true,
                        selectedPaymentMethod = _state.value.selectedPaymentMethod
                            .takeIf { it in cart.paymentMethods }
                            ?: cart.paymentMethods.firstOrNull().orEmpty(),
                        error = cart.errors.firstOrNull()?.message
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = friendlyError(error, "محاسبه ارسال انجام نشد.")
                    )
                }
        }
    }

    fun selectShipping(packageId: Int, rateId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { repository.selectShipping(packageId, rateId) }
                .onSuccess { cart ->
                    _state.value = _state.value.copy(
                        loading = false,
                        cart = cart,
                        error = cart.errors.firstOrNull()?.message
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = friendlyError(error, "روش ارسال انتخاب نشد.")
                    )
                }
        }
    }

    fun setPaymentMethod(method: String) {
        _state.value = _state.value.copy(selectedPaymentMethod = method)
    }

    fun submit(address: StoreAddress, note: String) {
        val cart = _state.value.cart ?: run {
            _state.value = _state.value.copy(error = "سبد خرید آماده پرداخت نیست.")
            return
        }
        val paymentMethod = _state.value.selectedPaymentMethod
        if (paymentMethod.isBlank()) {
            _state.value = _state.value.copy(error = "روش پرداخت را انتخاب کنید.")
            return
        }
        if (cart.errors.isNotEmpty()) {
            _state.value = _state.value.copy(
                error = cart.errors.firstOrNull()?.message ?: "سبد خرید نیاز به اصلاح دارد."
            )
            return
        }
        if (cart.itemsCount <= 0 || moneyToLong(cart.totals.totalPrice) == null) {
            _state.value = _state.value.copy(error = "مبلغ سفارش معتبر نیست. لطفاً مرحله ارسال را دوباره محاسبه کنید.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(submitting = true, error = null)
            runCatching {
                repository.submitOrder(
                    address = address,
                    paymentMethod = paymentMethod,
                    expectedTotal = cart.totals.totalPrice,
                    customerNote = note
                )
            }.onSuccess { result ->
                val paymentUrl = result.paymentResult?.redirectUrl
                    ?.takeIf { it.isNotBlank() }
                val waitsForGateway = paymentUrl != null

                // For online gateways the order exists before payment is confirmed.
                // Keep the local cart until WooCommerce reports a paid status so the
                // customer does not lose the basket after a cancelled/failed payment.
                if (result.orderId > 0 && waitsForGateway) {
                    LocalStore.savePendingPayment(
                        PendingPayment(
                            orderId = result.orderId,
                            paymentUrl = paymentUrl,
                            status = "pending"
                        )
                    )
                } else {
                    LocalStore.savePendingPayment(null)
                }

                if (result.orderId > 0 && !waitsForGateway) {
                    CartStore.clear()
                }

                _state.value = _state.value.copy(
                    submitting = false,
                    orderId = result.orderId.takeIf { it > 0 },
                    redirectUrl = paymentUrl,
                    paymentUrl = paymentUrl,
                    paymentStatus = if (waitsForGateway) "pending" else "placed",
                    paymentStatusBusy = false,
                    paymentStatusError = null,
                    error = null
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    submitting = false,
                    error = friendlyError(error, "ثبت سفارش انجام نشد.")
                )
            }
        }
    }

    fun consumeRedirect() {
        _state.value = _state.value.copy(redirectUrl = null)
    }

    fun refreshPaymentStatus() {
        val orderId = _state.value.orderId ?: return
        if (_state.value.paymentStatusBusy) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                paymentStatusBusy = true,
                paymentStatusError = null
            )

            runCatching { authRepository.orderDetail(orderId) }
                .onSuccess { order ->
                    val status = PaymentStatusPolicy.normalize(order.status)

                    when (PaymentStatusPolicy.action(status)) {
                        PaymentTerminalAction.COMPLETE_AND_CLEAR_CART -> {
                            CartStore.clear()
                            LocalStore.savePendingPayment(null)
                            LocalStore.saveStoreCartToken(null)
                        }
                        PaymentTerminalAction.RELEASE_SESSION_KEEP_CART -> {
                            // Keep the local basket so the customer can retry,
                            // but drop the old WooCommerce cart session and rebuild
                            // it on the next checkout attempt.
                            LocalStore.savePendingPayment(null)
                            LocalStore.saveStoreCartToken(null)
                        }
                        PaymentTerminalAction.KEEP_PENDING -> {
                            LocalStore.savePendingPayment(
                                PendingPayment(
                                    orderId = orderId,
                                    paymentUrl = _state.value.paymentUrl,
                                    status = status
                                )
                            )
                        }
                    }

                    _state.value = _state.value.copy(
                        paymentStatusBusy = false,
                        paymentStatus = status,
                        paymentStatusError = null
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        paymentStatusBusy = false,
                        paymentStatusError = authRepository.readableError(
                            error,
                            "وضعیت پرداخت از فروشگاه دریافت نشد. دوباره تلاش کنید."
                        )
                    )
                }
        }
    }

    private fun friendlyError(error: Throwable, fallback: String): String {
        if (error is StoreApiException && !error.message.isNullOrBlank()) {
            return error.message ?: fallback
        }

        val raw = error.message.orEmpty()
        if (
            raw.contains("BEGIN_OBJECT", ignoreCase = true) ||
            raw.contains("BEGIN_ARRAY", ignoreCase = true) ||
            raw.contains("Json", ignoreCase = true) ||
            raw.contains("Malformed", ignoreCase = true)
        ) {
            return "پاسخ فروشگاه قابل پردازش نبود. اتصال سبد خرید تازه‌سازی شد؛ دوباره تلاش کنید."
        }

        return fallback
    }
}
