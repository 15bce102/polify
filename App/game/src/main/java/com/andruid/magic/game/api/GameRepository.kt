package com.andruid.magic.game.api

import com.andruid.magic.game.model.response.BattleResponse
import com.andruid.magic.game.model.response.LoginResponse
import com.andruid.magic.game.model.response.RoomResponse
import com.andruid.magic.game.server.RetrofitClient
import com.andruid.magic.game.server.RetrofitService
import com.andruid.magic.game.util.sendNetworkRequest

object GameRepository {
    private val service = RetrofitClient.getRetrofitInstance().create(RetrofitService::class.java)

    suspend fun login(uid: String): LoginResponse? {
        val response = sendNetworkRequest { service.login(uid) }
        if (response?.isSuccessful == true)
            return response.body()
        return null
    }

    suspend fun createBattle(uid: String, coins: Int): BattleResponse? {
        val response = sendNetworkRequest { service.createBattle(uid, coins) }
        if (response?.isSuccessful == true)
            return response.body()
        return null
    }

    suspend fun joinBattle(uid: String, bid: String): BattleResponse? {
        val response = sendNetworkRequest { service.joinBattle(uid, bid) }
        if (response?.isSuccessful == true)
            return response.body()
        return null
    }

    suspend fun getMyRooms(uid: String, pageStart: Int, pageSize: Int): RoomResponse? {
        val response = sendNetworkRequest { service.getMyRooms(uid, pageStart, pageSize) }
        if (response?.isSuccessful == true)
            return response.body()
        return null
    }
}