package com.pakhshmahdi.app.data.store

import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressPayloadTest {
    private val gson = Gson()

    private val address = StoreAddress(
        firstName = "علی",
        lastName = "رضایی",
        company = "",
        address1 = "تهران، خیابان نمونه",
        address2 = "",
        city = "123",
        state = "558",
        postcode = "1234567890",
        country = "IR",
        phone = "09120000000",
        email = "09120000000@mobile.pakhshemahdi.com"
    )

    @Test
    fun updateCustomerPayload_omitsUnsupportedFields() {
        val json = gson.toJson(
            UpdateCustomerRequest(
                billingAddress = address.toBillingPayload(),
                shippingAddress = address.toShippingPayload()
            )
        )

        assertTrue(json.contains("\"billing_address\""))
        assertTrue(json.contains("\"shipping_address\""))
        assertTrue(json.contains("\"phone\":\"09120000000\""))
        assertTrue(json.contains("\"email\":\"09120000000@mobile.pakhshemahdi.com\""))
        assertFalse(json.contains("\"company\""))

        val shippingJson = gson.toJson(address.toShippingPayload())
        assertFalse(shippingJson.contains("\"phone\""))
        assertFalse(shippingJson.contains("\"email\""))
        assertFalse(shippingJson.contains("\"company\""))
    }
}
