package com.andruid.magic.game.model.data

import com.google.gson.annotations.SerializedName

data class Room(
        @SerializedName("_id")
        val roomId: String,
        val owner: String,
        @SerializedName("coins_pool")
        val coinsPool: Int,
        val createdAt: Long,
        val members: List<Player>
)