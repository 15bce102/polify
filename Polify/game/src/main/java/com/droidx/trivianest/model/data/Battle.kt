package com.droidx.trivianest.model.data

import android.os.Parcelable

abstract class Battle(
        open val battleId: String,
        open val startTime: Long,
        open val players: List<Player>,
        open val coinsPool: Int
) : Parcelable