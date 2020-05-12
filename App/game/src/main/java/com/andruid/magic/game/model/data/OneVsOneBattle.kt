package com.andruid.magic.game.model.data

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OneVsOneBattle(
        @SerializedName("_id")
        override val battleId: String,
        @SerializedName("start_time")
        override val startTime: Long,
        override val players: List<Player>,
        @SerializedName("coins_pool")
        override val coinsPool: Int
) : Battle(battleId, startTime, players, coinsPool)