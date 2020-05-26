package com.droidx.trivianest.util

import android.content.Context
import androidx.work.*
import com.droidx.trivianest.worker.ContactsWorker
import java.util.concurrent.TimeUnit

private const val CONTACTS_UPDATE_INTERVAL_HRS = 6L

fun Context.scheduleFriendsUpdate(refresh: Boolean = true) {
    val constraints = Constraints.Builder()
            //.setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    val request = PeriodicWorkRequestBuilder<ContactsWorker>(CONTACTS_UPDATE_INTERVAL_HRS, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

    WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("qna-contacts-worker",
                    if (refresh) ExistingPeriodicWorkPolicy.REPLACE else ExistingPeriodicWorkPolicy.KEEP,
                    request)
}