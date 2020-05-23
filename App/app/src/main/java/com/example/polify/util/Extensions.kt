package com.example.polify.util

import android.view.View
import com.example.polify.eventbus.SoundEvent
import org.greenrobot.eventbus.EventBus

fun View.setOnSoundClickListener(clickListener: (View) -> Unit) {
    val soundClickListener: (View) -> Unit = {
        EventBus.getDefault().post(SoundEvent(SoundEvent.Sound.TYPE_BUTTON_TAP))
        clickListener(it)
    }
    this.setOnClickListener(soundClickListener)
}