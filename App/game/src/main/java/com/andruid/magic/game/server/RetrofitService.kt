package com.andruid.magic.game.server

import com.andruid.magic.game.model.response.BattleResponse
import com.andruid.magic.game.model.response.LoginResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {
    @GET("/login")
    suspend fun login(@Query("uid") uid: String): Response<LoginResponse>

    @GET("/create-battle")
    suspend fun createBattle(@Query("uid") uid: String, @Query("coins") coins: Int): Response<BattleResponse>

    @GET("/join-battle")
    suspend fun joinBattle(@Query("uid") uid: String, @Query("bid") bid: String): Response<BattleResponse>
}