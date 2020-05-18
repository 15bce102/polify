package com.example.polify.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.annotation.RawRes
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.response.Result
import com.example.polify.R
import com.example.polify.data.STATUS_ONLINE
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class GameService : Service(), CoroutineScope {
    companion object {
        private val TAG = "${GameService::class.java.simpleName}Log"
        private const val STATUS_UPDATE_TIME_MS = (2 * 60 * 1000).toLong()
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
    private val exoPlayer by lazy {
        SimpleExoPlayer.Builder(this)
                .build()
    }

    override fun onBind(intent: Intent): IBinder? = mBinder

    override fun onCreate() {
        super.onCreate()

        initExoPlayer()
        initStatusUpdate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        exoPlayer.release()
        updateHandler.removeCallbacksAndMessages(null)
    }

    private fun initExoPlayer() {
        exoPlayer.apply {
            val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_GAME)
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .build()
            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true)
            setHandleWakeLock(true)
        }
    }

    private fun initStatusUpdate() {
        launch(Dispatchers.IO) {
            updateStatus()
            delay(STATUS_UPDATE_TIME_MS)
        }
    }

    fun playSong(@RawRes res: Int = R.raw.normal) {
        Log.d(TAG, "play song")
        val uri = RawResourceDataSource.buildRawResourceUri(res)
        val dataSource = RawResourceDataSource(this)
        dataSource.open(DataSpec(uri))
        val mediaSource = ProgressiveMediaSource.Factory(DataSource.Factory { dataSource })
                .createMediaSource(uri)
        val loopingMediaSource = LoopingMediaSource(mediaSource)

        exoPlayer.prepare(loopingMediaSource, false, false)
        exoPlayer.playWhenReady = true
    }

    fun pauseSong() {
        Log.d(TAG, "pause song")
        exoPlayer.playWhenReady = false
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