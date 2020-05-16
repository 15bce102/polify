package com.andruid.magic.game.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlayerResult(
        @SerializedName("new_level")
        val newLevel: String,
        val updated: Boolean = false,
        val player: Player
) : Parcelable