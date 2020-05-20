package com.andruid.magic.game.model.data

import com.google.gson.annotations.SerializedName

data class Friend(
        @SerializedName("_id")
        val uid: String,
        @SerializedName("user_name")
        val userName: String,
        val status: Int,
        val level: String,
        val avatar: String
)