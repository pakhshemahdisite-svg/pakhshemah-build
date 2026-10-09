import SwiftUI
import UIKit

struct ProductDetailView: View {
    let product: Product
    @EnvironmentObject private var cart: CartStore
    @EnvironmentObject private var wishlist: WishlistStore
    @State private var selectedVariationID: Int?
    @State private var quantity = 1
    @State private var selectedImage: String?

    init(product: Product) {
        self.product = product
        let first = product.variations.first(where: {
            $0.isInStock && ($0.stockQuantity == nil || $0.stockQuantity! > 0)
        }) ?? product.variations.first
        _selectedVariationID = State(initialValue: first?.id)
        _selectedImage = State(initialValue: product.image)
    }

    private var selectedVariation: ProductVariation? {
        product.variations.first { $0.id == selectedVariationID }
    }

    private var maxQuantity: Int? {
        selectedVariation?.stockQuantity ?? (selectedVariation == nil ? product.stockQuantity : nil)
    }

    private var inStock: Bool {
        let hasStock = maxQuantity == nil || maxQuantity! > 0
        if let variation = selectedVariation { return variation.isInStock && hasStock }
        return product.isInStock && hasStock
    }

    private var price: String {
        guard let variation = selectedVariation, !variation.price.isEmpty else { return product.price }
        return variation.price
    }

    private var regularPrice: String {
        guard let variation = selectedVariation, !variation.regularPrice.isEmpty else { return product.regularPrice }
        return variation.regularPrice
    }

    private var salePrice: String {
        selectedVariation?.salePrice ?? product.salePrice
    }

    private var images: [String] {
        var values: [String] = []
        if let image = product.image { values.append(image) }
        values.append(contentsOf: product.gallery.filter { !values.contains($0) })
        return values
    }

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(alignment: .trailing, spacing: 18) {
                    ProductImageView(url: selectedImage)
                        .frame(height: 340)
                        .clipShape(RoundedRectangle(cornerRadius: 24))

                    if images.count > 1 {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                ForEach(images, id: \.self) { image in
                                    Button {
                                        selectedImage = image
                                    } label: {
                                        ProductImageView(url: image)
                                            .frame(width: 66, height: 66)
                                            .clipShape(RoundedRectangle(cornerRadius: 12))
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 12)
                                                    .stroke(selectedImage == image ? PMColor.primary : PMColor.border, lineWidth: selectedImage == image ? 2 : 1)
                                            )
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                        }
                    }

                    if !product.categories.isEmpty {
                        Text(product.categories.prefix(2).map(\.name).joined(separator: " • "))
                            .font(.caption.bold())
                            .foregroundStyle(PMColor.primary)
                    }

                    Text(product.name)
                        .font(.title2.bold())
                        .frame(maxWidth: .infinity, alignment: .trailing)

                    HStack {
                        Text(inStock ? "موجود در انبار" : "ناموجود")
                            .font(.caption.bold())
                            .foregroundStyle(inStock ? PMColor.success : .red)
                        Spacer()
                    }

                    VStack(alignment: .trailing, spacing: 5) {
                        Text("قیمت عمده")
                            .font(.caption)
                            .foregroundStyle(.secondary)

                        if !salePrice.isEmpty && !regularPrice.isEmpty {
                            Text(toman(regularPrice))
                                .font(.caption)
                                .foregroundStyle(.secondary)
                                .strikethrough()
                        }

                        Text(toman(price))
                            .font(.title2.bold())
                            .foregroundStyle(PMColor.primary)
                    }
                    .frame(maxWidth: .infinity, alignment: .trailing)
                    .padding()
                    .background(PMColor.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 18))

                    if !product.variations.isEmpty {
                        Text("انتخاب مدل")
                            .font(.title3.bold())

                        ForEach(product.variations) { variation in
                            let variationAvailable = variation.isInStock &&
                                (variation.stockQuantity == nil || variation.stockQuantity! > 0)
                            Button {
                                selectedVariationID = variation.id
                                quantity = 1
                                if let image = variation.image, !image.isEmpty {
                                    selectedImage = image
                                }
                            } label: {
                                HStack {
                                    Image(systemName: selectedVariationID == variation.id ? "largecircle.fill.circle" : "circle")
                                        .foregroundStyle(PMColor.primary)
                                    VStack(alignment: .trailing, spacing: 3) {
                                        Text(variation.attributes.values.filter { !$0.isEmpty }.joined(separator: " • "))
                                            .foregroundStyle(.primary)
                                            .fontWeight(.semibold)
                                        Text(
                                            variation.isInStock
                                                ? (variation.stockQuantity.map { "موجودی: \($0) عدد" } ?? "موجود")
                                                : "ناموجود"
                                        )
                                        .font(.caption)
                                        .foregroundStyle(variationAvailable ? PMColor.success : .red)
                                    }
                                    Spacer()
                                    Text(toman(variation.price))
                                        .foregroundStyle(PMColor.primary)
                                        .fontWeight(.bold)
                                }
                                .padding(12)
                                .background(selectedVariationID == variation.id ? PMColor.primary.opacity(0.08) : PMColor.surface)
                                .clipShape(RoundedRectangle(cornerRadius: 14))
                                .overlay(
                                    RoundedRectangle(cornerRadius: 14)
                                        .stroke(selectedVariationID == variation.id ? PMColor.primary : PMColor.border, lineWidth: 1)
                                )
                            }
                            .buttonStyle(.plain)
                            .disabled(!variationAvailable)
                        }
                    }

                    if !product.attributes.isEmpty {
                        Text("مشخصات محصول").font(.title3.bold())
                        VStack(spacing: 0) {
                            ForEach(Array(product.attributes.enumerated()), id: \.offset) { _, attribute in
                                HStack(alignment: .top) {
                                    Text(attribute.options.joined(separator: "، "))
                                        .frame(maxWidth: .infinity, alignment: .trailing)
                                    Text(attribute.name)
                                        .foregroundStyle(.secondary)
                                        .frame(width: 100, alignment: .trailing)
                                }
                                .padding(.vertical, 8)
                            }
                        }
                        .padding(.horizontal)
                        .background(PMColor.surface)
                        .clipShape(RoundedRectangle(cornerRadius: 18))
                    }

                    let rawDescription = product.shortDescription.isEmpty
                        ? product.description
                        : product.shortDescription
                    let text = plainText(fromHTML: rawDescription)
                    if !text.isEmpty {
                        Text("معرفی محصول").font(.title3.bold())
                        Text(text)
                            .foregroundStyle(.secondary)
                            .frame(maxWidth: .infinity, alignment: .trailing)
                    }

                    HStack {
                        Button {
                            if quantity > 1 { quantity -= 1 }
                        } label: {
                            Image(systemName: "minus")
                        }

                        Text("\(quantity)")
                            .fontWeight(.bold)
                            .frame(minWidth: 36)

                        Button {
                            if maxQuantity == nil || quantity < maxQuantity! { quantity += 1 }
                        } label: {
                            Image(systemName: "plus")
                        }
                        .disabled(maxQuantity.map { quantity >= $0 } ?? false)

                        Spacer()

                        if let maxQuantity, maxQuantity > 0 {
                            Text("موجودی: \(maxQuantity)")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                    .padding()
                    .background(PMColor.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                    HStack(spacing: 8) {
                        Circle()
                            .fill(PMColor.gold)
                            .frame(width: 8, height: 8)
                        Text("خرید عمده: حداقل ۶ عدد از هر کالا • حداقل مبلغ کل سفارش ۱۵ میلیون تومان")
                            .font(.caption)
                            .foregroundStyle(PMColor.secondary)
                            .frame(maxWidth: .infinity, alignment: .trailing)
                    }
                    .padding(12)
                    .background(PMColor.surfaceElevated)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .overlay(
                        RoundedRectangle(cornerRadius: 14)
                            .stroke(PMColor.gold.opacity(0.28), lineWidth: 1)
                    )
                }
                .padding()
            }

            Button {
                cart.add(product, variation: selectedVariation, quantity: quantity)
            } label: {
                Text(inStock ? "افزودن به سبد خرید" : "ناموجود")
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(inStock && product.purchasable ? PMColor.primary : Color.gray)
                    .foregroundStyle(PMColor.buttonForeground)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
            }
            .disabled(!inStock || !product.purchasable || (!product.variations.isEmpty && selectedVariation == nil))
            .padding()
        }
        .background(PMColor.background)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if AppConfig.featureWishlist {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        wishlist.toggle(product)
                    } label: {
                        Image(systemName: wishlist.contains(product.id) ? "heart.fill" : "heart")
                            .foregroundStyle(wishlist.contains(product.id) ? .red : PMColor.primary)
                    }
                }
            }
        }
    }
}


private func plainText(fromHTML html: String) -> String {
    guard !html.isEmpty, let data = html.data(using: .utf8) else { return html }
    if let attributed = try? NSAttributedString(
        data: data,
        options: [
            .documentType: NSAttributedString.DocumentType.html,
            .characterEncoding: String.Encoding.utf8.rawValue
        ],
        documentAttributes: nil
    ) {
        return attributed.string.trimmingCharacters(in: .whitespacesAndNewlines)
    }
    return html.replacingOccurrences(
        of: "<[^>]+>",
        with: "",
        options: .regularExpression
    ).trimmingCharacters(in: .whitespacesAndNewlines)
}
