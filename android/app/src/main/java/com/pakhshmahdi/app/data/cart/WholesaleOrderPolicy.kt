package com.pakhshmahdi.app.data.cart

import com.pakhshmahdi.app.data.store.moneyLineTotal

object WholesaleOrderPolicy {
    const val MINIMUM_TOTAL_QUANTITY = 6
    const val MINIMUM_ORDER_TOTAL = 15_000_000L

    fun subtotal(lines: List<CartLine>): Long =
        lines.sumOf { line -> moneyLineTotal(line.unitPrice, line.quantity) }

    fun totalQuantity(lines: List<CartLine>): Int =
        lines.sumOf { it.quantity.coerceAtLeast(0) }

    fun isEligible(lines: List<CartLine>): Boolean {
        if (lines.isEmpty()) return false

        val quantityEligible = totalQuantity(lines) >= MINIMUM_TOTAL_QUANTITY
        val totalEligible = subtotal(lines) >= MINIMUM_ORDER_TOTAL

        return quantityEligible || totalEligible
    }

    fun message(lines: List<CartLine>): String? {
        if (isEligible(lines)) return null

        val remainingQuantity =
            (MINIMUM_TOTAL_QUANTITY - totalQuantity(lines)).coerceAtLeast(0)
        val remainingAmount =
            (MINIMUM_ORDER_TOTAL - subtotal(lines)).coerceAtLeast(0L)

        return "حداقل خرید از پخش مهدی باید ۶ عدد کالا یا ۱۵ میلیون تومان باشد. " +
            "برای ادامه، $remainingQuantity عدد کالا اضافه کنید یا مبلغ سبد را " +
            "${formatToman(remainingAmount)} افزایش دهید."
    }

    private fun formatToman(value: Long): String =
        String.format("%,d تومان", value)
}
