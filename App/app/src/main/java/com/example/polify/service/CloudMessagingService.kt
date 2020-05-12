package com.example.polify.service

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.data.OneVsOneBattle
import com.andruid.magic.game.model.data.Player
import com.example.polify.data.ACTION_MATCH_FOUND
import com.example.polify.data.EXTRA_BATTLE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class CloudMessagingService : FirebaseMessagingService(), CoroutineScope {
    companion object {
        private val TAG = "${CloudMessagingService::class.java.simpleName}Log"
    }

    private val job: Job = Job()
    override val coroutineContext
        get() = job + Executors.newFixedThreadPool(100).asCoroutineDispatcher()

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        launch {
            FirebaseAuth.getInstance().currentUser?.let { user ->
                val response = GameRepository.updateFcmToken(user.uid, token)
                if (response?.success == true)
                    Log.d(TAG, "fcm token updated")
                else
                    Log.d(TAG, response?.message ?: "fcm token update failed")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val battle = message.data.toOneVsOneBattle()
        Log.d(TAG, "battle = $battle")

        val intent = Intent(ACTION_MATCH_FOUND)
                .putExtra(EXTRA_BATTLE, battle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun Map<String, String>.toOneVsOneBattle(): OneVsOneBattle {
        val json = get("players")
        val players: List<Player> = Gson().fromJson(json, object : TypeToken<List<Player>>() {}.type)

        return OneVsOneBattle(
                battleId = getOrDefault("_id", "defaultId"),
                startTime = getOrDefault("start_time", System.currentTimeMillis().toString()).toLong(),
                coinsPool = getOrDefault("coins_pool", 10.toString()).toInt(),
                players = players
        )
    }
}