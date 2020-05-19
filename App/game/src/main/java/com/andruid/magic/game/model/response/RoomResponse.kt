package com.andruid.magic.game.model.response

import com.andruid.magic.game.model.data.Room

data class RoomResponse(
        override val success: Boolean,
        override val message: String?,
        val room: Room
): ApiResponse()