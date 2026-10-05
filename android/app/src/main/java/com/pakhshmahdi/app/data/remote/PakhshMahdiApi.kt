package com.pakhshmahdi.app.data.remote

import com.pakhshmahdi.app.data.auth.AuthResponse
import com.pakhshmahdi.app.data.auth.BillingProfile
import com.pakhshmahdi.app.data.auth.BillingResponse
import com.pakhshmahdi.app.data.auth.ClaimOrderRequest
import com.pakhshmahdi.app.data.auth.ClaimOrderResponse
import com.pakhshmahdi.app.data.auth.OrderDetail
import com.pakhshmahdi.app.data.auth.OrdersPayload
import com.pakhshmahdi.app.data.auth.OtpRequest
import com.pakhshmahdi.app.data.auth.OtpRequestResponse
import com.pakhshmahdi.app.data.auth.OtpVerifyRequest
import com.pakhshmahdi.app.data.auth.UpdateProfileRequest
import com.pakhshmahdi.app.data.auth.UserResponse
import com.pakhshmahdi.app.data.model.CategoriesPayload
import com.pakhshmahdi.app.data.model.HomePayload
import com.pakhshmahdi.app.data.model.Product
import com.pakhshmahdi.app.data.model.ProductsPayload
import com.pakhshmahdi.app.data.store.ShippingQuoteRequest
import com.pakhshmahdi.app.data.store.ShippingQuoteResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PakhshMahdiApi {
    @POST("auth/request-otp")
    suspend fun requestOtp(@Body request: OtpRequest): OtpRequestResponse

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): AuthResponse

    @GET("auth/me")
    suspend fun me(@Header("Authorization") authorization: String): UserResponse

    @POST("auth/me")
    suspend fun updateMe(
        @Header("Authorization") authorization: String,
        @Body request: UpdateProfileRequest
    ): UserResponse

    @POST("auth/address")
    suspend fun updateAddress(
        @Header("Authorization") authorization: String,
        @Body request: BillingProfile
    ): BillingResponse

    @GET("auth/orders")
    suspend fun orders(@Header("Authorization") authorization: String): OrdersPayload

    @GET("auth/orders/{id}")
    suspend fun orderDetail(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long
    ): OrderDetail

    @POST("auth/orders/claim")
    suspend fun claimOrder(
        @Header("Authorization") authorization: String,
        @Body request: ClaimOrderRequest
    ): ClaimOrderResponse

    @GET("health")
    suspend fun health(): Map<String, Any>

    @GET("home")
    suspend fun home(): HomePayload

    @GET("categories")
    suspend fun categories(): CategoriesPayload

    @GET("products")
    suspend fun products(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("category") category: Long? = null,
        @Query("search") search: String? = null,
        @Query("orderby") orderBy: String = "date",
        @Query("order") order: String = "DESC"
    ): ProductsPayload

    @GET("products/{id}")
    suspend fun product(@Path("id") id: Long): Product

    @POST("shipping-quote")
    suspend fun shippingQuote(@Body request: ShippingQuoteRequest): ShippingQuoteResponse
}
