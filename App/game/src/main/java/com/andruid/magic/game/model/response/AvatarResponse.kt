package com.andruid.magic.game.model.response

data class AvatarResponse(
        val success: Boolean,
        val message: String?,
        val avatars: List<String>
)