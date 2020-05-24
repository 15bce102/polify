package com.example.polify.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.polify.R
import com.example.polify.databinding.ActivityOpenSourceLicensesBinding

class OpenSourceLicensesActivity : AppCompatActivity() {
    companion object {
        private const val ASSETS_LICENSES = "file:///android_asset/license.html"
    }

    private lateinit var binding: ActivityOpenSourceLicensesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenSourceLicensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        setTitle(R.string.open_source_licenses)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.webView.loadUrl(ASSETS_LICENSES)
    }
}