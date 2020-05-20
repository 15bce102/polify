package com.andruid.magic.game.model.response

import com.andruid.magic.game.model.data.Battle

data class BattleResponse(
        val success: Boolean,
        val message: String?,
        val battle: Battle?
)