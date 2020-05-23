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
import com.example.polify.eventbus.SoundEvent
import com.example.polify.service.GameService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@SuppressLint("Registered")
open class FullScreenActivity : AppCompatActivity() {
    var service: GameService? = null
    private var isBound = false

    private val sounds = IntArray(4)
    private val soundPool by lazy {
        val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        SoundPool.Builder()
                .setMaxStreams(4)
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
        initButtonSounds()
        //initTimerSound()

        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
        soundPool.release()
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
    fun onSoundEvent(soundEvent: SoundEvent) {
        Log.d("soundLog", "on sound event")
        when (soundEvent.type) {
            SoundEvent.Sound.TYPE_BUTTON_TAP ->
                soundPool.play(sounds[0], 1F, 1F, 1,0, 1.0F)
            /*SoundEvent.Sound.TYPE_TIMER_START -> {
                if (isTicking)
                    soundPool.resume(sounds[1])
                else {
                    isTicking = true
                    soundPool.play(sounds[1], 1F, 1F, 1, -1, 1.0F)
                }
            }
            SoundEvent.Sound.TYPE_TIMER_STOP ->
                soundPool.pause(sounds[1])*/
        }
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

    private fun initButtonSounds() {
        sounds[0] = soundPool.load(this, R.raw.tap, 1)
    }

    private fun initTimerSound() {
        sounds[1] = soundPool.load(this, R.raw.clock, 1)
    }
}