package com.andruid.magic.game.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Question(
        @SerializedName("_id")
        val qid: String,
        val category: String,
        @SerializedName("question")
        val questionText: String,
        val correctAnswer: String,
        val options: List<Option>
) : Parcelable