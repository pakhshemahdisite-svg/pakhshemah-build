package com.pakhshmahdi.app.data.cart

import com.pakhshmahdi.app.data.model.Product
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WholesaleOrderPolicyTest {

    private fun line(
        id: Long,
        unitPrice: String,
        quantity: Int
    ) = CartLine(
        product = Product(
            id = id,
            name = "محصول $id",
            price = unitPrice,
            purchasable = true,
            stockStatus = "instock"
        ),
        quantity = quantity
    )

    @Test
    fun allowsOrderWhenCartContainsAtLeastSixUnits() {
        val lines = listOf(
            line(1, "500000", 3),
            line(2, "300000", 3)
        )

        assertTrue(WholesaleOrderPolicy.isEligible(lines))
        assertNull(WholesaleOrderPolicy.message(lines))
    }

    @Test
    fun allowsOrderWhenSubtotalReachesFifteenMillion() {
        val lines = listOf(
            line(1, "6328000", 3)
        )

        assertTrue(WholesaleOrderPolicy.isEligible(lines))
    }

    @Test
    fun blocksOrderWhenNeitherConditionIsMet() {
        val lines = listOf(
            line(1, "508000", 2),
            line(2, "900000", 3)
        )

        assertFalse(WholesaleOrderPolicy.isEligible(lines))
        assertTrue(WholesaleOrderPolicy.message(lines)?.contains("1 عدد") == true)
    }

    @Test
    fun emptyCartIsNeverEligible() {
        assertFalse(WholesaleOrderPolicy.isEligible(emptyList()))
    }
}
