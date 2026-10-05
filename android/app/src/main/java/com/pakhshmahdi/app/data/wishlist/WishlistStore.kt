package com.pakhshmahdi.app.data.wishlist

import androidx.compose.runtime.mutableStateListOf
import com.pakhshmahdi.app.data.local.LocalStore
import com.pakhshmahdi.app.data.model.Product

object WishlistStore {
    val items = mutableStateListOf<Product>()
    private var restored = false

    fun restore() {
        if (restored) return
        restored = true
        items.clear()
        items.addAll(LocalStore.loadWishlist())
    }

    fun contains(productId: Long): Boolean = items.any { it.id == productId }

    fun toggle(product: Product) {
        val index = items.indexOfFirst { it.id == product.id }
        if (index >= 0) items.removeAt(index) else items.add(product)
        persist()
    }

    fun remove(productId: Long) {
        items.removeAll { it.id == productId }
        persist()
    }

    fun replaceAll(products: List<Product>) {
        items.clear()
        items.addAll(products.distinctBy { it.id })
        persist()
    }

    private fun persist() = LocalStore.saveWishlist(items.toList())
}
