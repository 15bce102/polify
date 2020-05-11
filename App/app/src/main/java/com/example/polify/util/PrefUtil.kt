package com.example.polify.util

import android.content.Context
import androidx.core.content.edit

private const val PREF_FIRST_TIME = "first_time_user_pref"
private const val KEY_FIRST_TIME = "first_time"

fun Context.isFirstTime() =
        getSharedPreferences(PREF_FIRST_TIME, Context.MODE_PRIVATE)
                .getBoolean(KEY_FIRST_TIME, true)

fun Context.setFirstTimeComplete() =
        getSharedPreferences(PREF_FIRST_TIME, Context.MODE_PRIVATE)
                .edit { putBoolean(KEY_FIRST_TIME, false) }
