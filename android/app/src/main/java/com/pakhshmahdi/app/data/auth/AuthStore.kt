package com.pakhshmahdi.app.data.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pakhshmahdi.app.data.local.LocalStore
import com.pakhshmahdi.app.data.store.StoreAddress

object AuthStore {
    var token by mutableStateOf<String?>(null)
        private set

    var user by mutableStateOf<AuthUser?>(null)
        private set

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank() && user != null

    fun restore() {
        token = LocalStore.loadAuthToken()
        user = LocalStore.loadAuthUser()
        if (token.isNullOrBlank() || user == null) {
            token = null
            user = null
        }
    }

    fun login(newToken: String, newUser: AuthUser) {
        token = newToken
        user = newUser
        LocalStore.saveAuthToken(newToken)
        LocalStore.saveAuthUser(newUser)
        syncCheckoutAddress(newUser)
    }

    fun updateUser(newUser: AuthUser) {
        user = newUser
        LocalStore.saveAuthUser(newUser)
        syncCheckoutAddress(newUser)
    }

    fun logout() {
        token = null
        user = null
        LocalStore.clearAccountSession()
    }

    private fun syncCheckoutAddress(user: AuthUser) {
        val billing = user.billing
        if (billing.phone.isBlank() && user.phone.isBlank()) return
        LocalStore.saveCheckoutAddress(
            StoreAddress(
                firstName = billing.firstName.ifBlank { user.firstName },
                lastName = billing.lastName.ifBlank { user.lastName },
                address1 = billing.address1,
                address2 = billing.address2,
                city = billing.city,
                state = billing.state.ifBlank { "558" },
                postcode = billing.postcode,
                country = billing.country.ifBlank { "IR" },
                phone = billing.phone.ifBlank { user.phone },
                email = billing.email.ifBlank { user.email }
            )
        )
    }
}
