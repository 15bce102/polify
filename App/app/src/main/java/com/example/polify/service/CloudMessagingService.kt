package com.example.polify.service

import com.google.firebase.messaging.FirebaseMessagingService

class CloudMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        //GameRepository.updateToken()
    }
}