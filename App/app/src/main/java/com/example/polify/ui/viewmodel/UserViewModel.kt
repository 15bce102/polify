package com.example.polify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.andruid.magic.game.api.GameRepository

class UserViewModel(uid: String) : ViewModel() {
    val user = liveData {
        val response = GameRepository.userProfile(uid)
        if (response?.success == true)
            response.user?.let { emit(it) }
    }
}