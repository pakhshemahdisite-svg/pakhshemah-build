package com.pakhshmahdi.app.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pakhshmahdi.app.data.auth.AuthUser
import com.pakhshmahdi.app.data.cart.CartLine
import com.pakhshmahdi.app.data.model.Product
import com.pakhshmahdi.app.data.store.PendingPayment
import com.pakhshmahdi.app.data.store.StoreAddress
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object LocalStore {
    private const val PREFS = "pakhsh_mahdi_local"
    private const val KEY_CART = "cart"
    private const val KEY_WISHLIST = "wishlist"
    private const val KEY_RECENTLY_VIEWED = "recently_viewed"
    private const val KEY_CHECKOUT_ADDRESS = "checkout_address"
    private const val KEY_STORE_CART_TOKEN = "store_cart_token"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_AUTH_USER = "auth_user"
    private const val KEY_PENDING_PAYMENT = "pending_payment"
    private const val KEY_ORDER_STATUSES = "order_statuses"
    private const val KEYSTORE_ALIAS = "pakhsh_mahdi_local_aes"
    private const val ENCRYPTED_PREFIX = "enc:v1:"

    private val gson = Gson()
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun saveCart(lines: List<CartLine>) {
        prefs()?.edit()?.putString(KEY_CART, gson.toJson(lines))?.apply()
    }

    fun loadCart(): List<CartLine> {
        val json = prefs()?.getString(KEY_CART, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<CartLine>>() {}.type
            gson.fromJson<List<CartLine>>(json, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    fun saveWishlist(items: List<Product>) {
        prefs()?.edit()?.putString(KEY_WISHLIST, gson.toJson(items))?.apply()
    }

    fun loadWishlist(): List<Product> {
        val json = prefs()?.getString(KEY_WISHLIST, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<Product>>() {}.type
            gson.fromJson<List<Product>>(json, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    fun saveRecentlyViewed(items: List<Product>) {
        prefs()?.edit()?.putString(KEY_RECENTLY_VIEWED, gson.toJson(items))?.apply()
    }

    fun loadRecentlyViewed(): List<Product> {
        val json = prefs()?.getString(KEY_RECENTLY_VIEWED, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<Product>>() {}.type
            gson.fromJson<List<Product>>(json, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    fun saveCheckoutAddress(address: StoreAddress) {
        securePut(KEY_CHECKOUT_ADDRESS, gson.toJson(address))
    }

    fun loadCheckoutAddress(): StoreAddress? {
        val json = secureGet(KEY_CHECKOUT_ADDRESS) ?: return null
        return runCatching { gson.fromJson(json, StoreAddress::class.java) }.getOrNull()
    }

    fun clearCheckoutAddress() = securePut(KEY_CHECKOUT_ADDRESS, null)

    fun saveStoreCartToken(token: String?) = securePut(KEY_STORE_CART_TOKEN, token)

    fun loadStoreCartToken(): String? = secureGet(KEY_STORE_CART_TOKEN)

    fun saveAuthToken(token: String?) = securePut(KEY_AUTH_TOKEN, token)

    fun loadAuthToken(): String? = secureGet(KEY_AUTH_TOKEN)

    fun saveAuthUser(user: AuthUser?) {
        securePut(KEY_AUTH_USER, user?.let { gson.toJson(it) })
    }

    fun loadAuthUser(): AuthUser? {
        val json = secureGet(KEY_AUTH_USER) ?: return null
        return runCatching { gson.fromJson(json, AuthUser::class.java) }.getOrNull()
    }

    fun clearAccountSession() {
        securePut(KEY_AUTH_TOKEN, null)
        securePut(KEY_AUTH_USER, null)
        securePut(KEY_PENDING_PAYMENT, null)
        securePut(KEY_STORE_CART_TOKEN, null)
        securePut(KEY_CHECKOUT_ADDRESS, null)
        saveOrderStatuses(emptyMap())
    }

    fun savePendingPayment(payment: PendingPayment?) {
        securePut(KEY_PENDING_PAYMENT, payment?.let { gson.toJson(it) })
    }

    fun loadPendingPayment(): PendingPayment? {
        val json = secureGet(KEY_PENDING_PAYMENT) ?: return null
        return runCatching { gson.fromJson(json, PendingPayment::class.java) }.getOrNull()
    }

    fun saveOrderStatuses(statuses: Map<Long, String>) {
        prefs()?.edit()?.putString(KEY_ORDER_STATUSES, gson.toJson(statuses))?.apply()
    }

    fun loadOrderStatuses(): Map<Long, String> {
        val json = prefs()?.getString(KEY_ORDER_STATUSES, null) ?: return emptyMap()
        return runCatching {
            val type = object : TypeToken<Map<Long, String>>() {}.type
            gson.fromJson<Map<Long, String>>(json, type) ?: emptyMap()
        }.getOrDefault(emptyMap())
    }

    private fun securePut(key: String, value: String?) {
        val editor = prefs()?.edit() ?: return
        if (value.isNullOrBlank()) {
            editor.remove(key).apply()
            return
        }

        val encrypted = runCatching { encrypt(value) }.getOrNull() ?: return
        editor.putString(key, encrypted).apply()
    }

    private fun secureGet(key: String): String? {
        val raw = prefs()?.getString(key, null) ?: return null
        if (!raw.startsWith(ENCRYPTED_PREFIX)) {
            // Migrate data stored by older app versions after the first successful read.
            securePut(key, raw)
            return raw
        }

        return runCatching { decrypt(raw) }.getOrNull()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, localSecretKey())
        val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val payload = Base64.encodeToString(
            cipher.doFinal(value.toByteArray(Charsets.UTF_8)),
            Base64.NO_WRAP
        )
        return "$ENCRYPTED_PREFIX$iv:$payload"
    }

    private fun decrypt(value: String): String {
        val parts = value.removePrefix(ENCRYPTED_PREFIX).split(':', limit = 2)
        require(parts.size == 2)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            localSecretKey(),
            GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP))
        )
        return String(
            cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)),
            Charsets.UTF_8
        )
    }

    private fun localSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEYSTORE_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        ).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            generateKey()
        }
    }

    private fun prefs() = context?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
