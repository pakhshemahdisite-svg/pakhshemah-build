package com.pakhshmahdi.app.data.cart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import com.pakhshmahdi.app.data.local.LocalStore
import com.pakhshmahdi.app.data.model.Product
import com.pakhshmahdi.app.data.model.ProductVariation

data class CartLine(
    val product: Product,
    val variation: ProductVariation? = null,
    val quantity: Int = 1
) {
    val key: String get() = "${product.id}:${variation?.id ?: 0L}"
    val unitPrice: String get() = variation?.price?.takeIf { it.isNotBlank() } ?: product.price
}

object CartStore {
    val lines = mutableStateListOf<CartLine>()

    var totalQuantity by mutableIntStateOf(0)
        private set

    private var restored = false

    fun restore() {
        if (restored) return
        restored = true
        lines.clear()
        lines.addAll(
            LocalStore.loadCart().mapNotNull { line ->
                if (line.quantity <= 0 || !line.product.purchasable || !line.product.isInStock) {
                    return@mapNotNull null
                }
                if (line.variation != null && line.variation.stockStatus != "instock") {
                    return@mapNotNull null
                }
                if (
                    line.variation == null &&
                    (line.product.type == "variable" || line.product.variations.isNotEmpty())
                ) {
                    return@mapNotNull null
                }

                val maxStock = line.variation?.stockQuantity ?: line.product.stockQuantity.takeIf { line.variation == null }
                if (maxStock != null && maxStock <= 0) {
                    null
                } else {
                    line.copy(quantity = maxStock?.let { line.quantity.coerceAtMost(it) } ?: line.quantity)
                }
            }
        )
        syncTotal()
        LocalStore.saveCart(lines.toList())
    }

    fun add(product: Product, variation: ProductVariation? = null, quantity: Int = 1) {
        if (quantity <= 0 || !product.isInStock || !product.purchasable) return
        if ((product.type == "variable" || product.variations.isNotEmpty()) && variation == null) return
        if (variation != null && variation.stockStatus != "instock") return

        val key = "${product.id}:${variation?.id ?: 0L}"
        val index = lines.indexOfFirst { it.key == key }
        val requested = if (index >= 0) lines[index].quantity + quantity else quantity
        val maxStock = variation?.stockQuantity ?: product.stockQuantity.takeIf { variation == null }
        val safeQuantity = maxStock?.let { requested.coerceAtMost(it.coerceAtLeast(0)) } ?: requested
        if (safeQuantity <= 0) return

        if (index >= 0) {
            lines[index] = lines[index].copy(quantity = safeQuantity)
        } else {
            lines.add(CartLine(product = product, variation = variation, quantity = safeQuantity))
        }
        persist()
    }

    fun increment(key: String) {
        val index = lines.indexOfFirst { it.key == key }
        if (index >= 0) {
            val line = lines[index]
            if (line.variation?.stockStatus == "instock" || line.variation == null) {
                val maxStock = line.variation?.stockQuantity ?: line.product.stockQuantity.takeIf { line.variation == null }
                val next = line.quantity + 1
                if (maxStock == null || next <= maxStock) {
                    lines[index] = line.copy(quantity = next)
                    persist()
                }
            }
        }
    }

    fun decrement(key: String) {
        val index = lines.indexOfFirst { it.key == key }
        if (index < 0) return
        val line = lines[index]
        if (line.quantity <= 1) lines.removeAt(index)
        else lines[index] = line.copy(quantity = line.quantity - 1)
        persist()
    }

    fun remove(key: String) {
        lines.removeAll { it.key == key }
        persist()
    }

    fun clear() {
        lines.clear()
        persist()
    }

    fun replaceAll(updated: List<CartLine>) {
        lines.clear()
        lines.addAll(updated)
        persist()
    }

    private fun persist() {
        syncTotal()
        LocalStore.saveCart(lines.toList())
    }

    private fun syncTotal() {
        totalQuantity = lines.sumOf { it.quantity }
    }
}
