package com.pakhshmahdi.app.data.assistant

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class AssistantContractTest {
    @Test fun rejectsPersonalContactBeforeProviderCall() {
        assertNull(AssistantInputPolicy.validate("ظروف نگهدارنده چه رنگی موجود دارید؟"))
        assertNotNull(AssistantInputPolicy.validate("با 09123456789 تماس بگیرید"))
        assertNotNull(AssistantInputPolicy.validate("این ایمیل من است a@example.com"))
        assertNotNull(AssistantInputPolicy.validate(" "))
        assertNotNull(AssistantInputPolicy.validate("x".repeat(501)))
    }

    @Test fun apiContractNeverCarriesRoleOrSharedSecrets() {
        val request = AssistantRequest(
            question = "چند مدل ظرف پلاستیکی موجود دارید؟",
            conversationId = "123e4567-e89b-42d3-a456-426614174000",
            history = listOf(AssistantTurn("user", "قیمت ظرف؟"))
        )
        val json = Gson().toJson(request)
        assertTrue(json.contains("\"conversation_id\""))
        assertTrue(json.contains("\"history\""))
        assertFalse(json.contains("\"role\":\"manager\""))
        assertFalse(json.contains("api_key"))
        assertFalse(json.contains("secret"))
        assertFalse(json.contains("access_token"))
    }
}
