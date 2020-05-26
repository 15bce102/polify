package com.droidx.trivianest.eventbus

data class SoundEvent(val type: Sound) {
    enum class Sound {
        TYPE_BUTTON_TAP
    }
}