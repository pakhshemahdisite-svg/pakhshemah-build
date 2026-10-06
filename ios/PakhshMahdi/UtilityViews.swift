import SwiftUI
import UserNotifications

struct NotificationsView: View {
    @EnvironmentObject private var session: SessionStore
    @State private var orders: [OrderSummary] = []
    @State private var changedIDs = Set<Int>()
    @State private var loading = false
    @State private var error: String?

    private let statusKey = "pakhsh_mahdi_ios_order_statuses_v1"

    var body: some View {
        Group {
            if loading && orders.isEmpty {
                ProgressView("دریافت اعلان‌ها...")
            } else if let error, orders.isEmpty {
                ContentUnavailableView(
                    "اعلان‌ها دریافت نشد",
                    systemImage: "wifi.exclamationmark",
                    description: Text(error)
                )
            } else if orders.isEmpty {
                ContentUnavailableView(
                    "اعلانی وجود ندارد",
                    systemImage: "bell",
                    description: Text("تغییر وضعیت سفارش‌ها در این بخش نمایش داده می‌شود.")
                )
            } else {
                List(orders) { order in
                    NavigationLink {
                        OrderDetailView(orderID: order.id)
                    } label: {
                        HStack {
                            if changedIDs.contains(order.id) {
                                Text("جدید")
                                    .font(.caption2.bold())
                                    .foregroundStyle(PMColor.primary)
                                    .padding(.horizontal, 7)
                                    .padding(.vertical, 3)
                                    .background(PMColor.primary.opacity(0.10))
                                    .clipShape(Capsule())
                            }

                            Spacer()

                            VStack(alignment: .trailing, spacing: 4) {
                                Text(orderTrackingTitle(order.status))
                                    .fontWeight(.semibold)
                                Text("سفارش #\(order.id)")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                }
                .refreshable { await load() }
            }
        }
        .navigationTitle("اطلاعیه‌ها")
        .task { await load() }
    }

    private func load() async {
        guard session.isLoggedIn, !loading else { return }
        loading = true
        error = nil
        defer { loading = false }

        do {
            let latest = try await session.orders()
            let previous = loadStatuses()

            changedIDs = Set(
                latest.compactMap { order in
                    guard let old = previous[order.id], old != order.status else { return nil }
                    return order.id
                }
            )

            for order in latest where changedIDs.contains(order.id) {
                await sendLocalNotification(order)
            }

            orders = latest
            saveStatuses(latest)
        } catch {
            self.error = error.localizedDescription
        }
    }

    private func loadStatuses() -> [Int: String] {
        guard
            let data = UserDefaults.standard.data(forKey: statusKey),
            let raw = try? JSONDecoder().decode([String: String].self, from: data)
        else { return [:] }

        return Dictionary(uniqueKeysWithValues: raw.compactMap { key, value in
            Int(key).map { ($0, value) }
        })
    }

    private func saveStatuses(_ orders: [OrderSummary]) {
        let raw = Dictionary(uniqueKeysWithValues: orders.map { (String($0.id), $0.status) })
        if let data = try? JSONEncoder().encode(raw) {
            UserDefaults.standard.set(data, forKey: statusKey)
        }
    }

    private func sendLocalNotification(_ order: OrderSummary) async {
        let center = UNUserNotificationCenter.current()
        let settings = await center.notificationSettings()
        guard settings.authorizationStatus == .authorized ||
              settings.authorizationStatus == .provisional else { return }

        let content = UNMutableNotificationContent()
        content.title = "وضعیت سفارش تغییر کرد"
        content.body = "سفارش #\(order.id): \(orderStatusTitle(order.status))"
        content.sound = .default

        let request = UNNotificationRequest(
            identifier: "order-\(order.id)-\(order.status)",
            content: content,
            trigger: nil
        )
        try? await center.add(request)
    }
}

struct SupportView: View {
    @Environment(\.openURL) private var openURL

    var body: some View {
        VStack(spacing: 18) {
            Image(systemName: "headphones.circle.fill")
                .font(.system(size: 72))
                .foregroundStyle(PMColor.primary)

            Text("چطور می‌توانیم کمک کنیم؟")
                .font(.title2.bold())

            Text("برای پیگیری سفارش، سوالات فروش و پشتیبانی از مسیرهای رسمی \(AppConfig.appName) استفاده کنید.")
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)

            Button("ثبت و پیگیری تیکت") {
                openURL(AppConfig.supportURL)
            }
            .buttonStyle(.borderedProminent)
            .tint(PMColor.primary)
            .frame(maxWidth: .infinity)

            Button("اطلاعات تماس") {
                openURL(AppConfig.contactURL)
            }
            .buttonStyle(.bordered)

            Spacer()
        }
        .padding()
        .navigationTitle("پشتیبانی")
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct SettingsView: View {
    @AppStorage("pakhsh_mahdi_theme") private var themeRaw = PMThemeMode.system.rawValue
    @State private var notificationStatus = ""

    var body: some View {
        Form {
            Section("ظاهر برنامه") {
                Picker("تم", selection: $themeRaw) {
                    ForEach(PMThemeMode.allCases) { mode in
                        Text(mode.title).tag(mode.rawValue)
                    }
                }
            }

            if AppConfig.featureNotifications {
                Section("اعلان‌ها") {
                    Button("فعال‌سازی اعلان وضعیت سفارش") {
                        Task { await requestNotifications() }
                    }
                    if !notificationStatus.isEmpty {
                        Text(notificationStatus)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
            }

            Section("نسخه") {
                LabeledContent(
                    "نسخه برنامه",
                    value: Bundle.main.object(
                        forInfoDictionaryKey: "CFBundleShortVersionString"
                    ) as? String ?? "0.1.0"
                )
            }

            Section {
                Text("توسعه دهنده امیر بخشی 09359492927")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
            }
        }
        .navigationTitle("تنظیمات")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            if AppConfig.featureNotifications {
                await refreshNotificationStatus()
            }
        }
    }

    private func requestNotifications() async {
        do {
            let granted = try await UNUserNotificationCenter.current().requestAuthorization(
                options: [.alert, .badge, .sound]
            )
            notificationStatus = granted ? "اعلان‌ها فعال هستند." : "اجازه اعلان داده نشد."
        } catch {
            notificationStatus = "تنظیم اعلان انجام نشد."
        }
    }

    private func refreshNotificationStatus() async {
        let settings = await UNUserNotificationCenter.current().notificationSettings()
        switch settings.authorizationStatus {
        case .authorized, .provisional, .ephemeral:
            notificationStatus = "اعلان‌ها فعال هستند."
        case .denied:
            notificationStatus = "اعلان‌ها غیرفعال هستند."
        default:
            notificationStatus = "اجازه اعلان هنوز درخواست نشده است."
        }
    }
}
