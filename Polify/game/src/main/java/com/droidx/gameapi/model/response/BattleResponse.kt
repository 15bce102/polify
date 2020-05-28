package com.droidx.gameapi.model.response

import com.droidx.gameapi.model.data.Battle

data class BattleResponse(
        val success: Boolean,
        val message: String?,
        val battle: Battle?
)