package com.example.polify.util

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import androidx.fragment.app.Fragment
import com.example.polify.eventbus.SoundEvent
import com.muddzdev.styleabletoast.StyleableToast
import org.greenrobot.eventbus.EventBus

fun View.setOnSoundClickListener(clickListener: (View) -> Unit) {
    val soundClickListener: (View) -> Unit = {
        EventBus.getDefault().post(SoundEvent(SoundEvent.Sound.TYPE_BUTTON_TAP))
        clickListener(it)
    }
    this.setOnClickListener(soundClickListener)
}

fun Context.infoToast(msg: String?) {
    StyleableToast.Builder(this)
            .textBold()
            .backgroundColor(Color.rgb(22, 36, 71))
            .textColor(Color.WHITE)
            .textSize(14F)
            .text(msg ?: "no message")
            .gravity(Gravity.BOTTOM).show()
}

fun Fragment.infoToast(msg: String?) {
    requireContext().infoToast(msg)
}

fun Context.errorToast(msg: String?) {
    StyleableToast.Builder(this)
            .textBold()
            .backgroundColor(Color.rgb(255, 0, 0))
            .textColor(Color.WHITE)
            .textSize(14F)
            .text(msg ?: "error")
            .gravity(Gravity.BOTTOM).show()
}

fun Fragment.errorToast(msg: String?) {
    requireContext().errorToast(msg)
}