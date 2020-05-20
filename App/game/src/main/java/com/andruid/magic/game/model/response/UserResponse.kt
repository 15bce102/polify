package com.andruid.magic.game.model.response

import com.andruid.magic.game.model.data.User

data class UserResponse(
        val success: Boolean,
        val message: String?,
        val user: User?
)