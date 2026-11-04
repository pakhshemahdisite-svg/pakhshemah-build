package com.pakhshmahdi.app.feature.assistant

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pakhshmahdi.app.data.auth.AuthStore
import com.pakhshmahdi.app.data.assistant.AssistantBubble
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.theme.PMTheme
import java.util.Locale

/** Native, Persian RTL customer AI. No provider/API secrets, no web view,
 * and no synthetic assistant messages. A working /ai/status + /ai/chat
 * server must be enabled behind the mobile Bearer authentication boundary.
 */
@Composable
fun AssistantScreen(onBack: () -> Unit, onLogin: () -> Unit) {
    val c = PMTheme.colors
    val vm: AssistantViewModel = viewModel()
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current
    var consent by rememberSaveable { mutableStateOf(false) }
    var voiceError by remember { mutableStateOf<String?>(null) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val listState = rememberLazyListState()
    val recognizer = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val phrase = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (!phrase.isNullOrBlank()) vm.editDraft(phrase.take(500))
    }

    LaunchedEffect(AuthStore.token) {
        consent = false
        vm.bindSession(AuthStore.token)
    }
    LaunchedEffect(state.bubbles.size, state.busy) {
        if (state.bubbles.isNotEmpty()) {
            listState.animateScrollToItem(state.bubbles.size - 1)
        }
    }
    DisposableEffect(ctx) {
        val engine = TextToSpeech(ctx) { status ->
            if (status != TextToSpeech.SUCCESS) {
                voiceError = "خواندن صوتی فارسی روی این گوشی در دسترس نیست."
            }
        }
        tts = engine
        onDispose {
            engine.stop()
            engine.shutdown()
            tts = null
        }
    }

    Column(Modifier.fillMaxSize().background(c.background)) {
        PMScreenTopBar(
            title = "دستیار هوشمند پخش مهدی",
            subtitle = "مشاوره خرید عمده و محصولات موجود",
            onBack = onBack
        )
        Surface(
            color = c.surface,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, c.border),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = c.accentGold)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("فروشنده هوشمند", color = c.textPrimary, fontWeight = FontWeight.Bold)
                    Text(
                        if (state.available) "پاسخ‌گویی متنی بر اساس اطلاعات فروشگاه"
                        else if (state.checking) "بررسی اتصال دستیار…"
                        else "در انتظار اتصال امن موتور هوشمند",
                        style = MaterialTheme.typography.labelMedium, color = c.textSecondary
                    )
                }
            }
        }

        if (!state.signedIn) {
            Column(Modifier.fillMaxWidth().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text("برای دریافت مشاوره هوشمند وارد حساب شوید.", color = c.textPrimary)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onLogin) { Text("ورود به حساب") }
            }
        } else if (!state.available) {
            Column(Modifier.fillMaxWidth().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                if (state.checking) {
                    CircularProgressIndicator(color = c.accentGold)
                } else {
                    Text(
                        state.notice ?: "سرویس هوشمند در حال آماده‌سازی است.",
                        color = c.textSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = vm::retry) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("بررسی مجدد")
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            state = listState,
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.bubbles.isEmpty()) {
                item {
                    Text(
                        "می‌توانید درباره محصولات موجود، قیمت‌های عمده، تنوع رنگ و شرایط ارسال سؤال کنید. " +
                            "تغییر سفارش یا پرداخت فقط با تأیید خودتان انجام می‌شود.",
                        modifier = Modifier.padding(10.dp),
                        color = c.textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            items(state.bubbles, key = { it.id }) { bubble ->
                AssistantMessageBubble(bubble, onSpeak = {
                    val engine = tts
                    if (engine != null) {
                        val language = engine.setLanguage(Locale.forLanguageTag("fa-IR"))
                        if (language == TextToSpeech.LANG_MISSING_DATA ||
                            language == TextToSpeech.LANG_NOT_SUPPORTED) {
                            voiceError = "صدای فارسی روی گوشی نصب نیست."
                        } else {
                            engine.speak(bubble.text, TextToSpeech.QUEUE_FLUSH, null, bubble.id)
                        }
                    }
                })
            }
            if (state.busy) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(17.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("در حال دریافت پاسخ…", color = c.textSecondary)
                    }
                }
            }
        }

        if (!state.notice.isNullOrBlank() && state.signedIn && state.available) {
            Text(state.notice.orEmpty(), color = c.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                style = MaterialTheme.typography.bodySmall)
        }
        if (voiceError != null) {
            Text(voiceError.orEmpty(), color = c.error,
                modifier = Modifier.padding(horizontal = 16.dp))
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = consent, onCheckedChange = { consent = it })
                Text(
                    "موافقم متن سؤال برای پاسخ‌گویی به سرویس هوش مصنوعی منتقل شود. " +
                        "اطلاعات شخصی یا سفارش محرمانه ارسال نمی‌کنم.",
                    modifier = Modifier.weight(1f),
                    color = c.textSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Row(verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = state.draft,
                    onValueChange = vm::editDraft,
                    placeholder = { Text("مثلاً ظروف نگهدارنده موجود را معرفی کن") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 3,
                    enabled = state.signedIn && state.available && !state.busy,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (consent) vm.send()
                    })
                )
                IconButton(
                    enabled = consent && state.available && !state.busy,
                    onClick = {
                        voiceError = null
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "سؤال خود را بگویید")
                        }
                        try {
                            recognizer.launch(intent)
                        } catch (_: ActivityNotFoundException) {
                            voiceError = "تشخیص گفتار فارسی روی گوشی در دسترس نیست."
                        }
                    }
                ) {
                    Icon(Icons.Outlined.Mic, contentDescription = "تبدیل گفتار به متن",
                        tint = c.primary)
                }
                FilledIconButton(
                    enabled = consent && state.available && !state.busy &&
                        state.draft.trim().length >= 2,
                    onClick = vm::send
                ) {
                    Icon(Icons.Outlined.Send, contentDescription = "ارسال سؤال")
                }
            }
        }
    }
}

@Composable
private fun AssistantMessageBubble(bubble: AssistantBubble, onSpeak: () -> Unit) {
    val c = PMTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (bubble.fromCustomer) Arrangement.End
                                else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(.88f),
            color = if (bubble.fromCustomer) c.primaryDark else c.surface,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, if (bubble.fromCustomer) c.accentGold else c.border)
        ) {
            Column(Modifier.padding(13.dp)) {
                Text(
                    bubble.text,
                    color = if (bubble.fromCustomer) c.accentGold else c.textPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!bubble.fromCustomer) {
                    TextButton(onClick = onSpeak) {
                        Icon(Icons.Outlined.VolumeUp, contentDescription = null,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("پخش پاسخ با صدا")
                    }
                }
            }
        }
    }
}
