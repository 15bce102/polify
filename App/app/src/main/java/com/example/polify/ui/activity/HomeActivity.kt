package com.example.polify.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import coil.api.load
import com.andruid.magic.game.api.GameRepository
import com.example.polify.R
import com.example.polify.databinding.ActivityHomeBinding
import com.example.polify.eventbus.AvatarEvent
import com.example.polify.ui.dialog.AvatarDialogFragment
import com.example.polify.ui.viewmodel.BaseViewModelFactory
import com.example.polify.ui.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeActivity : FullScreenActivity() {
    private lateinit var binding: ActivityHomeBinding

    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private val userViewModel by viewModels<UserViewModel> {
        BaseViewModelFactory {
            UserViewModel(mAuth.currentUser?.uid ?: "")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<ActivityHomeBinding>(this, R.layout.activity_home).apply {
            viewModel = userViewModel
            lifecycleOwner = this@HomeActivity
        }

        initListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAvatarEvent(avatarEvent: AvatarEvent) {
        val (avatarUrl) = avatarEvent
        binding.imgProfile.load(avatarUrl)
        lifecycleScope.launch {
            mAuth.currentUser?.let { user ->
                val userName = binding.txtProfileName.text.toString().trim()
                val response = GameRepository.updateProfile(user.uid, userName, avatarUrl)
                if (response?.success == true)
                    Toast.makeText(this@HomeActivity, "Avatar updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initListeners() {
        binding.imgProfile.setOnClickListener {
            val dialog = AvatarDialogFragment.getInstance()
            dialog.show(supportFragmentManager, "avatarDialog")
        }

        binding.imgOnevsone.setOnClickListener {
            startActivity(Intent(this, OneVsOneActivity::class.java))
        }

        binding.imgPrivateRoom.setOnClickListener {

        }

        binding.imgPractice.setOnClickListener {
            startActivity(Intent(this, PracticeActivity::class.java))
        }
    }
}