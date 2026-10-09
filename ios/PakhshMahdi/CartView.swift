import SwiftUI

struct CartView: View {
    @EnvironmentObject private var cart: CartStore
    @EnvironmentObject private var session: SessionStore

    var body: some View {
        VStack(spacing: 0) {
            if cart.lines.isEmpty {
                VStack(spacing: 14) {
                    Spacer()
                    Image(systemName: "cart")
                        .font(.system(size: 48, weight: .light))
                        .foregroundStyle(PMColor.goldDeep)
                    Text("سبد خرید خالی است")
                        .font(.title2.bold())
                    Text("محصولات موردنظر را به سبد اضافه کنید.")
                        .foregroundStyle(PMColor.secondary)
                    Spacer()
                }
                .frame(maxWidth: .infinity)
            } else {
                ScrollView {
                    VStack(spacing: 12) {
                        cartTitle
                        wholesaleRulesCard

                        ForEach(cart.lines) { line in
                            premiumCartLine(line)
                        }

                        summaryCard
                    }
                    .padding(.horizontal, 14)
                    .padding(.top, 10)
                    .padding(.bottom, 8)
                }
                .background(PMColor.background)

                checkoutButton
            }
        }
        .background(PMColor.background)
        .toolbar(.hidden, for: .navigationBar)
    }

    private var cartTitle: some View {
        HStack(alignment: .center) {
            Spacer()

            VStack(alignment: .trailing, spacing: 2) {
                Text("سبد خرید")
                    .font(.largeTitle.bold())
                Text("\(cart.totalQuantity) کالا • \(cart.lines.count) قلم")
                    .font(.caption)
                    .foregroundStyle(PMColor.secondary)
            }

            ZStack(alignment: .topTrailing) {
                Image(systemName: "cart")
                    .font(.title2.weight(.semibold))
                    .frame(width: 48, height: 48)
                    .background(PMColor.surface)
                    .clipShape(Circle())
                    .overlay(Circle().stroke(PMColor.border, lineWidth: 1))

                Text("\(min(cart.totalQuantity, 99))")
                    .font(.caption2.bold())
                    .foregroundStyle(.black)
                    .frame(minWidth: 19, minHeight: 19)
                    .background(PMColor.goldDeep)
                    .clipShape(Circle())
                    .offset(x: 5, y: -5)
            }
        }
        .padding(.vertical, 4)
    }

    private var wholesaleRulesCard: some View {
        VStack(spacing: 10) {
            HStack {
                ZStack {
                    Circle()
                        .fill(PMColor.gold.opacity(0.62))
                        .frame(width: 50, height: 50)
                    Image(systemName: "cart")
                        .font(.title3.bold())
                        .foregroundStyle(.black)
                }

                Spacer()

                Text("شرایط خرید عمده")
                    .font(.title2.bold())
                    .foregroundStyle(.primary)
            }

            ruleRow(
                icon: "shippingbox",
                title: "حداقل خرید هر کالا",
                value: "۶ عدد"
            )

            ruleRow(
                icon: "banknote",
                title: "حداقل مبلغ سفارش",
                value: "۱۵,۰۰۰,۰۰۰ تومان"
            )

            HStack(spacing: 7) {
                Image(systemName: "info.circle")
                    .foregroundStyle(PMColor.goldDeep)
                Text("برای ادامه خرید، این شرایط باید در سبد رعایت شود. کنترل نهایی توسط فروشگاه انجام می‌شود.")
                    .font(.caption)
                    .foregroundStyle(PMColor.secondary)
                    .frame(maxWidth: .infinity, alignment: .trailing)
                    .multilineTextAlignment(.trailing)
            }
            .padding(10)
            .background(PMColor.gold.opacity(0.17))
            .clipShape(RoundedRectangle(cornerRadius: 13))
        }
        .padding(15)
        .background(PMColor.surfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .overlay(
            RoundedRectangle(cornerRadius: 24)
                .stroke(PMColor.goldDeep.opacity(0.58), lineWidth: 1.2)
        )
        .shadow(color: .black.opacity(0.05), radius: 8, y: 3)
    }

    private func ruleRow(icon: String, title: String, value: String) -> some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .foregroundStyle(PMColor.goldDeep)
                .frame(width: 24)

            Text(title)
                .foregroundStyle(PMColor.secondary)

            Spacer()

            Text(value)
                .fontWeight(.bold)
                .foregroundStyle(PMColor.goldDeep)
        }
        .font(.subheadline)
        .padding(.horizontal, 12)
        .frame(height: 45)
        .background(PMColor.surface)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(PMColor.border, lineWidth: 1)
        )
    }

    private func premiumCartLine(_ line: CartStore.Line) -> some View {
        HStack(spacing: 11) {
            ProductImageView(url: line.variation?.image ?? line.product.image)
                .frame(width: 94, height: 94)
                .clipShape(RoundedRectangle(cornerRadius: 16))

            VStack(alignment: .trailing, spacing: 6) {
                Text(line.product.name)
                    .font(.subheadline.bold())
                    .lineLimit(2)
                    .frame(maxWidth: .infinity, alignment: .trailing)

                if let variation = line.variation {
                    let label = variation.attributes.values
                        .filter { !$0.isEmpty }
                        .joined(separator: " • ")
                    if !label.isEmpty {
                        Text(label)
                            .font(.caption)
                            .foregroundStyle(PMColor.secondary)
                    }
                }

                Text(toman(line.unitPrice))
                    .foregroundStyle(PMColor.goldDeep)
                    .font(.subheadline.bold())

                HStack(spacing: 0) {
                    Button { cart.decrement(line.id) } label: {
                        Image(systemName: "minus")
                            .frame(width: 34, height: 34)
                    }
                    .disabled(line.quantity <= 1)

                    Text("\(line.quantity)")
                        .font(.subheadline.bold())
                        .frame(minWidth: 36)

                    Button { cart.increment(line.id) } label: {
                        Image(systemName: "plus")
                            .frame(width: 34, height: 34)
                    }
                    .disabled(line.maxStock.map { line.quantity >= $0 } ?? false)
                }
                .foregroundStyle(.primary)
                .background(PMColor.gold.opacity(0.30))
                .clipShape(RoundedRectangle(cornerRadius: 13))
                .overlay(
                    RoundedRectangle(cornerRadius: 13)
                        .stroke(PMColor.goldDeep.opacity(0.28), lineWidth: 1)
                )

                Text(
                    "جمع: " + toman(String((Int(Double(line.unitPrice) ?? 0)) * line.quantity))
                )
                .font(.caption)
                .foregroundStyle(PMColor.secondary)
            }

            Button(role: .destructive) {
                cart.remove(line.id)
            } label: {
                Image(systemName: "trash")
                    .font(.subheadline)
                    .frame(width: 36, height: 36)
                    .background(PMColor.surfaceElevated)
                    .clipShape(Circle())
                    .overlay(Circle().stroke(PMColor.border, lineWidth: 1))
            }
        }
        .padding(10)
        .background(PMColor.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(PMColor.border, lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.035), radius: 5, y: 2)
    }

    private var summaryCard: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text("مجموع کل")
                    .font(.subheadline)
                    .foregroundStyle(PMColor.secondary)

                Text(toman(String(cart.subtotal)))
                    .font(.title2.bold())
                    .foregroundStyle(PMColor.goldDeep)
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 7) {
                Label("\(cart.totalQuantity) عدد", systemImage: "shippingbox")
                    .font(.subheadline.bold())
                Label("\(cart.lines.count) قلم", systemImage: "square.grid.2x2")
                    .font(.caption)
                    .foregroundStyle(PMColor.secondary)
            }
        }
        .padding(16)
        .background(PMColor.surfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 22))
        .overlay(
            RoundedRectangle(cornerRadius: 22)
                .stroke(PMColor.gold.opacity(0.35), lineWidth: 1)
        )
    }

    private var checkoutButton: some View {
        NavigationLink {
            if session.isLoggedIn {
                CheckoutView()
            } else {
                LoginView()
            }
        } label: {
            HStack(spacing: 9) {
                Image(systemName: "cart")
                Text(session.isLoggedIn ? "ادامه خرید و ثبت سفارش" : "ورود و ادامه خرید")
                    .font(.headline.bold())
            }
            .foregroundStyle(.black)
            .frame(maxWidth: .infinity)
            .frame(height: 58)
            .background(
                LinearGradient(
                    colors: [PMColor.gold, PMColor.goldDeep],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .clipShape(RoundedRectangle(cornerRadius: 18))
        }
        .buttonStyle(.plain)
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .background(PMColor.surface)
        .overlay(alignment: .top) {
            Rectangle().fill(PMColor.border).frame(height: 1)
        }
    }
}
