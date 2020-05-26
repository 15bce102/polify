package com.droidx.trivianest.model.response

import com.droidx.trivianest.model.data.User

data class UserResponse(
        val success: Boolean,
        val message: String?,
        val user: User?
)