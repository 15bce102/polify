package com.droidx.trivianest.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.droidx.trivianest.R
import com.droidx.gameapi.model.data.Room
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun Context.showMultiPlayerInviteDialog(room: Room): Boolean {
    lateinit var result: Continuation<Boolean>
    val owner = room.members.find { member -> member.uid == room.owner }!!

    AlertDialog.Builder(this)
            .setTitle(getString(R.string.title_multiplayer_invite))
            .setMessage(getString(R.string.desc_multiplayer_invite, owner.userName))
            .setCancelable(false)
            .setPositiveButton(R.string.accept) { dialogInterface, _ ->
                dialogInterface.dismiss()
                result.resume(true)
            }
            .setNegativeButton(R.string.reject) { dialogInterface, _ ->
                dialogInterface.dismiss()
                result.resume(false)
            }
            .create()
            .show()

    return suspendCoroutine { continuation -> result = continuation }
}

suspend fun Context.showConfirmationDialog(@StringRes title: Int, @StringRes msg: Int): Boolean {
    lateinit var result: Continuation<Boolean>

    AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                result.resume(true)
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
                result.resume(false)
            }
            .create()
            .show()

    return suspendCoroutine { continuation -> result = continuation }
}

suspend fun Fragment.showConfirmationDialog(@StringRes title: Int, @StringRes msg: Int) =
        requireContext().showConfirmationDialog(title, msg)