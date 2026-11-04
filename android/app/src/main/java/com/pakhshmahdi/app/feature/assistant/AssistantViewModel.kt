package com.pakhshmahdi.app.feature.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pakhshmahdi.app.data.assistant.AssistantBubble
import com.pakhshmahdi.app.data.assistant.AssistantInputPolicy
import com.pakhshmahdi.app.data.assistant.AssistantRepository
import com.pakhshmahdi.app.data.assistant.AssistantRequest
import com.pakhshmahdi.app.data.assistant.AssistantTurn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AssistantUiState(
    val draft: String = "",
    val busy: Boolean = false,
    val checking: Boolean = false,
    val available: Boolean = false,
    val signedIn: Boolean = false,
    val notice: String? = null,
    val bubbles: List<AssistantBubble> = emptyList()
)

class AssistantViewModel : ViewModel() {
    private val repository = AssistantRepository()
    private val mutable = MutableStateFlow(AssistantUiState())
    val state: StateFlow<AssistantUiState> = mutable.asStateFlow()
    private var sessionToken: String? = null
    private var conversationId = UUID.randomUUID().toString()

    /** Wipe conversation immediately on user-switch / sign-out. */
    fun bindSession(token: String?) {
        val clean = token?.takeIf { it.isNotBlank() }
        if (clean == sessionToken) return
        sessionToken = clean
        conversationId = UUID.randomUUID().toString()
        mutable.value = AssistantUiState(
            signedIn = clean != null, checking = clean != null,
            notice = if (clean == null) "برای گفت‌وگو با دستیار فروش وارد حساب کاربری شوید." else null
        )
        if (clean == null) return
        viewModelScope.launch {
            try {
                val ready = repository.status(clean).enabled
                if (sessionToken != clean) return@launch
                mutable.update {
                    it.copy(
                        available = ready, checking = false,
                        notice = if (ready) null
                            else "دستیار هوشمند هنوز روی سایت فعال نشده است."
                    )
                }
            } catch (error: Exception) {
                if (sessionToken != clean) return@launch
                mutable.update {
                    it.copy(checking = false, available = false,
                        notice = repository.humanError(error))
                }
            }
        }
    }

    fun editDraft(text: String) {
        if (text.length <= 500) mutable.update { it.copy(draft = text, notice = null) }
    }

    fun retry() {
        val current = sessionToken ?: return
        // Explicit retry, not a silent loop of paid model requests.
        sessionToken = null
        bindSession(current)
    }

    fun send() {
        val token = sessionToken ?: return
        val snapshot = mutable.value
        if (!snapshot.available || snapshot.busy) return
        val question = snapshot.draft.trim()
        val problem = AssistantInputPolicy.validate(question)
        if (problem != null) {
            mutable.update { it.copy(notice = problem) }
            return
        }
        val history = snapshot.bubbles.takeLast(6)
            .filterNot { AssistantInputPolicy.hasContact(it.text) }
            .map { bubble ->
            AssistantTurn(
                role = if (bubble.fromCustomer) "user" else "assistant",
                content = bubble.text.take(300)
            )
        }
        val customerBubble = AssistantBubble(fromCustomer = true, text = question)
        mutable.update {
            it.copy(draft = "", busy = true, notice = null,
                bubbles = it.bubbles + customerBubble)
        }
        viewModelScope.launch {
            try {
                val answer = repository.ask(
                    token, AssistantRequest(
                        question = question, conversationId = conversationId,
                        history = history, consent = true
                    )
                )
                if (token != sessionToken) return@launch
                mutable.update {
                    it.copy(busy = false,
                        bubbles = it.bubbles + AssistantBubble(
                            fromCustomer = false, text = answer
                        )
                    )
                }
            } catch (error: Exception) {
                if (token != sessionToken) return@launch
                // A failed read-only AI reply must not silently drop a
                // customer's question or create a duplicate chat bubble.
                // Retrying is always the customer's explicit decision.
                mutable.update {
                    it.copy(
                        busy = false,
                        draft = question,
                        notice = repository.humanError(error),
                        bubbles = it.bubbles.filterNot { bubble -> bubble.id == customerBubble.id }
                    )
                }
            }
        }
    }
}
