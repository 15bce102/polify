package com.droidx.trivianest.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.droidx.trivianest.R
import com.droidx.gameapi.api.GameRepository
import com.droidx.trivianest.data.*
import com.droidx.trivianest.databinding.FragmentRoomBinding
import com.droidx.trivianest.eventbus.FriendInviteEvent
import com.droidx.gameapi.model.data.Battle
import com.droidx.gameapi.model.data.Room
import com.droidx.trivianest.ui.adapter.FriendAdapter
import com.droidx.trivianest.ui.viewholder.FriendViewHolder
import com.droidx.trivianest.ui.viewmodel.BaseViewModelFactory
import com.droidx.trivianest.ui.viewmodel.FriendViewModel
import com.droidx.trivianest.util.errorToast
import com.droidx.trivianest.util.infoToast
import com.droidx.trivianest.util.showConfirmationDialog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import com.droidx.gameapi.model.response.Result
import com.droidx.trivianest.util.setOnSoundClickListener

class RoomFragment : Fragment() {
    private lateinit var binding: FragmentRoomBinding

    private val args by navArgs<RoomFragmentArgs>()
    private val room by lazy { args.room }

    private val friendAdapter = FriendAdapter()
    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private val friendViewModel by viewModels<FriendViewModel> {
        BaseViewModelFactory { FriendViewModel(mAuth.currentUser?.uid ?: "") }
    }
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            lifecycleScope.launch {
                val shouldLeave = showConfirmationDialog(R.string.title_leave_room, R.string.desc_leave_room)
                if (!shouldLeave)
                    return@launch

                val user = mAuth.currentUser ?: return@launch

                lifecycleScope.launch {
                    val result = GameRepository.leaveMultiPlayerRoom(user.uid, room.roomId)
                    if (result.status == Result.Status.SUCCESS) {
                        if (result.data?.success == true)
                            infoToast(getString(R.string.room_leave_success))
                        else
                            errorToast(result.data?.message)
                    } else
                        errorToast(result.message)

                    requireActivity().finish()
                }
            }
        }
    }
    private val roomReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_ROOM_UPDATE) {
                intent.extras?.let { extras ->
                    val message = extras[EXTRA_MESSAGE] as String
                    infoToast(message)

                    (extras[EXTRA_ROOM] as Room?)?.let {
                        this@RoomFragment.room.members = it.members
                        updatePlayerCards()
                    } ?: run {
                        requireActivity().finish()
                    }
                }
            } else if (intent.action == ACTION_MATCH_FOUND) {
                intent.extras?.let {
                    val battle = it.getParcelable<Battle>(EXTRA_BATTLE)!!

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        EventBus.getDefault().register(this)

        val intentFilter = IntentFilter().apply {
            addAction(ACTION_ROOM_UPDATE)
            addAction(ACTION_MATCH_FOUND)
        }
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(roomReceiver, intentFilter)
    }

    override fun onDestroy() {
        Log.d("roomLog", "onDestroy room fragment")
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(roomReceiver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentRoomBinding.inflate(inflater, container, false)

        initRecyclerView()
        initListeners()
        updatePlayerCards()

        friendViewModel.friends.observe(viewLifecycleOwner, Observer { result ->
            if (result.status == Result.Status.SUCCESS)
                friendAdapter.submitList(result.data?.friends)
        })

        return binding.root
    }

    private fun initListeners() {
        binding.start.setOnSoundClickListener {
            val user = mAuth.currentUser ?: return@setOnSoundClickListener

            lifecycleScope.launch {
                val result = GameRepository.startMultiPlayerBattle(user.uid, room.roomId)

                if (result.status == Result.Status.SUCCESS) {
                    if (result.data?.success == true)
                        infoToast("match will start shortly")
                    else
                        errorToast(result.data?.message)
                } else
                    errorToast(result.message)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFriendInviteEvent(friendInviteEvent: FriendInviteEvent) {
        val (friend) = friendInviteEvent
        val pos = friendAdapter.currentList.indexOfFirst {
            f -> f.uid == friend.uid
        }

        val viewHolder = binding.recyclerView.findViewHolderForAdapterPosition(pos) as FriendViewHolder

        val user = mAuth.currentUser ?: return

        lifecycleScope.launch {
            val result = GameRepository
                    .sendMultiPlayerRoomInvite(uid = user.uid, friendUid = friend.uid, roomId = room.roomId)
            if (result.status == Result.Status.SUCCESS) {
                if (result.data?.success == true){
                    infoToast(getString(R.string.sent_invite))
                    viewHolder.hide()
                }

                else
                    errorToast(result.data?.message)
            } else
                errorToast(result.message)
        }
    }

    private fun updatePlayerCards() {
        val players = room.members
        val owner = players.find { player -> player.uid == room.owner }
        val remaining = players.minus(owner)

        val members = listOf(owner, remaining.getOrNull(0), remaining.getOrNull(1), remaining.getOrNull(2))
        Log.d("memberLog", "list = ${members.joinToString(", ", "[", "]")}")

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

    private fun startBattle(battle: Battle, startTime: Long = -1L) {
        findNavController().navigate(
                RoomFragmentDirections.actionRoomFragmentToQuestionsFragment(battle, startTime, BATTLE_MULTIPLAYER))
    }
}