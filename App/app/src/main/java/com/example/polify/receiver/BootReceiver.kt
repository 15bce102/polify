package com.example.polify.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.polify.util.scheduleFriendsUpdate

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED)
            context.scheduleFriendsUpdate()
    }
}