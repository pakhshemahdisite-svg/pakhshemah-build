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
                List {
                    ForEach(cart.lines) { line in
                        HStack(spacing: 12) {
                            ProductImageView(url: line.variation?.image ?? line.product.image)
                                .frame(width: 64, height: 64)
                                .clipShape(RoundedRectangle(cornerRadius: 12))

                            VStack(alignment: .trailing, spacing: 5) {
                                Text(line.product.name)
                                    .font(.subheadline.bold())
                                    .lineLimit(2)
                                if let variation = line.variation {
                                    let label = variation.attributes.values.filter { !$0.isEmpty }.joined(separator: " • ")
                                    if !label.isEmpty {
                                        Text(label)
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                    }
                                }
                                Text(toman(String((Int(Double(line.unitPrice) ?? 0)) * line.quantity)))
                                    .foregroundStyle(PMColor.primary)
                                    .fontWeight(.bold)
                            }

                            Spacer()

                            VStack(spacing: 8) {
                                Button { cart.increment(line.id) } label: {
                                    Image(systemName: "plus.circle.fill")
                                }
                                .disabled(line.maxStock.map { line.quantity >= $0 } ?? false)

                                Text("\(line.quantity)").fontWeight(.bold)

                                Button { cart.decrement(line.id) } label: {
                                    Image(systemName: "minus.circle")
                                }
                            }
                            .foregroundStyle(PMColor.primary)
                        }
                    }
                    .onDelete { offsets in
                        offsets.map { cart.lines[$0].id }.forEach(cart.remove)
                    }

                    Section("خلاصه سفارش") {
                        HStack {
                            Text(toman(String(cart.subtotal)))
                                .foregroundStyle(PMColor.primary)
                                .fontWeight(.bold)
                            Spacer()
                            Text("جمع کالاها")
                        }
                    }
                }

                NavigationLink {
                    if session.isLoggedIn {
                        CheckoutView()
                    } else {
                        LoginView()
                    }
                } label: {
                    Text(session.isLoggedIn ? "ادامه و تکمیل سفارش" : "برای ادامه وارد حساب شوید")
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(PMColor.primary)
                        .foregroundStyle(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                }
                .padding()
            }
        }
        .navigationTitle("سبد خرید")
    }
}
