package com.droidx.gameapi.model.response

import com.droidx.gameapi.model.data.Question

data class QuestionsResponse(
        val success: Boolean,
        val message: String?,
        val questions: List<Question>
)