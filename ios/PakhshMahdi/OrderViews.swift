import SwiftUI

struct OrderDetailView: View {
    let orderID: Int
    @EnvironmentObject private var session: SessionStore

    @State private var detail: OrderDetail?
    @State private var loading = false
    @State private var error: String?

    var body: some View {
        Group {
            if loading && detail == nil {
                ProgressView("دریافت سفارش...")
            } else if let detail {
                List {
                    Section("اطلاعات سفارش") {
                        HStack {
                            statusBadge(detail.status)
                            Spacer()
                            Text("سفارش #\(detail.id)").font(.headline)
                        }
                        if !detail.date.isEmpty {
                            LabeledContent("تاریخ", value: String(detail.date.prefix(16)))
                        }
                        if !detail.paymentMethod.isEmpty {
                            LabeledContent("روش پرداخت", value: detail.paymentMethod)
                        }
                        if !detail.shippingMethod.isEmpty {
                            LabeledContent("روش ارسال", value: detail.shippingMethod)
                        }
                    }

                    Section("محصولات") {
                        ForEach(detail.items) { line in
                            HStack(spacing: 10) {
                                ProductImageView(url: line.image)
                                    .frame(width: 60, height: 60)
                                    .clipShape(RoundedRectangle(cornerRadius: 10))
                                VStack(alignment: .trailing, spacing: 4) {
                                    Text(line.name).font(.subheadline.bold())
                                    Text("تعداد: \(line.quantity)")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                                Spacer()
                                Text(toman(line.total))
                                    .font(.caption.bold())
                                    .foregroundStyle(PMColor.primary)
                            }
                        }
                    }

                    Section("جمع سفارش") {
                        LabeledContent("تخفیف", value: toman(detail.discountTotal))
                        LabeledContent("هزینه ارسال", value: toman(detail.shippingTotal))
                        LabeledContent("مبلغ نهایی", value: toman(detail.total))
                            .fontWeight(.bold)
                    }

                    Section {
                        NavigationLink {
                            OrderTrackingView(detail: detail)
                        } label: {
                            Label("پیگیری وضعیت سفارش", systemImage: "shippingbox")
                        }
                    }
                }
                .refreshable { await load(force: true) }
            } else {
                ContentUnavailableView(
                    "اطلاعات سفارش دریافت نشد",
                    systemImage: "exclamationmark.triangle",
                    description: Text(error ?? "لطفاً دوباره تلاش کنید.")
                )
            }
        }
        .navigationTitle("جزئیات سفارش")
        .navigationBarTitleDisplayMode(.inline)
        .task { await load() }
    }

    @ViewBuilder
    private func statusBadge(_ status: String) -> some View {
        Text(orderStatusTitle(status))
            .font(.caption.bold())
            .foregroundStyle(orderStatusColor(status))
            .padding(.horizontal, 9)
            .padding(.vertical, 5)
            .background(orderStatusColor(status).opacity(0.10))
            .clipShape(Capsule())
    }

    private func load(force: Bool = false) async {
        if detail != nil && !force { return }
        loading = true
        error = nil
        defer { loading = false }

        do {
            detail = try await session.orderDetail(id: orderID)
        } catch {
            self.error = error.localizedDescription
        }
    }
}

struct OrderTrackingView: View {
    let detail: OrderDetail

    private var step: Int {
        switch normalizedStatus(detail.status) {
        case "completed", "delivered": return 3
        case "shipped", "out-for-delivery", "out_for_delivery": return 2
        case "processing": return 1
        default: return 0
        }
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .trailing, spacing: 18) {
                VStack(alignment: .trailing, spacing: 6) {
                    Image(systemName: "shippingbox")
                        .font(.system(size: 32))
                        .foregroundStyle(PMColor.primary)
                    Text(orderTrackingTitle(detail.status))
                        .font(.title3.bold())
                    Text(
                        detail.shippingMethod.isEmpty
                            ? "روش ارسال پس از پردازش سفارش مشخص می‌شود."
                            : "روش ارسال: \(detail.shippingMethod)"
                    )
                    .foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity, alignment: .trailing)
                .padding()
                .background(PMColor.surface)
                .clipShape(RoundedRectangle(cornerRadius: 20))

                trackingStep(0, "ثبت سفارش", "سفارش با موفقیت ثبت شده است.", "checkmark")
                trackingStep(1, "در حال پردازش", "سفارش در حال آماده‌سازی است.", "shippingbox")
                trackingStep(2, "ارسال سفارش", "سفارش به واحد ارسال تحویل شده است.", "truck.box")
                trackingStep(3, "تحویل شده", "سفارش به مقصد تحویل شده است.", "checkmark.seal")
            }
            .padding()
        }
        .background(PMColor.background)
        .navigationTitle("پیگیری سفارش")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func trackingStep(
        _ index: Int,
        _ title: String,
        _ subtitle: String,
        _ icon: String
    ) -> some View {
        let complete = index <= step
        return HStack(alignment: .top, spacing: 12) {
            VStack {
                Image(systemName: icon)
                    .foregroundStyle(complete ? Color.white : Color.secondary)
                    .frame(width: 38, height: 38)
                    .background(complete ? PMColor.primary : PMColor.border.opacity(0.35))
                    .clipShape(Circle())
                if index < 3 {
                    Rectangle()
                        .fill(index < step ? PMColor.primary : PMColor.border)
                        .frame(width: 2, height: 42)
                }
            }
            VStack(alignment: .trailing, spacing: 4) {
                Text(title)
                    .fontWeight(.bold)
                    .foregroundStyle(complete ? Color.primary : Color.secondary)
                Text(subtitle)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity, alignment: .trailing)
        }
    }
}

func normalizedStatus(_ status: String) -> String {
    status.trimmingCharacters(in: .whitespacesAndNewlines)
        .lowercased()
        .replacingOccurrences(of: "wc-", with: "")
}

func orderTrackingTitle(_ status: String) -> String {
    switch normalizedStatus(status) {
    case "completed", "delivered": return "سفارش تحویل شده است"
    case "shipped": return "سفارش ارسال شده است"
    case "out-for-delivery", "out_for_delivery": return "سفارش در مسیر تحویل است"
    case "processing": return "سفارش در حال پردازش است"
    case "on-hold": return "سفارش در انتظار بررسی است"
    case "pending": return "در انتظار پرداخت"
    case "cancelled": return "سفارش لغو شده است"
    case "refunded": return "سفارش مرجوع شده است"
    case "failed": return "سفارش ناموفق بوده است"
    default: return "وضعیت سفارش"
    }
}
