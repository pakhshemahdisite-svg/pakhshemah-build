package com.pakhshmahdi.app.data.assistant

import com.pakhshmahdi.app.data.remote.ApiClient
import retrofit2.HttpException

/** The mobile access token is sent to the store-owned mobile REST namespace,
 * not to a public HMAC bot bridge or directly to an AI provider.
 */
class AssistantRepository {
    private val api = ApiClient.api

    suspend fun status(token: String): AssistantStatus =
        api.assistantStatus("Bearer $token")

    suspend fun ask(token: String, request: AssistantRequest): String {
        val answer = api.assistantChat("Bearer $token", request).answer.trim()
        if (answer.isBlank() || answer.length > 3000) {
            throw AssistantUnavailable("پاسخ معتبر از دستیار دریافت نشد.")
        }
        return answer
    }

    fun humanError(error: Throwable): String = when (error) {
        is AssistantUnavailable -> error.message ?: "دستیار در دسترس نیست."
        is HttpException -> when (error.code()) {
            401, 403 -> "احراز هویت دستیار تأیید نشد. نشست حساب یا اتصال امنیتی سایت باید بررسی شود."
            404, 501, 503 -> "موتور هوشمند هنوز روی سایت فعال نشده است. کمی بعد دوباره تلاش کنید."
            408, 504 -> "پاسخ دستیار طول کشید. لطفاً دوباره تلاش کنید."
            429 -> "تعداد درخواست‌ها زیاد شده است. کمی بعد دوباره امتحان کنید."
            else -> "ارتباط با دستیار برقرار نشد. سفارش یا پرداختی انجام نشده است."
        }
        else -> "اتصال دستیار برقرار نشد. لطفاً اینترنت خود را بررسی کنید."
    }
}

class AssistantUnavailable(message: String) : Exception(message)
