import SwiftUI

struct CheckoutView: View {
    @EnvironmentObject private var cart: CartStore
    @EnvironmentObject private var session: SessionStore
    @Environment(\.openURL) private var openURL
    @StateObject private var checkout = CheckoutStore()

    @State private var firstName = ""
    @State private var lastName = ""
    @State private var address1 = ""
    @State private var postcode = ""
    @State private var phone = ""
    @State private var email = ""
    @State private var provinceID = "558"
    @State private var cityID = ""
    @State private var cities: [PwsCity] = []
    @State private var cityLoading = false
    @State private var selectedPayment = ""
    @State private var note = ""
    @State private var initialized = false

    private let cityAPI = PwsCityClient()

    var body: some View {
        Form {
            if checkout.loading && checkout.cart == nil {
                Section { ProgressView("همگام‌سازی سبد با فروشگاه...") }
            }

            if let error = checkout.error {
                Section { Text(error).foregroundStyle(.red).font(.footnote) }
            }

            Section("اطلاعات گیرنده") {
                TextField("نام", text: $firstName)
                TextField("نام خانوادگی", text: $lastName)
                TextField("شماره موبایل", text: $phone).keyboardType(.phonePad)
                TextField("ایمیل", text: $email)
                    .keyboardType(.emailAddress)
                    .textInputAutocapitalization(.never)
            }

            Section("آدرس ارسال") {
                Picker("استان", selection: $provinceID) {
                    ForEach(iranProvinces) { Text($0.name).tag($0.id) }
                }
                .onChange(of: provinceID) { _, _ in
                    cityID = ""
                    Task { await loadCities() }
                }

                if cityLoading {
                    ProgressView("دریافت شهرها...")
                } else {
                    Picker("شهر", selection: $cityID) {
                        Text("انتخاب شهر").tag("")
                        ForEach(cities) { Text($0.name).tag($0.id) }
                    }
                }

                TextField("آدرس کامل", text: $address1, axis: .vertical)
                    .lineLimit(2...4)
                TextField("کدپستی", text: $postcode).keyboardType(.numberPad)

                Button("محاسبه روش ارسال") {
                    Task { await checkout.updateAddress(makeAddress()) }
                }
                .disabled(!addressIsValid || checkout.loading)
            }

            if let serverCart = checkout.cart {
                if !serverCart.shippingRates.isEmpty {
                    Section("روش ارسال") {
                        ForEach(serverCart.shippingRates, id: \.packageId) { package in
                            ForEach(package.shippingRates) { rate in
                                Button {
                                    Task {
                                        await checkout.selectShipping(
                                            packageID: package.packageId,
                                            rateID: rate.rateId
                                        )
                                    }
                                } label: {
                                    HStack {
                                        Image(systemName: rate.selected ? "largecircle.fill.circle" : "circle")
                                            .foregroundStyle(PMColor.primary)
                                        VStack(alignment: .trailing) {
                                            Text(rate.name).foregroundStyle(.primary)
                                            Text(shippingPrice(rate.price))
                                                .font(.caption)
                                                .foregroundStyle(.secondary)
                                        }
                                        Spacer()
                                    }
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }
                }

                Section("روش پرداخت") {
                    Picker("روش پرداخت", selection: $selectedPayment) {
                        Text("انتخاب روش پرداخت").tag("")
                        ForEach(serverCart.paymentMethods, id: \.self) {
                            Text(paymentTitle($0)).tag($0)
                        }
                    }
                }

                Section("یادداشت سفارش") {
                    TextField("توضیحات اختیاری", text: $note, axis: .vertical)
                        .lineLimit(2...4)
                }

                Section("جمع نهایی") {
                    moneyRow("جمع کالاها", serverCart.totals.totalItems)
                    moneyRow("تخفیف", serverCart.totals.totalDiscount)
                    if let shipping = serverCart.totals.totalShipping {
                        moneyRow("هزینه ارسال", shipping)
                    }
                    moneyRow("مبلغ قابل پرداخت", serverCart.totals.totalPrice, bold: true)
                }

                Section {
                    Button("ثبت نهایی سفارش") {
                        Task {
                            await checkout.submit(
                                address: makeAddress(),
                                paymentMethod: selectedPayment,
                                note: note,
                                session: session,
                                localCart: cart
                            )
                        }
                    }
                    .fontWeight(.bold)
                    .disabled(
                        checkout.submitting ||
                        selectedPayment.isEmpty ||
                        !addressIsValid ||
                        (serverCart.needsShipping && !hasSelectedShipping(serverCart))
                    )
                }
            }

            if let orderID = checkout.orderId {
                Section("سفارش ثبت شد") {
                    Label("شماره سفارش: #\(orderID)", systemImage: "checkmark.circle.fill")
                        .foregroundStyle(PMColor.success)
                    if let payment = checkout.paymentURL, let url = URL(string: payment) {
                        Button("ادامه پرداخت در درگاه") { openURL(url) }
                    } else {
                        Text("سفارش با موفقیت در فروشگاه ثبت شد.")
                            .foregroundStyle(.secondary)
                    }
                }
            }
        }
        .navigationTitle("تسویه حساب")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            guard !initialized else { return }
            initialized = true
            prefill()
            await loadCities()

            if checkout.orderId != nil {
                await checkout.verifyPendingPayment(
                    session: session,
                    localCart: cart
                )
            }

            if checkout.orderId == nil || checkout.paymentURL == nil {
                await checkout.prepare(localCart: cart)
            }
            autoSelectPayment()
        }
        .onChange(of: checkout.cart?.paymentMethods ?? []) { _, _ in
            autoSelectPayment()
        }
    }

    private var addressIsValid: Bool {
        !firstName.trimmingCharacters(in: .whitespaces).isEmpty &&
        !lastName.trimmingCharacters(in: .whitespaces).isEmpty &&
        phone.filter { $0.isNumber }.count >= 10 &&
        !address1.trimmingCharacters(in: .whitespaces).isEmpty &&
        !provinceID.isEmpty && !cityID.isEmpty
    }

    private func prefill() {
        guard let user = session.user else { return }
        let billing = user.billing
        firstName = billing.firstName.isEmpty ? user.firstName : billing.firstName
        lastName = billing.lastName.isEmpty ? user.lastName : billing.lastName
        address1 = billing.address1
        postcode = billing.postcode
        phone = billing.phone.isEmpty ? user.phone : billing.phone
        email = billing.email.isEmpty ? user.email : billing.email
        provinceID = billing.state.isEmpty ? "558" : billing.state
        cityID = billing.city
    }

    private func loadCities() async {
        cityLoading = true
        defer { cityLoading = false }
        do {
            cities = try await cityAPI.cities(stateID: provinceID)
            if !cityID.isEmpty && !cities.contains(where: { $0.id == cityID }) {
                cityID = ""
            }
        } catch {
            cities = []
            checkout.error = error.localizedDescription
        }
    }

    private func makeAddress() -> StoreAddress {
        StoreAddress(
            firstName: firstName.trimmingCharacters(in: .whitespacesAndNewlines),
            lastName: lastName.trimmingCharacters(in: .whitespacesAndNewlines),
            company: "",
            address1: address1.trimmingCharacters(in: .whitespacesAndNewlines),
            address2: "",
            city: cityID,
            state: provinceID,
            postcode: String(postcode.filter { $0.isNumber }),
            country: "IR",
            phone: String(phone.filter { $0.isNumber }),
            email: email.trimmingCharacters(in: .whitespacesAndNewlines)
        )
    }

    private func autoSelectPayment() {
        let methods = checkout.cart?.paymentMethods ?? []
        if methods.count == 1 { selectedPayment = methods[0] }
        else if !selectedPayment.isEmpty && !methods.contains(selectedPayment) {
            selectedPayment = ""
        }
    }

    private func hasSelectedShipping(_ cart: StoreCart) -> Bool {
        cart.shippingRates.flatMap(\.shippingRates).contains(where: \.selected)
    }

    private func shippingPrice(_ value: String) -> String {
        Int(Double(value) ?? 0) == 0 ? "پس‌کرایه / طبق شرایط فروشگاه" : toman(value)
    }

    @ViewBuilder
    private func moneyRow(_ label: String, _ value: String, bold: Bool = false) -> some View {
        HStack {
            Text(toman(value))
                .foregroundStyle(PMColor.primary)
                .fontWeight(bold ? .bold : .regular)
            Spacer()
            Text(label).fontWeight(bold ? .bold : .regular)
        }
    }

    private func paymentTitle(_ method: String) -> String {
        switch method {
        case "cod": return "پرداخت هنگام تحویل"
        case "bacs": return "واریز بانکی"
        case "cheque": return "پرداخت با چک"
        case "wc_zibal": return "پرداخت آنلاین"
        default: return method
        }
    }
}
