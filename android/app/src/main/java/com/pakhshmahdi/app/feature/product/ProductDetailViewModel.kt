package com.pakhshmahdi.app.feature.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pakhshmahdi.app.data.model.Product
import com.pakhshmahdi.app.data.repository.CatalogRepository
import com.pakhshmahdi.app.data.store.StoreApiClient
import com.pakhshmahdi.app.data.store.VariationResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProductDetailState(
    val loading: Boolean = true,
    val product: Product? = null,
    val error: String? = null
)

class ProductDetailViewModel : ViewModel() {
    private val repository = CatalogRepository()
    private val _state = MutableStateFlow(ProductDetailState())
    val state: StateFlow<ProductDetailState> = _state

    fun load(id: Long) {
        if (_state.value.product?.id == id) return
        viewModelScope.launch {
            _state.value = ProductDetailState(loading = true)
            runCatching { repository.product(id) }
                .onSuccess { product ->
                    val enriched = if (product.variations.isNotEmpty()) {
                        val storeApi = StoreApiClient.api
                        val storeVariations = runCatching {
                            val response = storeApi.productVariations(parent = product.id)
                            if (response.isSuccessful) response.body().orEmpty() else emptyList()
                        }.getOrDefault(emptyList())

                        val neededTaxonomies = product.attributes
                            .filter { it.variation && it.slug.startsWith("pa_") }
                            .map { it.slug }
                            .distinct()

                        val termSlugs = if (neededTaxonomies.isNotEmpty()) {
                            val taxonomies = runCatching {
                                val response = storeApi.productAttributeTaxonomies()
                                if (response.isSuccessful) response.body().orEmpty() else emptyList()
                            }.getOrDefault(emptyList())

                            neededTaxonomies.associateWith { taxonomySlug ->
                                val taxonomy = taxonomies.firstOrNull { it.taxonomy == taxonomySlug }
                                if (taxonomy == null) {
                                    emptyMap()
                                } else {
                                    runCatching {
                                        val response = storeApi.productAttributeTerms(taxonomy.id)
                                        if (response.isSuccessful) {
                                            response.body().orEmpty().associate { it.name to it.slug }
                                        } else {
                                            emptyMap()
                                        }
                                    }.getOrDefault(emptyMap())
                                }
                            }
                        } else {
                            emptyMap()
                        }

                        VariationResolver.enrich(
                            product = product,
                            storeVariations = storeVariations,
                            termSlugs = termSlugs
                        )
                    } else {
                        product
                    }

                    _state.value = ProductDetailState(
                        loading = false,
                        product = enriched
                    )
                }
                .onFailure {
                    _state.value = ProductDetailState(
                        loading = false,
                        error = "اطلاعات محصول دریافت نشد."
                    )
                }
        }
    }
}
