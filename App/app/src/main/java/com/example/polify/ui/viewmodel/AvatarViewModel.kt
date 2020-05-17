package com.example.polify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.response.Result


class AvatarViewModel : ViewModel() {
    val avatars = liveData {
        emit(Result.loading(null))

        val response = GameRepository.getAvatars()
        emit(response)
    }
}