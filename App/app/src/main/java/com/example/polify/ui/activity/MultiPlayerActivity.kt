package com.example.polify.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.polify.databinding.ActivityMultiPlayerBinding

class MultiPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMultiPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createBtn.setOnClickListener {

        }

        binding.joinBtn.setOnClickListener {
            
        }
    }
}