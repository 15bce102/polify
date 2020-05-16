package com.example.polify.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BattleSelect(
        @DrawableRes
        val image: Int,
        @StringRes
        val title: Int,
        @StringRes
        val desc: Int,
        @StringRes
        val coins: Int
) : Parcelable