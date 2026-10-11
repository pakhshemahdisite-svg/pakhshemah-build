package com.pakhshmahdi.app.data.store

import org.junit.Assert.assertEquals
import org.junit.Test

class AddressInputTest {

    @Test
    fun normalizesPersianPostcodeToAscii() {
        assertEquals(
            "1876453421",
            normalizeNumericAddressField("۱۸۷۶۴۵۳۴۲۱")
        )
    }

    @Test
    fun normalizesArabicIndicPhoneToAscii() {
        assertEquals(
            "09121234567",
            normalizeNumericAddressField("٠٩١٢١٢٣٤٥٦٧")
        )
    }

    @Test
    fun removesFormattingCharactersFromNumericFields() {
        assertEquals(
            "09121234567",
            normalizeNumericAddressField("۰۹۱۲-۱۲۳ ۴۵۶۷")
        )
    }
}
