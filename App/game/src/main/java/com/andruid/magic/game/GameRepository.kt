package com.andruid.magic.game

import com.andruid.magic.game.model.response.BattleResponse
import com.andruid.magic.game.model.response.LoginResponse
import com.andruid.magic.game.server.RetrofitClient
import com.andruid.magic.game.server.RetrofitService

object GameRepository {
    private val service = RetrofitClient.getRetrofitInstance().create(RetrofitService::class.java)

    suspend fun login(uid: String): LoginResponse? {
        val response = service.login(uid)
        if (response.isSuccessful)
            return response.body()
        return null
    }

    suspend fun createBattle(uid: String): BattleResponse? {
        val response = service.createBattle(uid)
        if (response.isSuccessful)
            return response.body()
        return null
    }
}