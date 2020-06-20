package com.droidx.trivianest.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.droidx.gameapi.api.GameRepository
import com.droidx.gameapi.model.response.Result
import com.droidx.trivianest.data.WAIT_TIME_LIMIT_SEC
import com.droidx.trivianest.databinding.FragmentWaitingBinding
import com.droidx.trivianest.util.errorToast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

class WaitingFragment : Fragment() {
    companion object {
        private val TAG = "${WaitingFragment::class.java.simpleName}Log"
    }

    private val timer = Timer()
    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            Log.d(TAG, "on back pressed fragment")

            val user = mAuth.currentUser
            if (user == null) {
                requireActivity().finish()
                return
            }

            lifecycleScope.launch {
                val response = GameRepository.leaveWaitingRoom(user.uid)
                if (response.status == Result.Status.SUCCESS)
                    Log.d(TAG, "left waiting room")
                else
                    Log.e(TAG, "left waiting room error")
                requireActivity().finish()
            }
        }
    }

    private var seconds = 0

    private lateinit var binding: FragmentWaitingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWaitingBinding.inflate(inflater, container, false)

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                seconds++

                requireActivity().runOnUiThread {
                    binding.timerTV.text = seconds.toString()
                }

                if (seconds == WAIT_TIME_LIMIT_SEC) {
                    val user = mAuth.currentUser ?: return

                    lifecycleScope.launch {
                        val result = GameRepository.playWithBot(user.uid)
                        if (result.status == Result.Status.SUCCESS) {
                            if (result.data?.success == true)
                                Log.d(TAG, "playing with bot")
                            else
                                errorToast("Could not connect with bot")
                        } else
                            errorToast("Could not connect with bot")
                    }
                }
            }
        }, 1000, 1000)

        FirebaseAuth.getInstance().currentUser?.let { user ->
            lifecycleScope.launch {
                val response = GameRepository.joinWaitingRoom(user.uid)
                if (response.status == Result.Status.SUCCESS) {
                    val data = response.data
                    if (data?.success == true)
                        Log.d(TAG, "waiting room joined")
                    else {
                        errorToast(data?.message)
                        requireActivity().finish()
                        Log.e(TAG, "waiting room join failed: ${data?.message}")
                    }
                } else {
                    errorToast(response.message)
                    requireActivity().finish()
                    Log.e(TAG, "waiting room join failed")
                }
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("cloudLog", "onDestroy")
        timer.cancel()
    }
}