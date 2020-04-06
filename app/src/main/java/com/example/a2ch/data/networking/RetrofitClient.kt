package com.example.a2ch.data.networking

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val DVACH_API = "https://2ch.hk/"

    val dvach: DvachApi = Retrofit.Builder()
        .baseUrl(DVACH_API)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DvachApi::class.java)
}