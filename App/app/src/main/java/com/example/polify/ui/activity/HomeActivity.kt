package com.example.polify.ui.activity

import android.animation.ArgbEvaluator
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import coil.api.load
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.response.Result
import com.example.polify.R
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
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
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
}