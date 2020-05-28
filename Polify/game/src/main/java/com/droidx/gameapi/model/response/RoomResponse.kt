package com.droidx.gameapi.model.response

import com.droidx.gameapi.model.data.Room

data class RoomResponse(
        val success: Boolean,
        val message: String?,
        val room: Room
)