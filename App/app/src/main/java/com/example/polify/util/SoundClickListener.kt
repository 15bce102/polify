package com.example.polify.util

import android.view.View
import com.example.polify.eventbus.ClickSoundEvent
import org.greenrobot.eventbus.EventBus

fun View.setOnSoundClickListener(clickListener: (View) -> Unit) {
    val soundClickListener: (View) -> Unit = {
        EventBus.getDefault().post(ClickSoundEvent(type = 0))
        clickListener(it)
    }
    this.setOnClickListener(soundClickListener)
}