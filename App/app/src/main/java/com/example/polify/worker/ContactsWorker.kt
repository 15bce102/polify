package com.example.polify.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.response.Result.Status
import com.example.polify.repository.ContactFetcher
import com.example.polify.util.toFullPhoneNumbers
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    companion object {
        private val TAG = "${ContactsWorker::class.java.simpleName}Log"
        private const val MAX_CONTACTS_SIZE = 50
    }

    private val mAuth by lazy { FirebaseAuth.getInstance() }

    override suspend fun doWork(): Result {
        Log.d(TAG, "started worker")
        return mAuth.currentUser?.let { user ->
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED)
                Result.success()

            Log.d(TAG, "fetching contacts")
            val contacts = withContext(Dispatchers.IO) { ContactFetcher.fetchAll() }
            Log.d(TAG, "getting numbers from contacts")
            val phoneNumbers = withContext(Dispatchers.IO) {
                contacts.map { contact -> contact.numbers }.flatten()
                        .map { contactPhone -> contactPhone.number }
                        .toFullPhoneNumbers(applicationContext).toSet()
            }

            Log.d(TAG, "contacts size = ${phoneNumbers.size}")
            phoneNumbers.chunked(MAX_CONTACTS_SIZE).forEach { numbers ->
                val response = GameRepository.updateFriends(user.uid, numbers)
                if (response.status == Status.SUCCESS) {
                    Log.d(TAG, "friends update successful")
                } else {
                    Log.d(TAG, "friends update failed")
                    return@let Result.retry()
                }
            }
            Result.success()
        } ?: run { Result.failure() }
    }
}