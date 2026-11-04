package com.pakhshmahdi.app.data.assistant

import com.google.gson.annotations.SerializedName
import java.util.UUID

/** Client contract for the existing /iamir-app/v1 authentication boundary.
 * The server must derive role from the verified Bearer session, never from JSON.
 */
data class AssistantStatus(
    val enabled: Boolean = false,
    val mode: String = "unavailable"
)

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
    private val contact = Regex(
        "(?:[+]?98|0)9[\\p{Nd}]{9}|[\\w.%+-]+@[\\w.-]+[.][A-Za-z]{2,}",
        RegexOption.IGNORE_CASE
    )
    fun validate(text: String): String? {
        val normalized = text.trim()
        if (normalized.length !in 2..500) return "پیام باید بین ۲ تا ۵۰۰ نویسه باشد."
        if (contact.containsMatchIn(normalized)) {
            return "شماره تماس یا ایمیل را در گفت‌وگو نفرستید. برای اطلاعات حساب از بخش پشتیبانی استفاده کنید."
        }
        return null
    }
}
