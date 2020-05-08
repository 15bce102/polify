package com.example.polify.ui.datasource

import android.util.Log
import androidx.paging.PageKeyedDataSource
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.Battle
import com.example.polify.data.FIRST_PAGE
import com.example.polify.data.PAGE_SIZE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RoomDataSource(
        private val scope: CoroutineScope,
        private val uid: String
) : PageKeyedDataSource<Int, Battle>() {
    companion object {
        private val TAG = "${RoomDataSource::class.java.simpleName}Log"
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Battle>) {
        scope.launch {
            val response = GameRepository.getMyRooms(uid, FIRST_PAGE, PAGE_SIZE)
            if (response?.success == true) {
                val roomsList = response.rooms
                Log.d(TAG, "rooms = ${roomsList?.joinToString(", ", "[", "]")}")
                val hasMore = response.hasMore
                val key = if (hasMore) FIRST_PAGE + 1 else null

                callback.onResult(roomsList ?: listOf(), null, key)
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Battle>) {
        scope.launch {
            val response = GameRepository.getMyRooms(uid, params.key, PAGE_SIZE)
            if (response?.success == true) {
                val roomsList = response.rooms
                val hasMore = response.hasMore
                val key = if (hasMore) params.key + 1 else null

                callback.onResult(roomsList ?: listOf(), key)
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Battle>) {
        scope.launch {
            val response = GameRepository.getMyRooms(uid, params.key, PAGE_SIZE)
            if (response?.success == true) {
                val roomsList = response.rooms
                val adjacentKey = if (params.key > FIRST_PAGE)
                    params.key - 1
                else null

                callback.onResult(roomsList ?: listOf(), adjacentKey)
            }
        }
    }
}