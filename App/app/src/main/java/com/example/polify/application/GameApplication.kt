package com.example.polify.application

import android.app.Application
import com.andruid.magic.game.api.GameRepository
import com.example.polify.repository.ContactFetcher

class GameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        GameRepository.init(this)
        ContactFetcher.init(this)
    }
}