package com.droidx.trivianest.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Room(
        @SerializedName("_id")
        val roomId: String,
        val owner: String,
        @SerializedName("coins_pool")
        val coinsPool: Int,
        val createdAt: Long,
        var members: List<Player>
) : Parcelable