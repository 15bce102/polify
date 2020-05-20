package com.andruid.magic.game.model.response

import com.andruid.magic.game.model.data.Friend

data class FriendsResponse(
        val success: Boolean,
        val message: String?,
        val friends: List<Friend>
)
