package com.droidx.trivianest.service

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.droidx.gameapi.api.GameRepository
import com.droidx.gameapi.model.data.*
import com.droidx.gameapi.model.response.Result
import com.droidx.trivianest.data.*
import com.droidx.trivianest.util.buildNotification
import com.droidx.trivianest.util.isHomeActivityVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import splitties.systemservices.notificationManager
import java.util.concurrent.Executors

class CloudMessagingService : FirebaseMessagingService(), CoroutineScope {
    companion object {
        private val TAG = "${this::class.java.simpleName}Log"
        private const val INVITE_NOTI_ID = 1
    }

    private val job: Job = Job()
    override val coroutineContext
        get() = job + Executors.newFixedThreadPool(100).asCoroutineDispatcher()

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        launch {
            FirebaseAuth.getInstance().currentUser?.let { user ->
                val response = GameRepository.updateFcmToken(user.uid, token)
                if (response.status == Result.Status.SUCCESS)
                    Log.d(TAG, "fcm token updated")
                else
                    Log.d(TAG, response.message ?: "fcm token update failed")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val map = message.data

        Log.d(TAG, "received: $map")

        when (map[KEY_TYPE]) {
            TYPE_MATCHMAKING -> {
                map[KEY_PAYLOAD]?.toBattle()?.let { battle ->
                    Log.d(TAG, "battle = $battle")

                    val intent = Intent(ACTION_MATCH_FOUND)
                            .putExtra(EXTRA_BATTLE, battle)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

                    Log.d(TAG, "sent match found broadcast")
                }
            }

            TYPE_SCORE_UPDATE -> {
                val battleId = map[KEY_BATTLE_ID]

                Log.d(TAG, "battleId = $battleId")

                map[KEY_PAYLOAD]?.toPlayerResults().let { results ->
                    val intent = Intent(ACTION_MATCH_RESULTS)
                            .putExtra(EXTRA_BATTLE_ID, battleId)
                            .putExtra(EXTRA_PLAYERS, results)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
            }

            TYPE_ROOM_INVITE -> {
                Log.d(TAG, "room invite: ${map[KEY_PAYLOAD]}")

                map[KEY_PAYLOAD]?.toRoom()?.let { room ->
                    if (isHomeActivityVisible())
                        sendDialogBroadcast(room)
                    else
                        showNotification(room)
                }
            }

            TYPE_MULTIPLAYER_JOIN -> {
                Log.d(TAG, "player joined: ${map[KEY_PAYLOAD]}")

                val payload: Map<String, String> =
                        Gson().fromJson(map[KEY_PAYLOAD], object : TypeToken<Map<String, String>>() {}.type)

                val roomMessage = payload["message"]

                payload["room"]?.toRoom().let { room ->
                    val intent = Intent(ACTION_ROOM_UPDATE)
                            .putExtra(EXTRA_MESSAGE, roomMessage)
                            .putExtra(EXTRA_ROOM, room)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
            }

            TYPE_LEAVE_ROOM -> {
                Log.d(TAG, "player left: ${map[KEY_PAYLOAD]}")

                val payload: Map<String, String> =
                        Gson().fromJson(map[KEY_PAYLOAD], object : TypeToken<Map<String, String>>() {}.type)

                val roomMessage = payload["message"]
                val room = payload["room"]?.toRoom()

                val intent = Intent(ACTION_ROOM_UPDATE)
                        .putExtra(EXTRA_MESSAGE, roomMessage)
                        .putExtra(EXTRA_ROOM, room)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }

            TYPE_START_MATCH -> {
                Log.d("mpLog", "match start: ${map[KEY_PAYLOAD]}")

                map[KEY_PAYLOAD]?.toBattle()?.let { battle ->
                    Log.d("mpLog", "battle = $battle")

                    val intent = Intent(ACTION_MATCH_FOUND)
                            .putExtra(EXTRA_BATTLE, battle)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
            }
        }
    }

    private fun showNotification(room: Room) {
        val builder = buildNotification(room)

        notificationManager.notify(INVITE_NOTI_ID, builder.build())
    }

    private fun sendDialogBroadcast(room: Room) {
        val intent = Intent(ACTION_ROOM_INVITE)
                .putExtra(EXTRA_ROOM, room)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun String.toBattle(): Battle? {
        val gson = Gson()
        val map: Map<String, String> = gson.fromJson(this, object : TypeToken<Map<String, String>>() {}.type)

        val playersJson = map["players"]
        val players: List<Player> = Gson().fromJson(playersJson, object : TypeToken<List<Player>>() {}.type)

        return when (map.getOrDefault("type", BATTLE_ONE_VS_ONE)) {
            BATTLE_ONE_VS_ONE -> {
                OneVsOneBattle(
                        battleId = map.getOrDefault("_id", "defaultId"),
                        startTime = map.getOrDefault("start_time", System.currentTimeMillis().toString()).toLong(),
                        coinsPool = map.getOrDefault("coins_pool", 10.toString()).toInt(),
                        players = players
                )
            }
            BATTLE_MULTIPLAYER -> {
                MultiPlayerBattle(
                        battleId = map.getOrDefault("_id", "defaultId"),
                        startTime = map.getOrDefault("start_time", System.currentTimeMillis().toString()).toLong(),
                        coinsPool = map.getOrDefault("coins_pool", 10.toString()).toInt(),
                        players = players,
                        owner = map.getOrDefault("owner", "")
                )
            }
            else -> null
        }
    }

    private fun String.toPlayerResults(): ArrayList<PlayerResult> =
            Gson().fromJson(this, object : TypeToken<ArrayList<PlayerResult>>() {}.type)

    private fun String.toRoom(): Room =
            Gson().fromJson(this, Room::class.java)
}