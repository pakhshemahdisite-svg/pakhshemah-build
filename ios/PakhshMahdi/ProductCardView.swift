import SwiftUI

struct ProductCardView: View {
    let product: Product
    @EnvironmentObject private var cart: CartStore
    @EnvironmentObject private var wishlist: WishlistStore

    private var canQuickAdd: Bool {
        product.purchasable && product.isInStock && product.type != "variable" && product.variations.isEmpty
    }

    var body: some View {
        VStack(alignment: .trailing, spacing: 9) {
            ZStack(alignment: .topTrailing) {
                ProductImageView(url: product.image)
                    .frame(height: 150)
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                if AppConfig.featureWishlist {
                    VStack {
                        HStack {
                            Button {
                                wishlist.toggle(product)
                            } label: {
                                Image(systemName: wishlist.contains(product.id) ? "heart.fill" : "heart")
                                    .font(.caption.bold())
                                    .foregroundStyle(wishlist.contains(product.id) ? .red : PMColor.primary)
                                    .frame(width: 32, height: 32)
                                    .background(PMColor.surface.opacity(0.94))
                                    .clipShape(Circle())
                            }
                            .buttonStyle(.plain)
                            Spacer()
                        }
                        Spacer()
                    }
                    .padding(7)
                }

                if product.hasSale {
                    Text("تخفیف")
                        .font(.caption2.bold())
                        .foregroundStyle(PMColor.primary)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color(hex: 0xD5AE57))
                        .clipShape(Capsule())
                        .padding(7)
                }
            }

            Text(product.name)
                .font(.headline)
                .lineLimit(2)
                .frame(maxWidth: .infinity, alignment: .trailing)

            HStack(spacing: 5) {
                Circle()
                    .fill(product.isInStock ? PMColor.success : .red)
                    .frame(width: 6, height: 6)
                Text(product.isInStock ? "موجود" : "ناموجود")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Spacer()
            }

            if product.hasSale && !product.regularPrice.isEmpty {
                Text(toman(product.regularPrice))
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .strikethrough()
                    .frame(maxWidth: .infinity, alignment: .trailing)
            }

            HStack {
                if canQuickAdd {
                    Button {
                        cart.add(product)
                    } label: {
                        Image(systemName: "cart.badge.plus")
                            .foregroundStyle(.white)
                            .padding(10)
                            .background(PMColor.primary)
                            .clipShape(Circle())
                    }
                    .disabled(!product.isInStock || !product.purchasable)
                } else {
                    Image(systemName: "chevron.left")
                        .foregroundStyle(.white)
                        .padding(10)
                        .background((product.isInStock && product.purchasable) ? PMColor.primary : Color.gray)
                        .clipShape(Circle())
                }

                Spacer()

                Text(toman(product.price))
                    .fontWeight(.bold)
                    .foregroundStyle(PMColor.primary)
                    .lineLimit(1)
            }
        }
        .padding(10)
        .background(PMColor.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(PMColor.border.opacity(0.7), lineWidth: 1)
        )
    }
}

func toman(_ value: String) -> String {
    let int = Int(Double(value) ?? 0)
    let formatter = NumberFormatter()
    formatter.numberStyle = .decimal
    return "\(formatter.string(from: NSNumber(value: int)) ?? value) تومان"
}
