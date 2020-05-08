package com.andruid.magic.game.model

import com.google.gson.annotations.SerializedName

data class Battle(
        @SerializedName("_id")
        val battleId: String,

        val creator: String,
        val started: Boolean = false,
        val time: Long,

        @SerializedName("coins_pool")
        val coinsPool: Int,

        val members: List<Player>?
)