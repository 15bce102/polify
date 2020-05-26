package com.droidx.trivianest.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidx.trivianest.api.GameRepository
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import com.droidx.trivianest.model.response.Result
import com.droidx.trivianest.model.response.UserResponse

class UserViewModel(private val uid: String) : ViewModel() {
    private val _user = MutableLiveData<Result<UserResponse>>()

    val user: LiveData<Result<UserResponse>>
        get() = _user

    init {
        loadProfile()
    }

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