package com.pakhshmahdi.app.data.store

import com.google.gson.annotations.SerializedName

data class StoreTotals(
    @SerializedName("total_items") val totalItems: String = "0",
    @SerializedName("total_discount") val totalDiscount: String = "0",
    @SerializedName("total_shipping") val totalShipping: String? = null,
    @SerializedName("total_price") val totalPrice: String = "0",
    @SerializedName("currency_symbol") val currencySymbol: String = "تومان"
)

data class StoreShippingRate(
    @SerializedName("rate_id") val rateId: String,
    val name: String = "",
    val price: String = "0",
    val selected: Boolean = false
)

data class StoreShippingPackage(
    @SerializedName("package_id") val packageId: Int = 0,
    val name: String = "",
    @SerializedName("shipping_rates") val shippingRates: List<StoreShippingRate> = emptyList()
)

data class StoreApiError(
    val code: String = "",
    val message: String = ""
)

data class StoreCoupon(
    val code: String = "",
    @SerializedName("discount_type") val discountType: String = ""
)

data class StoreCart(
    @SerializedName("items_count") val itemsCount: Int = 0,
    val coupons: List<StoreCoupon> = emptyList(),
    val totals: StoreTotals = StoreTotals(),
    @SerializedName("shipping_rates") val shippingRates: List<StoreShippingPackage> = emptyList(),
    @SerializedName("payment_methods") val paymentMethods: List<String> = emptyList(),
    @SerializedName("needs_payment") val needsPayment: Boolean = false,
    @SerializedName("needs_shipping") val needsShipping: Boolean = false,
    val errors: List<StoreApiError> = emptyList()
)

data class StoreAddress(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val company: String = "",
    @SerializedName("address_1") val address1: String,
    @SerializedName("address_2") val address2: String = "",
    val city: String,
    val state: String,
    val postcode: String,
    val country: String = "IR",
    val phone: String,
    val email: String = ""
)

data class StoreShippingAddress(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val company: String = "",
    @SerializedName("address_1") val address1: String,
    @SerializedName("address_2") val address2: String = "",
    val city: String,
    val state: String,
    val postcode: String,
    val country: String = "IR",
    val phone: String
)

fun StoreAddress.toShippingAddress() = StoreShippingAddress(
    firstName = firstName,
    lastName = lastName,
    company = company,
    address1 = address1,
    address2 = address2,
    city = city,
    state = state,
    postcode = postcode,
    country = country,
    phone = phone
)

data class StoreBillingPayload(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("address_1") val address1: String,
    @SerializedName("address_2") val address2: String = "",
    val city: String,
    val state: String,
    val postcode: String,
    val country: String = "IR",
    val email: String = "",
    val phone: String
)

data class StoreShippingPayload(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("address_1") val address1: String,
    @SerializedName("address_2") val address2: String = "",
    val city: String,
    val state: String,
    val postcode: String,
    val country: String = "IR"
)

fun StoreAddress.toBillingPayload() = StoreBillingPayload(
    firstName = firstName,
    lastName = lastName,
    address1 = address1,
    address2 = address2,
    city = city,
    state = state,
    postcode = postcode,
    country = country,
    email = email,
    phone = phone
)

fun StoreAddress.toShippingPayload() = StoreShippingPayload(
    firstName = firstName,
    lastName = lastName,
    address1 = address1,
    address2 = address2,
    city = city,
    state = state,
    postcode = postcode,
    country = country
)

data class UpdateCustomerRequest(
    @SerializedName("billing_address") val billingAddress: StoreBillingPayload,
    @SerializedName("shipping_address") val shippingAddress: StoreShippingPayload
)

data class AddItemRequest(
    val id: Long,
    val quantity: Int
)

data class CouponRequest(
    val code: String
)

data class SelectShippingRateRequest(
    @SerializedName("package_id") val packageId: Int,
    @SerializedName("rate_id") val rateId: String
)

data class ShippingQuoteLine(
    val id: Long,
    val quantity: Int
)

data class ShippingQuoteRequest(
    val lines: List<ShippingQuoteLine>,
    val state: String,
    val city: String,
    val postcode: String
)

data class ShippingQuoteResponse(
    val rateId: String = "pm_app_shipping_fallback",
    val name: String = "ارسال",
    val price: String = "0",
    val totalWeight: Int = 0,
    val source: String = ""
)

data class CheckoutRequest(
    @SerializedName("billing_address") val billingAddress: StoreBillingPayload,
    @SerializedName("shipping_address") val shippingAddress: StoreShippingPayload,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("payment_data") val paymentData: List<PaymentDataItem> = emptyList(),
    @SerializedName("expected_total") val expectedTotal: String,
    @SerializedName("customer_note") val customerNote: String = "",
    @SerializedName("create_account") val createAccount: Boolean = false
)

data class PaymentDataItem(
    val key: String,
    val value: String
)

data class PaymentResult(
    @SerializedName("payment_status") val paymentStatus: String? = null,
    @SerializedName("redirect_url") val redirectUrl: String? = null
)

data class CheckoutResponse(
    @SerializedName("order_id") val orderId: Long = 0,
    val status: String = "",
    @SerializedName("order_key") val orderKey: String = "",
    @SerializedName("payment_result") val paymentResult: PaymentResult? = null
)

data class PendingPayment(
    val orderId: Long,
    val paymentUrl: String? = null,
    val status: String = "pending"
)

data class IranProvince(val code: String, val name: String)

val iranProvinces = listOf(
    IranProvince("351", "آذربایجان شرقی"),
    IranProvince("1081", "چهار محال بختیاری"),
    IranProvince("300", "آذربایجان غربی"),
    IranProvince("978", "اردبیل"),
    IranProvince("423", "اصفهان"),
    IranProvince("538", "البرز"),
    IranProvince("1013", "ایلام"),
    IranProvince("1041", "بوشهر"),
    IranProvince("558", "تهران"),
    IranProvince("1150", "خراسان جنوبی"),
    IranProvince("609", "خراسان رضوی"),
    IranProvince("1125", "خراسان شمالی"),
    IranProvince("687", "خوزستان"),
    IranProvince("956", "زنجان"),
    IranProvince("1182", "سمنان"),
    IranProvince("1203", "سیستان و بلوچستان"),
    IranProvince("775", "فارس"),
    IranProvince("1254", "قزوین"),
    IranProvince("1281", "قم"),
    IranProvince("1288", "کردستان"),
    IranProvince("1321", "کرمان"),
    IranProvince("1399", "کرمانشاه"),
    IranProvince("1435", "کهگیویه و بویراحمد"),
    IranProvince("1456", "گلستان"),
    IranProvince("1491", "گیلان"),
    IranProvince("1547", "لرستان"),
    IranProvince("892", "مازندران"),
    IranProvince("1579", "مرکزی"),
    IranProvince("1618", "هرمزگان"),
    IranProvince("1665", "همدان"),
    IranProvince("1700", "یزد")
)
