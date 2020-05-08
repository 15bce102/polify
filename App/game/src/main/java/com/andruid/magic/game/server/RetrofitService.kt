package com.andruid.magic.game.server

import com.andruid.magic.game.model.response.BattleResponse
import com.andruid.magic.game.model.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitService {
    @POST("/login")
    suspend fun login(@Body uid: String): Response<LoginResponse>

    @POST("/create-battle")
    suspend fun createBattle(@Body uid: String): Response<BattleResponse>
}