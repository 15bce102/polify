package com.example.polify.ui.viewmodel

import androidx.lifecycle.*
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.response.FriendsResponse
import com.andruid.magic.game.model.response.Result
import kotlinx.coroutines.*

class FriendViewModel(uid: String) : ViewModel() {
    private val _friends: MutableLiveData<Result<FriendsResponse>> = MutableLiveData(Result.loading(null))
    val friends: LiveData<Result<FriendsResponse>>
        get() = _friends

    init {
        viewModelScope.launch {
            while (isActive) {
                val response = GameRepository.getMyFriends(uid)
                _friends.postValue(response)
                delay(10000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}