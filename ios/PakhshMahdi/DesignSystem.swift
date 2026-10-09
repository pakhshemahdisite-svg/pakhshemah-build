import SwiftUI
import UIKit

private func clientHex(_ value: String, fallback: UInt) -> UInt {
    let normalized = value
        .trimmingCharacters(in: .whitespacesAndNewlines)
        .replacingOccurrences(of: "#", with: "")
    return UInt(normalized, radix: 16) ?? fallback
}

enum PMColor {
    static let black = Color(hex: 0x111111)
    static let pureBlack = Color(hex: 0x000000)
    static let gold = Color(hex: clientHex(AppConfig.brandAccentHex, fallback: 0xD4AF37))

    static let primary = Color(
        light: clientHex(AppConfig.brandPrimaryHex, fallback: 0x111111),
        dark: clientHex(AppConfig.brandAccentHex, fallback: 0xD4AF37)
    )
    static let primaryDark = Color(
        light: clientHex(AppConfig.brandPrimaryDarkHex, fallback: 0x000000),
        dark: 0x000000
    )
    static let secondary = Color(light: 0x5F5F5F, dark: 0xC8C6C0)
    static let muted = Color(light: 0x8A8A86, dark: 0x8D8B85)
    static let surface = Color(light: 0xFFFFFF, dark: 0x101010)
    static let surfaceElevated = Color(light: 0xF2F1ED, dark: 0x191919)
    static let border = Color(light: 0xE5E2DA, dark: 0x2B2924)
    static let background = Color(
        light: 0xF8F8F6,
        dark: clientHex(AppConfig.brandDarkBackgroundHex, fallback: 0x050505)
    )
    static let success = Color(light: 0x26865A, dark: 0x5FC28B)
    static let warning = Color(light: 0xC4932F, dark: 0xD4AF37)
    static let buttonForeground = Color(light: 0xFFFFFF, dark: 0x000000)
}

private let pakhshMahdiGoldLogoBase64 = "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"

struct BrandLogoView: View {
    private var logo: UIImage? {
        guard let data = Data(base64Encoded: pakhshMahdiGoldLogoBase64) else { return nil }
        return UIImage(data: data)
    }

    var body: some View {
        Group {
            if let logo {
                Image(uiImage: logo)
                    .resizable()
                    .scaledToFit()
            } else {
                Text(AppConfig.appName)
                    .font(.headline.bold())
                    .foregroundStyle(PMColor.gold)
            }
        }
        .accessibilityLabel("لوگوی \(AppConfig.appName)")
    }
}

extension Color {
    init(light: UInt, dark: UInt) {
        self.init(
            UIColor { trait in
                let hex = trait.userInterfaceStyle == .dark ? dark : light
                return UIColor(
                    red: CGFloat((hex >> 16) & 0xff) / 255,
                    green: CGFloat((hex >> 8) & 0xff) / 255,
                    blue: CGFloat(hex & 0xff) / 255,
                    alpha: 1
                )
            }
        )
    }

    init(hex: UInt, alpha: Double = 1) {
        self.init(.sRGB,
                  red: Double((hex >> 16) & 0xff) / 255,
                  green: Double((hex >> 08) & 0xff) / 255,
                  blue: Double((hex >> 00) & 0xff) / 255,
                  opacity: alpha)
    }
}

enum PMThemeMode: String, CaseIterable, Identifiable {
    case system
    case light
    case dark

    var id: String { rawValue }

    var title: String {
        switch self {
        case .system: return "سیستم"
        case .light: return "روشن"
        case .dark: return "تیره"
        }
    }

    var colorScheme: ColorScheme? {
        switch self {
        case .system: return nil
        case .light: return .light
        case .dark: return .dark
        }
    }
}
