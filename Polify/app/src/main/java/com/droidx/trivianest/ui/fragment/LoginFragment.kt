package com.droidx.trivianest.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.droidx.trivianest.R
import com.droidx.gameapi.api.GameRepository
import com.droidx.trivianest.databinding.FragmentLoginBinding
import com.droidx.trivianest.util.errorToast
import com.droidx.trivianest.util.isValidPhoneNumber
import com.droidx.trivianest.util.setOnSoundClickListener
import kotlinx.coroutines.launch
import com.droidx.gameapi.model.response.Result

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
                if (requireContext().isValidPhoneNumber(number, countryCodePicker.selectedCountryCode)) {
                    lifecycleScope.launch {
                        val result = GameRepository.checkIfUserExists(countryCodePicker.fullNumberWithPlus)
                        if (result.status == Result.Status.SUCCESS) {
                            if (result.data?.success == true)
                                findNavController().navigate(
                                        LoginFragmentDirections.actionLoginFragmentToOtpFragment(
                                                countryCodePicker.fullNumberWithPlus, null, null))
                            else
                                errorToast(result.data?.message ?: "")
                        }
                    }
                } else
                    errorToast(getString(R.string.error_invalid_number))
            }

            textRegister.setOnSoundClickListener {
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSignupFragment())
            }
        }
    }
}