import SwiftUI

struct AccountEditView: View {
    @EnvironmentObject private var session: SessionStore
    @Environment(\.dismiss) private var dismiss

    @State private var firstName = ""
    @State private var lastName = ""
    @State private var email = ""
    @State private var phone = ""
    @State private var address1 = ""
    @State private var address2 = ""
    @State private var postcode = ""
    @State private var provinceID = "558"
    @State private var cityID = ""
    @State private var cities: [PwsCity] = []
    @State private var cityLoading = false
    @State private var saving = false
    @State private var error: String?
    @State private var initialized = false

    private let cityAPI = PwsCityClient()

    var body: some View {
        Form {
            if let error {
                Section {
                    Text(error)
                        .foregroundStyle(.red)
                        .font(.footnote)
                }
            }

            Section("مشخصات حساب") {
                TextField("نام", text: $firstName)
                    .textContentType(.givenName)
                TextField("نام خانوادگی", text: $lastName)
                    .textContentType(.familyName)
                TextField("ایمیل", text: $email)
                    .keyboardType(.emailAddress)
                    .textInputAutocapitalization(.never)
            }

            Section("آدرس پیش‌فرض") {
                TextField("شماره موبایل", text: $phone)
                    .keyboardType(.phonePad)

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

                TextField("آدرس", text: $address1, axis: .vertical)
                    .lineLimit(2...4)
                TextField("ادامه آدرس (اختیاری)", text: $address2)
                TextField("کدپستی", text: $postcode)
                    .keyboardType(.numberPad)
            }

            Section {
                Button {
                    Task { await save() }
                } label: {
                    HStack {
                        Spacer()
                        if saving { ProgressView() }
                        Text("ذخیره تغییرات").fontWeight(.bold)
                        Spacer()
                    }
                }
                .disabled(saving || firstName.isEmpty || lastName.isEmpty)
            }
        }
        .navigationTitle("اطلاعات و آدرس")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            guard !initialized else { return }
            initialized = true
            fill()
            await loadCities()
        }
    }

    private func fill() {
        guard let user = session.user else { return }
        firstName = user.firstName
        lastName = user.lastName
        email = user.email
        let billing = user.billing
        phone = billing.phone.isEmpty ? user.phone : billing.phone
        address1 = billing.address1
        address2 = billing.address2
        postcode = billing.postcode
        provinceID = billing.state.isEmpty ? "558" : billing.state
        cityID = billing.city
    }

    private func loadCities() async {
        cityLoading = true
        defer { cityLoading = false }

        do {
            cities = try await cityAPI.cities(stateID: provinceID)
            if !cityID.isEmpty, !cities.contains(where: { $0.id == cityID }) {
                cityID = ""
            }
        } catch {
            cities = []
            self.error = error.localizedDescription
        }
    }

    private func save() async {
        saving = true
        error = nil
        defer { saving = false }

        do {
            try await session.updateProfile(
                firstName: firstName.trimmingCharacters(in: .whitespacesAndNewlines),
                lastName: lastName.trimmingCharacters(in: .whitespacesAndNewlines),
                email: email.trimmingCharacters(in: .whitespacesAndNewlines)
            )

            let billing = BillingProfile(
                firstName: firstName.trimmingCharacters(in: .whitespacesAndNewlines),
                lastName: lastName.trimmingCharacters(in: .whitespacesAndNewlines),
                address1: address1.trimmingCharacters(in: .whitespacesAndNewlines),
                address2: address2.trimmingCharacters(in: .whitespacesAndNewlines),
                city: cityID,
                state: provinceID,
                postcode: String(postcode.filter { $0.isNumber }),
                country: "IR",
                phone: String(phone.filter { $0.isNumber }),
                email: email.trimmingCharacters(in: .whitespacesAndNewlines)
            )
            try await session.updateAddress(billing)
            dismiss()
        } catch {
            self.error = error.localizedDescription
        }
    }
}
