package com.pakhshmahdi.app.data.store

import com.pakhshmahdi.app.data.model.Product
import com.pakhshmahdi.app.data.model.ProductAttribute
import com.pakhshmahdi.app.data.model.ProductVariation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VariationResolverTest {

    @Test
    fun globalAttributeUsesWooSlugAndShowsPersianLabel() {
        val product = Product(
            id = 100,
            name = "محصول متغیر",
            type = "variable",
            price = "97000",
            attributes = listOf(
                ProductAttribute(
                    name = "رنگ",
                    slug = "pa_color",
                    options = listOf("طلایی", "مسی"),
                    variation = true
                )
            ),
            variations = listOf(
                ProductVariation(id = 101, price = "97000"),
                ProductVariation(id = 102, price = "97000")
            )
        )

        val enriched = VariationResolver.enrich(
            product,
            listOf(
                StoreProductVariation(
                    id = 101,
                    variation = "رنگ: طلایی",
                    permalink = "https://example.com/product/x/?attribute_pa_color=%D8%B7%D9%84%D8%A7%DB%8C%DB%8C"
                )
            )
        )

        val variation = enriched.variations.first()
        assertEquals("طلایی", variation.attributes["رنگ"])
        assertEquals("pa_color", variation.selection.first().attribute)
        assertEquals("طلایی", variation.selection.first().value)
        assertEquals("رنگ", variation.selection.first().label)
    }

    @Test
    fun productSpecificAttributeKeepsAttributeQueryKey() {
        val product = Product(
            id = 200,
            name = "محصول مدل‌دار",
            type = "variable",
            price = "100000",
            attributes = listOf(
                ProductAttribute(
                    name = "مدل",
                    slug = "model",
                    options = listOf("بزرگ"),
                    variation = true
                )
            ),
            variations = listOf(ProductVariation(id = 201, price = "100000"))
        )

        val enriched = VariationResolver.enrich(
            product,
            listOf(
                StoreProductVariation(
                    id = 201,
                    variation = "مدل: بزرگ",
                    permalink = "https://example.com/product/x/?attribute_model=large"
                )
            )
        )

        val selection = enriched.variations.first().selection.single()
        assertEquals("attribute_model", selection.attribute)
        assertEquals("large", selection.value)
        assertEquals("مدل", selection.label)
        assertEquals("بزرگ", selection.displayValue)
    }

    @Test
    fun singleAttributeFallsBackToParentOptionWithoutGenericOptionLabel() {
        val product = Product(
            id = 300,
            name = "محصول رنگی",
            type = "variable",
            price = "100000",
            attributes = listOf(
                ProductAttribute(
                    name = "رنگ",
                    slug = "pa_color",
                    options = listOf("طلایی", "مسی"),
                    variation = true
                )
            ),
            variations = listOf(
                ProductVariation(id = 301, price = "100000"),
                ProductVariation(id = 302, price = "100000")
            )
        )

        val enriched = VariationResolver.enrich(
            product = product,
            storeVariations = emptyList(),
            termSlugs = mapOf(
                "pa_color" to mapOf(
                    "طلایی" to "gold",
                    "مسی" to "copper"
                )
            )
        )

        assertEquals("طلایی", enriched.variations[0].attributes["رنگ"])
        assertEquals("مسی", enriched.variations[1].attributes["رنگ"])
        assertEquals("gold", enriched.variations[0].selection.single().value)
        assertEquals("copper", enriched.variations[1].selection.single().value)
        assertTrue(enriched.variations.all { it.selection.isNotEmpty() })
    }
}
