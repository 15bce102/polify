package com.example.polify.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.api.load
import com.andruid.magic.game.server.RetrofitClient.DEFAULT_AVATAR_URL
import com.example.polify.R
import com.example.polify.databinding.FragmentSignupBinding
import com.example.polify.util.isValidPhoneNumber
import com.example.polify.util.isValidUserName

class SignupFragment : Fragment() {
    private lateinit var binding: FragmentSignupBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignupBinding.inflate(inflater, container, false)

        initListeners()

        return binding.root
    }

    private fun initListeners() {
        binding.apply {
            imgAvatar.load(DEFAULT_AVATAR_URL)

            countryCodePicker.registerCarrierNumberEditText(phoneET)

            phoneET.addTextChangedListener {
                if (!isValidPhoneNumber(countryCodePicker.fullNumberWithPlus))
                    phoneTextInput.error = getString(R.string.error_invalid_number)
                else
                    phoneTextInput.error = null
            }

            submitBtn.setOnClickListener {
                val number = countryCodePicker.fullNumberWithPlus
                val userName = userNameET.text.toString().trim()
                val avatarUri = DEFAULT_AVATAR_URL

                if (isValidPhoneNumber(number) && isValidUserName(userName) && avatarUri.isNotEmpty())
                    findNavController().navigate(
                            SignupFragmentDirections.actionSignupFragmentToOtpFragment(number, userName, avatarUri))
                else
                    Toast.makeText(requireContext(), R.string.error_invalid_number, Toast.LENGTH_SHORT).show()
            }

            textLogin.setOnClickListener {
                findNavController().navigate(SignupFragmentDirections.actionSignupFragmentToLoginFragment())
            }
        }
    }
}