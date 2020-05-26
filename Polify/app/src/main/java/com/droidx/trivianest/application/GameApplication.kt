package com.droidx.trivianest.application

import android.app.Application
import com.droidx.trivianest.api.GameRepository
import com.droidx.trivianest.repository.ContactFetcher

@Suppress("unused")
class GameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        GameRepository.init(this)
        ContactFetcher.init(this)
    }
}