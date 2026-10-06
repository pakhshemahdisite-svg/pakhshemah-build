import SwiftUI

struct CatalogView: View {
    let categoryID: Int?
    let title: String

    @StateObject private var catalog = CatalogStore()
    @State private var searchTask: Task<Void, Never>?
    init(categoryID: Int? = nil, title: String = "محصولات") {
        self.categoryID = categoryID
        self.title = title
    }

    private let columns = [
        GridItem(.flexible(), spacing: 10),
        GridItem(.flexible(), spacing: 10)
    ]

    var body: some View {
        ScrollView {
            VStack(alignment: .trailing, spacing: 12) {
                Text(title)
                    .font(.title.bold())
                    .frame(maxWidth: .infinity, alignment: .trailing)

                if let error = catalog.error, catalog.products.isEmpty {
                    ContentUnavailableView(
                        "اتصال برقرار نشد",
                        systemImage: "wifi.exclamationmark",
                        description: Text(error)
                    )
                    Button("تلاش دوباره") {
                        Task { await catalog.loadProducts(reset: true, category: categoryID) }
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(PMColor.primary)
                } else {
                    LazyVGrid(columns: columns, spacing: 10) {
                        ForEach(catalog.products) { product in
                            NavigationLink(value: product) {
                                ProductCardView(product: product)
                            }
                            .buttonStyle(.plain)
                            .onAppear {
                                if product.id == catalog.products.last?.id {
                                    Task { await catalog.loadProducts(category: categoryID) }
                                }
                            }
                        }
                    }

                    if catalog.loading || catalog.loadingMore {
                        ProgressView().tint(PMColor.primary).padding()
                    }
                }
            }
            .padding()
        }
        .background(PMColor.background)
        .navigationDestination(for: Product.self) { ProductDetailView(product: $0) }
        .searchable(text: $catalog.query, prompt: "جستجوی محصولات...")
        .onSubmit(of: .search) {
            searchTask?.cancel()
            Task { await catalog.search(catalog.query, category: categoryID) }
        }
        .onChange(of: catalog.query) { _, value in
            searchTask?.cancel()
            searchTask = Task {
                try? await Task.sleep(for: .milliseconds(450))
                guard !Task.isCancelled else { return }
                await catalog.search(value, category: categoryID)
            }
        }
        .onDisappear {
            searchTask?.cancel()
        }
        .task {
            if catalog.products.isEmpty {
                await catalog.loadProducts(reset: true, category: categoryID)
            }
        }
        .refreshable {
            await catalog.loadProducts(reset: true, category: categoryID)
        }
    }
}
