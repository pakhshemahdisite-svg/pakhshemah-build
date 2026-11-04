package com.pakhshmahdi.app.data.assistant

import com.google.gson.annotations.SerializedName
import java.util.UUID

/** Client contract for the existing /iamir-app/v1 authentication boundary.
 * The server must derive role from the verified Bearer session, never from JSON.
 */
data class AssistantStatus(
    val enabled: Boolean = false,
    val mode: String = "unavailable"
) {
    /** Reject enabled=true from a legacy, misconfigured or manager endpoint. */
    fun isTrustedCustomerReady(): Boolean =
        enabled && mode == "server_verified_customer_only"
}

data class AssistantTurn(
    val role: String,
    val content: String
)

data class AssistantRequest(
    val question: String,
    @SerializedName("conversation_id") val conversationId: String,
    val history: List<AssistantTurn>,
    val consent: Boolean
)

data class AssistantReply(
    val answer: String
)

data class AssistantBubble(
    val id: String = UUID.randomUUID().toString(),
    val fromCustomer: Boolean,
    val text: String
)

/** Validation is repeated on the server. Never submit contact information or
 * personal order details to a model without a separate, authenticated workflow.
 */
object AssistantInputPolicy {
    private val email = Regex(
        """[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""",
        RegexOption.IGNORE_CASE
    )
    private val phone = Regex("""(?:\+?98|0)9[0-9]{9}(?![0-9])""")
    private val separators = Regex("""[\s()\-–]""")

    private fun normalizeDigits(text: String): String = buildString {
        for (char in text) {
            when (char) {
                in '۰'..'۹' -> append((char.code - '۰'.code).toString())
                in '٠'..'٩' -> append((char.code - '٠'.code).toString())
                else -> append(char)
            }
        }
    }

    fun hasContact(text: String): Boolean =
        email.containsMatchIn(text) ||
            phone.containsMatchIn(normalizeDigits(text).replace(separators, ""))

    fun validate(text: String): String? {
        val normalized = text.trim()
        if (normalized.length !in 2..500) return "پیام باید بین ۲ تا ۵۰۰ نویسه باشد."
        if (hasContact(normalized)) {
            return "شماره تماس یا ایمیل را در گفت‌وگو نفرستید. برای اطلاعات حساب از بخش پشتیبانی استفاده کنید."
        }
        return null
    }
}
