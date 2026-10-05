package com.pakhshmahdi.app.data.model

data class Category(
    val id: Long,
    val name: String,
    val slug: String,
    val image: String? = null,
    val count: Int = 0
)

data class ProductAttribute(
    val name: String = "",
    val slug: String = "",
    val options: List<String> = emptyList(),
    val variation: Boolean = false
)

data class ProductVariation(
    val id: Long,
    val price: String = "",
    val regularPrice: String = "",
    val salePrice: String = "",
    val stockStatus: String = "instock",
    val stockQuantity: Int? = null,
    val attributes: Map<String, String> = emptyMap(),
    val image: String? = null
)

data class Product(
    val id: Long,
    val name: String,
    val slug: String = "",
    val type: String = "simple",
    val price: String,
    val regularPrice: String = "",
    val salePrice: String = "",
    val onSale: Boolean = false,
    val purchasable: Boolean = true,
    val stockStatus: String = "instock",
    val stockQuantity: Int? = null,
    val image: String? = null,
    val gallery: List<String> = emptyList(),
    val shortDescription: String = "",
    val description: String = "",
    val averageRating: String = "0",
    val ratingCount: Int = 0,
    val categories: List<Category> = emptyList(),
    val attributes: List<ProductAttribute> = emptyList(),
    val variations: List<ProductVariation> = emptyList()
) {
    val isInStock: Boolean get() = stockStatus == "instock"
    val hasSale: Boolean get() = onSale || (salePrice.isNotBlank() && salePrice != regularPrice)
}

data class HomePayload(
    val categories: List<Category> = emptyList(),
    val latestProducts: List<Product> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val onSaleProducts: List<Product> = emptyList()
)

data class ProductsPayload(
    val items: List<Product> = emptyList(),
    val page: Int = 1,
    val total: Int = 0,
    val totalPages: Int = 1
)

data class CategoriesPayload(
    val items: List<Category> = emptyList()
)
