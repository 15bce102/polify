package com.example.polify.ui.activity

import android.animation.ArgbEvaluator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import coil.api.load
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.data.Room
import com.andruid.magic.game.model.response.Result
import com.example.polify.R
import com.example.polify.data.ACTION_ROOM_INVITE
import com.example.polify.data.EXTRA_ROOM
import com.example.polify.databinding.ActivityHomeBinding
import com.example.polify.eventbus.AvatarEvent
import com.example.polify.eventbus.BattleSelectEvent
import com.example.polify.model.BattleSelect
import com.example.polify.ui.adapter.SelectBattleAdapter
import com.example.polify.ui.dialog.AvatarDialogFragment
import com.example.polify.ui.viewmodel.BaseViewModelFactory
import com.example.polify.ui.viewmodel.UserViewModel
import com.example.polify.util.scheduleFriendsUpdate
import com.example.polify.util.setOnSoundClickListener
import com.example.polify.util.showMultiPlayerInviteDialog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import splitties.resources.color
import splitties.toast.toast

class HomeActivity : FullScreenActivity() {
    private lateinit var binding: ActivityHomeBinding

    private val argbEvaluator by lazy { ArgbEvaluator() }
    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private val userViewModel by viewModels<UserViewModel> {
        BaseViewModelFactory {
            UserViewModel(mAuth.currentUser!!.uid)
        }
    }
    private val multiPlayerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_ROOM_INVITE) {
                intent.extras?.let { extras ->
                    val room = extras[EXTRA_ROOM] as Room
                    showRoomInviteDialog(room)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<ActivityHomeBinding>(this, R.layout.activity_home).apply {
            lifecycleOwner = this@HomeActivity
        }

        userViewModel.user.observe(this, Observer { result ->
            if (result.status == Result.Status.SUCCESS)
                binding.user = result.data?.user
        })

        initViewPager()
        initListeners()

        scheduleFriendsUpdate()

        binding.imgPlus.setOnSoundClickListener {
            toast("refreshing friends now")
            scheduleFriendsUpdate(refresh = true)
        }

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(multiPlayerReceiver, IntentFilter(ACTION_ROOM_INVITE))
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(multiPlayerReceiver)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAvatarEvent(avatarEvent: AvatarEvent) {
        val (avatarUrl) = avatarEvent

        binding.imgProfile.load(avatarUrl)

        lifecycleScope.launch {
            mAuth.currentUser?.let { user ->
                val userName = binding.txtProfileName.text.toString().trim()
                val response = GameRepository.updateProfile(user.uid, userName, avatarUrl)
                if (response.status == Result.Status.SUCCESS)
                    toast("Avatar updated")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBattleSelectEvent(battleSelectEvent: BattleSelectEvent) {
        when (battleSelectEvent.battle.title) {
            R.string.title_1v1 -> {
                startActivity(Intent(this, OneVsOneActivity::class.java))
            }
            R.string.title_multiplayer -> {
                startActivity(Intent(this, MultiPlayerActivity::class.java))
            }
            R.string.title_test -> {
                startActivity(Intent(this, PracticeActivity::class.java))
            }
        }
    }

    private fun initListeners() {
        binding.imgProfile.setOnSoundClickListener {
            val dialog = AvatarDialogFragment.getInstance()
            dialog.show(supportFragmentManager, "avatarDialog")
        }

        binding.signOut.setOnSoundClickListener {
            mAuth.signOut()
            finish()
        }
    }

    private fun initViewPager() {
        val selectBattles = listOf(
                BattleSelect(R.drawable.b, R.string.title_1v1, R.string.desc_1v1, R.string.coins_1v1),
                BattleSelect(R.drawable.multi, R.string.title_multiplayer, R.string.desc_multiplayer, R.string.coins_multiplayer),
                BattleSelect(R.drawable.test, R.string.title_test, R.string.desc_test, R.string.coins_test)
        )
        val selectAdapter = SelectBattleAdapter(this, selectBattles)

        binding.viewPager.apply {
            adapter = selectAdapter
            setPadding(130, 0, 130, 0)
            offscreenPageLimit = 3

            val colors = arrayOf(
                    color(R.color.page1),
                    color(R.color.page2),
                    color(R.color.page3))

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    if (position < selectAdapter.itemCount - 1 && position < colors.size - 1) {
                        setBackgroundColor(
                                (argbEvaluator.evaluate(
                                        positionOffset,
                                        colors[position],
                                        colors[position + 1]
                                ) as Int)
                        )
                    } else
                        setBackgroundColor(colors[colors.size - 1])
                }
            })
        }
    }

    private fun showRoomInviteDialog(room: Room) {
        val user = mAuth.currentUser ?: return

        lifecycleScope.launch {
            val accepted = showMultiPlayerInviteDialog(room)
            if (accepted) {
                Log.d("inviteLog", "before request")
                val result = GameRepository.joinMultiPlayerRoom(user.uid, room.roomId)
                Log.d("inviteLog", "after request status = ${result.status}")
                if (result.status == Result.Status.SUCCESS) {
                    result.data?.let { data ->
                        Log.d("inviteLog", "data not null: success = ${data.success}, room = ${data.room}")
                        if (data.success) {
                            val intent = Intent(this@HomeActivity, MultiPlayerActivity::class.java)
                                    .putExtra(EXTRA_ROOM, data.room)
                            startActivity(intent)
                        } else
                            toast(data.message ?: "")
                    } ?: run {
                        Log.d("inviteLog", "result.data is null")
                    }
                } else
                    toast(result.message ?: "")
            } else
                toast("Invite declined")
        }
    }
}