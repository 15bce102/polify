package com.andruid.magic.game.api

import com.andruid.magic.game.model.response.*
import com.andruid.magic.game.server.RetrofitClient
import com.andruid.magic.game.server.RetrofitService
import com.andruid.magic.game.util.sendNetworkRequest

object GameRepository {
    private val service = RetrofitClient.getRetrofitInstance().create(RetrofitService::class.java)

    suspend fun login(uid: String): UserResponse? {
        val response = sendNetworkRequest { service.login(uid) }
        if (response?.isSuccessful == true)
            return response.body()
        return null
    }

    suspend fun userProfile(uid: String): UserResponse? {
        val response = sendNetworkRequest { service.getProfile(uid) }
        if (response?.isSuccessful == true)
            return response.body()
        return null
    }

    suspend fun updateProfile(uid: String, userName: String, avatarUri: String): UserResponse? {
        val response = sendNetworkRequest { service.updateProfile(uid, userName, avatarUri) }
        if (response?.isSuccessful == true)
            return response.body()
        return null
    }

    suspend fun updateFcmToken(uid: String, token: String): ApiResponse? {
        val response = sendNetworkRequest { service.updateToken(uid, token) }
        if (response?.isSuccessful == true)
            return response.body()
        return null
    }

    suspend fun joinWaitingRoom(uid: String): BattleResponse? {
        val response = sendNetworkRequest { service.joinWaitingRoom(uid) }
        if (response?.isSuccessful == true)
            return response.body()
        return null
    }

    suspend fun leaveWaitingRoom(uid: String): BattleResponse? {
        val response = sendNetworkRequest { service.leaveWaitingRoom(uid) }
        if (response?.isSuccessful == true)
            return response.body()
        return null
    }

    suspend fun getBattleQuestions(bid: String): QuestionsResponse? {
        val response = sendNetworkRequest { service.getQuestions(bid) }
        if (response?.isSuccessful == true)
            return response.body()
        return null
    }

    suspend fun updateBattleScore(bid: String, uid: String, score: Int): BattleResponse? {
        val response = sendNetworkRequest { service.updateScore(bid, uid, score) }
        if (response?.isSuccessful == true)
            return response.body()
        return null
    }

    suspend fun getAvatars(): AvatarResponse? {
        val response = sendNetworkRequest { service.getAvatars() }
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