package com.example.polify.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.response.Result
import com.andruid.magic.game.model.response.UserResponse
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class UserViewModel(private val uid: String) : ViewModel() {
    private val _user = MutableLiveData<Result<UserResponse>>()

    val user: LiveData<Result<UserResponse>>
        get() = _user

    private fun loadProfile() {
        viewModelScope.launch {
            _user.postValue(Result.loading(null))
            val response = GameRepository.userProfile(uid)
            _user.postValue(response)
        }
    }

    fun refresh() = loadProfile()

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}