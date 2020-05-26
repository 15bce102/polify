package com.droidx.trivianest.model.response

import com.droidx.trivianest.model.data.Friend

data class FriendsResponse(
        val success: Boolean,
        val message: String?,
        val friends: List<Friend>
)
