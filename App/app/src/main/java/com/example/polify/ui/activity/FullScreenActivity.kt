package com.example.polify.ui.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.polify.R
import com.example.polify.eventbus.ClickSoundEvent
import com.example.polify.service.GameService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@SuppressLint("Registered")
open class FullScreenActivity : AppCompatActivity() {
    var service: GameService? = null
    private var isBound = false

    private val sounds = IntArray(3)
    private val soundPool by lazy {
        val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(attrs)
                .build()
    }
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
            service = (binder as GameService.ServiceBinder).getService()
            //service?.playSong(R.raw.normal)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        doBindService()
        initSoundPool()

        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus)
            hideSystemUI()
    }

    override fun onResume() {
        super.onResume()
        //service?.playSong(R.raw.normal)
    }

    override fun onPause() {
        super.onPause()
        //service?.pauseSong()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onClickEvent(clickSoundEvent: ClickSoundEvent) {
        Log.d("soundLog", "on click sound event")
        soundPool.play(sounds[0], 1F, 1F, 1,0, 1.0F)
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun doBindService() {
        bindService(Intent(this, GameService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        isBound = true
    }

    private fun doUnbindService() {
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun initSoundPool() {
        sounds[0] = soundPool.load(this, R.raw.tap, 1)
    }
}