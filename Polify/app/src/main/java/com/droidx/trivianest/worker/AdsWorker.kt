package com.droidx.trivianest.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.droidx.trivianest.util.clearAdCount

class AdsWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        applicationContext.clearAdCount()
        return Result.success()
    }
}