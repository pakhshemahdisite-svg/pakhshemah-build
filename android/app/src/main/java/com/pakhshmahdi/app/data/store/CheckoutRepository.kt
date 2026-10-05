package com.pakhshmahdi.app.data.store

import com.google.gson.Gson
import com.pakhshmahdi.app.data.auth.AuthStore
import com.pakhshmahdi.app.data.auth.ClaimOrderRequest
import com.pakhshmahdi.app.data.cart.CartLine
import com.pakhshmahdi.app.data.local.LocalStore
import com.pakhshmahdi.app.data.remote.ApiClient
import retrofit2.Response

class StoreApiException(message: String) : Exception(message)

class CheckoutRepository {
    private val api = StoreApiClient.api
    private val gson = Gson()
    private var preparedLines: List<CartLine> = emptyList()

    suspend fun refreshLines(lines: List<CartLine>): List<CartLine> {
        if (lines.isEmpty()) throw StoreApiException("سبد خرید خالی است.")

        return lines.map { line ->
            val freshProduct = runCatching {
                ApiClient.api.product(line.product.id)
            }.getOrElse {
                throw StoreApiException("اطلاعات «${line.product.name}» به‌روزرسانی نشد. دوباره تلاش کنید.")
            }

            if (!freshProduct.purchasable || !freshProduct.isInStock) {
                throw StoreApiException("«${freshProduct.name}» در حال حاضر قابل سفارش نیست.")
            }

            val freshVariation = line.variation?.let { previous ->
                freshProduct.variations.firstOrNull { it.id == previous.id }
                    ?: throw StoreApiException("گزینه انتخاب‌شده «${freshProduct.name}» دیگر موجود نیست.")
            }

            if (freshVariation != null && freshVariation.stockStatus != "instock") {
                throw StoreApiException("گزینه انتخاب‌شده «${freshProduct.name}» ناموجود شده است.")
            }

            val maxStock = freshVariation?.stockQuantity
                ?: freshProduct.stockQuantity.takeIf { freshVariation == null }
            if (maxStock != null && maxStock <= 0) {
                throw StoreApiException("«${freshProduct.name}» ناموجود شده است.")
            }

            line.copy(
                product = freshProduct,
                variation = freshVariation,
                quantity = maxStock?.let { line.quantity.coerceAtMost(it) } ?: line.quantity
            )
        }
    }

    suspend fun prepareCart(lines: List<CartLine>): StoreCart {
        if (lines.isEmpty()) throw StoreApiException("سبد خرید خالی است.")
        preparedLines = lines.toList()

        val token = ensureToken()

        // DELETE /cart/items returns [] on success, not a cart object.
        val clearResponse = api.clearCart(token)
        if (!clearResponse.isSuccessful) {
            throw StoreApiException(errorMessage(clearResponse, "پاک‌سازی سبد سرور انجام نشد."))
        }

        var cart: StoreCart? = null
        lines.forEach { line ->
            val itemId = line.variation?.id ?: line.product.id
            cart = requireBody(
                api.addItem(
                    token,
                    AddItemRequest(
                        id = itemId,
                        quantity = line.quantity
                    )
                ),
                "افزودن محصول به سبد سرور انجام نشد."
            )
        }

        val prepared = cart ?: requireBody(api.cart(token), "سبد خرید دریافت نشد.")
        if (prepared.itemsCount <= 0) {
            throw StoreApiException("سبد خرید در ووکامرس همگام نشد. دوباره تلاش کنید.")
        }

        return prepared
    }

    suspend fun applyCoupon(code: String): StoreCart {
        val token = ensureToken()
        return requireBody(
            api.applyCoupon(token, CouponRequest(code.trim())),
            "اعمال کد تخفیف انجام نشد."
        )
    }

    suspend fun removeCoupon(code: String): StoreCart {
        val token = ensureToken()
        return requireBody(
            api.removeCoupon(token, CouponRequest(code.trim())),
            "حذف کد تخفیف انجام نشد."
        )
    }

    suspend fun updateAddress(address: StoreAddress): StoreCart {
        val token = ensureToken()
        var cart = requireBody(
            api.updateCustomer(
                token,
                UpdateCustomerRequest(
                    billingAddress = address,
                    shippingAddress = address.toShippingAddress()
                )
            ),
            "محاسبه آدرس و روش ارسال انجام نشد."
        )

        if (cart.itemsCount <= 0) {
            throw StoreApiException("سبد خرید سرور خالی است. به سبد خرید برگردید و دوباره ادامه دهید.")
        }

        // Some legacy Persian shipping methods calculate after the customer update
        // request has finished. Refresh once before using the app-specific fallback.
        if (cart.needsShipping && !cart.hasShippingRates()) {
            val refreshed = api.cart(token)
            if (refreshed.isSuccessful && refreshed.body() != null) {
                cart = refreshed.body()!!
            }
        }

        // The live site now injects a WooCommerce Store API-compatible fallback
        // rate based on the site's real PWS weight/class rules. If an older Store
        // API response still arrives without rates, ask the same server-side
        // calculator for the rate and immediately select it in WooCommerce.
        if (cart.needsShipping && !cart.hasShippingRates() && preparedLines.isNotEmpty()) {
            val quote = runCatching {
                ApiClient.api.shippingQuote(
                    ShippingQuoteRequest(
                        lines = preparedLines.map { line ->
                            ShippingQuoteLine(
                                id = line.variation?.id ?: line.product.id,
                                quantity = line.quantity
                            )
                        },
                        state = address.state,
                        city = address.city,
                        postcode = address.postcode
                    )
                )
            }.getOrElse {
                throw StoreApiException("هزینه ارسال برای این سفارش محاسبه نشد. دوباره تلاش کنید.")
            }

            val selected = api.selectShippingRate(
                token,
                SelectShippingRateRequest(
                    packageId = 0,
                    rateId = quote.rateId
                )
            )

            cart = requireBody(
                selected,
                "هزینه ارسال محاسبه شد اما روش ارسال در ووکامرس انتخاب نشد."
            )
        }

        if (cart.needsShipping && !cart.hasShippingRates()) {
            throw StoreApiException("برای این آدرس امکان محاسبه ارسال وجود ندارد. آدرس و کدپستی را بررسی کنید.")
        }

        LocalStore.saveCheckoutAddress(address)
        return cart
    }

    suspend fun selectShipping(packageId: Int, rateId: String): StoreCart {
        val token = ensureToken()
        return requireBody(
            api.selectShippingRate(
                token,
                SelectShippingRateRequest(packageId = packageId, rateId = rateId)
            ),
            "انتخاب روش ارسال انجام نشد."
        )
    }

    suspend fun submitOrder(
        address: StoreAddress,
        paymentMethod: String,
        expectedTotal: String,
        customerNote: String
    ): CheckoutResponse {
        val token = ensureToken()
        val result = requireBody(
            api.checkout(
                token,
                CheckoutRequest(
                    billingAddress = address,
                    shippingAddress = address.toShippingAddress(),
                    paymentMethod = paymentMethod,
                    expectedTotal = expectedTotal,
                    customerNote = customerNote
                )
            ),
            "ثبت سفارش انجام نشد."
        )

        if (result.orderId > 0) {
            LocalStore.saveCheckoutAddress(address)
            val authToken = AuthStore.token
            if (!authToken.isNullOrBlank() && result.orderKey.isNotBlank()) {
                runCatching {
                    ApiClient.api.claimOrder(
                        authorization = "Bearer $authToken",
                        request = ClaimOrderRequest(
                            orderId = result.orderId,
                            orderKey = result.orderKey
                        )
                    )
                }
            }

            // Keep the WooCommerce cart session alive while an online payment is
            // still pending. This makes returning from/cancelling the gateway and
            // retrying checkout resilient. Non-gateway orders can safely release it.
            val waitsForGateway = !result.paymentResult?.redirectUrl.isNullOrBlank()
            if (!waitsForGateway) {
                LocalStore.saveStoreCartToken(null)
            }
        }
        return result
    }

    private fun StoreCart.hasShippingRates(): Boolean =
        shippingRates.any { pack -> pack.shippingRates.isNotEmpty() }

    private suspend fun ensureToken(): String {
        val stored = LocalStore.loadStoreCartToken()
        val first = api.cart(stored)

        if (first.isSuccessful) {
            val token = first.headers()["Cart-Token"] ?: stored
            if (!token.isNullOrBlank()) {
                LocalStore.saveStoreCartToken(token)
                return token
            }
        }

        LocalStore.saveStoreCartToken(null)
        val fresh = api.cart(null)
        if (!fresh.isSuccessful) {
            throw StoreApiException(errorMessage(fresh, "ایجاد نشست سبد خرید انجام نشد."))
        }

        val token = fresh.headers()["Cart-Token"]
            ?: throw StoreApiException("توکن امن سبد خرید از ووکامرس دریافت نشد.")

        LocalStore.saveStoreCartToken(token)
        return token
    }

    private fun <T> requireBody(response: Response<T>, fallback: String): T {
        if (!response.isSuccessful) {
            throw StoreApiException(errorMessage(response, fallback))
        }
        return response.body() ?: throw StoreApiException(fallback)
    }

    private fun <T> errorMessage(response: Response<T>, fallback: String): String {
        val raw = runCatching { response.errorBody()?.string() }.getOrNull()
        if (!raw.isNullOrBlank()) {
            val parsed = runCatching { gson.fromJson(raw, StoreApiError::class.java) }.getOrNull()
            if (!parsed?.message.isNullOrBlank()) return parsed?.message ?: fallback
        }
        return fallback
    }
}
