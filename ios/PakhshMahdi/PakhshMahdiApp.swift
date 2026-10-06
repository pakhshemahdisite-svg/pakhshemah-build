import SwiftUI

@main
struct PakhshMahdiApp: App {
    @StateObject private var catalog = CatalogStore()
    @StateObject private var cart = CartStore()
    @StateObject private var wishlist = WishlistStore()
    @StateObject private var session = SessionStore()
    @AppStorage("pakhsh_mahdi_theme") private var themeRaw = PMThemeMode.system.rawValue

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(catalog)
                .environmentObject(cart)
                .environmentObject(wishlist)
                .environmentObject(session)
                .environment(\.layoutDirection, .rightToLeft)
                .preferredColorScheme(PMThemeMode(rawValue: themeRaw)?.colorScheme)
        }
    }
}
