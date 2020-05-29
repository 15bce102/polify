package com.droidx.trivianest.application

import android.app.Application
import com.droidx.gameapi.api.GameRepository
import com.droidx.trivianest.repository.ContactFetcher
import com.google.android.gms.ads.MobileAds

@Suppress("unused")
class GameApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this)

        GameRepository.init(this)
        ContactFetcher.init(this)
    }
}