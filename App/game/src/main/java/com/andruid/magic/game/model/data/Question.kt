package com.andruid.magic.game.model.data

import com.google.gson.annotations.SerializedName

data class Question(
        @SerializedName("_id")
        val qid: Int,
        val category: String,
        @SerializedName("question")
        val questionText: String,
        val correctAnswer: String,
        val options: List<Option>
)