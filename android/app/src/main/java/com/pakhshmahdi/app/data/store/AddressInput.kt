package com.pakhshmahdi.app.data.store

/**
 * Normalizes Persian/Arabic-Indic digits to ASCII so WooCommerce postcode and
 * phone validation receives the same numeric format it expects server-side.
 */
fun normalizeIranianDigits(value: String): String = buildString(value.length) {
    value.forEach { ch ->
        append(
            when (ch) {
                '۰', '٠' -> '0'
                '۱', '١' -> '1'
                '۲', '٢' -> '2'
                '۳', '٣' -> '3'
                '۴', '٤' -> '4'
                '۵', '٥' -> '5'
                '۶', '٦' -> '6'
                '۷', '٧' -> '7'
                '۸', '٨' -> '8'
                '۹', '٩' -> '9'
                else -> ch
            }
        )
    }
}

fun normalizeNumericAddressField(value: String): String =
    normalizeIranianDigits(value).filter { it in '0'..'9' }
