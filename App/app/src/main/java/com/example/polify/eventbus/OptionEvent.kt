package com.example.polify.eventbus

import com.andruid.magic.game.model.data.Option

data class OptionEvent(val questionId: String, val option: Option)