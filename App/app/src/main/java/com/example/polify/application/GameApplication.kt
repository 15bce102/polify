package com.example.polify.application

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.andruid.magic.game.api.GameRepository
import com.example.polify.data.ACTION_START_STATUS_UPDATE
import com.example.polify.repository.ContactFetcher
import com.example.polify.service.StatusUpdateService
import com.example.polify.util.toFullPhoneNumbers

class GameApplication : Application(), LifecycleObserver {
    companion object {
        private val TAG = "${GameApplication::class.java.simpleName}Log"
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        GameRepository.init(this)
        ContactFetcher.init(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Log.d(TAG, "App in background")
        stopService(Intent(this, StatusUpdateService::class.java))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        Log.d(TAG, "App in foreground")
        val intent = Intent(this, StatusUpdateService::class.java)
                .setAction(ACTION_START_STATUS_UPDATE)
        startService(intent)
    }
}