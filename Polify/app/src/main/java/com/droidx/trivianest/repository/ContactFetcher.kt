package com.droidx.trivianest.repository

import android.app.Application
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContentResolverCompat
import com.droidx.trivianest.model.Contact
import com.google.firebase.auth.FirebaseAuth

object ContactFetcher {
    private lateinit var context: Context
    private val mAuth by lazy { FirebaseAuth.getInstance() }

    fun init(application: Application) {
        context = application.applicationContext
    }

    fun fetchAll(): List<Contact> {
        val projectionFields = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        )
        val listContacts: ArrayList<Contact> = ArrayList()
        val cursor = ContentResolverCompat.query(
                context.contentResolver,
                ContactsContract.Contacts.CONTENT_URI,
                projectionFields,  // the columns to retrieve
                null,  // the selection criteria (none)
                null,  // the selection args (none)
                null, // the sort order (default)
                null
        )

        cursor?.use { c ->
            val contactsMap: HashMap<String, Contact>? = HashMap(c.count)

            if (c.moveToFirst()) {
                val idIndex: Int = c.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIndex: Int = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                do {
                    val contactId: String = c.getString(idIndex)
                    val contactDisplayName: String = c.getString(nameIndex)
                    val contact = Contact(contactId, contactDisplayName)
                    contactsMap?.set(contactId, contact)
                    listContacts.add(contact)
                } while (c.moveToNext())
            }

            contactsMap?.let { matchContactNumbers(it) }
        }

        return listContacts
    }

    private fun matchContactNumbers(contactsMap: Map<String, Contact>) { // Get numbers
        val numberProjection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        )
        val cursor = ContentResolverCompat.query(
                context.contentResolver,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                numberProjection,
                null,
                null,
                null,
                null
        )
        cursor?.use { phone ->
            if (phone.moveToFirst()) {
                val contactNumberColumnIndex: Int = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val contactTypeColumnIndex: Int = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                val contactIdColumnIndex: Int = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)

                while (!phone.isAfterLast) {
                    val number: String = phone.getString(contactNumberColumnIndex)
                    val contactId: String = phone.getString(contactIdColumnIndex)
                    val contact: Contact = contactsMap[contactId] ?: continue
                    val type: Int = phone.getInt(contactTypeColumnIndex)
                    val customLabel = "Custom"
                    val phoneType =
                            ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                                    context.resources,
                                    type,
                                    customLabel
                            )
                    var num = number.replace("\\s".toRegex(),"")
                    val authNumber = mAuth.currentUser!!.phoneNumber!!.substring(3,mAuth.currentUser!!.phoneNumber!!.length)
                    if(num.length>=3&&num.substring(0,3)=="+91"){
                        num = num.substring(3,num.length)
                    }
                    
                    Log.d("hello","$num $authNumber")
                    if(num != authNumber){
                        contact.addNumber(number, phoneType.toString())
                    }

                    phone.moveToNext()
                }
            }
        }
    }
}