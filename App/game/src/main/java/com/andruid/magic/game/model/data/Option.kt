package com.andruid.magic.game.model.data

import com.google.gson.annotations.SerializedName

data class Option(
        @SerializedName("id")
        val optId: Int,
        val opt: String
)