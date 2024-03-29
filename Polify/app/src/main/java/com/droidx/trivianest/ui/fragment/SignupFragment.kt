package com.droidx.trivianest.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.api.load
import com.droidx.gameapi.server.RetrofitClient.DEFAULT_AVATAR_URL
import com.droidx.trivianest.R
import com.droidx.trivianest.databinding.FragmentSignupBinding
import com.droidx.trivianest.eventbus.AvatarEvent
import com.droidx.trivianest.ui.dialog.AvatarDialogFragment
import com.droidx.trivianest.util.errorToast
import com.droidx.trivianest.util.isValidPhoneNumber
import com.droidx.trivianest.util.isValidUserName
import com.droidx.trivianest.util.setOnSoundClickListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SignupFragment : Fragment() {
    private lateinit var binding: FragmentSignupBinding

    private var avatarUrl = DEFAULT_AVATAR_URL

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignupBinding.inflate(inflater, container, false)

        initListeners()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun initListeners() {
        binding.apply {
            imgAvatar.load(DEFAULT_AVATAR_URL)

            imgAvatar.setOnSoundClickListener {
                val dialog = AvatarDialogFragment.getInstance()
                dialog.show(childFragmentManager, "avatarDialog")
            }

            countryCodePicker.registerCarrierNumberEditText(phoneET)

            phoneET.addTextChangedListener {
                if (!requireContext().isValidPhoneNumber(it.toString(), countryCodePicker.selectedCountryCode))
                    phoneTextInput.error = getString(R.string.error_invalid_number)
                else
                    phoneTextInput.error = null
            }

            submitBtn.setOnSoundClickListener {
                val number = countryCodePicker.fullNumberWithPlus
                val userName = userNameET.text.toString().trim()

                val isPhone = requireContext().isValidPhoneNumber(number, countryCodePicker.selectedCountryCode)
                val isUserName = isValidUserName(userName)

                if (isPhone && isUserName)
                    findNavController().navigate(
                            SignupFragmentDirections.actionSignupFragmentToOtpFragment(
                                    countryCodePicker.fullNumberWithPlus, userName, avatarUrl))
                else if (!isPhone)
                    errorToast(getString(R.string.error_invalid_number))
                else if (!isUserName)
                    errorToast(getString(R.string.error_invalid_user_name))
            }

            textLogin.setOnSoundClickListener {
                findNavController().navigate(SignupFragmentDirections.actionSignupFragmentToLoginFragment())
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAvatarEvent(avatarEvent: AvatarEvent) {
        avatarUrl = avatarEvent.avatarUrl
        binding.imgAvatar.load(avatarUrl)
    }
}