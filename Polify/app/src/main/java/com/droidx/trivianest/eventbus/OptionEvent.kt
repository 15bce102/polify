package com.droidx.trivianest.eventbus

import android.view.View
import com.droidx.trivianest.model.data.Option

data class OptionEvent(val questionId: String, val option: Option, val view: View)