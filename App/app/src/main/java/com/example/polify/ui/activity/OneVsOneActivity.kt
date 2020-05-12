package com.example.polify.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import com.andruid.magic.game.model.data.OneVsOneBattle
import com.example.polify.R
import com.example.polify.data.ACTION_MATCH_FOUND
import com.example.polify.data.EXTRA_BATTLE
import com.example.polify.ui.fragment.WaitingFragmentDirections

class OneVsOneActivity : FullScreenActivity() {
    companion object {
        private val TAG = "${OneVsOneActivity::class.java.simpleName}Log"
    }

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val gameReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_MATCH_FOUND -> {
                    Log.d(TAG, "match found")
                    intent.extras?.let {
                        battle = it.getParcelable(EXTRA_BATTLE)!!

                        navController.navigate(WaitingFragmentDirections.actionWaitingFragmentToQuestionsFragment())
                    }
                }
            }
        }
    }

    private lateinit var battle: OneVsOneBattle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_vs_one)

        LocalBroadcastManager.getInstance(this).registerReceiver(gameReceiver, IntentFilter(ACTION_MATCH_FOUND))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gameReceiver)
    }
}