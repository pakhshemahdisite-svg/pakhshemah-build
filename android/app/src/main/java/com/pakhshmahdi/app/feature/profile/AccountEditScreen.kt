package com.pakhshmahdi.app.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pakhshmahdi.app.data.auth.AuthStore
import com.pakhshmahdi.app.data.auth.BillingProfile
import com.pakhshmahdi.app.data.store.PwsCity
import com.pakhshmahdi.app.data.store.PwsCityRepository
import com.pakhshmahdi.app.data.store.iranProvinces
import com.pakhshmahdi.app.ui.components.PMPrimaryButton
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun AccountEditScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val c = PMTheme.colors
    val vm: AccountEditViewModel = viewModel()
    val state by vm.state.collectAsState()
    val user = AuthStore.user

    if (user == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val billing = user.billing
    val cityRepository = remember { PwsCityRepository() }
    val savedCityValue = remember(user.id) { billing.city }
    var firstName by remember(user.id) { mutableStateOf(billing.firstName.ifBlank { user.firstName }) }
    var lastName by remember(user.id) { mutableStateOf(billing.lastName.ifBlank { user.lastName }) }
    var email by remember(user.id) { mutableStateOf(billing.email.ifBlank { user.email }) }
    var cityId by remember(user.id) {
        mutableStateOf(savedCityValue.takeIf { value -> value.isNotBlank() && value.all(Char::isDigit) }.orEmpty())
    }
    var cities by remember { mutableStateOf<List<PwsCity>>(emptyList()) }
    var cityLoading by remember { mutableStateOf(false) }
    var cityError by remember { mutableStateOf<String?>(null) }
    var cityMenu by remember { mutableStateOf(false) }
    var allowSavedCityMatch by remember { mutableStateOf(true) }
    var address1 by remember(user.id) { mutableStateOf(billing.address1) }
    var address2 by remember(user.id) { mutableStateOf(billing.address2) }
    var postcode by remember(user.id) { mutableStateOf(billing.postcode) }
    var provinceCode by remember(user.id) { mutableStateOf(billing.state.ifBlank { "558" }) }
    var provinceMenu by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.success) {
        if (state.success) onSaved()
    }

    LaunchedEffect(provinceCode) {
        cityLoading = true
        cityError = null
        runCatching { cityRepository.cities(provinceCode) }
            .onSuccess { loaded ->
                cities = loaded
                val currentStillValid = loaded.any { it.id == cityId }
                if (!currentStillValid) {
                    cityId = if (allowSavedCityMatch) {
                        loaded.firstOrNull { city ->
                            city.id == savedCityValue || city.name == savedCityValue
                        }?.id.orEmpty()
                    } else {
                        ""
                    }
                }
                cityLoading = false
            }
            .onFailure {
                cities = emptyList()
                cityId = ""
                cityLoading = false
                cityError = "فهرست شهرها دریافت نشد. دوباره تلاش کنید."
            }
    }

    val selectedProvince = iranProvinces.firstOrNull { it.code == provinceCode }
    val selectedCity = cities.firstOrNull { it.id == cityId }

    fun save() {
        val cleanPostcode = postcode.filter(Char::isDigit)
        validationError = when {
            firstName.isBlank() -> "نام را وارد کنید."
            lastName.isBlank() -> "نام خانوادگی را وارد کنید."
            email.isNotBlank() && !email.contains("@") -> "ایمیل معتبر وارد کنید."
            cityId.isBlank() -> "شهر را انتخاب کنید."
            address1.isBlank() -> "آدرس را وارد کنید."
            cleanPostcode.isNotBlank() && cleanPostcode.length != 10 -> "کد پستی باید ۱۰ رقم باشد."
            else -> null
        }
        if (validationError != null) return

        vm.save(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            email = email.trim(),
            billing = BillingProfile(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                address1 = address1.trim(),
                address2 = address2.trim(),
                city = cityId,
                state = provinceCode,
                postcode = cleanPostcode,
                country = "IR",
                phone = user.phone.ifBlank { billing.phone },
                email = email.trim()
            )
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        PMScreenTopBar(
            title = "اطلاعات حساب",
            subtitle = "ویرایش مشخصات و آدرس پیش‌فرض",
            onBack = onBack
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            EditSection(Icons.Outlined.PersonOutline, "اطلاعات شخصی") {
                AccountField(firstName, { firstName = it }, "نام")
                AccountField(lastName, { lastName = it }, "نام خانوادگی")
                AccountField(
                    value = user.phone.ifBlank { billing.phone },
                    onValueChange = {},
                    label = "شماره موبایل",
                    enabled = false
                )
                AccountField(email, { email = it }, "ایمیل (اختیاری)")
            }

            EditSection(Icons.Outlined.LocationOn, "آدرس پیش‌فرض") {
                Box(Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { provinceMenu = true },
                        shape = PMTheme.shapes.medium,
                        color = c.surfaceElevated,
                        border = BorderStroke(1.dp, c.border)
                    ) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
                            Text(
                                "استان",
                                style = MaterialTheme.typography.labelMedium,
                                color = c.textMuted
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                selectedProvince?.name ?: "انتخاب استان",
                                color = c.textPrimary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = provinceMenu,
                        onDismissRequest = { provinceMenu = false },
                        modifier = Modifier.heightIn(max = 360.dp)
                    ) {
                        iranProvinces.forEach { province ->
                            DropdownMenuItem(
                                text = { Text(province.name) },
                                onClick = {
                                    if (provinceCode != province.code) {
                                        allowSavedCityMatch = false
                                        cityId = ""
                                    }
                                    provinceCode = province.code
                                    provinceMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Box(Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !cityLoading && cities.isNotEmpty()) { cityMenu = true },
                        shape = PMTheme.shapes.medium,
                        color = c.surfaceElevated,
                        border = BorderStroke(1.dp, c.border)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "شهر",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = c.textMuted
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    when {
                                        cityLoading -> "در حال دریافت شهرها..."
                                        selectedCity != null -> selectedCity.name
                                        else -> "انتخاب شهر"
                                    },
                                    color = if (selectedCity != null) c.textPrimary else c.textMuted
                                )
                            }
                            if (cityLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = c.primary
                                )
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = cityMenu,
                        onDismissRequest = { cityMenu = false },
                        modifier = Modifier.heightIn(max = 360.dp)
                    ) {
                        cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city.name) },
                                onClick = {
                                    cityId = city.id
                                    cityMenu = false
                                }
                            )
                        }
                    }
                }

                if (!cityError.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        cityError ?: "",
                        color = c.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Spacer(Modifier.height(10.dp))
                AccountField(address1, { address1 = it }, "آدرس", singleLine = false)
                AccountField(address2, { address2 = it }, "پلاک / واحد / توضیحات (اختیاری)")
                AccountField(postcode, { postcode = it.filter(Char::isDigit).take(10) }, "کد پستی")
            }

            val error = validationError ?: state.error
            if (!error.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = PMTheme.shapes.medium,
                    color = c.error.copy(alpha = .08f),
                    border = BorderStroke(1.dp, c.error.copy(alpha = .22f))
                ) {
                    Text(
                        error,
                        color = c.error,
                        modifier = Modifier.padding(13.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }

        Surface(
            color = c.surface,
            border = BorderStroke(1.dp, c.border),
            shadowElevation = 8.dp
        ) {
            PMPrimaryButton(
                text = "ذخیره اطلاعات",
                onClick = ::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(12.dp),
                loading = state.saving
            )
        }
    }
}

@Composable
private fun EditSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = PMTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        shape = PMTheme.shapes.card,
        color = c.surface,
        border = BorderStroke(1.dp, c.border)
    ) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = PMTheme.shapes.medium,
                    color = c.primary.copy(alpha = .09f)
                ) {
                    Icon(
                        icon,
                        null,
                        tint = c.primary,
                        modifier = Modifier.padding(8.dp).size(20.dp)
                    )
                }
                Spacer(Modifier.width(9.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = c.textPrimary
                )
            }
            Spacer(Modifier.height(13.dp))
            content()
        }
    }
}

@Composable
private fun AccountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    val c = PMTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        shape = PMTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = c.primary,
            unfocusedBorderColor = c.border,
            disabledBorderColor = c.border,
            focusedContainerColor = c.surface,
            unfocusedContainerColor = c.surface,
            disabledContainerColor = c.surfaceElevated,
            focusedTextColor = c.textPrimary,
            unfocusedTextColor = c.textPrimary,
            disabledTextColor = c.textMuted
        )
    )
}
