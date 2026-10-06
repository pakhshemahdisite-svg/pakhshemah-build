import Foundation

enum APIError: LocalizedError {
    case invalidURL
    case http(Int, String)
    case decoding

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "آدرس سرویس معتبر نیست."
        case .http(_, let message):
            return message.isEmpty ? "ارتباط با فروشگاه انجام نشد." : message
        case .decoding:
            return "پاسخ فروشگاه قابل پردازش نبود."
        }
    }
}

struct APIClient {
    private let session: URLSession

    init() {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 30
        configuration.timeoutIntervalForResource = 60
        configuration.waitsForConnectivity = true
        session = URLSession(configuration: configuration)
    }

    func home() async throws -> HomePayload {
        try await get("home")
    }

    func categories() async throws -> CategoriesPayload {
        try await get("categories")
    }

    func products(
        page: Int = 1,
        perPage: Int = 20,
        category: Int? = nil,
        search: String? = nil,
        orderBy: String = "date",
        order: String = "DESC"
    ) async throws -> ProductsPayload {
        var items = [
            URLQueryItem(name: "page", value: String(max(1, page))),
            URLQueryItem(name: "per_page", value: String(min(50, max(1, perPage)))),
            URLQueryItem(name: "orderby", value: orderBy),
            URLQueryItem(name: "order", value: order)
        ]
        if let category { items.append(URLQueryItem(name: "category", value: String(category))) }
        if let search, !search.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            items.append(URLQueryItem(name: "search", value: search))
        }
        return try await get("products", queryItems: items)
    }

    func product(id: Int) async throws -> Product {
        try await get("products/\(id)")
    }

    func requestOTP(phone: String) async throws -> OTPRequestResponse {
        try await post("auth/request-otp", body: ["phone": phone])
    }

    func verifyOTP(phone: String, code: String) async throws -> AuthResponse {
        try await post("auth/verify-otp", body: ["phone": phone, "code": code])
    }

    func me(token: String) async throws -> UserResponse {
        try await get("auth/me", bearerToken: token)
    }

    func updateMe(
        token: String,
        firstName: String,
        lastName: String,
        email: String
    ) async throws -> UserResponse {
        try await post(
            "auth/me",
            body: UpdateProfileRequest(
                firstName: firstName,
                lastName: lastName,
                email: email
            ),
            bearerToken: token
        )
    }

    func updateAddress(
        token: String,
        billing: BillingProfile
    ) async throws -> BillingResponse {
        try await post(
            "auth/address",
            body: billing,
            bearerToken: token
        )
    }

    func orders(token: String) async throws -> OrdersPayload {
        try await get("auth/orders", bearerToken: token)
    }

    func orderDetail(token: String, id: Int) async throws -> OrderDetail {
        try await get("auth/orders/\(id)", bearerToken: token)
    }

    func claimOrder(token: String, orderId: Int, orderKey: String) async throws -> ClaimOrderResponse {
        try await post(
            "auth/orders/claim",
            body: ClaimOrderRequest(orderId: orderId, orderKey: orderKey),
            bearerToken: token
        )
    }

    private func get<T: Decodable>(
        _ path: String,
        queryItems: [URLQueryItem] = [],
        bearerToken: String? = nil
    ) async throws -> T {
        guard var components = URLComponents(
            url: AppConfig.baseURL.appendingPathComponent(path),
            resolvingAgainstBaseURL: true
        ) else {
            throw APIError.invalidURL
        }
        if !queryItems.isEmpty { components.queryItems = queryItems }
        guard let url = components.url else { throw APIError.invalidURL }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let bearerToken, !bearerToken.isEmpty {
            request.setValue("Bearer \(bearerToken)", forHTTPHeaderField: "Authorization")
        }
        return try await send(request)
    }

    private func post<T: Decodable, Body: Encodable>(
        _ path: String,
        body: Body,
        bearerToken: String? = nil
    ) async throws -> T {
        let url = AppConfig.baseURL.appendingPathComponent(path)
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if let bearerToken, !bearerToken.isEmpty {
            request.setValue("Bearer \(bearerToken)", forHTTPHeaderField: "Authorization")
        }
        request.httpBody = try JSONEncoder().encode(body)
        return try await send(request)
    }

    private func send<T: Decodable>(_ request: URLRequest) async throws -> T {
        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse else {
            throw APIError.http(0, "پاسخ معتبری از فروشگاه دریافت نشد.")
        }
        guard 200..<300 ~= http.statusCode else {
            let message = (try? JSONSerialization.jsonObject(with: data) as? [String: Any])?["message"] as? String ?? ""
            throw APIError.http(http.statusCode, message)
        }

        do {
            return try JSONDecoder().decode(T.self, from: data)
        } catch {
            throw APIError.decoding
        }
    }
}


struct StoreAPIClient {
    private let session: URLSession

    init() {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 30
        configuration.timeoutIntervalForResource = 60
        configuration.waitsForConnectivity = true
        session = URLSession(configuration: configuration)
    }

    func cart(token: String?) async throws -> (StoreCart, String?) {
        var request = URLRequest(url: AppConfig.storeURL.appendingPathComponent("cart"))
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let token, !token.isEmpty {
            request.setValue(token, forHTTPHeaderField: "Cart-Token")
        }
        return try await send(request, captureCartToken: true)
    }

    func clearCart(token: String) async throws {
        var request = URLRequest(url: AppConfig.storeURL.appendingPathComponent("cart/items"))
        request.httpMethod = "DELETE"
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.setValue(token, forHTTPHeaderField: "Cart-Token")
        _ = try await sendRaw(request)
    }

    func addItem(token: String, id: Int, quantity: Int) async throws -> StoreCart {
        try await post(
            "cart/add-item",
            token: token,
            body: AddItemRequest(id: id, quantity: quantity)
        )
    }

    func updateCustomer(token: String, address: StoreAddress) async throws -> StoreCart {
        try await post(
            "cart/update-customer",
            token: token,
            body: UpdateCustomerRequest(
                billingAddress: address,
                shippingAddress: address.shipping
            )
        )
    }

    func selectShipping(token: String, packageId: Int, rateId: String) async throws -> StoreCart {
        try await post(
            "cart/select-shipping-rate",
            token: token,
            body: SelectShippingRateRequest(packageId: packageId, rateId: rateId)
        )
    }

    func checkout(
        token: String,
        address: StoreAddress,
        paymentMethod: String,
        expectedTotal: String,
        customerNote: String
    ) async throws -> CheckoutResponse {
        try await post(
            "checkout",
            token: token,
            body: CheckoutRequest(
                billingAddress: address,
                shippingAddress: address.shipping,
                paymentMethod: paymentMethod,
                paymentData: [],
                expectedTotal: expectedTotal,
                customerNote: customerNote,
                createAccount: false
            )
        )
    }

    private func post<T: Decodable, Body: Encodable>(
        _ path: String,
        token: String,
        body: Body
    ) async throws -> T {
        var request = URLRequest(url: AppConfig.storeURL.appendingPathComponent(path))
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(token, forHTTPHeaderField: "Cart-Token")
        request.httpBody = try JSONEncoder().encode(body)
        let (result, _): (T, String?) = try await send(request)
        return result
    }

    private func send<T: Decodable>(
        _ request: URLRequest,
        captureCartToken: Bool = false
    ) async throws -> (T, String?) {
        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse else {
            throw APIError.http(0, "پاسخ معتبری از ووکامرس دریافت نشد.")
        }
        guard 200..<300 ~= http.statusCode else {
            let message = (try? JSONSerialization.jsonObject(with: data) as? [String: Any])?["message"] as? String ?? ""
            throw APIError.http(http.statusCode, message)
        }

        do {
            let decoded = try JSONDecoder().decode(T.self, from: data)
            let token = captureCartToken ? http.value(forHTTPHeaderField: "Cart-Token") : nil
            return (decoded, token)
        } catch {
            throw APIError.decoding
        }
    }

    private func sendRaw(_ request: URLRequest) async throws -> Data {
        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse, 200..<300 ~= http.statusCode else {
            let code = (response as? HTTPURLResponse)?.statusCode ?? 0
            throw APIError.http(code, "عملیات سبد خرید انجام نشد.")
        }
        return data
    }
}


struct PwsCityClient {
    func cities(stateID: String) async throws -> [PwsCity] {
        guard !stateID.isEmpty else { return [] }
        let url = AppConfig.pwsAjaxURL
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(
            "application/x-www-form-urlencoded; charset=utf-8",
            forHTTPHeaderField: "Content-Type"
        )

        var components = URLComponents()
        components.queryItems = [
            URLQueryItem(name: "action", value: "mahdiy_load_cities"),
            URLQueryItem(name: "state_id", value: stateID),
            URLQueryItem(name: "type", value: "billing")
        ]
        request.httpBody = components.percentEncodedQuery?.data(using: .utf8)

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse, 200..<300 ~= http.statusCode else {
            throw APIError.http(
                (response as? HTTPURLResponse)?.statusCode ?? 0,
                "دریافت فهرست شهرها انجام نشد."
            )
        }

        let html = String(decoding: data, as: UTF8.self)
        return parseOptions(html)
    }

    private func parseOptions(_ html: String) -> [PwsCity] {
        guard let regex = try? NSRegularExpression(
            pattern: #"<option\s+[^>]*value=['\"](\d+)['\"][^>]*>(.*?)</option>"#,
            options: [.caseInsensitive, .dotMatchesLineSeparators]
        ) else { return [] }

        let ns = html as NSString
        let range = NSRange(location: 0, length: ns.length)
        var seen = Set<String>()
        return regex.matches(in: html, range: range).compactMap { match in
            guard match.numberOfRanges >= 3 else { return nil }
            let id = ns.substring(with: match.range(at: 1))
            guard id != "0", !seen.contains(id) else { return nil }

            var name = ns.substring(with: match.range(at: 2))
            name = name.replacingOccurrences(
                of: "<[^>]+>",
                with: "",
                options: .regularExpression
            )
            name = name
                .replacingOccurrences(of: "&nbsp;", with: " ")
                .replacingOccurrences(of: "&amp;", with: "&")
                .replacingOccurrences(of: "&#039;", with: "'")
                .trimmingCharacters(in: .whitespacesAndNewlines)

            guard !name.isEmpty else { return nil }
            seen.insert(id)
            return PwsCity(id: id, name: name)
        }
    }
}
