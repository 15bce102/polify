package com.droidx.gameapi.model.response

import com.droidx.gameapi.model.data.User

data class UserResponse(
        val success: Boolean,
        val message: String?,
        val user: User?
)