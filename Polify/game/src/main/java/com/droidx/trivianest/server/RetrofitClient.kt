package com.droidx.trivianest.server

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    const val BASE_URL = "http://polify.herokuapp.com/"

    const val DEFAULT_AVATAR_URL = "${BASE_URL}avatars/avatar1.jpg"

    private var INSTANCE = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    fun getRetrofitInstance(): Retrofit = INSTANCE
}