package com.example.polify.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.andruid.magic.game.api.GameRepository
import com.example.polify.data.ACTION_START_STATUS_UPDATE
import com.example.polify.data.STATUS_ONLINE
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StatusUpdateService : Service() {
    companion object {
        private val TAG = "${StatusUpdateService::class.java.simpleName}Log"
        private const val STATUS_UPDATE_TIME_MS = (2 * 60 * 1000).toLong()
    }

    private val updateHandler = Handler()
    private val mAuth by lazy { FirebaseAuth.getInstance() }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == ACTION_START_STATUS_UPDATE)
            updateHandler.postDelayed({
                updateStatus()
            }, STATUS_UPDATE_TIME_MS)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        updateHandler.removeCallbacksAndMessages(null)
    }

    private fun updateStatus() {
        GlobalScope.launch {
            mAuth.currentUser?.let { user ->
                val response = GameRepository.updateStatus(user.uid, STATUS_ONLINE)
                if (response?.success == true)
                    Log.d(TAG, "status updated")
                else
                    Log.d(TAG, "status update failed")
            }
        }
    }
}