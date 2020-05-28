package com.droidx.gameapi.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlayerResult(
        @SerializedName("new_level")
        val newLevel: String,
        val updated: Boolean = false,
        @SerializedName("coins")
        val coinsUpdate: String = "+0",
        val player: Player
) : Parcelable