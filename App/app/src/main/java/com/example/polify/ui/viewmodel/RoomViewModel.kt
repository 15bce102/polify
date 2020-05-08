package com.example.polify.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.andruid.magic.game.model.Battle
import com.example.polify.data.PAGE_SIZE
import com.example.polify.ui.datasource.BaseDataSourceFactory
import com.example.polify.ui.datasource.RoomDataSource
import kotlinx.coroutines.cancel

class RoomViewModel(uid: String) : ViewModel() {
    val roomLiveData: LiveData<PagedList<Battle>>

    init {
        val config = PagedList.Config.Builder()
                .setPageSize(PAGE_SIZE)
                .setEnablePlaceholders(false)
                .build()
        roomLiveData = LivePagedListBuilder(BaseDataSourceFactory {
            RoomDataSource(viewModelScope, uid)
        }, config)
                .build()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}