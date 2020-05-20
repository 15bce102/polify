package com.example.polify.ui.fragment

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.polify.databinding.FragmentWelcomeBinding
import com.example.polify.util.setOnSoundClickListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import splitties.toast.toast

class WelcomeFragment : Fragment() {
    private lateinit var binding: FragmentWelcomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)

        initListeners()

        return binding.root
    }

    private fun initListeners() {
        binding.btnLogin.setOnSoundClickListener {
            Dexter.withContext(requireContext())
                    .withPermission(Manifest.permission.READ_CONTACTS)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse) {
                            findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToSignupFragment())
                        }

                        override fun onPermissionRationaleShouldBeShown(request: PermissionRequest, token: PermissionToken) {
                            token.continuePermissionRequest()
                        }

                        override fun onPermissionDenied(deniedResponse: PermissionDeniedResponse) {
                            toast("${deniedResponse.permissionName} permission denied")
                        }
                    }).check()
        }

        binding.textRegister.setOnSoundClickListener {
            Dexter.withContext(requireContext())
                    .withPermission(Manifest.permission.READ_CONTACTS)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse) {
                            findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToLoginFragment())
                        }

                        override fun onPermissionRationaleShouldBeShown(request: PermissionRequest, token: PermissionToken) {
                            token.continuePermissionRequest()
                        }

                        override fun onPermissionDenied(deniedResponse: PermissionDeniedResponse) {
                            toast("${deniedResponse.permissionName} permission denied")
                        }
                    }).check()
        }
    }
}