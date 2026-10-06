import SwiftUI
import UIKit

private func clientHex(_ value: String, fallback: UInt) -> UInt {
    let normalized = value
        .trimmingCharacters(in: .whitespacesAndNewlines)
        .replacingOccurrences(of: "#", with: "")
    return UInt(normalized, radix: 16) ?? fallback
}

enum PMColor {
    static let primary = Color(
        light: clientHex(AppConfig.brandPrimaryHex, fallback: 0x01082B),
        dark: clientHex(AppConfig.brandAccentHex, fallback: 0xD5AE57)
    )
    static let secondary = Color(
        light: clientHex(AppConfig.brandPrimaryDarkHex, fallback: 0x07012B),
        dark: 0xB8C2D8
    )
    static let surface = Color(
        light: 0xFFFFFF,
        dark: clientHex(AppConfig.brandPrimaryDarkHex, fallback: 0x06142B)
    )
    static let border = Color(light: 0xD2D2D4, dark: 0x2E3F5F)
    static let background = Color(
        light: 0xF2F2F2,
        dark: clientHex(AppConfig.brandDarkBackgroundHex, fallback: 0x01050D)
    )
    static let success = Color(hex: 0x16A34A)
    static let gold = Color(hex: clientHex(AppConfig.brandAccentHex, fallback: 0xD5AE57))
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
