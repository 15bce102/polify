package com.example.polify.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.response.Result
import com.example.polify.data.STATUS_ONLINE
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class GameService : Service(), CoroutineScope {
    companion object {
        private val TAG = "${this::class.java.simpleName}Log"
        private const val STATUS_UPDATE_TIME_MS = (1 * 60 * 1000).toLong()
    }

    inner class ServiceBinder : Binder() {
        fun getService(): GameService = this@GameService
    }

    private val mBinder = ServiceBinder()
    private val job: Job = Job()
    override val coroutineContext
        get() = job + Executors.newFixedThreadPool(100).asCoroutineDispatcher()

    private val updateHandler = Handler()
    private val mAuth by lazy { FirebaseAuth.getInstance() }

    override fun onBind(intent: Intent): IBinder? = mBinder

    override fun onCreate() {
        super.onCreate()

        initStatusUpdate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        updateHandler.removeCallbacksAndMessages(null)
    }

    private fun initStatusUpdate() {
        launch(Dispatchers.IO) {
            updateStatus()
            delay(STATUS_UPDATE_TIME_MS)
        }
    }

    private suspend fun updateStatus() {
        mAuth.currentUser?.let { user ->
            val response = GameRepository.updateStatus(user.uid, STATUS_ONLINE)
            if (response.status == Result.Status.SUCCESS)
                Log.d(TAG, "status updated")
            else
                Log.d(TAG, "status update failed")
        }
    }
}