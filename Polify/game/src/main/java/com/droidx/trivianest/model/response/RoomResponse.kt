package com.droidx.trivianest.model.response

import com.droidx.trivianest.model.data.Room

data class RoomResponse(
        val success: Boolean,
        val message: String?,
        val room: Room
)