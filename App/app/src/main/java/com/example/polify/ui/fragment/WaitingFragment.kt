package com.example.polify.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.andruid.magic.game.api.GameRepository
import com.example.polify.databinding.FragmentWaitingBinding
import com.example.polify.eventbus.BattleEvent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class WaitingFragment : Fragment() {
    companion object {
        private val TAG = "${WaitingFragment::class.java.simpleName}Log"
    }

    private val timer = Timer()
    private val mAuth by lazy { FirebaseAuth.getInstance() }

    private var seconds = 0

    private lateinit var binding: FragmentWaitingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWaitingBinding.inflate(inflater, container, false)

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

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("cloudLog", "onDestroy")
        EventBus.getDefault().unregister(this)
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBattleEvent(battleEvent: BattleEvent) {
        val battleId = battleEvent.battle.battleId
        Log.d("cloudLog", "eventbus event received: $battleId")
        Log.d("cloudLog", "after navigation to questions fragment command")
    }
}