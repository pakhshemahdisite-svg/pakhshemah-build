import Foundation

enum AppConfig {
    private static func info(_ key: String, fallback: String) -> String {
        let value = Bundle.main.object(forInfoDictionaryKey: key) as? String
        let trimmed = value?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return trimmed.isEmpty ? fallback : trimmed
    }

    private static func url(_ key: String, fallback: String) -> URL {
        URL(string: info(key, fallback: fallback))!
    }

    private static func bool(_ key: String, fallback: Bool) -> Bool {
        guard let raw = Bundle.main.object(forInfoDictionaryKey: key) else {
            return fallback
        }
        if let value = raw as? Bool { return value }
        if let value = raw as? NSNumber { return value.boolValue }
        if let value = raw as? String {
            switch value.lowercased() {
            case "true", "yes", "1": return true
            case "false", "no", "0": return false
            default: return fallback
            }
        }
        return fallback
    }

    static let clientId = info("WLClientId", fallback: "pakhsh-mahdi")
    static let appName = info(
        "WLAppName",
        fallback: Bundle.main.object(forInfoDictionaryKey: "CFBundleDisplayName") as? String ?? "پخش مهدی"
    )
    static let appSubtitle = info(
        "WLAppSubtitle",
        fallback: "عمده‌فروشی لوازم خانه و آشپزخانه"
    )

    static let baseURL = url(
        "WLApiBaseURL",
        fallback: "https://pakhshemahdi.com/wp-json/pm-app/v1/"
    )
    static let storeURL = url(
        "WLStoreApiURL",
        fallback: "https://pakhshemahdi.com/wp-json/wc/store/v1/"
    )
    static let pwsAjaxURL = url(
        "WLPwsAjaxURL",
        fallback: "https://pakhshemahdi.com/wp-admin/admin-ajax.php"
    )
    static let supportURL = url(
        "WLSupportURL",
        fallback: "https://pakhshemahdi.com/support-tickets/"
    )
    static let contactURL = url(
        "WLContactURL",
        fallback: "https://pakhshemahdi.com/contact-us/"
    )

    static let brandPrimaryHex = info("WLBrandPrimary", fallback: "01082B")
    static let brandPrimaryDarkHex = info("WLBrandPrimaryDark", fallback: "06142B")
    static let brandAccentHex = info("WLBrandAccent", fallback: "D5AE57")
    static let brandDarkBackgroundHex = info("WLBrandDarkBackground", fallback: "01050D")

    static let featureWishlist = bool("WLFeatureWishlist", fallback: true)
    static let featureOTP = bool("WLFeatureOTP", fallback: true)
    static let featureCoupons = bool("WLFeatureCoupons", fallback: true)
    static let featureNotifications = bool("WLFeatureNotifications", fallback: true)
    static let featureSupport = bool("WLFeatureSupport", fallback: true)
    static let featurePwsShipping = bool("WLFeaturePwsShipping", fallback: true)
    static let featureOnlinePayment = bool("WLFeatureOnlinePayment", fallback: true)

    static let keychainService = Bundle.main.bundleIdentifier ?? "com.iamir.commerce"
}
