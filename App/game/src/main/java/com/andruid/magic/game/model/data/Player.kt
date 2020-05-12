package com.andruid.magic.game.model.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Player(
        val uid: String,
        val score: Int
) : Parcelable