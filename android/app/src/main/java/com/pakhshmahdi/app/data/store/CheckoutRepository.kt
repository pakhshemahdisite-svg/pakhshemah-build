package com.pakhshmahdi.app.data.store

import com.google.gson.Gson
import com.pakhshmahdi.app.data.auth.AuthStore
import com.pakhshmahdi.app.data.auth.ClaimOrderRequest
import com.pakhshmahdi.app.data.cart.CartLine
import com.pakhshmahdi.app.data.cart.MIN_WHOLESALE_QUANTITY
import com.pakhshmahdi.app.data.local.LocalStore
import com.pakhshmahdi.app.data.remote.ApiClient
import retrofit2.Response

class StoreApiException(message: String) : Exception(message)

class CheckoutRepository {
    private val api = StoreApiClient.api
    private val gson = Gson()
    private var preparedLines: List<CartLine> = emptyList()
    private var lastCalculatedCart: StoreCart? = null
    private var lastFallbackRateId: String? = null

    suspend fun refreshLines(lines: List<CartLine>): List<CartLine> {
        if (lines.isEmpty()) throw StoreApiException("سبد خرید خالی است.")

        return lines.map { line ->
            val freshProductRaw = runCatching {
                ApiClient.api.product(line.product.id)
            }.getOrElse {
                throw StoreApiException("اطلاعات «${line.product.name}» به‌روزرسانی نشد. دوباره تلاش کنید.")
            }

            val freshProduct = if (freshProductRaw.variations.isNotEmpty()) {
                enrichVariableProduct(freshProductRaw)
            } else {
                freshProductRaw
            }

            if (!freshProduct.purchasable || !freshProduct.isInStock) {
                throw StoreApiException("«${freshProduct.name}» در حال حاضر قابل سفارش نیست.")
            }

            val freshVariation = line.variation?.let { previous ->
                val fresh = freshProduct.variations.firstOrNull { it.id == previous.id }
                    ?: throw StoreApiException("گزینه انتخاب‌شده «${freshProduct.name}» دیگر موجود نیست.")

                fresh.copy(
                    attributes = if (fresh.attributes.isNotEmpty()) fresh.attributes else previous.attributes,
                    selection = if (fresh.selection.isNotEmpty()) fresh.selection else previous.selection
                )
            }

            if (freshVariation != null && freshVariation.stockStatus != "instock") {
                throw StoreApiException("گزینه انتخاب‌شده «${freshProduct.name}» ناموجود شده است.")
            }

            val maxStock = freshVariation?.stockQuantity
                ?: freshProduct.stockQuantity.takeIf { freshVariation == null }
            if (maxStock != null && maxStock <= 0) {
                throw StoreApiException("«${freshProduct.name}» ناموجود شده است.")
            }

            val wholesaleQuantity = line.quantity.coerceAtLeast(MIN_WHOLESALE_QUANTITY)
            line.copy(
                product = freshProduct,
                variation = freshVariation,
                quantity = maxStock?.let { wholesaleQuantity.coerceAtMost(it) } ?: wholesaleQuantity
            )
        }
    }

    suspend fun prepareCart(lines: List<CartLine>): StoreCart {
        if (lines.isEmpty()) throw StoreApiException("سبد خرید خالی است.")
        preparedLines = lines.toList()

        val token = ensureToken()

        // Only clear when the server cart actually contains items. Some WooCommerce
        // installations reject DELETE /cart/items for an already-empty cart.
        val existingCart = api.cart(token)
        if (existingCart.isSuccessful && (existingCart.body()?.itemsCount ?: 0) > 0) {
            val clearResponse = api.clearCart(token)
            if (!clearResponse.isSuccessful) {
                throw StoreApiException(errorMessage(clearResponse, "پاک‌سازی سبد سرور انجام نشد."))
            }
        }

        var cart: StoreCart? = null
        lines.forEach { line ->
            val selectedVariation = line.variation
            val variationPayload = selectedVariation
                ?.let { VariationResolver.selectionForCart(line.product, it) }
                .orEmpty()
                .map { item ->
                    StoreVariationAttribute(
                        attribute = item.attribute,
                        value = item.value
                    )
                }

            if (selectedVariation != null && variationPayload.isEmpty()) {
                throw StoreApiException(
                    "اطلاعات متغیر انتخاب‌شده برای «${line.product.name}» کامل نیست. گزینه محصول را دوباره انتخاب کنید."
                )
            }

            cart = requireBody(
                api.addItem(
                    token,
                    AddItemRequest(
                        id = selectedVariation?.id ?: line.product.id,
                        quantity = line.quantity,
                        variation = variationPayload
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
        val normalizedAddress = address.normalizedForWooCommerce()
        var token = ensureToken()
        val payload = UpdateCustomerRequest(
            billingAddress = normalizedAddress,
            shippingAddress = normalizedAddress.toShippingAddress()
        )

        var response = api.updateCustomer(token, payload)

        // Checkout step 1 can fail when WooCommerce expires the Store API cart
        // session between opening checkout and submitting the address. Rebuild
        // the server cart once from the local basket and retry without changing
        // the stable 0.10.3 API endpoints.
        if ((!response.isSuccessful || response.body()?.itemsCount == 0) && preparedLines.isNotEmpty()) {
            LocalStore.saveStoreCartToken(null)
            prepareCart(preparedLines)
            token = ensureToken()
            response = api.updateCustomer(token, payload)
        }

        var cart = requireBody(
            response,
            "محاسبه آدرس و روش ارسال انجام نشد."
        )

        if (cart.itemsCount <= 0) {
            throw StoreApiException("سبد خرید سرور همگام نشد. دوباره تلاش کنید.")
        }

        // Some legacy Persian shipping methods calculate after the customer update
        // request has finished. Refresh once before using the app-specific fallback.
        if (cart.needsShipping && !cart.hasShippingRates()) {
            val refreshed = api.cart(token)
            if (refreshed.isSuccessful && refreshed.body() != null) {
                cart = refreshed.body()!!
            }
        }

        // If WooCommerce does not expose a shipping rate in Store API, use the
        // site's own shipping calculator. Some Persian shipping plugins calculate
        // correctly but do not serialize their rate back into Store API JSON.
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
                        state = normalizedAddress.state,
                        city = normalizedAddress.city,
                        postcode = normalizedAddress.postcode
                    )
                )
            }.getOrElse {
                throw StoreApiException(
                    "هزینه ارسال برای شهر انتخاب‌شده محاسبه نشد. شهر را دوباره انتخاب کنید."
                )
            }

            lastFallbackRateId = quote.rateId

            val selectedResponse = runCatching {
                api.selectShippingRate(
                    token,
                    SelectShippingRateRequest(
                        packageId = 0,
                        rateId = quote.rateId
                    )
                )
            }.getOrNull()

            val selectedCart = selectedResponse
                ?.takeIf { it.isSuccessful }
                ?.body()

            if (selectedCart != null) {
                cart = selectedCart
            }

            // The quote itself is authoritative for the app. If Store API still
            // omits the rate, expose the calculated rate locally instead of
            // incorrectly blocking checkout with an address/postcode error.
            if (!cart.hasShippingRates()) {
                cart = cart.withFallbackShippingQuote(quote)
            }
        }

        if (cart.needsShipping && !cart.hasShippingRates()) {
            throw StoreApiException(
                "برای شهر انتخاب‌شده روش ارسال پیدا نشد. شهر را دوباره انتخاب کنید."
            )
        }

        lastCalculatedCart = cart
        LocalStore.saveCheckoutAddress(normalizedAddress)
        return cart
    }

    suspend fun selectShipping(packageId: Int, rateId: String): StoreCart {
        val token = ensureToken()
        val response = runCatching {
            api.selectShippingRate(
                token,
                SelectShippingRateRequest(packageId = packageId, rateId = rateId)
            )
        }.getOrNull()

        val serverCart = response
            ?.takeIf { it.isSuccessful }
            ?.body()

        if (serverCart != null && serverCart.hasShippingRates()) {
            lastCalculatedCart = serverCart
            return serverCart
        }

        if (rateId == lastFallbackRateId) {
            return lastCalculatedCart
                ?: throw StoreApiException("روش ارسال محاسبه‌شده در دسترس نیست. دوباره محاسبه کنید.")
        }

        if (response != null) {
            return requireBody(response, "انتخاب روش ارسال انجام نشد.")
        }

        throw StoreApiException("انتخاب روش ارسال انجام نشد.")
    }

    suspend fun submitOrder(
        address: StoreAddress,
        paymentMethod: String,
        expectedTotal: String,
        customerNote: String
    ): CheckoutResponse {
        val normalizedAddress = address.normalizedForWooCommerce()
        val token = ensureToken()
        val result = requireBody(
            api.checkout(
                token,
                CheckoutRequest(
                    billingAddress = normalizedAddress,
                    shippingAddress = normalizedAddress.toShippingAddress(),
                    paymentMethod = paymentMethod,
                    expectedTotal = expectedTotal,
                    customerNote = customerNote
                )
            ),
            "ثبت سفارش انجام نشد."
        )

        if (result.orderId > 0) {
            LocalStore.saveCheckoutAddress(normalizedAddress)
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

    private suspend fun enrichVariableProduct(
        product: com.pakhshmahdi.app.data.model.Product
    ): com.pakhshmahdi.app.data.model.Product {
        val storeVariations = runCatching {
            val response = api.productVariations(parent = product.id)
            if (response.isSuccessful) response.body().orEmpty() else emptyList()
        }.getOrDefault(emptyList())

        val neededTaxonomies = product.attributes
            .filter { it.slug.startsWith("pa_") && it.options.isNotEmpty() }
            .map { it.slug }
            .distinct()

        val termSlugs = if (neededTaxonomies.isNotEmpty()) {
            val taxonomies = runCatching {
                val response = api.productAttributeTaxonomies()
                if (response.isSuccessful) response.body().orEmpty() else emptyList()
            }.getOrDefault(emptyList())

            neededTaxonomies.associateWith { taxonomySlug ->
                val taxonomy = taxonomies.firstOrNull { it.taxonomy == taxonomySlug }
                if (taxonomy == null) {
                    emptyMap()
                } else {
                    runCatching {
                        val response = api.productAttributeTerms(taxonomy.id)
                        if (response.isSuccessful) {
                            response.body().orEmpty().associate { it.name to it.slug }
                        } else {
                            emptyMap()
                        }
                    }.getOrDefault(emptyMap())
                }
            }
        } else {
            emptyMap()
        }

        return VariationResolver.enrich(
            product = product,
            storeVariations = storeVariations,
            termSlugs = termSlugs
        )
    }

    private fun StoreCart.withFallbackShippingQuote(
        quote: ShippingQuoteResponse
    ): StoreCart {
        val previousShipping = moneyToLong(totals.totalShipping) ?: 0L
        val quotedShipping = moneyToLong(quote.price) ?: 0L
        val currentTotal = moneyToLong(totals.totalPrice)

        val adjustedTotal = currentTotal
            ?.let { total ->
                (total - previousShipping + quotedShipping)
                    .coerceAtLeast(0L)
                    .toString()
            }
            ?: totals.totalPrice

        return copy(
            shippingRates = listOf(
                StoreShippingPackage(
                    packageId = 0,
                    name = "ارسال",
                    shippingRates = listOf(
                        StoreShippingRate(
                            rateId = quote.rateId,
                            name = quote.name.ifBlank { "ارسال" },
                            price = quote.price,
                            selected = true
                        )
                    )
                )
            ),
            totals = totals.copy(
                totalShipping = quote.price,
                totalPrice = adjustedTotal
            )
        )
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
