package com.example.polify.eventbus

data class SoundEvent(val type: Sound) {
    enum class Sound {
        TYPE_BUTTON_TAP
    }
}