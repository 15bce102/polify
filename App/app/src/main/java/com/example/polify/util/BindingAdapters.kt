package com.example.polify.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.api.load
import com.example.polify.R

@BindingAdapter("app:imageUrl")
fun ImageView.setImageUrl(url: String?) {
    load(url) {
        placeholder(R.mipmap.ic_profile)
    }
}