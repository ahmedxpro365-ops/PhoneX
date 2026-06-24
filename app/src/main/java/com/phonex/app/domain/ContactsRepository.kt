package com.phonex.app.domain

import android.content.Context
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ContactEntry(
    val id: String,
    val name: String,
    val number: String,
    val photoUri: String?
)

class ContactsRepository(private val context: Context) {
    suspend fun getContacts(searchQuery: String = ""): List<ContactEntry> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<ContactEntry>()
        try {
            val selection = if (searchQuery.isNotEmpty()) {
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
            } else null
            val selectionArgs = if (searchQuery.isNotEmpty()) {
                arrayOf("%$searchQuery%")
            } else null

            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI
                ),
                selection, selectionArgs,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.use {
                val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoUriIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                while (it.moveToNext()) {
                    contacts.add(
                        ContactEntry(
                            id = if (idIndex != -1) it.getString(idIndex) else "",
                            name = if (nameIndex != -1) it.getString(nameIndex) ?: "" else "",
                            number = if (numberIndex != -1) it.getString(numberIndex) ?: "" else "",
                            photoUri = if (photoUriIndex != -1) it.getString(photoUriIndex) else null
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        
        // Filter out duplicates by number roughly
        contacts.distinctBy { it.number.replace(" ", "").replace("-", "") }
    }
}
