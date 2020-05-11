package com.example.polify.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.polify.databinding.FragmentOtpBinding
import com.example.polify.ui.activity.HomeActivity
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import java.util.concurrent.TimeUnit

class OtpFragment : Fragment() {
    companion object {
        private val TAG = "${OtpFragment::class.java.simpleName}Log"
    }

    private lateinit var binding: FragmentOtpBinding
    private lateinit var phoneNumber: String

    private var userName: String? = null
    private var avatarUri: String? = null

    private val mAuth = FirebaseAuth.getInstance()
    private lateinit var verificationId: String

    private val mCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onCodeSent(s: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(s, forceResendingToken)
            Log.d(TAG, "onCodeSent: code sent")
            verificationId = s
        }

        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted: verify completed")
            signInWithCredential(phoneAuthCredential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w(TAG, "onVerificationFailed", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val safeArgs = OtpFragmentArgs.fromBundle(it)

            phoneNumber = safeArgs.phoneNumber
            userName = safeArgs.userName
            avatarUri = safeArgs.avatarUri
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentOtpBinding.inflate(inflater, container, false)

        binding.verifyBtn.setOnClickListener {
            val code = binding.otpView.text.toString().trim()
            if (code.length == 6)
                verifyCode(code)
        }

        sendVerificationCode()

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
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val profileUpdates = userProfileChangeRequest {
                            displayName = userName
                            photoUri = Uri.parse(avatarUri)
                        }

                        mAuth.currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                val intent = Intent(requireActivity(), HomeActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                requireActivity().finish()
                            } else
                                Toast.makeText(requireContext(), task.exception!!.message, Toast.LENGTH_SHORT).show()
                        }
                    } else
                        Toast.makeText(requireContext(), task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
    }
}