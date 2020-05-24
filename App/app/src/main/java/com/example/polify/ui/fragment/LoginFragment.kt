package com.example.polify.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.response.Result
import com.example.polify.R
import com.example.polify.databinding.FragmentLoginBinding
import com.example.polify.util.isValidPhoneNumber
import com.example.polify.util.setOnSoundClickListener
import com.muddzdev.styleabletoast.StyleableToast
import kotlinx.coroutines.launch
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
                if (requireContext().isValidPhoneNumber(number, countryCodePicker.selectedCountryCode)) {
                    lifecycleScope.launch {
                        val result = GameRepository.checkIfUserExists(countryCodePicker.fullNumberWithPlus)
                        if (result.status == Result.Status.SUCCESS) {
                            if (result.data?.success == true)
                                findNavController().navigate(
                                        LoginFragmentDirections.actionLoginFragmentToOtpFragment(
                                                countryCodePicker.fullNumberWithPlus, null, null))
                            else
                                StyleableToast.Builder(binding.root.context)
                                    .textBold()
                                    .backgroundColor(Color.rgb(242, 59, 35))
                                    .textColor(Color.WHITE)
                                    .textSize(14F)
                                    .text(result.data?.message ?: "")
                                    .gravity(Gravity.BOTTOM).show()

                        }
                    }
                }
                else
                    StyleableToast.Builder(binding.root.context)
                            .textBold()
                            .backgroundColor(Color.rgb(242, 59, 35))
                            .textColor(Color.WHITE)
                            .textSize(14F)
                            .text("Invalid number format")
                            .gravity(Gravity.BOTTOM).show()
            }

            textRegister.setOnSoundClickListener {
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSignupFragment())
            }
        }
    }
}