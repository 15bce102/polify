package com.andruid.magic.game.model

import com.google.gson.annotations.SerializedName

data class Player(
        @SerializedName("_id")
        val uid: String,
        val score: Int
)