package com.droidx.trivianest.ui.viewmodel

import androidx.lifecycle.*
import com.droidx.trivianest.api.GameRepository
import com.droidx.trivianest.model.response.FriendsResponse
import kotlinx.coroutines.*
import com.droidx.trivianest.model.response.Result

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