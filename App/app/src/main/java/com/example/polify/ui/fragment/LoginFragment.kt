package com.example.polify.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.polify.R
import com.example.polify.databinding.FragmentLoginBinding
import com.example.polify.util.isValidPhoneNumber
import com.example.polify.util.setOnSoundClickListener
import splitties.toast.toast

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        initListeners()

        return binding.root
    }

    private fun initListeners() {
        binding.apply {
            countryCodePicker.registerCarrierNumberEditText(phoneET)

            phoneET.addTextChangedListener {
                if (!requireContext().isValidPhoneNumber(it.toString(), countryCodePicker.selectedCountryCode))
                    phoneTextInput.error = getString(R.string.error_invalid_number)
                else
                    phoneTextInput.error = null
            }

            sendOtpBtn.setOnSoundClickListener {
                val number = phoneET.text.toString().trim()
                if (requireContext().isValidPhoneNumber(number, countryCodePicker.selectedCountryCode))
                    findNavController().navigate(
                            LoginFragmentDirections.actionLoginFragmentToOtpFragment(
                                    countryCodePicker.fullNumberWithPlus, null, null))
                else
                    toast(R.string.error_invalid_number)
            }

            textRegister.setOnSoundClickListener {
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSignupFragment())
            }
        }
    }
}