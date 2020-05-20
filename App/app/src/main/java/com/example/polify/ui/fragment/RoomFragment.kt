package com.example.polify.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.data.Room
import com.andruid.magic.game.model.response.Result
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
import splitties.toast.toast

class RoomFragment : Fragment() {
    private lateinit var binding: FragmentRoomBinding
    private lateinit var room: Room

    private val friendAdapter = FriendAdapter()
    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private val friendViewModel by viewModels<FriendViewModel> {
        BaseViewModelFactory { FriendViewModel(mAuth.currentUser?.uid ?: "") }
    }
    private val callback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!::room.isInitialized) {
                requireActivity().finish()
                return
            }

            mAuth.currentUser?.let { user ->
                lifecycleScope.launch {
                    val result = GameRepository.leaveMultiPlayerRoom(user.uid, room.roomId)
                    if (result.status == Result.Status.SUCCESS) {
                        result.data?.let { data ->
                            if (data.success) {
                                toast("Room left successfully")
                                requireActivity().finish()
                            }
                            else
                                toast("Could not leave room")
                        }
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
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
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

        binding.member1 = owner
        remaining.forEachIndexed { index, player ->
            when (index) {
                0 -> binding.member2 = player
                1 -> binding.member3 = player
                2 -> binding.member4 = player
            }
        }

        binding.executePendingBindings()
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = friendAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }
}