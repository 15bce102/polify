package com.droidx.trivianest.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.droidx.gameapi.model.data.Room
import com.droidx.trivianest.R
import com.droidx.trivianest.data.ACTION_ROOM_INVITE
import com.droidx.trivianest.data.EXTRA_ROOM
import com.droidx.trivianest.ui.activity.HomeActivity
import splitties.systemservices.notificationManager

private const val CHANNEL_ID = "room-invite-channel"
private const val CHANNEL_NAME = "Room invites"

fun Context.buildNotification(room: Room): NotificationCompat.Builder {
    val intent = Intent(this, HomeActivity::class.java)
            .setAction(ACTION_ROOM_INVITE)
            .setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
            .putExtra(EXTRA_ROOM, room)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel =
                NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        importance
                ).apply {
                    enableLights(true)
                    lightColor = Color.GREEN
                }
        notificationManager.createNotificationChannel(notificationChannel)
    }

    val owner = room.members.find { member -> member.uid == room.owner }!!

    return NotificationCompat.Builder(this, CHANNEL_ID).apply {
        setSmallIcon(R.mipmap.main_logo_round)

        priority = NotificationCompat.PRIORITY_HIGH
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setOnlyAlertOnce(true)
        setAutoCancel(true)

        setContentIntent(PendingIntent.getActivity(this@buildNotification, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        )
        setContentTitle(getString(R.string.title_multiplayer_invite))
        setContentText(getString(R.string.desc_multiplayer_invite, owner.userName))

        setShowWhen(true)
    }
}