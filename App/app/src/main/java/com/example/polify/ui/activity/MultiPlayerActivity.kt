package com.example.polify.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.data.Room
import com.andruid.magic.game.model.response.Result
import com.example.polify.R
import com.example.polify.data.EXTRA_ROOM
import com.example.polify.ui.fragment.RoomWaitingFragmentDirections
import com.example.polify.util.errorToast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import splitties.toast.toast

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