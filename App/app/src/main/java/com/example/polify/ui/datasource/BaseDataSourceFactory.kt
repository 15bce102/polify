package com.example.polify.ui.datasource

import androidx.paging.DataSource
import com.andruid.magic.game.model.Battle

class BaseDataSourceFactory<T : DataSource<Int, Battle>>(val creator: () -> T) :
        DataSource.Factory<Int, Battle>() {
    override fun create(): DataSource<Int, Battle> = creator()
}