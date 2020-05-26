package com.droidx.trivianest.util

import android.content.Context
import androidx.core.content.edit

private const val PREF_FIRST_TIME = "first_time_user_pref"
private const val KEY_FIRST_TIME = "first_time"

private const val PREF_AD_WATCHED = "ad_watched_pref"
private const val KEY_AD_COUNT = "ad_count"

private const val MAX_ADS_PER_DAY = 5

fun Context.isFirstTime() =
        getSharedPreferences(PREF_FIRST_TIME, Context.MODE_PRIVATE)
                .getBoolean(KEY_FIRST_TIME, true)

fun Context.setFirstTimeComplete() {
    getSharedPreferences(PREF_FIRST_TIME, Context.MODE_PRIVATE)
            .edit { putBoolean(KEY_FIRST_TIME, false) }
}

fun Context.canWatchMoreAds(): Boolean {
    val count = getSharedPreferences(PREF_AD_WATCHED, Context.MODE_PRIVATE)
            .getInt(KEY_AD_COUNT, 0)
    return count < MAX_ADS_PER_DAY
}

fun Context.incAdCount() {
    val prefs = getSharedPreferences(PREF_AD_WATCHED, Context.MODE_PRIVATE)

    val count = prefs.getInt(KEY_AD_COUNT, 0)
    prefs.edit { putInt(KEY_AD_COUNT, count+1) }
}

fun Context.clearAdCount() {
    getSharedPreferences(PREF_AD_WATCHED, Context.MODE_PRIVATE)
            .edit { putInt(KEY_AD_COUNT, 0) }
}