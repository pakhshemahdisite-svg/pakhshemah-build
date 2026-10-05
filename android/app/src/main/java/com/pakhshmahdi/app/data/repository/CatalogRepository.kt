package com.pakhshmahdi.app.data.repository

import com.pakhshmahdi.app.data.model.Category
import com.pakhshmahdi.app.data.model.HomePayload
import com.pakhshmahdi.app.data.model.Product
import com.pakhshmahdi.app.data.model.ProductsPayload
import com.pakhshmahdi.app.data.remote.ApiClient

class CatalogRepository {
    suspend fun home(): HomePayload = ApiClient.api.home()

    suspend fun categories(): List<Category> = ApiClient.api.categories().items

    suspend fun productsPage(
        page: Int = 1,
        perPage: Int = 20,
        category: Long? = null,
        search: String? = null,
        orderBy: String = "date",
        order: String = "DESC"
    ): ProductsPayload = ApiClient.api.products(
        page = page,
        perPage = perPage,
        category = category,
        search = search,
        orderBy = orderBy,
        order = order
    )

    suspend fun products(
        page: Int = 1,
        perPage: Int = 20,
        category: Long? = null,
        search: String? = null,
        orderBy: String = "date",
        order: String = "DESC"
    ): List<Product> = productsPage(
        page = page,
        perPage = perPage,
        category = category,
        search = search,
        orderBy = orderBy,
        order = order
    ).items

    suspend fun product(id: Long): Product = ApiClient.api.product(id)
}
