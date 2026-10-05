package com.pakhshmahdi.app.core

import com.pakhshmahdi.app.BuildConfig

object AppConfig {
    val CLIENT_ID: String = BuildConfig.CLIENT_ID
    val APP_NAME: String = BuildConfig.APP_NAME
    val APP_SUBTITLE: String = BuildConfig.APP_SUBTITLE

    val BASE_URL: String = BuildConfig.API_BASE_URL
    val STORE_API_URL: String = BuildConfig.STORE_API_URL
    val PWS_AJAX_URL: String = BuildConfig.PWS_AJAX_URL

    val SUPPORT_URL: String = BuildConfig.SUPPORT_URL
    val CONTACT_URL: String = BuildConfig.CONTACT_URL

    val FEATURE_WISHLIST: Boolean = BuildConfig.FEATURE_WISHLIST
    val FEATURE_OTP: Boolean = BuildConfig.FEATURE_OTP
    val FEATURE_COUPONS: Boolean = BuildConfig.FEATURE_COUPONS
    val FEATURE_NOTIFICATIONS: Boolean = BuildConfig.FEATURE_NOTIFICATIONS
    val FEATURE_SUPPORT: Boolean = BuildConfig.FEATURE_SUPPORT
    val FEATURE_PWS_SHIPPING: Boolean = BuildConfig.FEATURE_PWS_SHIPPING
    val FEATURE_ONLINE_PAYMENT: Boolean = BuildConfig.FEATURE_ONLINE_PAYMENT
}
