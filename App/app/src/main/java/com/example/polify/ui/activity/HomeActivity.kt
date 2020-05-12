package com.example.polify.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.polify.R
import com.example.polify.databinding.ActivityHomeBinding
import com.example.polify.ui.viewmodel.BaseViewModelFactory
import com.example.polify.ui.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

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

    private fun initListeners() {
        binding.imgOnevsone.setOnClickListener {
            startActivity(Intent(this, OneVsOneActivity::class.java))
        }

        binding.imgPrivateRoom.setOnClickListener {

        }

        binding.imgPractice.setOnClickListener {

        }
    }
}