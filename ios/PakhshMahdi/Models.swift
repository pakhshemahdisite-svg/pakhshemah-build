import Foundation

struct Category: Codable, Identifiable, Hashable {
    let id: Int
    let name: String
    let slug: String
    let image: String?
    let count: Int

    init(id: Int, name: String, slug: String = "", image: String? = nil, count: Int = 0) {
        self.id = id
        self.name = name
        self.slug = slug
        self.image = image
        self.count = count
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try c.decode(Int.self, forKey: .id)
        name = try c.decode(String.self, forKey: .name)
        slug = try c.decodeIfPresent(String.self, forKey: .slug) ?? ""
        image = try c.decodeIfPresent(String.self, forKey: .image)
        count = try c.decodeIfPresent(Int.self, forKey: .count) ?? 0
    }
}

struct ProductAttribute: Codable, Hashable {
    let name: String
    let slug: String
    let options: [String]
    let variation: Bool

    init(name: String = "", slug: String = "", options: [String] = [], variation: Bool = false) {
        self.name = name
        self.slug = slug
        self.options = options
        self.variation = variation
    }
}

struct ProductVariation: Codable, Identifiable, Hashable {
    let id: Int
    let price: String
    let regularPrice: String
    let salePrice: String
    let stockStatus: String
    let stockQuantity: Int?
    let attributes: [String: String]
    let image: String?

    init(
        id: Int,
        price: String = "",
        regularPrice: String = "",
        salePrice: String = "",
        stockStatus: String = "instock",
        stockQuantity: Int? = nil,
        attributes: [String: String] = [:],
        image: String? = nil
    ) {
        self.id = id
        self.price = price
        self.regularPrice = regularPrice
        self.salePrice = salePrice
        self.stockStatus = stockStatus
        self.stockQuantity = stockQuantity
        self.attributes = attributes
        self.image = image
    }

    var isInStock: Bool {
        stockStatus == "instock" && (stockQuantity == nil || stockQuantity! > 0)
    }
}

struct Product: Codable, Identifiable, Hashable {
    let id: Int
    let name: String
    let slug: String
    let type: String
    let price: String
    let regularPrice: String
    let salePrice: String
    let onSale: Bool
    let purchasable: Bool
    let stockStatus: String
    let stockQuantity: Int?
    let image: String?
    let gallery: [String]
    let shortDescription: String
    let description: String
    let averageRating: String
    let ratingCount: Int
    let categories: [Category]
    let attributes: [ProductAttribute]
    let variations: [ProductVariation]

    init(
        id: Int,
        name: String,
        slug: String = "",
        type: String = "simple",
        price: String,
        regularPrice: String = "",
        salePrice: String = "",
        onSale: Bool = false,
        purchasable: Bool = true,
        stockStatus: String = "instock",
        stockQuantity: Int? = nil,
        image: String? = nil,
        gallery: [String] = [],
        shortDescription: String = "",
        description: String = "",
        averageRating: String = "0",
        ratingCount: Int = 0,
        categories: [Category] = [],
        attributes: [ProductAttribute] = [],
        variations: [ProductVariation] = []
    ) {
        self.id = id
        self.name = name
        self.slug = slug
        self.type = type
        self.price = price
        self.regularPrice = regularPrice
        self.salePrice = salePrice
        self.onSale = onSale
        self.purchasable = purchasable
        self.stockStatus = stockStatus
        self.stockQuantity = stockQuantity
        self.image = image
        self.gallery = gallery
        self.shortDescription = shortDescription
        self.description = description
        self.averageRating = averageRating
        self.ratingCount = ratingCount
        self.categories = categories
        self.attributes = attributes
        self.variations = variations
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try c.decode(Int.self, forKey: .id)
        name = try c.decode(String.self, forKey: .name)
        slug = try c.decodeIfPresent(String.self, forKey: .slug) ?? ""
        type = try c.decodeIfPresent(String.self, forKey: .type) ?? "simple"
        price = try c.decodeIfPresent(String.self, forKey: .price) ?? "0"
        regularPrice = try c.decodeIfPresent(String.self, forKey: .regularPrice) ?? ""
        salePrice = try c.decodeIfPresent(String.self, forKey: .salePrice) ?? ""
        onSale = try c.decodeIfPresent(Bool.self, forKey: .onSale) ?? false
        purchasable = try c.decodeIfPresent(Bool.self, forKey: .purchasable) ?? true
        stockStatus = try c.decodeIfPresent(String.self, forKey: .stockStatus) ?? "instock"
        stockQuantity = try c.decodeIfPresent(Int.self, forKey: .stockQuantity)
        image = try c.decodeIfPresent(String.self, forKey: .image)
        gallery = try c.decodeIfPresent([String].self, forKey: .gallery) ?? []
        shortDescription = try c.decodeIfPresent(String.self, forKey: .shortDescription) ?? ""
        description = try c.decodeIfPresent(String.self, forKey: .description) ?? ""
        averageRating = try c.decodeIfPresent(String.self, forKey: .averageRating) ?? "0"
        ratingCount = try c.decodeIfPresent(Int.self, forKey: .ratingCount) ?? 0
        categories = try c.decodeIfPresent([Category].self, forKey: .categories) ?? []
        attributes = try c.decodeIfPresent([ProductAttribute].self, forKey: .attributes) ?? []
        variations = try c.decodeIfPresent([ProductVariation].self, forKey: .variations) ?? []
    }

    var isInStock: Bool {
        stockStatus == "instock" && (stockQuantity == nil || stockQuantity! > 0)
    }

    var hasSale: Bool {
        onSale || (!salePrice.isEmpty && salePrice != regularPrice)
    }
}

struct HomePayload: Codable {
    let categories: [Category]
    let latestProducts: [Product]
    let featuredProducts: [Product]
    let onSaleProducts: [Product]

    init(
        categories: [Category] = [],
        latestProducts: [Product] = [],
        featuredProducts: [Product] = [],
        onSaleProducts: [Product] = []
    ) {
        self.categories = categories
        self.latestProducts = latestProducts
        self.featuredProducts = featuredProducts
        self.onSaleProducts = onSaleProducts
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        categories = try c.decodeIfPresent([Category].self, forKey: .categories) ?? []
        latestProducts = try c.decodeIfPresent([Product].self, forKey: .latestProducts) ?? []
        featuredProducts = try c.decodeIfPresent([Product].self, forKey: .featuredProducts) ?? []
        onSaleProducts = try c.decodeIfPresent([Product].self, forKey: .onSaleProducts) ?? []
    }
}

struct ProductsPayload: Codable {
    let items: [Product]
    let page: Int
    let total: Int
    let totalPages: Int
}

struct CategoriesPayload: Codable {
    let items: [Category]
}

struct OTPRequestResponse: Codable {
    let success: Bool
    let expiresIn: Int
    let resendAfter: Int
}

struct BillingProfile: Codable, Hashable {
    let firstName: String
    let lastName: String
    let address1: String
    let address2: String
    let city: String
    let state: String
    let postcode: String
    let country: String
    let phone: String
    let email: String
}

struct AuthUser: Codable, Identifiable, Hashable {
    let id: Int
    let phone: String
    let firstName: String
    let lastName: String
    let email: String
    let displayName: String
    let billing: BillingProfile

    var fullName: String {
        let joined = [firstName, lastName].filter { !$0.isEmpty }.joined(separator: " ")
        return joined.isEmpty ? (displayName.isEmpty ? "کاربر \(AppConfig.appName)" : displayName) : joined
    }
}

struct AuthResponse: Codable {
    let token: String
    let user: AuthUser
}

struct UserResponse: Codable {
    let user: AuthUser
}

struct UpdateProfileRequest: Codable {
    let firstName: String
    let lastName: String
    let email: String
}

struct BillingResponse: Codable {
    let billing: BillingProfile
}

struct OrderSummary: Codable, Identifiable, Hashable {
    let id: Int
    let status: String
    let date: String
    let total: String
    let currency: String
    let itemCount: Int
    let paymentMethod: String
}

struct OrdersPayload: Codable {
    let items: [OrderSummary]
}


struct StoreTotals: Codable {
    let totalItems: String
    let totalDiscount: String
    let totalShipping: String?
    let totalPrice: String
    let currencySymbol: String

    enum CodingKeys: String, CodingKey {
        case totalItems = "total_items"
        case totalDiscount = "total_discount"
        case totalShipping = "total_shipping"
        case totalPrice = "total_price"
        case currencySymbol = "currency_symbol"
    }
}

struct StoreShippingRate: Codable, Identifiable, Hashable {
    let rateId: String
    let name: String
    let price: String
    let selected: Bool

    var id: String { rateId }

    enum CodingKeys: String, CodingKey {
        case rateId = "rate_id"
        case name, price, selected
    }
}

struct StoreShippingPackage: Codable, Hashable {
    let packageId: Int
    let name: String
    let shippingRates: [StoreShippingRate]

    enum CodingKeys: String, CodingKey {
        case packageId = "package_id"
        case name
        case shippingRates = "shipping_rates"
    }
}

struct StoreCoupon: Codable, Hashable {
    let code: String
    let discountType: String

    enum CodingKeys: String, CodingKey {
        case code
        case discountType = "discount_type"
    }
}

struct StoreAPIError: Codable {
    let code: String
    let message: String
}

struct StoreCart: Codable {
    let itemsCount: Int
    let coupons: [StoreCoupon]
    let totals: StoreTotals
    let shippingRates: [StoreShippingPackage]
    let paymentMethods: [String]
    let needsPayment: Bool
    let needsShipping: Bool
    let errors: [StoreAPIError]

    enum CodingKeys: String, CodingKey {
        case itemsCount = "items_count"
        case coupons, totals
        case shippingRates = "shipping_rates"
        case paymentMethods = "payment_methods"
        case needsPayment = "needs_payment"
        case needsShipping = "needs_shipping"
        case errors
    }

    var hasShippingRates: Bool {
        shippingRates.contains { !$0.shippingRates.isEmpty }
    }
}

struct StoreAddress: Codable, Hashable {
    let firstName: String
    let lastName: String
    let company: String
    let address1: String
    let address2: String
    let city: String
    let state: String
    let postcode: String
    let country: String
    let phone: String
    let email: String

    enum CodingKeys: String, CodingKey {
        case firstName = "first_name"
        case lastName = "last_name"
        case company
        case address1 = "address_1"
        case address2 = "address_2"
        case city, state, postcode, country, phone, email
    }
}

struct StoreShippingAddress: Codable, Hashable {
    let firstName: String
    let lastName: String
    let company: String
    let address1: String
    let address2: String
    let city: String
    let state: String
    let postcode: String
    let country: String
    let phone: String

    enum CodingKeys: String, CodingKey {
        case firstName = "first_name"
        case lastName = "last_name"
        case company
        case address1 = "address_1"
        case address2 = "address_2"
        case city, state, postcode, country, phone
    }
}

extension StoreAddress {
    var shipping: StoreShippingAddress {
        StoreShippingAddress(
            firstName: firstName,
            lastName: lastName,
            company: company,
            address1: address1,
            address2: address2,
            city: city,
            state: state,
            postcode: postcode,
            country: country,
            phone: phone
        )
    }
}

struct UpdateCustomerRequest: Codable {
    let billingAddress: StoreAddress
    let shippingAddress: StoreShippingAddress

    enum CodingKeys: String, CodingKey {
        case billingAddress = "billing_address"
        case shippingAddress = "shipping_address"
    }
}

struct AddItemRequest: Codable {
    let id: Int
    let quantity: Int
}

struct SelectShippingRateRequest: Codable {
    let packageId: Int
    let rateId: String

    enum CodingKeys: String, CodingKey {
        case packageId = "package_id"
        case rateId = "rate_id"
    }
}

struct PaymentDataItem: Codable {
    let key: String
    let value: String
}

struct CheckoutRequest: Codable {
    let billingAddress: StoreAddress
    let shippingAddress: StoreShippingAddress
    let paymentMethod: String
    let paymentData: [PaymentDataItem]
    let expectedTotal: String
    let customerNote: String
    let createAccount: Bool

    enum CodingKeys: String, CodingKey {
        case billingAddress = "billing_address"
        case shippingAddress = "shipping_address"
        case paymentMethod = "payment_method"
        case paymentData = "payment_data"
        case expectedTotal = "expected_total"
        case customerNote = "customer_note"
        case createAccount = "create_account"
    }
}

struct PaymentResult: Codable {
    let paymentStatus: String?
    let redirectURL: String?

    enum CodingKeys: String, CodingKey {
        case paymentStatus = "payment_status"
        case redirectURL = "redirect_url"
    }
}

struct CheckoutResponse: Codable {
    let orderId: Int
    let status: String
    let orderKey: String
    let paymentResult: PaymentResult?

    enum CodingKeys: String, CodingKey {
        case orderId = "order_id"
        case status
        case orderKey = "order_key"
        case paymentResult = "payment_result"
    }
}


struct PwsCity: Identifiable, Hashable {
    let id: String
    let name: String
}

struct IranProvince: Identifiable, Hashable {
    let id: String
    let name: String
}

let iranProvinces: [IranProvince] = [
    .init(id: "351", name: "آذربایجان شرقی"),
    .init(id: "1081", name: "چهار محال بختیاری"),
    .init(id: "300", name: "آذربایجان غربی"),
    .init(id: "978", name: "اردبیل"),
    .init(id: "423", name: "اصفهان"),
    .init(id: "538", name: "البرز"),
    .init(id: "1013", name: "ایلام"),
    .init(id: "1041", name: "بوشهر"),
    .init(id: "558", name: "تهران"),
    .init(id: "1150", name: "خراسان جنوبی"),
    .init(id: "609", name: "خراسان رضوی"),
    .init(id: "1125", name: "خراسان شمالی"),
    .init(id: "687", name: "خوزستان"),
    .init(id: "956", name: "زنجان"),
    .init(id: "1182", name: "سمنان"),
    .init(id: "1203", name: "سیستان و بلوچستان"),
    .init(id: "775", name: "فارس"),
    .init(id: "1254", name: "قزوین"),
    .init(id: "1281", name: "قم"),
    .init(id: "1288", name: "کردستان"),
    .init(id: "1321", name: "کرمان"),
    .init(id: "1399", name: "کرمانشاه"),
    .init(id: "1435", name: "کهگیویه و بویراحمد"),
    .init(id: "1456", name: "گلستان"),
    .init(id: "1491", name: "گیلان"),
    .init(id: "1547", name: "لرستان"),
    .init(id: "892", name: "مازندران"),
    .init(id: "1579", name: "مرکزی"),
    .init(id: "1618", name: "هرمزگان"),
    .init(id: "1665", name: "همدان"),
    .init(id: "1700", name: "یزد")
]

struct ClaimOrderRequest: Codable {
    let orderId: Int
    let orderKey: String
}

struct ClaimOrderResponse: Codable {
    let success: Bool
    let orderId: Int
}


struct OrderLineItem: Codable, Identifiable, Hashable {
    let id: Int
    let productId: Int
    let variationId: Int
    let name: String
    let quantity: Int
    let total: String
    let image: String?

    enum CodingKeys: String, CodingKey {
        case id
        case productId = "productId"
        case variationId = "variationId"
        case name, quantity, total, image
    }
}

struct OrderDetail: Codable, Identifiable, Hashable {
    let id: Int
    let status: String
    let date: String
    let total: String
    let shippingTotal: String
    let discountTotal: String
    let paymentMethod: String
    let shippingMethod: String
    let items: [OrderLineItem]

    enum CodingKeys: String, CodingKey {
        case id, status, date, total, items
        case shippingTotal = "shippingTotal"
        case discountTotal = "discountTotal"
        case paymentMethod = "paymentMethod"
        case shippingMethod = "shippingMethod"
    }
}
