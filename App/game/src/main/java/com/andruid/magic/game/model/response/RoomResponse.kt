package com.andruid.magic.game.model.response

import com.andruid.magic.game.model.Battle

data class RoomResponse(
        override val success: Boolean,
        override val message: String?,
        val rooms: List<Battle>?,
        val hasMore: Boolean
) : ApiResponse()