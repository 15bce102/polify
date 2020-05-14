package com.example.polify.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.polify.R

class QuestionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions)
        Log.d("cloudLog", "onCreate questions activity")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("cloudLog", "onDestroy questions activity")
    }
}