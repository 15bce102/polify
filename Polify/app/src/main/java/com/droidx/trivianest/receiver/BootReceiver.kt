package com.droidx.trivianest.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.droidx.trivianest.util.scheduleFriendsUpdate

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED)
            context.scheduleFriendsUpdate()
    }
}