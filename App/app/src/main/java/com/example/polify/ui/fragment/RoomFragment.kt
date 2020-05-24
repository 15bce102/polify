package com.example.polify.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
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
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.data.Battle
import com.andruid.magic.game.model.data.Room
import com.andruid.magic.game.model.response.Result
import com.example.polify.R
import com.example.polify.data.*
import com.example.polify.databinding.FragmentRoomBinding
import com.example.polify.eventbus.FriendInviteEvent
import com.example.polify.ui.adapter.FriendAdapter
import com.example.polify.ui.viewmodel.BaseViewModelFactory
import com.example.polify.ui.viewmodel.FriendViewModel
import com.example.polify.util.setOnSoundClickListener
import com.example.polify.util.showConfirmationDialog
import com.google.firebase.auth.FirebaseAuth
import com.muddzdev.styleabletoast.StyleableToast
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import splitties.toast.toast

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
                val shouldLeave = requireContext().showConfirmationDialog(R.string.title_leave_room, R.string.desc_leave_room)
                if (!shouldLeave)
                    return@launch

                val user = mAuth.currentUser ?: return@launch

                lifecycleScope.launch {
                    val result = GameRepository.leaveMultiPlayerRoom(user.uid, room.roomId)
                    if (result.status == Result.Status.SUCCESS) {
                        result.data?.let { data ->
                            if (data.success) {
                                StyleableToast.Builder(binding.root.context)
                                        .textBold()
                                        .backgroundColor(Color.rgb(22, 36, 71))
                                        .textColor(Color.WHITE)
                                        .textSize(14F)
                                        .text("Room Left Successfully")
                                        .gravity(Gravity.BOTTOM).show()
                            } else {
                                StyleableToast.Builder(binding.root.context)
                                        .textBold()
                                        .backgroundColor(Color.rgb(255, 0, 0))
                                        .textColor(Color.WHITE)
                                        .textSize(14F)
                                        .text("could not leave room")
                                        .gravity(Gravity.BOTTOM).show()
                            }
                        }
                    }
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
                    toast(message)

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
                Log.d("mpLog", "before start")
                val result = GameRepository.startMultiPlayerBattle(user.uid, room.roomId)
                Log.d("mpLog", "after start status = ${result.status}")

                if (result.status == Result.Status.SUCCESS) {
                    result.data?.let { data ->
                        if (data.success)
                            toast("match will start shortly")
                        else
                            toast(data.message ?: "null")
                    }
                }
            }
        }
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
                        {}
                    }
                }
            }
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