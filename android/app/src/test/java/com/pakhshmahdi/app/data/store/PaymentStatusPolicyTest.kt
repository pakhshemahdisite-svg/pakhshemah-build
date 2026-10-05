package com.pakhshmahdi.app.data.store

import org.junit.Assert.assertEquals
import org.junit.Test

class PaymentStatusPolicyTest {

    @Test
    fun normalizesWooStatusPrefixesAndCase() {
        assertEquals("processing", PaymentStatusPolicy.normalize(" WC-PROCESSING ".trim()))
        assertEquals("completed", PaymentStatusPolicy.normalize("wc-completed"))
    }

    @Test
    fun paidStatusesClearCart() {
        assertEquals(
            PaymentTerminalAction.COMPLETE_AND_CLEAR_CART,
            PaymentStatusPolicy.action("wc-processing")
        )
        assertEquals(
            PaymentTerminalAction.COMPLETE_AND_CLEAR_CART,
            PaymentStatusPolicy.action("completed")
        )
    }

    @Test
    fun terminalFailureKeepsLocalCartButReleasesSession() {
        listOf("failed", "cancelled", "refunded").forEach { status ->
            assertEquals(
                PaymentTerminalAction.RELEASE_SESSION_KEEP_CART,
                PaymentStatusPolicy.action(status)
            )
        }
    }

    @Test
    fun nonTerminalStatusesRemainPending() {
        listOf("pending", "on-hold", "checkout-draft", "").forEach { status ->
            assertEquals(
                PaymentTerminalAction.KEEP_PENDING,
                PaymentStatusPolicy.action(status)
            )
        }
    }
}
