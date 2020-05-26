package com.droidx.trivianest.model.response

import com.droidx.trivianest.model.data.Battle

data class BattleResponse(
        val success: Boolean,
        val message: String?,
        val battle: Battle?
)