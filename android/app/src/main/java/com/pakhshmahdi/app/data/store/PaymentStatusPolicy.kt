package com.pakhshmahdi.app.data.store

enum class PaymentTerminalAction {
    KEEP_PENDING,
    COMPLETE_AND_CLEAR_CART,
    RELEASE_SESSION_KEEP_CART
}

object PaymentStatusPolicy {
    fun normalize(raw: String): String =
        raw.trim().lowercase().removePrefix("wc-")

    fun action(raw: String): PaymentTerminalAction =
        when (normalize(raw)) {
            "processing", "completed" -> PaymentTerminalAction.COMPLETE_AND_CLEAR_CART
            "failed", "cancelled", "refunded" -> PaymentTerminalAction.RELEASE_SESSION_KEEP_CART
            else -> PaymentTerminalAction.KEEP_PENDING
        }
}
