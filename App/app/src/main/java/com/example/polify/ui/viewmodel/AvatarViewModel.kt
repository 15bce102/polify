package com.example.polify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.andruid.magic.game.api.GameRepository

class AvatarViewModel : ViewModel() {
    val avatars = liveData {
        val response = GameRepository.getAvatars()
        if (response?.success == true)
            emit(response.avatars)
    }
}