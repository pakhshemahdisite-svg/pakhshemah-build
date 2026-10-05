package com.pakhshmahdi.app.data.auth

import com.google.gson.Gson
import com.pakhshmahdi.app.data.remote.ApiClient
import retrofit2.HttpException

class AuthRepository {
    private val api = ApiClient.api
    private val gson = Gson()

    suspend fun requestOtp(phone: String): OtpRequestResponse =
        api.requestOtp(OtpRequest(phone))

    suspend fun verifyOtp(phone: String, code: String): AuthResponse =
        api.verifyOtp(OtpVerifyRequest(phone, code))

    suspend fun refreshMe(): AuthUser =
        authenticated { authorization ->
            api.me(authorization).user
        }

    suspend fun updateProfile(
        firstName: String,
        lastName: String,
        email: String,
        billing: BillingProfile
    ): AuthUser =
        authenticated { authorization ->
            api.updateMe(
                authorization,
                UpdateProfileRequest(
                    firstName = firstName,
                    lastName = lastName,
                    email = email
                )
            )
            api.updateAddress(authorization, billing)
            api.me(authorization).user
        }

    suspend fun orders(): List<OrderSummary> =
        authenticated { authorization ->
            api.orders(authorization).items
        }

    suspend fun orderDetail(id: Long): OrderDetail =
        authenticated { authorization ->
            api.orderDetail(authorization, id)
        }

    fun readableError(error: Throwable, fallback: String): String {
        if (error is AuthException) return error.message ?: fallback
        if (error is HttpException) {
            val raw = runCatching { error.response()?.errorBody()?.string() }.getOrNull()
            if (!raw.isNullOrBlank()) {
                val parsed = runCatching { gson.fromJson(raw, AuthApiError::class.java) }.getOrNull()
                if (!parsed?.message.isNullOrBlank()) return parsed?.message ?: fallback
            }
        }
        return fallback
    }

    private suspend fun <T> authenticated(
        call: suspend (authorization: String) -> T
    ): T {
        val token = AuthStore.token
            ?: throw AuthException("ابتدا وارد حساب کاربری شوید.")

        return try {
            call("Bearer $token")
        } catch (error: HttpException) {
            if (error.code() == 401 || error.code() == 403) {
                AuthStore.logout()
                throw AuthException("نشست شما منقضی شده است. دوباره وارد حساب کاربری شوید.")
            }
            throw error
        }
    }
}

class AuthException(message: String) : Exception(message)
