package com.andruid.magic.game.model.response

import com.andruid.magic.game.model.User

data class LoginResponse(
        override val success: Boolean,
        override val message: String?,
        val user: User
) : ApiResponse()