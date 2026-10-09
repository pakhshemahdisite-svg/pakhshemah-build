import SwiftUI

private enum PMRootTab: Hashable {
    case home
    case categories
    case cart
    case profile
}

struct RootView: View {
    @EnvironmentObject private var cart: CartStore

    @State private var selectedTab: PMRootTab = .home
    @State private var homeResetID = UUID()
    @State private var categoriesResetID = UUID()
    @State private var cartResetID = UUID()
    @State private var profileResetID = UUID()

    var body: some View {
        VStack(spacing: 0) {
            Group {
                switch selectedTab {
                case .home:
                    NavigationStack { HomeView() }
                        .id(homeResetID)
                case .categories:
                    NavigationStack { CategoriesView() }
                        .id(categoriesResetID)
                case .cart:
                    NavigationStack { CartView() }
                        .id(cartResetID)
                case .profile:
                    NavigationStack { ProfileView() }
                        .id(profileResetID)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            PMRootBottomNavigation(
                selected: selectedTab,
                cartCount: cart.totalQuantity,
                onSelect: selectTab
            )
        }
        .background(PMColor.background)
    }

    private func selectTab(_ tab: PMRootTab) {
        if tab == selectedTab {
            switch tab {
            case .home: homeResetID = UUID()
            case .categories: categoriesResetID = UUID()
            case .cart: cartResetID = UUID()
            case .profile: profileResetID = UUID()
            }
        } else if tab == .home {
            // Always rebuild the Home navigation stack so Home reliably returns
            // to the real root even when the user is deep inside another screen.
            homeResetID = UUID()
        }
        selectedTab = tab
    }
}

private struct PMRootBottomNavigation: View {
    let selected: PMRootTab
    let cartCount: Int
    let onSelect: (PMRootTab) -> Void

    private let items: [(PMRootTab, String, String)] = [
        (.home, "خانه", "house"),
        (.categories, "دسته‌بندی‌ها", "square.grid.2x2"),
        (.cart, "سبد خرید", "cart"),
        (.profile, "حساب کاربری", "person")
    ]

    var body: some View {
        HStack(spacing: 6) {
            ForEach(items, id: \.0) { item in
                let active = selected == item.0
                Button {
                    onSelect(item.0)
                } label: {
                    VStack(spacing: 3) {
                        ZStack(alignment: .topTrailing) {
                            Image(systemName: item.2)
                                .font(.system(size: 21, weight: active ? .bold : .medium))
                                .frame(width: 29, height: 25)

                            if item.0 == .cart && cartCount > 0 {
                                Text("\(min(cartCount, 99))")
                                    .font(.caption2.bold())
                                    .foregroundStyle(.black)
                                    .frame(minWidth: 18, minHeight: 18)
                                    .background(PMColor.goldDeep)
                                    .clipShape(Circle())
                                    .offset(x: 9, y: -7)
                            }
                        }

                        Text(item.1)
                            .font(.caption2.weight(active ? .bold : .semibold))
                            .lineLimit(1)
                    }
                    .foregroundStyle(active ? PMColor.gold : PMColor.secondary)
                    .frame(maxWidth: .infinity)
                    .frame(height: 56)
                    .background(active ? PMColor.pureBlack : Color.clear)
                    .clipShape(RoundedRectangle(cornerRadius: 17))
                    .overlay {
                        if active {
                            RoundedRectangle(cornerRadius: 17)
                                .stroke(PMColor.goldDeep.opacity(0.8), lineWidth: 1)
                        }
                    }
                    .shadow(
                        color: active ? PMColor.gold.opacity(0.30) : .clear,
                        radius: 8,
                        y: 3
                    )
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 8)
        .padding(.top, 7)
        .padding(.bottom, 3)
        .background(PMColor.surface)
        .overlay(alignment: .top) {
            Rectangle()
                .fill(PMColor.border)
                .frame(height: 1)
        }
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
            VStack(spacing: 14) {
                HStack {
                    Spacer()
                    VStack(alignment: .trailing, spacing: 3) {
                        Text("دسته‌بندی‌ها")
                            .font(.title2.bold())
                        Text("انتخاب گروه کالایی")
                            .font(.caption)
                            .foregroundStyle(PMColor.secondary)
                    }
                }
                .padding(.horizontal)
                .padding(.top, 12)

                if catalog.home.categories.isEmpty && catalog.loading {
                    ProgressView()
                        .tint(PMColor.goldDeep)
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
                                VStack(spacing: 0) {
                                    ProductImageView(url: category.image)
                                        .frame(maxWidth: .infinity)
                                        .aspectRatio(1.35, contentMode: .fill)
                                        .clipped()

                                    HStack(spacing: 8) {
                                        ZStack {
                                            Circle()
                                                .fill(PMColor.gold.opacity(0.22))
                                                .frame(width: 34, height: 34)
                                            Image(systemName: "chevron.left")
                                                .font(.caption.bold())
                                                .foregroundStyle(PMColor.goldDeep)
                                        }

                                        Spacer(minLength: 0)

                                        VStack(alignment: .trailing, spacing: 3) {
                                            Text(category.name)
                                                .font(.subheadline.bold())
                                                .foregroundStyle(.primary)
                                                .multilineTextAlignment(.trailing)
                                                .lineLimit(2)
                                            if category.count > 0 {
                                                Text("\(category.count) محصول")
                                                    .font(.caption2)
                                                    .foregroundStyle(PMColor.secondary)
                                            }
                                        }
                                    }
                                    .padding(10)
                                }
                                .background(PMColor.surface)
                                .clipShape(RoundedRectangle(cornerRadius: 20))
                                .overlay(
                                    RoundedRectangle(cornerRadius: 20)
                                        .stroke(PMColor.border, lineWidth: 1)
                                )
                                .shadow(color: .black.opacity(0.05), radius: 7, y: 3)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 12)
                }
            }
            .padding(.bottom, 18)
        }
        .background(PMColor.background)
        .task {
            if catalog.home.categories.isEmpty {
                await catalog.loadHome(force: true)
            }
        }
        .refreshable { await catalog.loadHome(force: true) }
    }
}


struct WishlistView: View {
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
