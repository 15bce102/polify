package com.andruid.magic.game.model.response

import com.andruid.magic.game.model.data.Room

data class RoomResponse(
        val success: Boolean,
        val message: String?,
        val room: Room
)