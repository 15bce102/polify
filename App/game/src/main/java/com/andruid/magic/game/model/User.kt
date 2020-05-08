package com.andruid.magic.game.model

import com.google.gson.annotations.SerializedName

data class User(
        @SerializedName("_id")
        val uid: String,
        val coins: Int
)