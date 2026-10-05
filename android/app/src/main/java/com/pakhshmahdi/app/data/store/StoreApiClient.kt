package com.pakhshmahdi.app.data.store

import com.pakhshmahdi.app.BuildConfig
import com.pakhshmahdi.app.core.AppConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object StoreApiClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val http = OkHttpClient.Builder()
        .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    val api: WooStoreApi = Retrofit.Builder()
        .baseUrl(AppConfig.STORE_API_URL)
        .client(http)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WooStoreApi::class.java)
}
