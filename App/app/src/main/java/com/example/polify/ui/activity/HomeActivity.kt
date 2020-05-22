package com.example.polify.ui.activity

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import coil.api.load
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.data.Player
import com.andruid.magic.game.model.data.Room
import com.andruid.magic.game.model.response.Result
import com.example.polify.R
import com.example.polify.data.ACTION_ROOM_INVITE
import com.example.polify.data.EXTRA_PLAYERS
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
import com.muddzdev.styleabletoast.StyleableToast
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import splitties.resources.color
import splitties.resources.drawable
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

    @RequiresApi(Build.VERSION_CODES.O)
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
        initFloatingMenu()

        scheduleFriendsUpdate()

        binding.imgPlus.setOnSoundClickListener {
            toast("refreshing friends now")
            scheduleFriendsUpdate(refresh = true)
        }

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(multiPlayerReceiver, IntentFilter(ACTION_ROOM_INVITE))
    }

    override fun onResume() {
        super.onResume()
        userViewModel.refresh()
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
                val user = binding.user!!
                val player = Player(
                        uid = user.uid,
                        userName = user.userName,
                        avatar = user.avatar,
                        level = user.level,
                        score = 0
                )
                val intent = Intent(this, PracticeActivity::class.java)
                        .putExtra(EXTRA_PLAYERS, arrayListOf(player))
                startActivity(intent)
            }
        }
    }

    private fun initListeners() {
        binding.imgProfile.setOnSoundClickListener {
            val dialog = AvatarDialogFragment.getInstance()
            dialog.show(supportFragmentManager, "avatarDialog")
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initFloatingMenu() {
        val fabIconNew = ImageView(this).apply {
            setImageDrawable(drawable(R.drawable.more))
        }
        val rightLowerButton = FloatingActionButton.Builder(this)
                .setContentView(fabIconNew)
                .build()
        val rLSubBuilder = SubActionButton.Builder(this)

        val rlIcon1 = ImageView(this).apply {
            setImageDrawable(drawable(R.drawable.refresh))

        }
        val rlIcon2 = ImageView(this).apply {
            setImageDrawable(drawable(R.drawable.logout))

        }
        val rlIcon3 = ImageView(this).apply {
            setImageDrawable(drawable(R.drawable.open_source))
        }

        // Build the menu with default options: light theme, 90 degrees, 72dp radius.
        // Set 4 default SubActionButtons

        // Build the menu with default options: light theme, 90 degrees, 72dp radius.
        // Set 4 default SubActionButtons
        FloatingActionMenu.Builder(this)
                .addSubActionView(rLSubBuilder.setContentView(rlIcon1).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon2).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon3).build())
                .attachTo(rightLowerButton)
                .setStateChangeListener(object : FloatingActionMenu.MenuStateChangeListener {
                    override fun onMenuOpened(menu: FloatingActionMenu) {
                        fabIconNew.rotation = 0F
                        val pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 45F)
                        val animation = ObjectAnimator.ofPropertyValuesHolder(fabIconNew, pvhR)
                        animation.start()
                    }

                    override fun onMenuClosed(menu: FloatingActionMenu) {
                        fabIconNew.rotation = 45F
                        val pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 0f)
                        val animation = ObjectAnimator.ofPropertyValuesHolder(fabIconNew, pvhR)
                        animation.start()
                    }
                })
                .build()

        rlIcon1.setOnSoundClickListener {
            StyleableToast.Builder(this)
                    .textBold()
                    .backgroundColor(Color.rgb(22,36,71))
                    .textColor(Color.WHITE)
                    .textSize(14F)
                    .text("Contacts Sync Started")
                    .gravity(Gravity.BOTTOM).show()
            scheduleFriendsUpdate()

        }

        rlIcon2.setOnSoundClickListener {

            StyleableToast.Builder(this)
                    .textBold()
                    .backgroundColor(Color.rgb(22,36,71))
                    .textColor(Color.WHITE)
                    .textSize(14F)
                    .text("Successfully Logged Out")
                    .gravity(Gravity.BOTTOM).show()
            mAuth.signOut()
            finish()
        }

        rlIcon3.setOnSoundClickListener {


       //     val st = StyleableToast.makeText(this, "Open Source Licences", Toast.LENGTH_LONG, R.style.mtToast)
            StyleableToast.Builder(this)
                    .textBold()
                    .backgroundColor(Color.rgb(22,36,71))
                    .textColor(Color.WHITE)
                    .textSize(14F)
                    .text("Open Source Licenses")
                    .gravity(Gravity.BOTTOM).show()
    //        st.show()
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