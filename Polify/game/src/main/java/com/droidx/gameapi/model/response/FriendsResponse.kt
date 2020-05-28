package com.droidx.gameapi.model.response

import com.droidx.gameapi.model.data.Friend

data class FriendsResponse(
        val success: Boolean,
        val message: String?,
        val friends: List<Friend>
)
