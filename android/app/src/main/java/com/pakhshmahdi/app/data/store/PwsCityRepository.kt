package com.pakhshmahdi.app.data.store

import android.text.Html
import com.pakhshmahdi.app.core.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

data class PwsCity(
    val id: String,
    val name: String
)

class PwsCityRepository {
    private val http = OkHttpClient()

    suspend fun cities(stateId: String): List<PwsCity> = withContext(Dispatchers.IO) {
        if (stateId.isBlank()) return@withContext emptyList()

        val form = FormBody.Builder()
            .add("action", "mahdiy_load_cities")
            .add("state_id", stateId)
            .add("type", "billing")
            .build()

        val request = Request.Builder()
            .url(AppConfig.PWS_AJAX_URL)
            .post(form)
            .build()

        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw StoreApiException("دریافت فهرست شهرها انجام نشد.")
            }

            parseOptions(response.body?.string().orEmpty())
        }
    }

    private fun parseOptions(html: String): List<PwsCity> {
        val optionRegex = Regex(
            """<option\s+[^>]*value=['"](\d+)['"][^>]*>(.*?)</option>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )

        return optionRegex.findAll(html)
            .mapNotNull { match ->
                val id = match.groupValues[1]
                if (id == "0") return@mapNotNull null

                val name = Html.fromHtml(
                    match.groupValues[2],
                    Html.FROM_HTML_MODE_LEGACY
                ).toString().trim()

                if (name.isBlank()) null else PwsCity(id = id, name = name)
            }
            .distinctBy { it.id }
            .toList()
    }
}
