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
import com.droidx.trivianest.data.WAIT_TIME_LIMIT_SEC
import com.droidx.trivianest.databinding.FragmentWaitingBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*
import com.droidx.gameapi.model.response.Result
import com.droidx.trivianest.util.errorToast

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
                val response = _root_ide_package_.com.droidx.gameapi.api.GameRepository.leaveWaitingRoom(user.uid)
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

                    if (seconds == WAIT_TIME_LIMIT_SEC) {
                        errorToast("No match found for battle")
                        requireActivity().finish()
                    }
                }
            }
        }, 1000, 1000)

        FirebaseAuth.getInstance().currentUser?.let { user ->
            lifecycleScope.launch {
                val response = _root_ide_package_.com.droidx.gameapi.api.GameRepository.joinWaitingRoom(user.uid)
                if (response.status == Result.Status.SUCCESS) {
                    val data = response.data
                    if (data?.success == true)
                        Log.d(TAG, "waiting room joined")
                    else
                        Log.e(TAG, "waiting room join failed: ${data?.message}")
                } else
                    Log.e(TAG, "waiting room join failed")
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