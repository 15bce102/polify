package com.example.polify.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.polify.R
import com.example.polify.databinding.FragmentLoginBinding
import com.example.polify.util.isValidPhoneNumber

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
                if (!isValidPhoneNumber(countryCodePicker.fullNumberWithPlus))
                    phoneTextInput.error = getString(R.string.error_invalid_number)
                else
                    phoneTextInput.error = null
            }

            sendOtpBtn.setOnClickListener {
                val number = countryCodePicker.fullNumberWithPlus
                if (isValidPhoneNumber(number))
                    findNavController().navigate(
                            LoginFragmentDirections.actionLoginFragmentToOtpFragment(number, null, null))
                else
                    Toast.makeText(requireContext(), R.string.error_invalid_number, Toast.LENGTH_SHORT).show()
            }

            textRegister.setOnClickListener {
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSignupFragment())
            }
        }
    }
}