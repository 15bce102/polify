package com.droidx.trivianest.util

import android.content.Context
import androidx.work.*
import com.droidx.trivianest.worker.AdsWorker
import com.droidx.trivianest.worker.ContactsWorker
import java.util.*
import java.util.concurrent.TimeUnit

private const val CONTACTS_UPDATE_INTERVAL_HRS = 6L
private const val ADS_COUNT_CLEAR_INTERVAL_HRS = 24L

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

fun Context.scheduleAdsCountClear() {
    val c = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_MONTH, 1)
        this[Calendar.HOUR_OF_DAY] = 0
        this[Calendar.MINUTE] = 0
        this[Calendar.SECOND] = 0
        this[Calendar.MILLISECOND] = 0
    }
    val howMany = c.timeInMillis - System.currentTimeMillis()

    val request = PeriodicWorkRequestBuilder<AdsWorker>(ADS_COUNT_CLEAR_INTERVAL_HRS, TimeUnit.HOURS)
            .setInitialDelay(howMany, TimeUnit.MILLISECONDS)
            .build()

    WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("qna-contacts-worker", ExistingPeriodicWorkPolicy.KEEP, request)
}