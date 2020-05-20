package com.andruid.magic.game.model.response

import com.andruid.magic.game.model.data.Question

data class QuestionsResponse(
        val success: Boolean,
        val message: String?,
        val questions: List<Question>
)