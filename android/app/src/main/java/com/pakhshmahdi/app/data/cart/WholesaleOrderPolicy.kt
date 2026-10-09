package com.pakhshmahdi.app.data.cart

import com.pakhshmahdi.app.data.store.moneyLineTotal

object WholesaleOrderPolicy {
    const val MINIMUM_PER_PRODUCT = 6
    const val MINIMUM_ORDER_TOTAL = 15_000_000L

    fun subtotal(lines: List<CartLine>): Long =
        lines.sumOf { line -> moneyLineTotal(line.unitPrice, line.quantity) }

    fun totalQuantity(lines: List<CartLine>): Int =
        lines.sumOf { it.quantity.coerceAtLeast(0) }

    fun underMinimumLines(lines: List<CartLine>): List<CartLine> =
        lines.filter { it.quantity in 1 until MINIMUM_PER_PRODUCT }

    fun isEligible(lines: List<CartLine>): Boolean {
        if (lines.isEmpty()) return false
        return underMinimumLines(lines).isEmpty() && subtotal(lines) >= MINIMUM_ORDER_TOTAL
    }

    fun message(lines: List<CartLine>): String? {
        if (isEligible(lines)) return null

        val lowLines = underMinimumLines(lines)
        if (lowLines.isNotEmpty()) {
            val first = lowLines.first()
            val remaining = (MINIMUM_PER_PRODUCT - first.quantity).coerceAtLeast(0)
            val suffix = if (lowLines.size > 1) " و ${lowLines.size - 1} کالای دیگر" else ""
            return "حداقل خرید هر کالا ۶ عدد است. برای «${first.product.name}» $remaining عدد دیگر اضافه کنید$suffix."
        }

        val remainingAmount =
            (MINIMUM_ORDER_TOTAL - subtotal(lines)).coerceAtLeast(0L)

        return "حداقل مبلغ نهایی سفارش ۱۵ میلیون تومان است. " +
            "برای ادامه، ${formatToman(remainingAmount)} دیگر به سبد اضافه کنید."
    }

    private fun formatToman(value: Long): String =
        String.format("%,d تومان", value)
}
