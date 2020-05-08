package com.example.polify.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAuth.getInstance().currentUser?.let {
            startActivity(Intent(this, HomeActivity::class.java))
        } ?: run {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}