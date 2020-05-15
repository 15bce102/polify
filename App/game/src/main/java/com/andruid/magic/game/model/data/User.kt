package com.andruid.magic.game.model.data

import com.google.gson.annotations.SerializedName

data class User(
        @SerializedName("_id")
        val uid: String,
        @SerializedName("user_name")
        val userName: String,
        val level: String,
        val coins: Int,
        @SerializedName("avatar")
        val avatar: String
)