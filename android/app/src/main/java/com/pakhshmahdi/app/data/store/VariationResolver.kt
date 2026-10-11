package com.pakhshmahdi.app.data.store

import com.pakhshmahdi.app.data.model.Product
import com.pakhshmahdi.app.data.model.ProductAttribute
import com.pakhshmahdi.app.data.model.ProductVariation
import com.pakhshmahdi.app.data.model.ProductVariationSelection
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object VariationResolver {

    fun enrich(
        product: Product,
        storeVariations: List<StoreProductVariation>
    ): Product {
        if (product.variations.isEmpty()) return product

        val storeById = storeVariations.associateBy { it.id }

        val enriched = product.variations.mapIndexed { index, variation ->
            val store = storeById[variation.id]
            val exactSelection = store?.let { selectionFromPermalink(product, variation, it) }
                .orEmpty()
                .ifEmpty { selectionFromAttributesMap(product, variation.attributes) }

            val displayAttributes = when {
                variation.attributes.isNotEmpty() -> variation.attributes
                exactSelection.isNotEmpty() -> exactSelection
                    .associate { it.label.ifBlank { it.attribute } to it.displayValue.ifBlank { it.value } }
                else -> fallbackDisplayAttributes(product, index)
            }

            val resolvedSelection = exactSelection.ifEmpty {
                selectionFromAttributesMap(product, displayAttributes)
            }

            variation.copy(
                attributes = displayAttributes,
                selection = resolvedSelection
            )
        }

        return product.copy(variations = enriched)
    }

    fun selectionForCart(
        product: Product,
        variation: ProductVariation
    ): List<ProductVariationSelection> =
        variation.selection.ifEmpty { selectionFromAttributesMap(product, variation.attributes) }

    private fun selectionFromPermalink(
        product: Product,
        variation: ProductVariation,
        store: StoreProductVariation
    ): List<ProductVariationSelection> {
        val rawQuery = store.permalink.substringAfter('?', "")
        if (rawQuery.isBlank()) return emptyList()

        val displayPairs = parseVariationLabel(store.variation)
        val customDisplay = variation.attributes

        return rawQuery
            .split('&')
            .mapNotNull { part ->
                val pieces = part.split('=', limit = 2)
                if (pieces.size != 2) return@mapNotNull null

                val rawName = decode(pieces[0])
                val rawValue = decode(pieces[1])
                if (!rawName.startsWith("attribute_") || rawValue.isBlank()) {
                    return@mapNotNull null
                }

                val requestAttribute = if (rawName.startsWith("attribute_pa_")) {
                    rawName.removePrefix("attribute_")
                } else {
                    rawName
                }

                val parentAttribute = findParentAttribute(product, requestAttribute, rawName)
                val label = parentAttribute?.name
                    ?: customDisplay.keys.firstOrNull { normalize(it) == normalize(requestAttribute) }
                    ?: requestAttribute

                val displayValue = customDisplay.entries
                    .firstOrNull { entry ->
                        normalize(entry.key) == normalize(label) ||
                            normalize(entry.key) == normalize(parentAttribute?.slug.orEmpty())
                    }
                    ?.value
                    ?.takeIf { it.isNotBlank() }
                    ?: displayPairs.entries
                        .firstOrNull { normalize(it.key) == normalize(label) }
                        ?.value
                    ?: displayPairs.values.firstOrNull { normalize(it) == normalize(rawValue) }
                    ?: rawValue

                ProductVariationSelection(
                    attribute = requestAttribute,
                    value = rawValue,
                    label = label,
                    displayValue = displayValue
                )
            }
    }

    private fun selectionFromAttributesMap(
        product: Product,
        attributes: Map<String, String>
    ): List<ProductVariationSelection> =
        attributes.mapNotNull { (labelOrSlug, displayValue) ->
            if (displayValue.isBlank()) return@mapNotNull null

            val parent = product.attributes.firstOrNull { attribute ->
                attribute.variation && (
                    normalize(attribute.name) == normalize(labelOrSlug) ||
                        normalize(attribute.slug) == normalize(labelOrSlug)
                    )
            }

            val requestAttribute = when {
                parent == null -> labelOrSlug
                parent.slug.startsWith("pa_") -> parent.slug
                parent.slug.startsWith("attribute_") -> parent.slug
                else -> parent.name
            }

            ProductVariationSelection(
                attribute = requestAttribute,
                value = displayValue,
                label = parent?.name ?: labelOrSlug,
                displayValue = displayValue
            )
        }

    private fun fallbackDisplayAttributes(
        product: Product,
        variationIndex: Int
    ): Map<String, String> {
        val variationAttributes = product.attributes.filter { it.variation }
        if (variationAttributes.size != 1) return emptyMap()

        val attribute = variationAttributes.first()
        val option = attribute.options.getOrNull(variationIndex) ?: return emptyMap()
        return mapOf(attribute.name to option)
    }

    private fun findParentAttribute(
        product: Product,
        requestAttribute: String,
        rawName: String
    ): ProductAttribute? {
        val rawWithoutPrefix = rawName.removePrefix("attribute_")
        return product.attributes.firstOrNull { attribute ->
            attribute.variation && (
                normalize(attribute.slug) == normalize(requestAttribute) ||
                    normalize(attribute.slug) == normalize(rawWithoutPrefix) ||
                    normalize(attribute.name) == normalize(requestAttribute) ||
                    normalize(attribute.name) == normalize(rawWithoutPrefix)
                )
        }
    }

    private fun parseVariationLabel(value: String): LinkedHashMap<String, String> {
        val result = linkedMapOf<String, String>()
        value.split(',', '،').forEach { part ->
            val clean = part.trim()
            val separator = clean.indexOf(':')
            if (separator > 0 && separator < clean.lastIndex) {
                val key = clean.substring(0, separator).trim()
                val itemValue = clean.substring(separator + 1).trim()
                if (key.isNotBlank() && itemValue.isNotBlank()) {
                    result[key] = itemValue
                }
            }
        }
        return result
    }

    private fun decode(value: String): String =
        runCatching {
            URLDecoder.decode(value, StandardCharsets.UTF_8.name())
        }.getOrDefault(value)

    private fun normalize(value: String): String =
        value.trim()
            .lowercase()
            .removePrefix("attribute_")
            .replace("-", "")
            .replace("_", "")
            .replace(" ", "")
}
