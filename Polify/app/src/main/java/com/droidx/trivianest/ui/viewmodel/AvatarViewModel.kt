package com.droidx.trivianest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.droidx.gameapi.api.GameRepository
import com.droidx.gameapi.model.response.Result

class AvatarViewModel : ViewModel() {
    val avatars = liveData {
        emit(Result.loading(null))

        val response = GameRepository.getAvatars()
        emit(response)
    }
}