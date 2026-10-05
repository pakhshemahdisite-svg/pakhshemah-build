package com.pakhshmahdi.app.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pakhshmahdi.app.ui.components.BrandLogo
import com.pakhshmahdi.app.ui.components.PMPrimaryButton
import com.pakhshmahdi.app.ui.theme.PMTheme
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val c = PMTheme.colors
    val vm: LoginViewModel = viewModel()
    val state by vm.state.collectAsState()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var tab by remember { mutableIntStateOf(0) }

    LaunchedEffect(state.success) {
        if (state.success) onSuccess()
    }

    LaunchedEffect(state.otpSent, state.resendAvailableAt) {
        while (state.otpSent && System.currentTimeMillis() < state.resendAvailableAt) {
            now = System.currentTimeMillis()
            delay(1000)
        }
        now = System.currentTimeMillis()
    }

    val remaining = ((state.resendAvailableAt - now + 999) / 1000).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = c.surface,
                border = BorderStroke(1.dp, c.border)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, "بازگشت", tint = c.textPrimary)
                }
            }
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandLogo(Modifier.width(170.dp).height(120.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                "پخش مهدی",
                style = MaterialTheme.typography.headlineLarge,
                color = c.primary,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "عمده‌فروش لوازم خانه و آشپزخانه",
                color = c.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth()) {
            AuthTab(
                title = "ورود",
                selected = tab == 0,
                onClick = { tab = 0 },
                modifier = Modifier.weight(1f)
            )
            AuthTab(
                title = "ثبت نام",
                selected = tab == 1,
                onClick = { tab = 1 },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(22.dp))

        if (!state.otpSent) {
            Text(
                if (tab == 0) "ورود با شماره موبایل" else "ایجاد حساب با شماره موبایل",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = c.textPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "برای ورود امن، یک کد تایید یکبار مصرف برای شما پیامک می‌شود.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary
            )
            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = state.phone,
                onValueChange = vm::setPhone,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("شماره موبایل") },
                placeholder = { Text("09123456789") },
                leadingIcon = { Icon(Icons.Outlined.PhoneAndroid, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = PMTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = c.primary,
                    unfocusedBorderColor = c.border,
                    focusedContainerColor = c.surface,
                    unfocusedContainerColor = c.surface,
                    focusedTextColor = c.textPrimary,
                    unfocusedTextColor = c.textPrimary
                )
            )

            Spacer(Modifier.height(16.dp))

            PMPrimaryButton(
                text = "دریافت کد تایید",
                onClick = vm::requestOtp,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.phone.length == 11,
                loading = state.loading,
                icon = Icons.Outlined.VerifiedUser
            )
        } else {
            Text(
                "تایید شماره موبایل",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = c.textPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "کد ارسال‌شده به ${state.phone} را وارد کنید.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary
            )
            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = state.code,
                onValueChange = vm::setCode,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("کد تایید") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = PMTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = c.primary,
                    unfocusedBorderColor = c.border,
                    focusedContainerColor = c.surface,
                    unfocusedContainerColor = c.surface,
                    focusedTextColor = c.textPrimary,
                    unfocusedTextColor = c.textPrimary
                )
            )

            Spacer(Modifier.height(16.dp))

            PMPrimaryButton(
                text = if (tab == 0) "ورود به حساب" else "تکمیل ثبت نام",
                onClick = vm::verifyOtp,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.code.length >= 4,
                loading = state.loading
            )

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = vm::editPhone) {
                    Text("ویرایش شماره", color = c.primary)
                }
                TextButton(
                    onClick = vm::requestOtp,
                    enabled = remaining == 0L && !state.loading
                ) {
                    Text(
                        if (remaining > 0L) "ارسال مجدد (${remaining} ثانیه)" else "ارسال مجدد",
                        color = if (remaining == 0L) c.primary else c.textMuted
                    )
                }
            }
        }

        val error = state.error
        if (!error.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = PMTheme.shapes.medium,
                color = c.error.copy(alpha = .08f),
                border = BorderStroke(1.dp, c.error.copy(alpha = .2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    error,
                    color = c.error,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            "ورود و ثبت نام در اپ با کد تایید انجام می‌شود؛ بنابراین نیازی به رمز عبور یا بازیابی رمز نیست.",
            style = MaterialTheme.typography.labelMedium,
            color = c.textMuted,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun AuthTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = PMTheme.colors
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                title,
                color = if (selected) c.textPrimary else c.textMuted,
                fontWeight = if (selected) FontWeight.Black else FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = if (selected) c.accentGold else c.border
            ) {}
        }
    }
}
