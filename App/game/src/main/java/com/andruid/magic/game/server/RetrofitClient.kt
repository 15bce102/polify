package com.andruid.magic.game.server

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    //private const val BASE_URL = "http://192.168.0.104:5000/"
    private const val BASE_URL = "http://polify.herokuapp.com/"

    const val DEFAULT_AVATAR_URL = "${BASE_URL}avatars/avatar1.png"

    private var INSTANCE = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    fun getRetrofitInstance(): Retrofit = INSTANCE
}