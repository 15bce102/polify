package com.droidx.trivianest.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.droidx.trivianest.databinding.FragmentWelcomeBinding
import com.droidx.trivianest.util.setOnSoundClickListener

class WelcomeFragment : Fragment() {
    private lateinit var binding: FragmentWelcomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)

        initListeners()

        return binding.root
    }

    private fun initListeners() {
        binding.btnLogin.setOnSoundClickListener {
            findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToSignupFragment())
        }

        binding.textRegister.setOnSoundClickListener {
            findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToLoginFragment())
        }
    }
}