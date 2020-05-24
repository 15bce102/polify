package com.example.polify.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.andruid.magic.game.model.data.Room
import com.example.polify.R
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun Context.showMultiPlayerInviteDialog(room: Room): Boolean {
    lateinit var result: Continuation<Boolean>
    val owner = room.members.find { member -> member.uid == room.owner }!!

    AlertDialog.Builder(this)
            .setTitle(getString(R.string.title_multiplayer_invite))
            .setMessage(getString(R.string.desc_multiplayer_invite, owner.userName))
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

fun Context.buildLoadingDialog(): AlertDialog {
    return AlertDialog.Builder(this, R.style.TranslucentDialog)
            .setCancelable(false)
            .setView(R.layout.dialog_loading)
            .create()
}