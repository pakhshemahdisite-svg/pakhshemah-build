package com.pakhshmahdi.app.data.recent

import androidx.compose.runtime.mutableStateListOf
import com.pakhshmahdi.app.data.local.LocalStore
import com.pakhshmahdi.app.data.model.Product

object RecentlyViewedStore {
    val items = mutableStateListOf<Product>()
    private var restored = false

    fun restore() {
        if (restored) return
        restored = true
        items.clear()
        items.addAll(LocalStore.loadRecentlyViewed().take(10))
    }

    fun add(product: Product) {
        items.removeAll { it.id == product.id }
        items.add(0, product)
        while (items.size > 10) items.removeAt(items.lastIndex)
        LocalStore.saveRecentlyViewed(items.toList())
    }
}
