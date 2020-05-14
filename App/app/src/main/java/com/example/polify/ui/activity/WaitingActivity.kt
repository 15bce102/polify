package com.example.polify.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.data.Battle
import com.example.polify.data.ACTION_MATCH_FOUND
import com.example.polify.data.EXTRA_BATTLE
import com.example.polify.data.EXTRA_START_TIME
import com.example.polify.databinding.ActivityWaitingBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

class WaitingActivity : AppCompatActivity() {
    companion object {
        private val TAG = "${WaitingActivity::class.java.simpleName}Log"
    }

    private val timer = Timer()
    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private val gameReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_MATCH_FOUND -> {
                    Log.d("cloudLog", "broadcast of match found in activity")

                    val battle = intent.extras?.getParcelable<Battle>(EXTRA_BATTLE)!!

                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        Log.d("cloudLog", "activity is foreground")
                        startBattle(battle)
                    }
                    else {
                        Log.d("cloudLog", "activity is background")
                    }
                }
            }
        }
    }

    private var seconds = 0

    private lateinit var binding: ActivityWaitingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWaitingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(gameReceiver, IntentFilter(ACTION_MATCH_FOUND))

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                seconds++
                binding.timerTV.text = seconds.toString()
            }
        }, 1000, 1000)

        FirebaseAuth.getInstance().currentUser?.let { user ->
            lifecycleScope.launch {
                val response = GameRepository.joinWaitingRoom(user.uid)
                if (response?.success == true)
                    Log.d(TAG, "waiting room joined")
                else
                    Log.e(TAG, "waiting room join failed")
            }
        }
    }

    private fun startBattle(battle: Battle, startTime: Int = 0) {
        val intent = Intent(this, QuestionsActivity::class.java)
                .putExtra(EXTRA_BATTLE, battle)
                .putExtra(EXTRA_START_TIME, startTime)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("cloudLog", "onDestroy")
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gameReceiver)
        timer.cancel()

        lifecycleScope.launch {
            mAuth.currentUser?.let { user ->
                val response = GameRepository.leaveWaitingRoom(user.uid)
                if (response?.success == true)
                    Log.d(TAG, "left waiting room")
                else
                    Log.e(TAG, "left waiting room error")
            }
        }
    }
}