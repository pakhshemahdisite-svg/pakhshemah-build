import SwiftUI

struct RootView: View {
    @EnvironmentObject private var cart: CartStore
    @EnvironmentObject private var wishlist: WishlistStore

    var body: some View {
        TabView {
            NavigationStack { HomeView() }
                .tabItem { Label("خانه", systemImage: "house") }

            NavigationStack { CategoriesView() }
                .tabItem { Label("دسته‌بندی", systemImage: "square.grid.2x2") }

            if AppConfig.featureWishlist {
                NavigationStack { WishlistView() }
                    .tabItem { Label("علاقه‌مندی", systemImage: "heart") }
                    .badge(wishlist.items.count)
            }

            NavigationStack { CartView() }
                .tabItem { Label("سبد خرید", systemImage: "cart") }
                .badge(cart.totalQuantity)

            NavigationStack { ProfileView() }
                .tabItem { Label("پروفایل", systemImage: "person") }
        }
        .tint(PMColor.gold)
    }
}


struct CategoriesView: View {
    @EnvironmentObject private var catalog: CatalogStore

    private let columns = [
        GridItem(.flexible(), spacing: 10),
        GridItem(.flexible(), spacing: 10)
    ]

    var body: some View {
        ScrollView {
            if catalog.home.categories.isEmpty && catalog.loading {
                ProgressView()
                    .padding(.top, 80)
            } else if let error = catalog.error, catalog.home.categories.isEmpty {
                ContentUnavailableView(
                    "دسته‌بندی‌ها دریافت نشد",
                    systemImage: "wifi.exclamationmark",
                    description: Text(error)
                )
                .padding(.top, 40)
            } else {
                LazyVGrid(columns: columns, spacing: 10) {
                    ForEach(catalog.home.categories) { category in
                        NavigationLink {
                            CatalogView(categoryID: category.id, title: category.name)
                        } label: {
                            VStack(spacing: 9) {
                                ProductImageView(url: category.image)
                                    .frame(width: 104, height: 104)
                                    .clipShape(RoundedRectangle(cornerRadius: 16))
                                Text(category.name)
                                    .font(.subheadline.bold())
                                    .foregroundStyle(.primary)
                                    .multilineTextAlignment(.center)
                                    .lineLimit(2)
                                if category.count > 0 {
                                    Text("\(category.count) محصول")
                                        .font(.caption2)
                                        .foregroundStyle(.secondary)
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(PMColor.surface)
                            .clipShape(RoundedRectangle(cornerRadius: 18))
                            .overlay(
                                RoundedRectangle(cornerRadius: 18)
                                    .stroke(PMColor.border.opacity(0.7), lineWidth: 1)
                            )
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding()
            }
        }
        .background(PMColor.background)
        .navigationTitle("دسته‌بندی‌ها")
        .task {
            if catalog.home.categories.isEmpty {
                await catalog.loadHome(force: true)
            }
        }
        .refreshable { await catalog.loadHome(force: true) }
    }
}

private struct WishlistView: View {
    @EnvironmentObject private var wishlist: WishlistStore
    @EnvironmentObject private var cart: CartStore

    private let columns = [
        GridItem(.flexible(), spacing: 10),
        GridItem(.flexible(), spacing: 10)
    ]

    var body: some View {
        Group {
            if wishlist.items.isEmpty {
                ContentUnavailableView(
                    "هنوز محصولی ذخیره نکرده‌اید",
                    systemImage: "heart",
                    description: Text("محصولات دلخواه شما در این بخش نمایش داده می‌شوند.")
                )
            } else {
                ScrollView {
                    LazyVGrid(columns: columns, spacing: 10) {
                        ForEach(wishlist.items) { product in
                            NavigationLink(value: product) {
                                ProductCardView(product: product)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding()
                }
                .background(PMColor.background)
                .refreshable { await wishlist.refresh() }
            }
        }
        .navigationTitle("علاقه‌مندی‌ها")
        .navigationDestination(for: Product.self) { ProductDetailView(product: $0) }
        .task { await wishlist.refresh() }
    }
}

private struct ProfileView: View {
    @EnvironmentObject private var session: SessionStore
    @State private var orders: [OrderSummary] = []
    @State private var ordersLoading = false
    @State private var ordersError: String?

    var body: some View {
        Group {
            if !session.isLoggedIn {
                VStack(spacing: 18) {
                    BrandLogoView()
                        .frame(width: 180, height: 76)

                    Text("حساب کاربری")
                        .font(.title2.bold())

                    Text("برای مشاهده سفارش‌ها و اطلاعات حساب وارد شوید.")
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)

                    NavigationLink("ورود با شماره موبایل") {
                        LoginView()
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(PMColor.gold)
                }
                .padding()
            } else {
                List {
                    if let user = session.user {
                        Section("حساب کاربری") {
                            VStack(alignment: .trailing, spacing: 5) {
                                Text(user.fullName).font(.headline)
                                Text(user.phone).foregroundStyle(.secondary)
                                if !user.email.isEmpty {
                                    Text(user.email).font(.caption).foregroundStyle(.secondary)
                                }
                            }
                            .frame(maxWidth: .infinity, alignment: .trailing)

                            NavigationLink {
                                AccountEditView()
                            } label: {
                                Label("ویرایش اطلاعات و آدرس", systemImage: "person.crop.circle.badge.checkmark")
                            }
                        }
                    }

                    Section("سفارش‌ها") {
                        if ordersLoading && orders.isEmpty {
                            HStack { Spacer(); ProgressView(); Spacer() }
                        } else if let ordersError, orders.isEmpty {
                            VStack(alignment: .trailing, spacing: 8) {
                                Text(ordersError).foregroundStyle(.secondary)
                                Button("تلاش دوباره") { Task { await loadOrders() } }
                            }
                        } else if orders.isEmpty {
                            Text("هنوز سفارشی ثبت نشده است.")
                                .foregroundStyle(.secondary)
                        } else {
                            ForEach(orders) { order in
                                NavigationLink {
                                    OrderDetailView(orderID: order.id)
                                } label: {
                                    HStack {
                                        Text(toman(order.total))
                                            .foregroundStyle(PMColor.primary)
                                            .fontWeight(.bold)
                                        Spacer()
                                        VStack(alignment: .trailing, spacing: 3) {
                                            Text("سفارش #\(order.id)").fontWeight(.semibold)
                                            Text(orderStatusTitle(order.status))
                                                .font(.caption)
                                                .foregroundStyle(orderStatusColor(order.status))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Section("خدمات") {
                        if AppConfig.featureNotifications {
                            NavigationLink {
                                NotificationsView()
                            } label: {
                                Label("اطلاعیه‌ها", systemImage: "bell")
                            }
                        }

                        if AppConfig.featureSupport {
                            NavigationLink {
                                SupportView()
                            } label: {
                                Label("پشتیبانی", systemImage: "headphones")
                            }
                        }

                        NavigationLink {
                            SettingsView()
                        } label: {
                            Label("تنظیمات", systemImage: "gearshape")
                        }
                    }

                    Section {
                        Button("خروج از حساب", role: .destructive) {
                            session.logout()
                            orders = []
                        }
                    }
                }
                .refreshable {
                    await session.refresh()
                    await loadOrders()
                }
            }
        }
        .navigationTitle("پروفایل")
        .task {
            if session.isLoggedIn {
                await session.refresh()
                await loadOrders()
            }
        }
    }

    private func loadOrders() async {
        guard session.isLoggedIn, !ordersLoading else { return }
        ordersLoading = true
        ordersError = nil
        defer { ordersLoading = false }

        do {
            orders = try await session.orders()
        } catch {
            ordersError = error.localizedDescription
        }
    }
}

struct LoginView: View {
    @EnvironmentObject private var session: SessionStore
    @Environment(\.dismiss) private var dismiss

    @State private var phone = ""
    @State private var code = ""
    @State private var otpSent = false
    @State private var busy = false
    @State private var error: String?

    var body: some View {
        ScrollView {
            VStack(spacing: 18) {
                BrandLogoView()
                    .frame(width: 190, height: 80)

                Text("ورود به \(AppConfig.appName)")
                    .font(.title2.bold())

                TextField("شماره موبایل", text: $phone)
                    .keyboardType(.phonePad)
                    .textContentType(.telephoneNumber)
                    .padding()
                    .background(PMColor.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .onChange(of: phone) { _, newValue in
                        phone = String(newValue.filter(\.isNumber).prefix(11))
                    }

                if otpSent {
                    TextField("کد تایید", text: $code)
                        .keyboardType(.numberPad)
                        .textContentType(.oneTimeCode)
                        .padding()
                        .background(PMColor.surface)
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                        .onChange(of: code) { _, newValue in
                            code = String(newValue.filter(\.isNumber).prefix(6))
                        }
                }

                if let error {
                    Text(error)
                        .font(.caption)
                        .foregroundStyle(.red)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                }

                Button {
                    Task { await submit() }
                } label: {
                    HStack {
                        if busy { ProgressView().tint(.white) }
                        Text(otpSent ? "تایید و ورود" : "ارسال کد تایید")
                            .fontWeight(.bold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(PMColor.primary)
                    .foregroundStyle(PMColor.buttonForeground)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                }
                .disabled(busy)
            }
            .padding()
        }
        .background(PMColor.background)
        .navigationTitle("ورود")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func submit() async {
        let cleanPhone = phone.filter(\.isNumber)
        guard cleanPhone.count == 11, cleanPhone.hasPrefix("09") else {
            error = "شماره موبایل معتبر وارد کنید."
            return
        }

        busy = true
        error = nil
        defer { busy = false }

        do {
            if !otpSent {
                let result = try await session.requestOTP(phone: cleanPhone)
                otpSent = result.success
                if !result.success { error = "ارسال کد تایید انجام نشد." }
            } else {
                guard (4...6).contains(code.count) else {
                    error = "کد تایید را کامل وارد کنید."
                    return
                }
                try await session.verifyOTP(phone: cleanPhone, code: code)
                dismiss()
            }
        } catch {
            self.error = error.localizedDescription
        }
    }
}

func orderStatusTitle(_ raw: String) -> String {
    switch raw.replacingOccurrences(of: "wc-", with: "").lowercased() {
    case "pending": return "در انتظار پرداخت"
    case "processing": return "در حال پردازش"
    case "on-hold": return "در انتظار بررسی"
    case "completed": return "تکمیل شده"
    case "cancelled": return "لغو شده"
    case "refunded": return "مرجوع شده"
    case "failed": return "پرداخت ناموفق"
    default: return raw
    }
}

func orderStatusColor(_ raw: String) -> Color {
    switch raw.replacingOccurrences(of: "wc-", with: "").lowercased() {
    case "completed": return PMColor.success
    case "processing": return PMColor.primary
    case "cancelled", "failed", "refunded": return .red
    default: return .orange
    }
}
