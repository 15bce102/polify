package com.droidx.trivianest.ui.activity

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import coil.api.load
import com.droidx.trivianest.R
import com.droidx.trivianest.api.GameRepository
import com.droidx.trivianest.data.ACTION_ROOM_INVITE
import com.droidx.trivianest.data.EXTRA_PLAYERS
import com.droidx.trivianest.data.EXTRA_ROOM
import com.droidx.trivianest.databinding.ActivityHomeBinding
import com.droidx.trivianest.eventbus.AvatarEvent
import com.droidx.trivianest.eventbus.BattleSelectEvent
import com.droidx.trivianest.model.BattleSelect
import com.droidx.trivianest.model.data.Player
import com.droidx.trivianest.model.data.Room
import com.droidx.trivianest.ui.adapter.SelectBattleAdapter
import com.droidx.trivianest.ui.dialog.AvatarDialogFragment
import com.droidx.trivianest.ui.viewmodel.BaseViewModelFactory
import com.droidx.trivianest.ui.viewmodel.UserViewModel
import com.droidx.trivianest.util.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import splitties.resources.color
import splitties.resources.drawable
import com.droidx.trivianest.model.response.Result

class HomeActivity : FullScreenActivity(), RewardedVideoAdListener {
    companion object {
        private val TAG = "${this::class.java.simpleName}Log"
    }

    private lateinit var mRewardedVideoAd: RewardedVideoAd

    private var dialogShowing = false
    private var retry = false

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

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<ActivityHomeBinding>(this, R.layout.activity_home).apply {
            lifecycleOwner = this@HomeActivity
        }

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713")
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this
        loadRewardedVideoAd()

        userViewModel.user.observe(this, Observer { result ->
            when (result.status) {
                Result.Status.LOADING -> {
                    binding.loadingAnimView.setAnimation(R.raw.loading)
                    binding.loadingView.visibility = View.VISIBLE
                    binding.loadingAnimView.playAnimation()
                }
                Result.Status.SUCCESS -> {
                    binding.user = result.data?.user
                    binding.loadingView.visibility = View.GONE
                    retry = false
                }
                Result.Status.ERROR -> {
                    binding.loadingAnimView.setAnimation(R.raw.error)
                    binding.loadingView.visibility = View.VISIBLE
                    binding.loadingAnimView.playAnimation()
                    retry = true
                }
            }
        })

        initViewPager()
        initListeners()
        initConnectivityCallback()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            initFloatingMenu()

        scheduleFriendsUpdate()

        binding.imgPlus.setOnSoundClickListener {
            if (mRewardedVideoAd.isLoaded) {
                mRewardedVideoAd.show()
            }
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

        val user = mAuth.currentUser ?: return

        lifecycleScope.launch {
            val response = GameRepository.updateAvatar(user.uid, avatarUrl)
            if (response.status == Result.Status.SUCCESS) {
                if (response.data?.success == true)
                    infoToast(getString(R.string.avatar_updated))
                else
                    errorToast(response.data?.message ?: "")
            } else
                errorToast(response.message ?: "")
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
            infoToast(getString(R.string.contacts_sync_start))

            scheduleFriendsUpdate()
        }

        rlIcon2.setOnSoundClickListener {
            infoToast(getString(R.string.logout_success))

            mAuth.signOut()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        rlIcon3.setOnSoundClickListener {
            startActivity(Intent(this, OpenSourceLicensesActivity::class.java))
        }
    }

    private fun initConnectivityCallback() {
        val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()

        getSystemService<ConnectivityManager>()?.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)

                if (retry)
                    userViewModel.refresh()
            }
        })
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

        binding.apply {
            TabLayoutMediator(tabLayout, viewPager) { _, _ ->
            }.attach()
        }
    }

    private fun showRoomInviteDialog(room: Room) {
        if (dialogShowing)
            return

        val user = mAuth.currentUser ?: return

        dialogShowing = true

        lifecycleScope.launch {
            val accepted = showMultiPlayerInviteDialog(room)
            dialogShowing = false
            if (accepted) {
                val result = GameRepository.joinMultiPlayerRoom(user.uid, room.roomId)

                Log.d(TAG, "after request status = ${result.status}")

                if (result.status == Result.Status.SUCCESS) {
                    result.data?.let { data ->
                        if (data.success) {
                            val intent = Intent(this@HomeActivity, MultiPlayerActivity::class.java)
                                    .putExtra(EXTRA_ROOM, data.room)
                            startActivity(intent)
                        } else
                            errorToast(data.message ?: "")
                    }
                } else
                    errorToast(result.message ?: "")
            } else
                infoToast(getString(R.string.decline_invite))
        }
    }

    private fun loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", AdRequest.Builder().build())
    }


    override fun onRewardedVideoAdClosed() {
        Log.d(TAG, "OnRewardedVideoAdClosed")
        loadRewardedVideoAd()
    }

    override fun onRewardedVideoAdLeftApplication() {
        Log.d(TAG, "onRewardedVideoAdLeftApplication")
    }

    override fun onRewardedVideoAdLoaded() {
        Log.d(TAG, "onRewardedVideoAdLoaded")
    }

    override fun onRewardedVideoAdOpened() {

        Log.d(TAG, "onRewardedVideoAdOpened")
    }

    override fun onRewardedVideoCompleted() {

        Log.d(TAG, "onRewardedVideoCompleted")
    }

    override fun onRewarded(p0: RewardItem?) {

        Log.d(TAG, "onRewarded")
        val user = mAuth.currentUser ?: return

        lifecycleScope.launch {
            val result = GameRepository.addCoins(user.uid)
            if (result.status == Result.Status.SUCCESS) {
                if (result.data?.success == true) {
                    infoToast("100 Coins added")
                    userViewModel.refresh()

                } else
                    errorToast("Could not add coins")
            } else
                errorToast("Could not add coins")
        }
    }

    override fun onRewardedVideoStarted() {

        Log.d(TAG, "onRewardedVideoStarted")
    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
        Log.d(TAG, "onRewardedVideoAdFailedToLoad")
    }
}