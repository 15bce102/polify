package com.droidx.trivianest.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import com.droidx.trivianest.R
import com.droidx.trivianest.data.ACTION_MATCH_FOUND
import com.droidx.trivianest.data.BATTLE_ONE_VS_ONE
import com.droidx.trivianest.data.EXTRA_BATTLE
import com.droidx.trivianest.model.data.Battle
import com.droidx.trivianest.model.data.OneVsOneBattle
import com.droidx.trivianest.ui.fragment.WaitingFragmentDirections

class OneVsOneActivity : FullScreenActivity() {
    companion object {
        private val TAG = "${OneVsOneActivity::class.java.simpleName}Log"
    }

    private val gameReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_MATCH_FOUND -> {
                    Log.d(TAG, "broadcast of match found in activity")

                    intent.extras?.let {
                        battle = it.getParcelable(EXTRA_BATTLE)!!

                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
                            startBattle(battle)
                        else {
                            val startTime = System.currentTimeMillis()

                            lifecycle.addObserver(object : LifecycleObserver {
                                @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
                                fun onForeground() {
                                    startBattle(battle, startTime)
                                }
                            })
                        }
                    }
                }
            }
        }
    }

    private lateinit var battle: OneVsOneBattle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_vs_one)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        LocalBroadcastManager.getInstance(this).registerReceiver(gameReceiver, IntentFilter(ACTION_MATCH_FOUND))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gameReceiver)
    }

    private fun startBattle(battle: Battle, startTime: Long = -1L) {
        Log.d(TAG, "battle=${battle}, startTime = $startTime")

        findNavController(R.id.nav_host_fragment).navigate(
                WaitingFragmentDirections.actionWaitingFragmentToQuestionsFragment(battle, startTime, BATTLE_ONE_VS_ONE))
    }
}