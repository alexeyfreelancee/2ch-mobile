package com.dvach_2ch.a2ch.data.networking

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val DVACH_API = "https://2ch.hk/"

    private val interceptor =  HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    private val client = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
      //  .addInterceptor(interceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .build()

    val dvach: DvachApi = Retrofit.Builder()
        .baseUrl(DVACH_API)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DvachApi::class.java)
}