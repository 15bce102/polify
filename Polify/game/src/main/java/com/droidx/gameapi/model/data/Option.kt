package com.droidx.gameapi.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Option(
        @SerializedName("id")
        val optId: String,
        val opt: String
) : Parcelable