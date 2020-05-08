package com.andruid.magic.game.model.response

import com.andruid.magic.game.model.Battle

data class BattleResponse(
        override val success: Boolean,
        override val message: String?,
        val battle: Battle?
) : ApiResponse()