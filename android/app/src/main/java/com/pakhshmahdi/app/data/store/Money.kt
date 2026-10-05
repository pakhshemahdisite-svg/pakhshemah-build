package com.pakhshmahdi.app.data.store

import java.math.RoundingMode

internal fun moneyToLong(value: String?): Long? {
    val normalized = value
        ?.trim()
        ?.replace(",", "")
        ?.takeIf { it.isNotEmpty() }
        ?: return null

    val decimal = normalized.toBigDecimalOrNull() ?: return null
    return runCatching {
        decimal
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    }.getOrNull()
}

internal fun moneyLineTotal(unitPrice: String?, quantity: Int): Long {
    if (quantity <= 0) return 0L
    val unit = moneyToLong(unitPrice) ?: return 0L
    return runCatching {
        Math.multiplyExact(unit, quantity.toLong())
    }.getOrDefault(0L)
}
