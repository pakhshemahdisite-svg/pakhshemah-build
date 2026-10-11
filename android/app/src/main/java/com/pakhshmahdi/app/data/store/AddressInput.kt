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


fun StoreAddress.normalizedForWooCommerce(): StoreAddress {
    val normalizedPhone = normalizeNumericAddressField(phone)
    val normalizedPostcode = normalizeNumericAddressField(postcode)
    val normalizedCity = normalizeIranianDigits(city.trim())
    val normalizedState = normalizeIranianDigits(state.trim())

    return copy(
        firstName = firstName.trim(),
        lastName = lastName.trim(),
        company = company.trim(),
        address1 = address1.trim(),
        address2 = address2.trim(),
        city = normalizedCity,
        state = normalizedState,
        postcode = normalizedPostcode,
        country = country.trim().uppercase().ifBlank { "IR" },
        phone = normalizedPhone,
        email = email.trim().ifBlank {
            if (normalizedPhone.isNotBlank()) {
                "$normalizedPhone@mobile.pakhshemahdi.com"
            } else {
                ""
            }
        }
    )
}
