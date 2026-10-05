package com.pakhshmahdi.app.feature.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class OrderStatusTest {

    @Test
    fun normalizesWooCommercePrefix() {
        assertEquals("processing", normalizedOrderStatus("wc-processing"))
        assertEquals("shipped", normalizedOrderStatus(" WC-SHIPPED "))
    }

    @Test
    fun mapsCoreOrderProgression() {
        assertEquals(0, statusStep("pending"))
        assertEquals(1, statusStep("processing"))
        assertEquals(2, statusStep("shipped"))
        assertEquals(3, statusStep("completed"))
    }

    @Test
    fun mapsShippingAliases() {
        assertEquals(2, statusStep("out-for-delivery"))
        assertEquals(2, statusStep("out_for_delivery"))
        assertEquals(3, statusStep("delivered"))
    }

    @Test
    fun producesPersianStatusTitles() {
        assertEquals("سفارش ارسال شده است", statusTitle("wc-shipped"))
        assertEquals("سفارش تحویل شده است", statusTitle("completed"))
        assertEquals("پرداخت یا سفارش ناموفق بوده است", statusTitle("failed"))
    }
}
