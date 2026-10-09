import SwiftUI

struct CartView: View {
    @EnvironmentObject private var cart: CartStore
    @EnvironmentObject private var session: SessionStore

    var body: some View {
        VStack(spacing: 0) {
            if cart.lines.isEmpty {
                ContentUnavailableView(
                    "سبد خرید خالی است",
                    systemImage: "cart",
                    description: Text("محصولات موردنظر را به سبد اضافه کنید.")
                )
            } else {
                ScrollView {
                    VStack(spacing: 12) {
                        wholesaleRuleCard

                        ForEach(cart.lines) { line in
                            cartLine(line)
                        }

                        summaryCard

                        if let message = cart.wholesaleMessage {
                            HStack(alignment: .top, spacing: 9) {
                                Image(systemName: "info.circle.fill")
                                    .foregroundStyle(PMColor.gold)
                                Text(message)
                                    .font(.footnote)
                                    .foregroundStyle(PMColor.secondary)
                                    .frame(maxWidth: .infinity, alignment: .trailing)
                            }
                            .padding(14)
                            .background(PMColor.gold.opacity(0.09))
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                            .overlay(
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(PMColor.gold.opacity(0.28), lineWidth: 1)
                            )
                        }
                    }
                    .padding(16)
                }
                .background(PMColor.background)

                NavigationLink {
                    if session.isLoggedIn {
                        CheckoutView()
                    } else {
                        LoginView()
                    }
                } label: {
                    HStack {
                        Image(systemName: "arrow.left")
                        Text(session.isLoggedIn ? "ادامه و تکمیل سفارش" : "برای ادامه وارد حساب شوید")
                            .fontWeight(.bold)
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 56)
                    .background(cart.isWholesaleEligible ? PMColor.primary : PMColor.border)
                    .foregroundStyle(cart.isWholesaleEligible ? PMColor.buttonForeground : PMColor.muted)
                    .clipShape(RoundedRectangle(cornerRadius: 17))
                }
                .disabled(!cart.isWholesaleEligible)
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(PMColor.surface)
                .overlay(alignment: .top) {
                    Rectangle().fill(PMColor.border).frame(height: 1)
                }
            }
        }
        .background(PMColor.background)
        .navigationTitle("سبد خرید")
        .navigationBarTitleDisplayMode(.inline)
    }

    private var wholesaleRuleCard: some View {
        HStack(spacing: 10) {
            Circle()
                .fill(PMColor.gold)
                .frame(width: 9, height: 9)
            Text("قانون خرید عمده: حداقل ۶ عدد از هر کالا • حداقل سفارش ۱۵ میلیون تومان")
                .font(.caption)
                .foregroundStyle(PMColor.secondary)
                .frame(maxWidth: .infinity, alignment: .trailing)
        }
        .padding(13)
        .background(PMColor.surfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(PMColor.border, lineWidth: 1)
        )
    }

    private func cartLine(_ line: CartStore.Line) -> some View {
        HStack(spacing: 12) {
            ProductImageView(url: line.variation?.image ?? line.product.image)
                .frame(width: 82, height: 82)
                .clipShape(RoundedRectangle(cornerRadius: 16))

            VStack(alignment: .trailing, spacing: 7) {
                Text(line.product.name)
                    .font(.subheadline.bold())
                    .lineLimit(2)
                    .frame(maxWidth: .infinity, alignment: .trailing)

                if let variation = line.variation {
                    let label = variation.attributes.values.filter { !$0.isEmpty }.joined(separator: " • ")
                    if !label.isEmpty {
                        Text(label)
                            .font(.caption)
                            .foregroundStyle(PMColor.secondary)
                    }
                }

                Text(toman(String((Int(Double(line.unitPrice) ?? 0)) * line.quantity)))
                    .foregroundStyle(PMColor.primary)
                    .fontWeight(.bold)
            }

            VStack(spacing: 7) {
                Button { cart.increment(line.id) } label: {
                    Image(systemName: "plus")
                        .frame(width: 30, height: 30)
                        .background(PMColor.surfaceElevated)
                        .clipShape(Circle())
                }
                .disabled(line.maxStock.map { line.quantity >= $0 } ?? false)

                Text("\(line.quantity)")
                    .font(.subheadline.bold())
                    .frame(minWidth: 30)

                Button { cart.decrement(line.id) } label: {
                    Image(systemName: "minus")
                        .frame(width: 30, height: 30)
                        .background(PMColor.surfaceElevated)
                        .clipShape(Circle())
                }
                .disabled(line.quantity <= WholesalePolicy.minimumPerProduct)
            }
            .foregroundStyle(PMColor.primary)

            Button(role: .destructive) {
                cart.remove(line.id)
            } label: {
                Image(systemName: "trash")
                    .font(.subheadline)
            }
        }
        .padding(12)
        .background(PMColor.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(PMColor.border, lineWidth: 1)
        )
    }

    private var summaryCard: some View {
        VStack(spacing: 12) {
            HStack {
                Text(toman(String(cart.subtotal)))
                    .foregroundStyle(PMColor.primary)
                    .fontWeight(.bold)
                Spacer()
                Text("جمع کالاها")
                    .foregroundStyle(PMColor.secondary)
            }

            Divider()

            HStack {
                Text(cart.isWholesaleEligible ? "آماده ادامه خرید" : "نیاز به تکمیل سبد")
                    .foregroundStyle(cart.isWholesaleEligible ? PMColor.success : PMColor.warning)
                    .fontWeight(.semibold)
                Spacer()
                Text("وضعیت سفارش")
                    .fontWeight(.semibold)
            }
        }
        .padding(16)
        .background(PMColor.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(PMColor.border, lineWidth: 1)
        )
    }
}
