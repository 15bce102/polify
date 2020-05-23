package com.example.polify.eventbus

data class SoundEvent(val type: Sound) {
    enum class Sound {
        TYPE_BUTTON_TAP,
        TYPE_TIMER_START,
        TYPE_TIMER_STOP
    }
}