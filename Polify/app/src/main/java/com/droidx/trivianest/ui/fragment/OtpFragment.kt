package com.droidx.trivianest.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.droidx.trivianest.R
import com.droidx.trivianest.api.GameRepository
import com.droidx.trivianest.databinding.FragmentOtpBinding
import com.droidx.trivianest.ui.activity.HomeActivity
import com.droidx.trivianest.util.errorToast
import com.droidx.trivianest.util.setOnSoundClickListener
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.launch
import splitties.toast.toast
import java.util.concurrent.TimeUnit
import com.droidx.trivianest.model.response.Result

class OtpFragment : Fragment() {
    companion object {
        private val TAG = "${OtpFragment::class.java.simpleName}Log"
    }

    private var check = false

    private lateinit var verificationId: String
    private lateinit var binding: FragmentOtpBinding

    private val args by navArgs<OtpFragmentArgs>()
    private val userName by lazy { args.userName }
    private val avatarUri by lazy { args.avatarUri }
    private val phoneNumber by lazy { args.phoneNumber }

    private val mAuth = FirebaseAuth.getInstance()
    private val mCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onCodeSent(verificationId: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(verificationId, forceResendingToken)
            check = true
            Log.d(TAG, "onCodeSent: code sent")
            this@OtpFragment.verificationId = verificationId
        }

        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted: verify completed")
            phoneAuthCredential.smsCode?.let { otp ->
                binding.otpView.setText(otp)
            }
            signInWithCredential(phoneAuthCredential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w(TAG, "onVerificationFailed", e)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentOtpBinding.inflate(inflater, container, false)

        sendVerificationCode()

        binding.verifyBtn.setOnSoundClickListener {
            val code = binding.otpView.text.toString().trim()
            if (check && code.length == 6)
                verifyCode(code)
            else
                errorToast(getString(R.string.enter_valid_code))
        }

        return binding.root
    }

    private fun sendVerificationCode() {
        Log.d(TAG, "sendVerificationCode: sending code to $phoneNumber")
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD, mCallBack)
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { result ->
            val token = result.token
            Log.d(TAG, "token = $token")

            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                         //   toast(task.exception!!.message.toString())
                            return@addOnCompleteListener
                        }

                        Log.d(TAG, "login successful")

                        val user = task.result?.user ?: return@addOnCompleteListener

                        lifecycleScope.launch {
                            if (userName != null && avatarUri != null) {
                                val response = GameRepository.signupUser(user.uid, avatarUri!!, userName!!, token)
                                if (response.status == Result.Status.SUCCESS) {
                                    if (response.data?.success == true)
                                        startHomeActivity()
                                    else
                                        errorToast(response.data?.message)
                                } else
                                    errorToast(response.message)
                            } else {
                                val response = GameRepository.login(user.uid, token)
                                if (response.status == Result.Status.SUCCESS) {
                                    if (response.data?.success == true)
                                        startHomeActivity()
                                    else
                                        errorToast(response.data?.message)
                                } else
                                    errorToast(response.message)
                            }
                        }
                    }
        }
    }

    private fun startHomeActivity() {
        val intent = Intent(requireContext(), HomeActivity::class.java)
        requireContext().startActivity(intent)
        requireActivity().finish()
    }
}