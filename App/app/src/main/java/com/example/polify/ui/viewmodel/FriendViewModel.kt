package com.example.polify.ui.viewmodel

import androidx.lifecycle.*
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.response.FriendsResponse
import com.andruid.magic.game.model.response.Result
import kotlinx.coroutines.*

class FriendViewModel(uid: String) : ViewModel() {
    companion object {
        private const val STATUS_REFRESH_TIME_MS = (10 * 1000).toLong()
    }

    private val _friends: MutableLiveData<Result<FriendsResponse>> = MutableLiveData(Result.loading(null))
    val friends: LiveData<Result<FriendsResponse>>
        get() = _friends

    init {
        viewModelScope.launch {
            while (isActive) {
                val response = GameRepository.getMyFriends(uid)
                _friends.postValue(response)
                delay(STATUS_REFRESH_TIME_MS)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}