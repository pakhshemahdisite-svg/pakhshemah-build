package com.pakhshmahdi.app.feature.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pakhshmahdi.app.data.cart.CartStore
import com.pakhshmahdi.app.data.local.LocalStore
import com.pakhshmahdi.app.data.store.StoreAddress
import com.pakhshmahdi.app.data.store.PwsCity
import com.pakhshmahdi.app.data.store.PwsCityRepository
import com.pakhshmahdi.app.data.store.iranProvinces
import com.pakhshmahdi.app.data.store.moneyLineTotal
import com.pakhshmahdi.app.data.store.moneyToLong
import com.pakhshmahdi.app.ui.components.PMCheckoutSteps
import com.pakhshmahdi.app.ui.components.PMPrimaryButton
import com.pakhshmahdi.app.ui.components.PMScreenTopBar
import com.pakhshmahdi.app.ui.components.PMSelectableCard
import com.pakhshmahdi.app.ui.components.toman
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun CheckoutScreen(onBack: () -> Unit) {
    val c = PMTheme.colors
    val vm: CheckoutViewModel = viewModel()
    val state by vm.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val savedAddress = remember { LocalStore.loadCheckoutAddress() }
    val cityRepository = remember { PwsCityRepository() }
    val savedCityValue = remember { savedAddress?.city.orEmpty() }

    var firstName by remember { mutableStateOf(savedAddress?.firstName.orEmpty()) }
    var lastName by remember { mutableStateOf(savedAddress?.lastName.orEmpty()) }
    var phone by remember { mutableStateOf(savedAddress?.phone.orEmpty()) }
    var email by remember {
        mutableStateOf(
            savedAddress?.email.orEmpty()
                .takeUnless { it.endsWith("@mobile.pakhshemahdi.com") }
                .orEmpty()
        )
    }
    var provinceCode by remember {
        mutableStateOf(savedAddress?.state?.takeIf { it.isNotBlank() } ?: "558")
    }
    var cityId by remember {
        mutableStateOf(savedCityValue.takeIf { value -> value.isNotBlank() && value.all(Char::isDigit) }.orEmpty())
    }
    var cities by remember { mutableStateOf<List<PwsCity>>(emptyList()) }
    var cityLoading by remember { mutableStateOf(false) }
    var cityError by remember { mutableStateOf<String?>(null) }
    var cityMenu by remember { mutableStateOf(false) }
    var allowSavedCityMatch by remember { mutableStateOf(true) }
    var address1 by remember { mutableStateOf(savedAddress?.address1.orEmpty()) }
    var postcode by remember { mutableStateOf(savedAddress?.postcode.orEmpty()) }
    var note by remember { mutableStateOf("") }
    var couponCode by remember { mutableStateOf("") }
    var provinceMenu by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vm.prepare(CartStore.lines.toList())
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
                cityError = "فهرست شهرها دریافت نشد. اتصال اینترنت را بررسی و دوباره تلاش کنید."
            }
    }

    LaunchedEffect(state.redirectUrl) {
        val url = state.redirectUrl
        if (!url.isNullOrBlank()) {
            runCatching { uriHandler.openUri(url) }
            vm.consumeRedirect()
        }
    }

    DisposableEffect(lifecycleOwner, state.orderId, state.paymentUrl) {
        val observer = LifecycleEventObserver { _, event ->
            if (
                event == Lifecycle.Event.ON_RESUME &&
                state.orderId != null &&
                !state.paymentUrl.isNullOrBlank() &&
                !state.paymentStatusBusy
            ) {
                vm.refreshPaymentStatus()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val cart = state.cart
    val selectedProvince = iranProvinces.firstOrNull { it.code == provinceCode }
    val selectedCity = cities.firstOrNull { it.id == cityId }

    val localItemsTotal = CartStore.lines.sumOf { line ->
        moneyLineTotal(line.unitPrice, line.quantity)
    }
    fun amountOrFallback(serverValue: String?, fallback: Long): String {
        val parsed = moneyToLong(serverValue)
        return if (parsed != null && parsed >= 0L) parsed.toString() else fallback.toString()
    }

    val itemsTotalForDisplay = amountOrFallback(cart?.totals?.totalItems, localItemsTotal)
    val shippingTotalForDisplay = moneyToLong(cart?.totals?.totalShipping)?.coerceAtLeast(0L) ?: 0L
    val discountTotalForDisplay = moneyToLong(cart?.totals?.totalDiscount)?.coerceAtLeast(0L) ?: 0L
    val fallbackFinalTotal = (localItemsTotal + shippingTotalForDisplay - discountTotalForDisplay).coerceAtLeast(0L)
    val finalTotalForDisplay = amountOrFallback(cart?.totals?.totalPrice, fallbackFinalTotal)

    val shippingRates = cart?.shippingRates.orEmpty().flatMap { pack ->
        pack.shippingRates.map { pack.packageId to it }
    }
    val hasBlockingCartErrors = !cart?.errors.isNullOrEmpty()
    val hasSelectedShipping = !cart?.needsShipping.orFalse() || shippingRates.any { it.second.selected }
    val selectedShippingRate = shippingRates.firstOrNull { it.second.selected }?.second
    val shippingIsCollect = selectedShippingRate?.name?.contains("پس کرایه") == true
    val activeStep = when {
        !state.addressCalculated -> 0
        !hasSelectedShipping -> 1
        state.selectedPaymentMethod.isBlank() -> 2
        else -> 2
    }

    fun addressOrNull(): StoreAddress? {
        val cleanPhone = phone.filter(Char::isDigit)
        val cleanPostcode = postcode.filter(Char::isDigit)

        formError = when {
            firstName.isBlank() -> "نام را وارد کنید."
            lastName.isBlank() -> "نام خانوادگی را وارد کنید."
            cleanPhone.length != 11 || !cleanPhone.startsWith("09") -> "شماره موبایل معتبر وارد کنید."
            email.isNotBlank() && !email.contains("@") -> "ایمیل واردشده معتبر نیست."
            cityId.isBlank() -> "شهر را انتخاب کنید."
            address1.isBlank() -> "آدرس را وارد کنید."
            cleanPostcode.length != 10 -> "کد پستی ۱۰ رقمی وارد کنید."
            else -> null
        }
        if (formError != null) return null

        return StoreAddress(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            address1 = address1.trim(),
            city = cityId,
            state = provinceCode,
            postcode = cleanPostcode,
            phone = cleanPhone,
            email = email.trim().ifBlank { "${cleanPhone}@mobile.pakhshemahdi.com" },
            country = "IR"
        )
    }

    if (state.orderId != null) {
        OrderResultScreen(
            orderId = state.orderId ?: 0L,
            paymentUrl = state.paymentUrl,
            paymentStatus = state.paymentStatus,
            checking = state.paymentStatusBusy,
            error = state.paymentStatusError,
            onCheck = vm::refreshPaymentStatus,
            onOpenPayment = { url -> runCatching { uriHandler.openUri(url) } },
            onBack = onBack
        )
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        PMScreenTopBar(
            title = "تکمیل خرید",
            subtitle = "اطلاعات ارسال و پرداخت",
            onBack = onBack
        )

        PMCheckoutSteps(
            active = activeStep,
            labels = listOf("اطلاعات ارسال", "روش ارسال", "پرداخت")
        )

        if (state.loading && cart == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.primary)
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            if (state.loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = c.primary,
                    trackColor = c.surfaceElevated
                )
                Spacer(Modifier.height(10.dp))
            }

            CheckoutSection(
                icon = Icons.Outlined.LocationOn,
                title = "اطلاعات ارسال"
            ) {
                CheckoutField(firstName, { firstName = it }, "نام")
                CheckoutField(lastName, { lastName = it }, "نام خانوادگی")
                CheckoutField(phone, { phone = it.filter(Char::isDigit).take(11) }, "شماره موبایل")
                CheckoutField(email, { email = it }, "ایمیل (اختیاری)")

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
                                        cities = emptyList()
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
                            .clickable(enabled = !cityLoading && cities.isNotEmpty()) {
                                cityMenu = true
                            },
                        shape = PMTheme.shapes.medium,
                        color = c.surfaceElevated,
                        border = BorderStroke(1.dp, c.border)
                    ) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
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
                                color = if (selectedCity == null) c.textMuted else c.textPrimary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = cityMenu,
                        onDismissRequest = { cityMenu = false },
                        modifier = Modifier.heightIn(max = 420.dp)
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
                CheckoutField(address1, { address1 = it }, "آدرس کامل", singleLine = false)
                CheckoutField(postcode, { postcode = it.filter(Char::isDigit).take(10) }, "کد پستی")

                if (!formError.isNullOrBlank()) {
                    Text(
                        formError ?: "",
                        color = c.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(8.dp))
                }

                PMPrimaryButton(
                    text = if (state.addressCalculated) "محاسبه مجدد ارسال" else "تایید اطلاعات و محاسبه ارسال",
                    onClick = { addressOrNull()?.let(vm::calculateAddress) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.loading && !cityLoading && cityId.isNotBlank(),
                    icon = Icons.Outlined.LocalShipping
                )
            }

            CheckoutSection(
                icon = Icons.Outlined.LocalOffer,
                title = "کد تخفیف"
            ) {
                cart?.coupons.orEmpty().forEach { coupon ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = PMTheme.shapes.medium,
                        color = c.primary.copy(alpha = .08f),
                        border = BorderStroke(1.dp, c.primary.copy(alpha = .25f))
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                coupon.code,
                                modifier = Modifier.weight(1f),
                                color = c.primary,
                                fontWeight = FontWeight.Black
                            )
                            TextButton(
                                onClick = { vm.removeCoupon(coupon.code) },
                                enabled = !state.couponBusy
                            ) {
                                Text("حذف", color = c.error)
                            }
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = couponCode,
                        onValueChange = { couponCode = it },
                        label = { Text("کد تخفیف") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = PMTheme.shapes.medium,
                        colors = checkoutFieldColors()
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            vm.applyCoupon(couponCode)
                            couponCode = ""
                        },
                        enabled = couponCode.isNotBlank() && !state.couponBusy,
                        modifier = Modifier.height(56.dp),
                        shape = PMTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = c.primary,
                            contentColor = if (c.primary == c.accentGold) c.primaryDark else androidx.compose.ui.graphics.Color.White
                        )
                    ) {
                        if (state.couponBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(19.dp),
                                strokeWidth = 2.dp,
                                color = LocalContentColor.current
                            )
                        } else {
                            Text("اعمال")
                        }
                    }
                }
            }

            if (state.addressCalculated) {
                CheckoutSection(
                    icon = Icons.Outlined.LocalShipping,
                    title = "روش ارسال"
                ) {
                    if (!cart?.needsShipping.orFalse()) {
                        Text("این سفارش نیاز به ارسال ندارد.", color = c.textSecondary)
                    } else if (shippingRates.isEmpty()) {
                        Text(
                            "برای این آدرس روش ارسالی پیدا نشد.",
                            color = c.error
                        )
                    } else {
                        shippingRates.forEach { (packageId, rate) ->
                            PMSelectableCard(
                                selected = rate.selected,
                                title = rate.name,
                                subtitle = if (rate.name.contains("پس کرایه")) {
                                    "هزینه حمل هنگام تحویل تسویه می‌شود"
                                } else {
                                    toman(rate.price)
                                },
                                onClick = { vm.selectShipping(packageId, rate.rateId) },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }

                CheckoutSection(
                    icon = Icons.Outlined.CreditCard,
                    title = "روش پرداخت"
                ) {
                    val methods = cart?.paymentMethods.orEmpty()
                    if (methods.isEmpty()) {
                        Text(
                            "روش پرداختی از فروشگاه دریافت نشد.",
                            color = c.error
                        )
                    } else {
                        methods.forEach { method ->
                            PMSelectableCard(
                                selected = state.selectedPaymentMethod == method,
                                title = paymentLabel(method),
                                subtitle = if (method == "wc_zibal") "انتقال امن به درگاه پرداخت" else paymentSubtitle(method),
                                onClick = { vm.setPaymentMethod(method) },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }

                CheckoutSection(
                    icon = Icons.Outlined.CheckCircle,
                    title = "خلاصه پرداخت"
                ) {
                    PaymentRow("جمع کالاها", toman(itemsTotalForDisplay))
                    if (discountTotalForDisplay > 0L) {
                        PaymentRow("تخفیف", toman(discountTotalForDisplay.toString()))
                    }
                    PaymentRow(
                        "هزینه ارسال",
                        if (shippingIsCollect) "پس کرایه" else toman(shippingTotalForDisplay.toString())
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = c.border)
                    PaymentRow("مبلغ نهایی", toman(finalTotalForDisplay), bold = true)
                    Spacer(Modifier.height(12.dp))
                    CheckoutField(note, { note = it }, "یادداشت سفارش (اختیاری)", singleLine = false)
                }
            }

            val error = state.error
            if (!error.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = PMTheme.shapes.medium,
                    color = c.error.copy(alpha = .08f),
                    border = BorderStroke(1.dp, c.error.copy(alpha = .20f))
                ) {
                    Text(
                        error,
                        color = c.error,
                        modifier = Modifier.padding(13.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
        }

        if (state.addressCalculated) {
            Surface(
                color = c.surface,
                border = BorderStroke(1.dp, c.border),
                shadowElevation = 10.dp
            ) {
                PMPrimaryButton(
                    text = "پرداخت و ثبت نهایی",
                    onClick = { addressOrNull()?.let { vm.submit(it, note) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(12.dp),
                    enabled = !state.loading &&
                        !state.submitting &&
                        !hasBlockingCartErrors &&
                        hasSelectedShipping &&
                        state.selectedPaymentMethod.isNotBlank(),
                    loading = state.submitting,
                    icon = Icons.Outlined.CreditCard
                )
            }
        }
    }
}

@Composable
private fun CheckoutSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = PMTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        shape = PMTheme.shapes.card,
        color = c.surface,
        border = BorderStroke(1.dp, c.border),
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = PMTheme.shapes.medium,
                    color = c.primary.copy(alpha = .09f)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
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
private fun CheckoutField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        shape = PMTheme.shapes.medium,
        colors = checkoutFieldColors()
    )
}

@Composable
private fun checkoutFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PMTheme.colors.primary,
    unfocusedBorderColor = PMTheme.colors.border,
    focusedContainerColor = PMTheme.colors.surface,
    unfocusedContainerColor = PMTheme.colors.surface,
    focusedTextColor = PMTheme.colors.textPrimary,
    unfocusedTextColor = PMTheme.colors.textPrimary
)

@Composable
private fun PaymentRow(
    label: String,
    value: String,
    bold: Boolean = false
) {
    val c = PMTheme.colors
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            color = if (bold) c.textPrimary else c.textSecondary,
            fontWeight = if (bold) FontWeight.Black else FontWeight.Normal
        )
        Text(
            value,
            color = c.primary,
            fontWeight = if (bold) FontWeight.Black else FontWeight.Bold
        )
    }
}

@Composable
private fun OrderResultScreen(
    orderId: Long,
    paymentUrl: String?,
    paymentStatus: String?,
    checking: Boolean,
    error: String?,
    onCheck: () -> Unit,
    onOpenPayment: (String) -> Unit,
    onBack: () -> Unit
) {
    val c = PMTheme.colors
    val normalizedStatus = paymentStatus
        .orEmpty()
        .removePrefix("wc-")
        .lowercase()

    val paymentConfirmed = normalizedStatus == "processing" || normalizedStatus == "completed"
    val paymentFailed = normalizedStatus == "failed" ||
        normalizedStatus == "cancelled" ||
        normalizedStatus == "refunded"
    val waitsForGateway = !paymentUrl.isNullOrBlank() && !paymentConfirmed && !paymentFailed

    val title = when {
        paymentConfirmed -> "پرداخت با موفقیت تأیید شد"
        paymentFailed -> "پرداخت ناموفق یا لغو شد"
        waitsForGateway -> "سفارش ثبت شد؛ در انتظار تأیید پرداخت"
        else -> "سفارش شما ثبت شد"
    }

    val message = when {
        paymentConfirmed -> "پرداخت سفارش #$orderId در ووکامرس تأیید شده و سفارش وارد مرحله پردازش شده است."
        paymentFailed -> "وضعیت سفارش #$orderId نشان می‌دهد پرداخت تکمیل نشده است. می‌توانید دوباره درگاه را باز کنید."
        waitsForGateway -> "پس از پرداخت و بازگشت از درگاه، وضعیت سفارش #$orderId را از فروشگاه بررسی کنید."
        else -> "شماره سفارش: #$orderId"
    }

    val accent = when {
        paymentConfirmed -> c.success
        paymentFailed -> c.error
        else -> c.primary
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(92.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = accent.copy(alpha = .12f),
            border = BorderStroke(1.dp, accent.copy(alpha = .25f))
        ) {
            Icon(
                if (paymentFailed) Icons.Outlined.CreditCard else Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.padding(22.dp)
            )
        }

        Spacer(Modifier.height(22.dp))

        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = c.textPrimary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            message,
            color = c.textSecondary
        )

        if (!error.isNullOrBlank()) {
            Spacer(Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = PMTheme.shapes.medium,
                color = c.error.copy(alpha = .08f),
                border = BorderStroke(1.dp, c.error.copy(alpha = .20f))
            ) {
                Text(
                    error,
                    color = c.error,
                    modifier = Modifier.padding(13.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        when {
            waitsForGateway -> {
                PMPrimaryButton(
                    text = "بررسی وضعیت پرداخت",
                    onClick = onCheck,
                    modifier = Modifier.fillMaxWidth(),
                    loading = checking,
                    icon = Icons.Outlined.CheckCircle
                )

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = { paymentUrl?.let(onOpenPayment) },
                    enabled = !checking && !paymentUrl.isNullOrBlank()
                ) {
                    Text("باز کردن دوباره درگاه پرداخت")
                }

                TextButton(
                    onClick = onBack,
                    enabled = !checking
                ) {
                    Text("بازگشت")
                }
            }

            paymentFailed -> {
                if (!paymentUrl.isNullOrBlank()) {
                    PMPrimaryButton(
                        text = "باز کردن دوباره درگاه",
                        onClick = { onOpenPayment(paymentUrl) },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Outlined.CreditCard
                    )
                    Spacer(Modifier.height(8.dp))
                }

                TextButton(onClick = onBack) {
                    Text("بازگشت به سبد خرید")
                }
            }

            else -> {
                PMPrimaryButton(
                    text = "بازگشت",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun paymentLabel(method: String): String = when (method) {
    "wc_zibal" -> "پرداخت آنلاین زیبال"
    "cod" -> "پرداخت هنگام تحویل"
    "bacs" -> "واریز بانکی"
    "cheque" -> "پرداخت با چک"
    else -> method
}

private fun paymentSubtitle(method: String): String = when (method) {
    "cod" -> "پرداخت در محل طبق شرایط فروشگاه"
    "bacs" -> "اطلاعات حساب پس از ثبت سفارش"
    "cheque" -> "بررسی و تایید توسط فروشگاه"
    else -> ""
}

private fun Boolean?.orFalse(): Boolean = this ?: false
