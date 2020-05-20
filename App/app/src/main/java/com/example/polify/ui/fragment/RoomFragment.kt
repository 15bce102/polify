package com.example.polify.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.data.Room
import com.andruid.magic.game.model.response.Result
import com.example.polify.data.ACTION_ROOM_UPDATE
import com.example.polify.data.EXTRA_MESSAGE
import com.example.polify.data.EXTRA_ROOM
import com.example.polify.databinding.FragmentRoomBinding
import com.example.polify.eventbus.FriendInviteEvent
import com.example.polify.ui.adapter.FriendAdapter
import com.example.polify.ui.viewmodel.BaseViewModelFactory
import com.example.polify.ui.viewmodel.FriendViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import splitties.toast.longToast
import splitties.toast.toast

class RoomFragment : Fragment() {
    private lateinit var binding: FragmentRoomBinding
    private lateinit var room: Room

    private val friendAdapter = FriendAdapter()
    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private val friendViewModel by viewModels<FriendViewModel> {
        BaseViewModelFactory { FriendViewModel(mAuth.currentUser?.uid ?: "") }
    }
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!::room.isInitialized) {
                requireActivity().finish()
                return
            }

            val user = mAuth.currentUser ?: return

            lifecycleScope.launch {
                val result = GameRepository.leaveMultiPlayerRoom(user.uid, room.roomId)
                if (result.status == Result.Status.SUCCESS) {
                    result.data?.let { data ->
                        if (data.success) {
                            toast("Room left successfully")
                            requireActivity().finish()
                        } else {
                            toast("Could not leave room")
                            requireActivity().finish()
                        }
                    }
                }
            }
        }
    }
    private val roomReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_ROOM_UPDATE) {
                intent.extras?.let { extras ->
                    val message = extras[EXTRA_MESSAGE] as String

                    toast(message)

                    (extras[EXTRA_ROOM] as Room?)?.let {
                        this@RoomFragment.room = it
                        updatePlayerCards()
                    } ?: run {
                        requireActivity().finish()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val safeArgs = RoomFragmentArgs.fromBundle(it)
            room = safeArgs.room

            longToast("room members are ${room.members.joinToString(", ", "[", "]")}")
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        EventBus.getDefault().register(this)
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(roomReceiver, IntentFilter(ACTION_ROOM_UPDATE))
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(roomReceiver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentRoomBinding.inflate(inflater, container, false)

        initRecyclerView()
        updatePlayerCards()

        friendViewModel.friends.observe(viewLifecycleOwner, Observer { result ->
            if (result.status == Result.Status.SUCCESS)
                friendAdapter.submitList(result.data?.friends)
        })

        return binding.root
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFriendInviteEvent(friendInviteEvent: FriendInviteEvent) {
        val (friend) = friendInviteEvent

        mAuth.currentUser?.let { user ->
            lifecycleScope.launch {
                val result = GameRepository
                        .sendMultiPlayerRoomInvite(uid = user.uid, friendUid = friend.uid, roomId = room.roomId)
                if (result.status == Result.Status.SUCCESS) {
                    result.data?.let { data ->
                        if (data.success)
                            toast("Invite sent!!")
                    }
                }
            }
        }
    }

    private fun updatePlayerCards() {
        val players = room.members
        val owner = players.find { player -> player.uid == room.owner }
        val remaining = players.minus(owner)

        binding.apply {
            member1 = owner
            member2 = remaining.getOrNull(0)
            member3 = remaining.getOrNull(1)
            member4 = remaining.getOrNull(2)

            executePendingBindings()
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = friendAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }
}