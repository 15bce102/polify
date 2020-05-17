package com.andruid.magic.game.api

import android.app.Application
import android.content.Context
import com.andruid.magic.game.model.data.Question
import com.andruid.magic.game.model.response.Result
import com.andruid.magic.game.model.response.UserResponse
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

    suspend fun login(uid: String): Result<UserResponse> =
            sendNetworkRequest { service.login(uid) }

    suspend fun userProfile(uid: String) =
            sendNetworkRequest { service.getProfile(uid) }

    suspend fun updateProfile(uid: String, userName: String, avatarUri: String) =
            sendNetworkRequest { service.updateProfile(uid, userName, avatarUri) }

    suspend fun updateFcmToken(uid: String, token: String) =
            sendNetworkRequest { service.updateToken(uid, token) }

    suspend fun joinWaitingRoom(uid: String) =
            sendNetworkRequest { service.joinWaitingRoom(uid) }

    suspend fun leaveWaitingRoom(uid: String) =
            sendNetworkRequest { service.leaveWaitingRoom(uid) }

    suspend fun getBattleQuestions(bid: String) =
            sendNetworkRequest { service.getQuestions(bid) }

    suspend fun updateBattleScore(bid: String, uid: String, score: Int) =
            sendNetworkRequest { service.updateScore(bid, uid, score) }

    suspend fun getAvatars() =
            sendNetworkRequest { service.getAvatars() }

    suspend fun updateStatus(uid: String, status: Int) =
            sendNetworkRequest { service.updateStatus(uid, status) }

    suspend fun updateFriends(uid: String, phoneNumbers: List<String>): Result<UserResponse> {
        val map = mapOf(
                "uid" to uid,
                "phoneNumbers" to phoneNumbers
        )
        return sendNetworkRequest { service.updateFriends(map) }
    }

    suspend fun getPracticeQuestions(): List<Question> {
        return withContext(Dispatchers.IO) {
            val json = context.assets.open(ASSETS_PRACTICE_QUESTIONS).bufferedReader().use {
                it.readText()
            }
            Gson().fromJson<List<Question>>(json, object : TypeToken<List<Question>>() {}.type)
        }
    }

    suspend fun createBattle(uid: String, coins: Int) =
            sendNetworkRequest { service.createBattle(uid, coins) }

    suspend fun joinBattle(uid: String, bid: String) =
            sendNetworkRequest { service.joinBattle(uid, bid) }
}