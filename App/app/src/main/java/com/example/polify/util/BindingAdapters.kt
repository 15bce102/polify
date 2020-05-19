package com.example.polify.util

import android.graphics.Color
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import coil.api.load
import com.example.polify.R
import com.example.polify.data.STATUS_BUSY
import com.example.polify.data.STATUS_OFFLINE
import com.example.polify.data.STATUS_ONLINE

@BindingAdapter("imageUrl")
fun ImageView.setImageUrl(url: String?) {
    load(url) {
        placeholder(R.mipmap.ic_profile)
    }
}

@BindingAdapter("imageRes")
fun ImageView.setImageRes(@DrawableRes res: Int) {
    load(res) {
        placeholder(R.mipmap.ic_profile)
    }
}

@BindingAdapter("friendStatus")
fun ImageView.setStatusColor(status: Int) {
    val color = when (status) {
        STATUS_ONLINE -> Color.GREEN
        STATUS_BUSY -> Color.parseColor("#fc6203")
        else -> Color.GRAY
    }
    setBackgroundColor(color)
}