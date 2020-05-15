package com.andruid.magic.game.model.response

data class AvatarResponse(
        override val success: Boolean,
        override val message: String?,
        val avatars: List<String>
) : ApiResponse()