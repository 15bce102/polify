package com.droidx.trivianest.model.response

import com.droidx.trivianest.model.data.Question

data class QuestionsResponse(
        val success: Boolean,
        val message: String?,
        val questions: List<Question>
)