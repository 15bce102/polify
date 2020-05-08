package com.andruid.magic.game.model.response

abstract class ApiResponse {
    abstract val success: Boolean
    abstract val message: String?
}