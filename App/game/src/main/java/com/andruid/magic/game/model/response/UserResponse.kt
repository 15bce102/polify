package com.andruid.magic.game.model.response

import com.andruid.magic.game.model.data.User

data class UserResponse(
        override val success: Boolean,
        override val message: String?,
        val user: User?
) : ApiResponse()