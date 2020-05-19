package com.andruid.magic.game.model.response

import com.andruid.magic.game.model.data.Friend

data class FriendsResponse(
        override val success: Boolean,
        override val message: String?,
        val friends: List<Friend>
) : ApiResponse()
