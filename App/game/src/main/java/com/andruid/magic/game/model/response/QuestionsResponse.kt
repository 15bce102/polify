package com.andruid.magic.game.model.response

import com.andruid.magic.game.model.data.Question

data class QuestionsResponse(
        override val success: Boolean,
        override val message: String,
        val questions: List<Question>
) : ApiResponse()