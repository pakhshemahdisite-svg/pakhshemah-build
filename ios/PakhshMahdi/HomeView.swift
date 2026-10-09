import SwiftUI

struct HomeView: View {
    @EnvironmentObject private var catalog: CatalogStore

    @State private var bannerIndex = 0

    private let bannerURLs = [
        "https://raw.githubusercontent.com/pakhshemahdisite-svg/pakhshemah-build/main/assets/app-ui/home-banner-1.jpg",
        "https://raw.githubusercontent.com/pakhshemahdisite-svg/pakhshemah-build/main/assets/app-ui/home-banner-2.jpg",
        "https://raw.githubusercontent.com/pakhshemahdisite-svg/pakhshemah-build/main/assets/app-ui/home-banner-3.jpg"
    ]

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                premiumHeader
                approvedSlider

                if !catalog.home.categories.isEmpty {
                    sectionHeader("دسته‌بندی‌ها", destination: AnyView(CategoriesView()))
                    categoryShortcuts
                }

                if !catalog.home.featuredProducts.isEmpty {
                    sectionHeader("پیشنهاد ویژه", destination: AnyView(CatalogView()))
                    productRow(catalog.home.featuredProducts)
                }

                if !catalog.home.onSaleProducts.isEmpty {
                    sectionHeader("تخفیف‌های منتخب", destination: AnyView(CatalogView()))
                    productRow(catalog.home.onSaleProducts)
                }

                sectionHeader("جدیدترین محصولات", destination: AnyView(CatalogView()))
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
                    .tint(PMColor.goldDeep)
                }
            }
            .padding(.bottom, 24)
        }
        .background(PMColor.background)
        .navigationDestination(for: Product.self) { ProductDetailView(product: $0) }
        .task {
            await catalog.loadHome()
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 4_500_000_000)
                guard !Task.isCancelled else { break }
                withAnimation(.easeInOut(duration: 0.45)) {
                    bannerIndex = (bannerIndex + 1) % bannerURLs.count
                }
            }
        }
        .refreshable { await catalog.loadHome(force: true) }
    }

    private var premiumHeader: some View {
        VStack(spacing: 11) {
            ZStack {
                VStack(spacing: 2) {
                    BrandLogoView()
                        .frame(width: 114, height: 72)

                    Text("پخش مهدی")
                        .font(.headline.bold())
                        .foregroundStyle(.primary)
                }

                HStack(spacing: 8) {
                    if AppConfig.featureNotifications {
                        NavigationLink {
                            NotificationsView()
                        } label: {
                            headerIcon("bell")
                        }
                        .buttonStyle(.plain)
                    }

                    if AppConfig.featureWishlist {
                        NavigationLink {
                            WishlistView()
                        } label: {
                            headerIcon("heart")
                        }
                        .buttonStyle(.plain)
                    }

                    Spacer()

                    HStack(spacing: 4) {
                        Image(systemName: "location")
                            .font(.subheadline.bold())
                        Text("تهران")
                            .font(.subheadline.weight(.semibold))
                    }
                    .foregroundStyle(.primary)
                }
            }
            .frame(height: 100)

            NavigationLink {
                CatalogView()
            } label: {
                HStack(spacing: 10) {
                    Image(systemName: "magnifyingglass")
                        .font(.title3.weight(.semibold))
                        .foregroundStyle(.primary)

                    Text("جستجوی محصول، برند، دسته‌بندی ...")
                        .font(.subheadline)
                        .foregroundStyle(PMColor.muted)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                }
                .padding(.horizontal, 15)
                .frame(height: 52)
                .background(PMColor.surface)
                .clipShape(RoundedRectangle(cornerRadius: 18))
                .overlay(
                    RoundedRectangle(cornerRadius: 18)
                        .stroke(PMColor.goldDeep, lineWidth: 1.2)
                )
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 16)
        .padding(.top, 8)
    }

    private func headerIcon(_ name: String) -> some View {
        Image(systemName: name)
            .font(.title3)
            .foregroundStyle(.primary)
            .frame(width: 40, height: 40)
            .background(PMColor.surface)
            .clipShape(Circle())
            .overlay(Circle().stroke(PMColor.border, lineWidth: 1))
    }

    private var approvedSlider: some View {
        VStack(spacing: 9) {
            TabView(selection: $bannerIndex) {
                ForEach(Array(bannerURLs.enumerated()), id: \.offset) { index, url in
                    NavigationLink {
                        CatalogView()
                    } label: {
                        ZStack(alignment: .leading) {
                            AsyncImage(url: URL(string: url)) { phase in
                                switch phase {
                                case .success(let image):
                                    image
                                        .resizable()
                                        .scaledToFill()
                                default:
                                    PMColor.surfaceElevated
                                }
                            }
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .clipped()

                            LinearGradient(
                                colors: [
                                    Color.white.opacity(0.97),
                                    Color.white.opacity(0.74),
                                    Color.clear
                                ],
                                startPoint: .leading,
                                endPoint: .trailing
                            )

                            VStack(alignment: .leading, spacing: 8) {
                                Text("کیفیت در\nهر آشپزخانه")
                                    .font(.title.bold())
                                    .foregroundStyle(Color(hex: 0x101010))
                                    .multilineTextAlignment(.leading)

                                Text("انتخاب حرفه‌ای‌ها\nبا پخش مهدی")
                                    .font(.subheadline.weight(.semibold))
                                    .foregroundStyle(Color(hex: 0x3E3A33))

                                Text("مشاهده محصولات  ‹")
                                    .font(.caption.bold())
                                    .foregroundStyle(.black)
                                    .padding(.horizontal, 13)
                                    .padding(.vertical, 9)
                                    .background(PMColor.gold)
                                    .clipShape(RoundedRectangle(cornerRadius: 13))
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 13)
                                            .stroke(PMColor.goldDeep.opacity(0.5), lineWidth: 1)
                                    )
                            }
                            .padding(.leading, 18)
                            .frame(maxWidth: 195, alignment: .leading)
                        }
                        .clipShape(RoundedRectangle(cornerRadius: 24))
                        .overlay(
                            RoundedRectangle(cornerRadius: 24)
                                .stroke(PMColor.goldDeep.opacity(0.25), lineWidth: 1)
                        )
                    }
                    .buttonStyle(.plain)
                    .padding(.horizontal, 16)
                    .tag(index)
                }
            }
            .frame(height: 220)
            .tabViewStyle(.page(indexDisplayMode: .never))

            HStack(spacing: 6) {
                ForEach(bannerURLs.indices, id: \.self) { index in
                    Capsule()
                        .fill(index == bannerIndex ? PMColor.goldDeep : PMColor.border)
                        .frame(width: index == bannerIndex ? 22 : 8, height: 7)
                        .animation(.easeInOut(duration: 0.2), value: bannerIndex)
                }
            }
        }
    }

    private var categoryShortcuts: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                NavigationLink {
                    CategoriesView()
                } label: {
                    VStack(spacing: 7) {
                        ZStack {
                            Circle()
                                .fill(PMColor.gold.opacity(0.22))
                                .frame(width: 68, height: 68)
                            Image(systemName: "square.grid.2x2")
                                .font(.title2)
                                .foregroundStyle(.primary)
                        }
                        Text("همه دسته‌بندی‌ها")
                            .font(.caption2.bold())
                            .foregroundStyle(.primary)
                            .multilineTextAlignment(.center)
                            .lineLimit(2)
                    }
                    .frame(width: 84)
                }
                .buttonStyle(.plain)

                ForEach(catalog.home.categories.prefix(8)) { category in
                    NavigationLink {
                        CatalogView(categoryID: category.id, title: category.name)
                    } label: {
                        VStack(spacing: 7) {
                            ProductImageView(url: category.image)
                                .frame(width: 68, height: 68)
                                .clipShape(Circle())
                                .overlay(
                                    Circle().stroke(PMColor.border, lineWidth: 1)
                                )

                            Text(category.name)
                                .font(.caption2.weight(.semibold))
                                .foregroundStyle(.primary)
                                .lineLimit(2)
                                .multilineTextAlignment(.center)
                        }
                        .frame(width: 84)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
        }
    }

    private func productRow(_ products: [Product]) -> some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 10) {
                ForEach(products) { product in
                    NavigationLink(value: product) {
                        ProductCardView(product: product)
                            .frame(width: 178)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
        }
    }

    private func sectionHeader(_ title: String, destination: AnyView) -> some View {
        HStack {
            NavigationLink {
                destination
            } label: {
                HStack(spacing: 5) {
                    Image(systemName: "chevron.left")
                    Text("مشاهده همه")
                }
                .font(.caption.weight(.semibold))
                .foregroundStyle(PMColor.goldDeep)
            }

            Spacer()

            Text(title)
                .font(.title3.bold())
                .foregroundStyle(.primary)
        }
        .padding(.horizontal, 16)
        .padding(.top, 4)
    }
}
