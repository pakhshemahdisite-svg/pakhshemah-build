import SwiftUI

struct HomeView: View {
    @EnvironmentObject private var catalog: CatalogStore

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                header

                if let featured = catalog.home.featuredProducts.first
                    ?? catalog.home.latestProducts.first {
                    hero(product: featured)
                }

                if !catalog.home.categories.isEmpty {
                    sectionHeader("دسته‌بندی‌ها")
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            ForEach(catalog.home.categories.prefix(10)) { category in
                                NavigationLink {
                                    CatalogView(categoryID: category.id, title: category.name)
                                } label: {
                                    VStack(spacing: 7) {
                                        ProductImageView(url: category.image)
                                            .frame(width: 68, height: 68)
                                            .clipShape(Circle())
                                            .overlay(
                                                Circle().stroke(PMColor.border.opacity(0.7), lineWidth: 1)
                                            )

                                        Text(category.name)
                                            .font(.caption)
                                            .foregroundStyle(.primary)
                                            .lineLimit(2)
                                            .multilineTextAlignment(.center)
                                    }
                                    .frame(width: 84)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(.horizontal)
                    }
                }

                if !catalog.home.featuredProducts.isEmpty {
                    sectionHeader("پرفروش و منتخب")
                    productRow(catalog.home.featuredProducts)
                }

                if !catalog.home.onSaleProducts.isEmpty {
                    sectionHeader("پیشنهادهای ویژه")
                    productRow(catalog.home.onSaleProducts)
                }

                sectionHeader("جدیدترین محصولات")
                productRow(catalog.home.latestProducts)

                if let error = catalog.error, catalog.home.latestProducts.isEmpty {
                    ContentUnavailableView(
                        "اتصال برقرار نشد",
                        systemImage: "wifi.exclamationmark",
                        description: Text(error)
                    )
                    Button("تلاش دوباره") {
                        Task { await catalog.loadHome(force: true) }
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(PMColor.primary)
                }
            }
            .padding(.bottom, 24)
        }
        .background(PMColor.background)
        .navigationDestination(for: Product.self) { ProductDetailView(product: $0) }
        .task { await catalog.loadHome() }
        .refreshable { await catalog.loadHome(force: true) }
    }

    private var header: some View {
        VStack(alignment: .trailing, spacing: 12) {
            HStack {
                NavigationLink {
                    CatalogView()
                } label: {
                    Image(systemName: "magnifyingglass")
                        .font(.title3)
                        .foregroundStyle(PMColor.primary)
                        .frame(width: 42, height: 42)
                        .background(PMColor.surface)
                        .clipShape(Circle())
                        .overlay(Circle().stroke(PMColor.border.opacity(0.7), lineWidth: 1))
                }

                Spacer()

                VStack(alignment: .trailing, spacing: 2) {
                    Text(AppConfig.appName)
                        .font(.title2.bold())
                        .foregroundStyle(PMColor.primary)
                    Text(AppConfig.appSubtitle)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }

            NavigationLink {
                CatalogView()
            } label: {
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundStyle(.secondary)
                    Text("جستجوی محصولات، دسته‌ها و برندها...")
                        .foregroundStyle(.secondary)
                    Spacer()
                }
                .padding(.horizontal, 14)
                .frame(height: 48)
                .background(PMColor.surface)
                .clipShape(RoundedRectangle(cornerRadius: 15))
                .overlay(
                    RoundedRectangle(cornerRadius: 15)
                        .stroke(PMColor.border.opacity(0.7), lineWidth: 1)
                )
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal)
        .padding(.top, 10)
    }

    private func hero(product: Product) -> some View {
        NavigationLink(value: product) {
            HStack(spacing: 12) {
                VStack(alignment: .trailing, spacing: 8) {
                    Text("انتخاب‌های ویژه \(AppConfig.appName)")
                        .font(.caption.bold())
                        .foregroundStyle(Color(hex: 0x9A741C))
                    Text(product.name)
                        .font(.title3.bold())
                        .foregroundStyle(PMColor.primary)
                        .lineLimit(2)
                        .multilineTextAlignment(.trailing)
                    Text(toman(product.price))
                        .font(.subheadline.bold())
                        .foregroundStyle(PMColor.primary)
                }
                .frame(maxWidth: .infinity, alignment: .trailing)

                ProductImageView(url: product.image)
                    .frame(width: 130, height: 120)
                    .clipShape(RoundedRectangle(cornerRadius: 18))
            }
            .padding(16)
            .background(PMColor.surface)
            .clipShape(RoundedRectangle(cornerRadius: 22))
            .overlay(
                RoundedRectangle(cornerRadius: 22)
                    .stroke(PMColor.border.opacity(0.7), lineWidth: 1)
            )
            .padding(.horizontal)
        }
        .buttonStyle(.plain)
    }

    private func productRow(_ products: [Product]) -> some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                ForEach(products) { product in
                    NavigationLink(value: product) {
                        ProductCardView(product: product)
                            .frame(width: 178)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal)
        }
    }

    private func sectionHeader(_ title: String) -> some View {
        HStack {
            Spacer()
            Text(title)
                .font(.title3.bold())
                .foregroundStyle(PMColor.primary)
        }
        .padding(.horizontal)
        .padding(.top, 4)
    }
}
