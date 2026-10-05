package com.pakhshmahdi.app.data.auth

import com.pakhshmahdi.app.core.AppConfig

data class OtpRequest(
    val phone: String
)

data class OtpVerifyRequest(
    val phone: String,
    val code: String
)

data class OtpRequestResponse(
    val success: Boolean = false,
    val expiresIn: Int = 300,
    val resendAfter: Int = 60
)

data class BillingProfile(
    val firstName: String = "",
    val lastName: String = "",
    val address1: String = "",
    val address2: String = "",
    val city: String = "",
    val state: String = "",
    val postcode: String = "",
    val country: String = "IR",
    val phone: String = "",
    val email: String = ""
)

data class UpdateProfileRequest(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = ""
)

data class BillingResponse(
    val billing: BillingProfile
)

data class AuthUser(
    val id: Long,
    val phone: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val displayName: String = "",
    val billing: BillingProfile = BillingProfile()
) {
    val fullName: String
        get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
            .ifBlank { displayName.ifBlank { "کاربر ${AppConfig.APP_NAME}" } }
}

data class AuthResponse(
    val token: String,
    val user: AuthUser
)

data class UserResponse(
    val user: AuthUser
)

data class OrderSummary(
    val id: Long,
    val status: String = "",
    val date: String = "",
    val total: String = "0",
    val currency: String = "IRT",
    val itemCount: Int = 0,
    val paymentMethod: String = ""
)

data class OrdersPayload(
    val items: List<OrderSummary> = emptyList()
)

data class OrderLineItem(
    val id: Long,
    val productId: Long = 0,
    val variationId: Long = 0,
    val name: String = "",
    val quantity: Int = 0,
    val total: String = "0",
    val image: String? = null
)

data class OrderDetail(
    val id: Long,
    val status: String = "",
    val date: String = "",
    val total: String = "0",
    val shippingTotal: String = "0",
    val discountTotal: String = "0",
    val paymentMethod: String = "",
    val shippingMethod: String = "",
    val items: List<OrderLineItem> = emptyList()
)

data class ClaimOrderRequest(
    val orderId: Long,
    val orderKey: String
)

data class ClaimOrderResponse(
    val success: Boolean = false,
    val orderId: Long = 0
)

data class AuthApiError(
    val code: String = "",
    val message: String = ""
)
