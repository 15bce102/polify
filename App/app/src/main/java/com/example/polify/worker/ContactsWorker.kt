package com.example.polify.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.andruid.magic.game.api.GameRepository
import com.example.polify.repository.ContactFetcher
import com.example.polify.util.toFullPhoneNumbers
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    companion object {
        private val TAG = "${ContactsWorker::class.java.simpleName}Log"
    }

    private val mAuth by lazy { FirebaseAuth.getInstance() }

    override suspend fun doWork(): Result {
        return mAuth.currentUser?.let { user ->
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED)
                Result.success()

            val contacts = withContext(Dispatchers.IO) { ContactFetcher.fetchAll() }
            val phoneNumbers = withContext(Dispatchers.IO) {
                contacts.asSequence().map { contact -> contact.numbers }.flatten()
                        .map { contactPhone -> contactPhone.number }.toSet().toList().toFullPhoneNumbers(applicationContext)
            }
            val response = GameRepository.updateFriends(user.uid, phoneNumbers)
            if (response?.success == true) {
                Log.d(TAG, "friends update successful")
                Result.success()
            } else {
                Log.d(TAG, "friends update failed")
                Result.retry()
            }
        } ?: run { Result.failure() }
    }
}