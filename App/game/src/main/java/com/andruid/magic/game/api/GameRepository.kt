package com.andruid.magic.game.api

import android.app.Application
import android.content.Context
import android.util.Log
import com.andruid.magic.game.model.data.Question
import com.andruid.magic.game.model.response.*
import com.andruid.magic.game.server.RetrofitClient
import com.andruid.magic.game.server.RetrofitService
import com.andruid.magic.game.util.sendNetworkRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GameRepository {
    private const val ASSETS_PRACTICE_QUESTIONS = "practice.json"

    private lateinit var service: RetrofitService
    private lateinit var context: Context

    fun init(application: Application) {
        context = application.applicationContext
        service = RetrofitClient.getRetrofitInstance().create(RetrofitService::class.java)
    }

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

    suspend fun updateStatus(uid: String, status: Int): UserResponse? {
        val response = sendNetworkRequest { service.updateStatus(uid, status) }
        if (response?.isSuccessful == true)
            return response.body()
        return null
    }

    suspend fun updateFriends(uid: String, phoneNumbers: List<String>): UserResponse? {
        val map = mapOf(
                "uid" to uid,
                "phoneNumbers" to phoneNumbers
        )
        val response = sendNetworkRequest { service.updateFriends(map) }
        if (response?.isSuccessful == true)
            return response.body()
        return null
    }

    suspend fun getPracticeQuestions(): List<Question> {
        return withContext(Dispatchers.IO) {
            val json = context.assets.open(ASSETS_PRACTICE_QUESTIONS).bufferedReader().use {
                it.readText()
            }
            Log.d("jsonLog", json)
            return@withContext Gson().fromJson<List<Question>>(json, object : TypeToken<List<Question>>() {}.type)
        }
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