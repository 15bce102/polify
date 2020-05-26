package com.droidx.trivianest.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Player(
        val uid: String,
        @SerializedName("user_name")
        val userName: String,
        val avatar: String,
        val level: String,
        val score: Int
) : Parcelable