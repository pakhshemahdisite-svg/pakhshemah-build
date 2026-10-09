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
    fun allowsOrderOnlyWhenEveryLineHasSixAndTotalIsFifteenMillion() {
        val lines = listOf(
            line(1, "1500000", 6),
            line(2, "1000000", 6)
        )

        assertTrue(WholesaleOrderPolicy.isEligible(lines))
        assertNull(WholesaleOrderPolicy.message(lines))
    }

    @Test
    fun blocksHighValueOrderWhenAProductIsBelowSix() {
        val lines = listOf(
            line(1, "10000000", 2),
            line(2, "1000000", 6)
        )

        assertFalse(WholesaleOrderPolicy.isEligible(lines))
        assertTrue(WholesaleOrderPolicy.message(lines)?.contains("حداقل خرید هر کالا ۶ عدد") == true)
    }

    @Test
    fun blocksSixUnitLinesWhenSubtotalIsBelowFifteenMillion() {
        val lines = listOf(
            line(1, "500000", 6),
            line(2, "750000", 6)
        )

        assertFalse(WholesaleOrderPolicy.isEligible(lines))
        assertTrue(WholesaleOrderPolicy.message(lines)?.contains("۱۵ میلیون") == true)
    }

    @Test
    fun emptyCartIsNeverEligible() {
        assertFalse(WholesaleOrderPolicy.isEligible(emptyList()))
    }
}
