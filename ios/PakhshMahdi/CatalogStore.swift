import Foundation
import SwiftUI
import Security

@MainActor
final class CatalogStore: ObservableObject {
    @Published var home = HomePayload()
    @Published var products: [Product] = []
    @Published var categories: [Category] = []
    @Published var loading = false
    @Published var loadingMore = false
    @Published var error: String?
    @Published var query = ""

    private let api = APIClient()
    private var page = 1
    private var totalPages = 1

    func loadHome(force: Bool = false) async {
        if !force && !home.latestProducts.isEmpty { return }
        loading = true
        error = nil
        defer { loading = false }

        do {
            home = try await api.home()
            categories = home.categories
        } catch {
            self.error = error.localizedDescription
        }
    }

    func loadProducts(reset: Bool = false, category: Int? = nil) async {
        if loading || loadingMore { return }
        if reset {
            page = 1
            totalPages = 1
            products = []
        } else if page > totalPages {
            return
        }

        if products.isEmpty { loading = true } else { loadingMore = true }
        error = nil
        defer {
            loading = false
            loadingMore = false
        }

        do {
            let payload = try await api.products(
                page: page,
                perPage: 20,
                category: category,
                search: query.isEmpty ? nil : query
            )
            if page == 1 { products = payload.items }
            else { products.append(contentsOf: payload.items) }
            totalPages = max(1, payload.totalPages)
            page += 1
        } catch {
            self.error = error.localizedDescription
        }
    }

    func search(_ value: String, category: Int? = nil) async {
        query = value
        await loadProducts(reset: true, category: category)
    }

    func refreshProduct(_ id: Int) async throws -> Product {
        try await api.product(id: id)
    }
}

enum WholesalePolicy {
    static let minimumPerProduct = 6
    static let minimumOrderTotal = 15_000_000
}

@MainActor
final class CartStore: ObservableObject {
    struct Line: Codable, Identifiable, Hashable {
        let product: Product
        let variation: ProductVariation?
        var quantity: Int

        var id: String { "\(product.id):\(variation?.id ?? 0)" }
        var unitPrice: String {
            if let variation, !variation.price.isEmpty { return variation.price }
            return product.price
        }

        var maxStock: Int? {
            variation?.stockQuantity ?? (variation == nil ? product.stockQuantity : nil)
        }
    }

    @Published private(set) var lines: [Line] = [] {
        didSet { persist() }
    }

    private let storageKey = "pakhsh_mahdi_ios_cart_v1"

    init() {
        restore()
    }

    var totalQuantity: Int {
        lines.reduce(0) { $0 + $1.quantity }
    }

    var subtotal: Int {
        lines.reduce(0) { partial, line in
            partial + Int(Double(line.unitPrice) ?? 0) * line.quantity
        }
    }

    var wholesaleMessage: String? {
        guard !lines.isEmpty else { return "سبد خرید خالی است." }

        if let low = lines.first(where: { $0.quantity < WholesalePolicy.minimumPerProduct }) {
            let remaining = max(0, WholesalePolicy.minimumPerProduct - low.quantity)
            return "حداقل خرید هر کالا ۶ عدد است. برای «\(low.product.name)» \(remaining) عدد دیگر اضافه کنید."
        }

        if subtotal < WholesalePolicy.minimumOrderTotal {
            let remaining = WholesalePolicy.minimumOrderTotal - subtotal
            return "حداقل مبلغ کل سفارش ۱۵ میلیون تومان است. \(toman(String(remaining))) دیگر به سبد اضافه کنید."
        }

        return nil
    }

    var isWholesaleEligible: Bool {
        !lines.isEmpty && wholesaleMessage == nil
    }

    func add(_ product: Product, variation: ProductVariation? = nil, quantity: Int = WholesalePolicy.minimumPerProduct) {
        guard quantity > 0, product.purchasable, product.isInStock else { return }
        if (product.type == "variable" || !product.variations.isEmpty) && variation == nil { return }
        if let variation, !variation.isInStock { return }

        let id = "\(product.id):\(variation?.id ?? 0)"
        let maxStock = variation?.stockQuantity ?? (variation == nil ? product.stockQuantity : nil)
        if lines.firstIndex(where: { $0.id == id }) == nil,
           let maxStock,
           maxStock < WholesalePolicy.minimumPerProduct {
            return
        }

        if let index = lines.firstIndex(where: { $0.id == id }) {
            let requested = lines[index].quantity + quantity
            lines[index].quantity = maxStock.map { min(requested, max(0, $0)) } ?? requested
        } else {
            let safe = maxStock.map { min(quantity, max(0, $0)) } ?? quantity
            guard safe > 0 else { return }
            lines.append(Line(product: product, variation: variation, quantity: safe))
        }
    }

    func increment(_ id: String) {
        guard let index = lines.firstIndex(where: { $0.id == id }) else { return }
        let next = lines[index].quantity + 1
        if let maxStock = lines[index].maxStock, next > maxStock { return }
        lines[index].quantity = next
    }

    func decrement(_ id: String) {
        guard let index = lines.firstIndex(where: { $0.id == id }) else { return }
        guard lines[index].quantity > WholesalePolicy.minimumPerProduct else { return }
        lines[index].quantity -= 1
    }

    func remove(_ id: String) {
        lines.removeAll { $0.id == id }
    }

    func clear() {
        lines.removeAll()
    }

    func replaceAll(_ updated: [Line]) {
        lines = updated
    }

    private func restore() {
        guard
            let data = UserDefaults.standard.data(forKey: storageKey),
            let saved = try? JSONDecoder().decode([Line].self, from: data)
        else { return }

        lines = saved.compactMap { line in
            guard line.quantity > 0, line.product.purchasable, line.product.isInStock else { return nil }
            if let variation = line.variation, !variation.isInStock { return nil }
            if line.variation == nil &&
                (line.product.type == "variable" || !line.product.variations.isEmpty) {
                return nil
            }
            if let max = line.maxStock, max <= 0 { return nil }
            let safe = line.maxStock.map { min(line.quantity, $0) } ?? line.quantity
            return Line(product: line.product, variation: line.variation, quantity: safe)
        }
    }

    private func persist() {
        guard let data = try? JSONEncoder().encode(lines) else { return }
        UserDefaults.standard.set(data, forKey: storageKey)
    }
}


@MainActor
final class WishlistStore: ObservableObject {
    @Published private(set) var items: [Product] = [] {
        didSet { persist() }
    }

    private let storageKey = "pakhsh_mahdi_ios_wishlist_v1"
    private let api = APIClient()

    init() {
        guard
            let data = UserDefaults.standard.data(forKey: storageKey),
            let saved = try? JSONDecoder().decode([Product].self, from: data)
        else { return }
        items = saved
    }

    func contains(_ productID: Int) -> Bool {
        items.contains { $0.id == productID }
    }

    func toggle(_ product: Product) {
        if let index = items.firstIndex(where: { $0.id == product.id }) {
            items.remove(at: index)
        } else {
            items.insert(product, at: 0)
        }
    }

    func remove(_ productID: Int) {
        items.removeAll { $0.id == productID }
    }

    func refresh() async {
        guard !items.isEmpty else { return }
        var refreshed: [Product] = []
        for product in items {
            let latest = (try? await api.product(id: product.id)) ?? product
            refreshed.append(latest)
        }
        items = refreshed
    }

    private func persist() {
        guard let data = try? JSONEncoder().encode(items) else { return }
        UserDefaults.standard.set(data, forKey: storageKey)
    }
}

@MainActor
final class SessionStore: ObservableObject {
    @Published private(set) var token: String?
    @Published private(set) var user: AuthUser?
    @Published var loading = false
    @Published var error: String?

    private let api = APIClient()
    private let userAccount = "auth-user"

    init() {
        token = KeychainStore.loadString(account: "auth-token")
        if let data = KeychainStore.loadData(account: userAccount) {
            user = try? JSONDecoder().decode(AuthUser.self, from: data)
        } else if
            let legacy = UserDefaults.standard.data(forKey: "pakhsh_mahdi_ios_auth_user_v1"),
            let saved = try? JSONDecoder().decode(AuthUser.self, from: legacy)
        {
            user = saved
            if KeychainStore.saveData(legacy, account: userAccount) {
                UserDefaults.standard.removeObject(forKey: "pakhsh_mahdi_ios_auth_user_v1")
            }
        }

        if token == nil || user == nil {
            token = nil
            user = nil
        }
    }

    var isLoggedIn: Bool {
        token != nil && user != nil
    }

    func requestOTP(phone: String) async throws -> OTPRequestResponse {
        try await api.requestOTP(phone: phone)
    }

    func verifyOTP(phone: String, code: String) async throws {
        loading = true
        error = nil
        defer { loading = false }

        do {
            let response = try await api.verifyOTP(phone: phone, code: code)
            token = response.token
            user = response.user
            KeychainStore.saveString(response.token, account: "auth-token")
            persistUser(response.user)
        } catch {
            self.error = error.localizedDescription
            throw error
        }
    }

    func refresh() async {
        guard let token else { return }
        loading = true
        error = nil
        defer { loading = false }

        do {
            let response = try await api.me(token: token)
            user = response.user
            persistUser(response.user)
        } catch APIError.http(let status, _) where status == 401 || status == 403 {
            logout()
        } catch {
            self.error = error.localizedDescription
        }
    }

    func orders() async throws -> [OrderSummary] {
        guard let token else { return [] }
        return try await api.orders(token: token).items
    }

    func orderDetail(id: Int) async throws -> OrderDetail {
        guard let token else {
            throw APIError.http(401, "ورود به حساب کاربری لازم است.")
        }
        return try await api.orderDetail(token: token, id: id)
    }

    func updateProfile(firstName: String, lastName: String, email: String) async throws {
        guard let token else {
            throw APIError.http(401, "ورود به حساب کاربری لازم است.")
        }
        let response = try await api.updateMe(
            token: token,
            firstName: firstName,
            lastName: lastName,
            email: email
        )
        user = response.user
        persistUser(response.user)
    }

    func updateAddress(_ billing: BillingProfile) async throws {
        guard let token, let current = user else {
            throw APIError.http(401, "ورود به حساب کاربری لازم است.")
        }
        let response = try await api.updateAddress(token: token, billing: billing)
        let updated = AuthUser(
            id: current.id,
            phone: current.phone,
            firstName: current.firstName,
            lastName: current.lastName,
            email: current.email,
            displayName: current.displayName,
            billing: response.billing
        )
        user = updated
        persistUser(updated)
    }

    func logout() {
        token = nil
        user = nil
        error = nil
        KeychainStore.clear(account: "auth-token")
        KeychainStore.clear(account: userAccount)
        UserDefaults.standard.removeObject(forKey: "pakhsh_mahdi_ios_auth_user_v1")
    }

    private func persistUser(_ user: AuthUser) {
        guard let data = try? JSONEncoder().encode(user) else { return }
        _ = KeychainStore.saveData(data, account: userAccount)
    }
}

enum KeychainStore {
    private static var service: String { AppConfig.keychainService }

    @discardableResult
    static func saveString(_ value: String, account: String) -> Bool {
        saveData(Data(value.utf8), account: account)
    }

    static func loadString(account: String) -> String? {
        loadData(account: account).flatMap { String(data: $0, encoding: .utf8) }
    }

    @discardableResult
    static func saveData(_ data: Data, account: String) -> Bool {
        clear(account: account)
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
            kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
            kSecValueData as String: data
        ]
        return SecItemAdd(query as CFDictionary, nil) == errSecSuccess
    }

    static func loadData(account: String) -> Data? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        var item: CFTypeRef?
        guard SecItemCopyMatching(query as CFDictionary, &item) == errSecSuccess else {
            return nil
        }
        return item as? Data
    }

    static func clear(account: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account
        ]
        SecItemDelete(query as CFDictionary)
    }
}


private struct IOSPendingPayment: Codable {
    let orderId: Int
    let paymentURL: String
    let status: String
}

@MainActor
final class CheckoutStore: ObservableObject {
    @Published var cart: StoreCart?
    @Published var loading = false
    @Published var submitting = false
    @Published var error: String?
    @Published var orderId: Int?
    @Published var paymentURL: String?

    private let storeAPI = StoreAPIClient()
    private let catalogAPI = APIClient()
    private let cartTokenAccount = "store-cart-token"
    private let pendingPaymentAccount = "pending-payment"

    init() {
        if
            let data = KeychainStore.loadData(account: pendingPaymentAccount),
            let pending = try? JSONDecoder().decode(IOSPendingPayment.self, from: data)
        {
            orderId = pending.orderId
            paymentURL = pending.paymentURL
        }
    }

    func prepare(localCart: CartStore) async {
        guard !localCart.lines.isEmpty, !loading else { return }
        loading = true
        error = nil
        defer { loading = false }

        do {
            let refreshed = try await refreshLines(localCart.lines)
            localCart.replaceAll(refreshed)

            let token = try await ensureToken()
            try await storeAPI.clearCart(token: token)

            var serverCart: StoreCart?
            for line in refreshed {
                let itemID = line.variation?.id ?? line.product.id
                serverCart = try await storeAPI.addItem(
                    token: token,
                    id: itemID,
                    quantity: line.quantity
                )
            }

            if serverCart == nil {
                serverCart = try await storeAPI.cart(token: token).0
            }
            guard let serverCart, serverCart.itemsCount > 0 else {
                throw APIError.http(0, "سبد خرید در ووکامرس همگام نشد.")
            }
            cart = serverCart
        } catch {
            self.error = error.localizedDescription
        }
    }

    func updateAddress(_ address: StoreAddress) async {
        guard !loading else { return }
        loading = true
        error = nil
        defer { loading = false }

        do {
            let token = try await ensureToken()
            var updated = try await storeAPI.updateCustomer(token: token, address: address)

            if updated.needsShipping && !updated.hasShippingRates {
                updated = try await storeAPI.cart(token: token).0
            }

            guard !updated.needsShipping || updated.hasShippingRates else {
                throw APIError.http(
                    0,
                    "روش ارسال برای این آدرس دریافت نشد. استان، شهر و کدپستی را بررسی کنید."
                )
            }

            cart = updated
        } catch {
            self.error = error.localizedDescription
        }
    }

    func selectShipping(packageID: Int, rateID: String) async {
        guard !loading else { return }
        loading = true
        error = nil
        defer { loading = false }

        do {
            let token = try await ensureToken()
            cart = try await storeAPI.selectShipping(
                token: token,
                packageId: packageID,
                rateId: rateID
            )
        } catch {
            self.error = error.localizedDescription
        }
    }

    func submit(
        address: StoreAddress,
        paymentMethod: String,
        note: String,
        session: SessionStore,
        localCart: CartStore
    ) async {
        guard let cart, cart.itemsCount > 0, !submitting else { return }
        guard !paymentMethod.isEmpty else {
            error = "روش پرداخت را انتخاب کنید."
            return
        }

        submitting = true
        error = nil
        defer { submitting = false }

        do {
            let token = try await ensureToken()
            let result = try await storeAPI.checkout(
                token: token,
                address: address,
                paymentMethod: paymentMethod,
                expectedTotal: cart.totals.totalPrice,
                customerNote: note
            )

            if result.orderId > 0,
               let authToken = session.token,
               !result.orderKey.isEmpty {
                _ = try? await catalogAPI.claimOrder(
                    token: authToken,
                    orderId: result.orderId,
                    orderKey: result.orderKey
                )
            }

            orderId = result.orderId > 0 ? result.orderId : nil
            paymentURL = result.paymentResult?.redirectURL

            if let paymentURL, result.orderId > 0 {
                persistPendingPayment(
                    orderId: result.orderId,
                    paymentURL: paymentURL,
                    status: result.status
                )
            } else if result.orderId > 0 {
                clearPendingPayment()
                localCart.clear()
                clearCartToken()
            }
        } catch {
            self.error = error.localizedDescription
        }
    }

    func resetResult() {
        orderId = nil
        paymentURL = nil
        clearPendingPayment()
    }

    func verifyPendingPayment(
        session: SessionStore,
        localCart: CartStore
    ) async {
        guard let orderId, session.isLoggedIn else { return }

        do {
            let detail = try await session.orderDetail(id: orderId)
            let normalized = detail.status
                .trimmingCharacters(in: .whitespacesAndNewlines)
                .lowercased()
                .replacingOccurrences(of: "wc-", with: "")

            if normalized == "processing" || normalized == "completed" {
                clearPendingPayment()
                paymentURL = nil
                localCart.clear()
                clearCartToken()
            } else if
                let paymentURL,
                let data = try? JSONEncoder().encode(
                    IOSPendingPayment(
                        orderId: orderId,
                        paymentURL: paymentURL,
                        status: normalized
                    )
                )
            {
                _ = KeychainStore.saveData(data, account: pendingPaymentAccount)
            }
        } catch APIError.http(let status, _) where status == 401 || status == 403 {
            return
        } catch {
            self.error = error.localizedDescription
        }
    }

    private func persistPendingPayment(
        orderId: Int,
        paymentURL: String,
        status: String
    ) {
        guard let data = try? JSONEncoder().encode(
            IOSPendingPayment(
                orderId: orderId,
                paymentURL: paymentURL,
                status: status
            )
        ) else { return }
        _ = KeychainStore.saveData(data, account: pendingPaymentAccount)
    }

    private func clearPendingPayment() {
        KeychainStore.clear(account: pendingPaymentAccount)
    }

    private func refreshLines(_ lines: [CartStore.Line]) async throws -> [CartStore.Line] {
        var refreshed: [CartStore.Line] = []
        for line in lines {
            let product = try await catalogAPI.product(id: line.product.id)
            guard product.purchasable, product.isInStock else {
                throw APIError.http(0, "«\(product.name)» در حال حاضر قابل سفارش نیست.")
            }

            let variation = line.variation.flatMap { previous in
                product.variations.first { $0.id == previous.id }
            }
            if line.variation != nil, variation == nil {
                throw APIError.http(0, "گزینه انتخاب‌شده «\(product.name)» دیگر موجود نیست.")
            }
            if let variation, !variation.isInStock {
                throw APIError.http(0, "گزینه انتخاب‌شده «\(product.name)» ناموجود شده است.")
            }

            let maxStock = variation?.stockQuantity ?? (variation == nil ? product.stockQuantity : nil)
            if let maxStock, maxStock <= 0 {
                throw APIError.http(0, "«\(product.name)» ناموجود شده است.")
            }
            let quantity = maxStock.map { min(line.quantity, $0) } ?? line.quantity
            refreshed.append(.init(product: product, variation: variation, quantity: quantity))
        }
        return refreshed
    }

    private func ensureToken() async throws -> String {
        var stored = KeychainStore.loadString(account: cartTokenAccount)
        if stored == nil,
           let legacy = UserDefaults.standard.string(forKey: "pakhsh_mahdi_ios_store_cart_token_v1"),
           !legacy.isEmpty {
            _ = KeychainStore.saveString(legacy, account: cartTokenAccount)
            UserDefaults.standard.removeObject(forKey: "pakhsh_mahdi_ios_store_cart_token_v1")
            stored = legacy
        }

        if let stored, !stored.isEmpty,
           let result = try? await storeAPI.cart(token: stored) {
            let token = result.1 ?? stored
            saveCartToken(token)
            return token
        }

        let result = try await storeAPI.cart(token: nil)
        guard let token = result.1, !token.isEmpty else {
            throw APIError.http(0, "توکن امن سبد خرید از ووکامرس دریافت نشد.")
        }
        saveCartToken(token)
        return token
    }

    private func saveCartToken(_ token: String) {
        _ = KeychainStore.saveString(token, account: cartTokenAccount)
    }

    private func clearCartToken() {
        KeychainStore.clear(account: cartTokenAccount)
        UserDefaults.standard.removeObject(forKey: "pakhsh_mahdi_ios_store_cart_token_v1")
    }
}
