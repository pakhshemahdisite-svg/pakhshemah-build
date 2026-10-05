package com.pakhshmahdi.app.data.store

import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface WooStoreApi {
    @GET("cart")
    suspend fun cart(
        @Header("Cart-Token") cartToken: String? = null
    ): Response<StoreCart>

    // WooCommerce Store API returns a JSON array ([]) when all cart items are deleted.
    // Do not deserialize this endpoint as StoreCart.
    @DELETE("cart/items")
    suspend fun clearCart(
        @Header("Cart-Token") cartToken: String
    ): Response<JsonElement>

    @POST("cart/add-item")
    suspend fun addItem(
        @Header("Cart-Token") cartToken: String,
        @Body request: AddItemRequest
    ): Response<StoreCart>

    @POST("cart/apply-coupon")
    suspend fun applyCoupon(
        @Header("Cart-Token") cartToken: String,
        @Body request: CouponRequest
    ): Response<StoreCart>

    @POST("cart/remove-coupon")
    suspend fun removeCoupon(
        @Header("Cart-Token") cartToken: String,
        @Body request: CouponRequest
    ): Response<StoreCart>

    @POST("cart/update-customer")
    suspend fun updateCustomer(
        @Header("Cart-Token") cartToken: String,
        @Body request: UpdateCustomerRequest
    ): Response<StoreCart>

    @POST("cart/select-shipping-rate")
    suspend fun selectShippingRate(
        @Header("Cart-Token") cartToken: String,
        @Body request: SelectShippingRateRequest
    ): Response<StoreCart>

    @POST("checkout")
    suspend fun checkout(
        @Header("Cart-Token") cartToken: String,
        @Body request: CheckoutRequest
    ): Response<CheckoutResponse>
}
