package com.example.polify.eventbus

import android.view.View
import com.andruid.magic.game.model.data.Option

data class OptionEvent(val questionId: String, val option: Option, val view: View)