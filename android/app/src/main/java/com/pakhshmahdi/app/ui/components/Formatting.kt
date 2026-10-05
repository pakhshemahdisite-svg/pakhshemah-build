package com.pakhshmahdi.app.ui.components

import com.pakhshmahdi.app.data.store.moneyToLong
import java.text.NumberFormat
import java.util.Locale

fun toman(value: String): String {
    val number = moneyToLong(value) ?: 0L
    return NumberFormat.getNumberInstance(Locale.US).format(number) + " تومان"
}
