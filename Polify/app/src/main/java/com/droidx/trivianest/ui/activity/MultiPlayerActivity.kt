package com.droidx.trivianest.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.droidx.trivianest.R
import com.droidx.gameapi.api.GameRepository
import com.droidx.trivianest.data.EXTRA_ROOM
import com.droidx.gameapi.model.data.Room
import com.droidx.trivianest.ui.fragment.RoomWaitingFragmentDirections
import com.droidx.trivianest.util.errorToast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import splitties.toast.toast
import com.droidx.gameapi.model.response.Result

class MultiPlayerActivity : FullScreenActivity() {
    private val mAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        intent.extras?.let { extras ->
            (extras[EXTRA_ROOM] as Room?)?.let { room ->
                findNavController(R.id.nav_host_fragment)
                        .navigate(RoomWaitingFragmentDirections.actionRoomWaitingFragmentToRoomFragment(room))
            } ?: run { createRoom() }
        } ?: run { createRoom() }
    }

    private fun createRoom() {
        val user = mAuth.currentUser ?: return

        lifecycleScope.launch {
            val result = GameRepository.createMultiPlayerRoom(user.uid)
            if (result.status == Result.Status.SUCCESS) {
                result.data?.let { data ->
                    if (data.success) {
                        val room = data.room
                        findNavController(R.id.nav_host_fragment).navigate(
                                RoomWaitingFragmentDirections.actionRoomWaitingFragmentToRoomFragment(room))
                    } else {
                        errorToast(data.message ?: "error")
                        finish()
                    }
                }
            } else {
                toast(result.message ?: "network error")
                finish()
            }
        }
    }
}