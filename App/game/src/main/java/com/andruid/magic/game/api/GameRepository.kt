package com.andruid.magic.game.api

import com.andruid.magic.game.model.response.BattleResponse
import com.andruid.magic.game.model.response.LoginResponse
import com.andruid.magic.game.model.response.RoomResponse
import com.andruid.magic.game.server.RetrofitClient
import com.andruid.magic.game.server.RetrofitService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GameRepository {
    private val service = RetrofitClient.getRetrofitInstance().create(RetrofitService::class.java)

    suspend fun login(uid: String): LoginResponse? {
        val response = withContext(Dispatchers.IO) { service.login(uid) }
        if (response.isSuccessful)
            return response.body()
        return null
    }

    suspend fun createBattle(uid: String, coins: Int): BattleResponse? {
        val response = withContext(Dispatchers.IO) { service.createBattle(uid, coins) }
        if (response.isSuccessful)
            return response.body()
        return null
    }

    suspend fun joinBattle(uid: String, bid: String): BattleResponse? {
        val response = withContext(Dispatchers.IO) { service.joinBattle(uid, bid) }
        if (response.isSuccessful)
            return response.body()
        return null
    }

    suspend fun getMyRooms(uid: String, pageStart: Int, pageSize: Int): RoomResponse? {
        val response = withContext(Dispatchers.IO) { service.getMyRooms(uid, pageStart, pageSize) }
        if (response.isSuccessful)
            return response.body()
        return null
    }
}