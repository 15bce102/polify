package com.andruid.magic.game.api

import android.app.Application
import android.content.Context
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

    suspend fun checkIfUserExists(phoneNumber: String): Result<ApiResponse> {
        val map = mapOf("phoneNumber" to phoneNumber)
        return sendNetworkRequest { service.checkIfUserExists(map) }
    }

    suspend fun signupUser(uid: String, avatar: String, userName: String, token: String): Result<ApiResponse> {
        val map = mapOf(
                "uid" to uid,
                "avatar" to avatar,
                "user_name" to userName,
                "token" to token
        )
        return sendNetworkRequest { service.signup(map) }
    }

    suspend fun login(uid: String, token: String): Result<ApiResponse> {
        val map = mapOf(
                "uid" to uid,
                "token" to token
        )
        return sendNetworkRequest { service.login(map) }
    }

    suspend fun updateFcmToken(uid: String, token: String): Result<ApiResponse> {
        val map = mapOf(
                "uid" to uid,
                "token" to token
        )
        return sendNetworkRequest { service.updateToken(map) }
    }

    suspend fun updateProfile(uid: String, userName: String, avatar: String): Result<ApiResponse> {
        val map = mapOf(
                "uid" to uid,
                "user_name" to userName,
                "avatar" to avatar
        )
        return sendNetworkRequest { service.updateProfile(map) }
    }

    suspend fun userProfile(uid: String): Result<UserResponse> {
        val map = mapOf("uid" to uid)
        return sendNetworkRequest { service.getProfile(map) }
    }

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

    suspend fun updateStatus(uid: String, status: Int): Result<ApiResponse> {
        val map = mapOf(
                "uid" to uid
        )
        return sendNetworkRequest { service.updateStatus(status, map) }
    }

    suspend fun updateFriends(uid: String, phoneNumbers: List<String>): Result<UserResponse> {
        val map = mapOf(
                "uid" to uid,
                "phoneNumbers" to phoneNumbers
        )
        return sendNetworkRequest { service.updateFriends(map) }
    }

    suspend fun createMultiPlayerRoom(uid: String): Result<RoomResponse> {
        val map = mapOf("uid" to uid)
        return sendNetworkRequest { service.createMultiPlayerRoom(map) }
    }

    suspend fun leaveMultiPlayerRoom(uid: String, roomId: String): Result<ApiResponse> {
        val map = mapOf(
                "uid" to uid,
                "room_id" to roomId
        )
        return sendNetworkRequest { service.leaveMultiPlayerRoom(map) }
    }

    suspend fun getMyFriends(uid: String): Result<FriendsResponse> {
        val map = mapOf("uid" to uid)
        return sendNetworkRequest { service.getMyFriends(map) }
    }

    suspend fun sendMultiPlayerRoomInvite(uid: String, friendUid: String, roomId: String): Result<ApiResponse> {
        val map = mapOf(
                "uid" to uid,
                "f_uid" to friendUid,
                "room_id" to roomId
        )
        return sendNetworkRequest { service.sendMultiPlayerInvite(map) }
    }

    suspend fun joinMultiPlayerRoom(uid: String, roomId: String): Result<RoomResponse> {
        val map = mapOf(
                "uid" to uid,
                "room_id" to roomId
        )
        return sendNetworkRequest { service.joinRoom(map) }
    }

    suspend fun startMultiPlayerBattle(uid: String, roomId: String): Result<ApiResponse> {
        val map = mapOf(
                "uid" to uid,
                "room_id" to roomId
        )
        return sendNetworkRequest { service.startMultiPlayerBattle(map) }
    }

    suspend fun getPracticeQuestions(): List<Question> {
        return withContext(Dispatchers.IO) {
            val json = context.assets.open(ASSETS_PRACTICE_QUESTIONS).bufferedReader().use {
                it.readText()
            }
            Gson().fromJson<List<Question>>(json, object : TypeToken<List<Question>>() {}.type)
        }
    }
}