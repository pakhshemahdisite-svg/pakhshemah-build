package com.pakhshmahdi.app.data.store

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {

    @Test
    fun parsesWholeTomanAmountsExactly() {
        assertEquals(15_000_000L, moneyToLong("15000000"))
        assertEquals(15_000_000L, moneyToLong("15,000,000"))
    }

    @Test
    fun roundsFractionalValuesHalfUp() {
        assertEquals(101L, moneyToLong("100.5"))
        assertEquals(100L, moneyToLong("100.49"))
    }

    @Test
    fun rejectsInvalidOrOverflowingValues() {
        assertNull(moneyToLong(""))
        assertNull(moneyToLong("not-a-price"))
        assertNull(moneyToLong("999999999999999999999999999"))
    }

    @Test
    fun multipliesLineTotalsWithoutFloatingPoint() {
        assertEquals(18_984_000L, moneyLineTotal("6328000", 3))
    }

    @Test
    fun returnsZeroForInvalidQuantityOrOverflow() {
        assertEquals(0L, moneyLineTotal("1000", 0))
        assertEquals(0L, moneyLineTotal(Long.MAX_VALUE.toString(), 2))
    }
}
