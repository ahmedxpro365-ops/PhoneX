package com.phonex.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.phonex.app.data.AppDatabase
import com.phonex.app.data.FavoriteContact
import com.phonex.app.domain.CallLogEntry
import com.phonex.app.domain.CallLogRepository
import com.phonex.app.domain.ContactEntry
import com.phonex.app.domain.ContactsRepository
import com.phonex.app.domain.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PhoneViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "phonex-db"
    ).build()

    private val contactsRepo = ContactsRepository(application)
    private val callLogRepo = CallLogRepository(application)
    private val favoritesRepo = FavoritesRepository(db.favoritesDao())

    private val _contacts = MutableStateFlow<List<ContactEntry>>(emptyList())
    val contacts: StateFlow<List<ContactEntry>> = _contacts.asStateFlow()

    private val _callLogs = MutableStateFlow<List<CallLogEntry>>(emptyList())
    val callLogs: StateFlow<List<CallLogEntry>> = _callLogs.asStateFlow()

    val favorites: StateFlow<List<FavoriteContact>> = favoritesRepo.allFavorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _dialerInput = MutableStateFlow("")
    val dialerInput: StateFlow<String> = _dialerInput.asStateFlow()

    val filteredContacts = combine(_contacts, _searchQuery) { contacts, query ->
        if (query.isEmpty()) {
            contacts
        } else {
            contacts.filter { it.name.contains(query, ignoreCase = true) || it.number.contains(query) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val suggestedContacts = combine(_contacts, _dialerInput) { contacts, input ->
        if (input.isEmpty()) {
            emptyList()
        } else {
            contacts.filter { it.number.replace(" ", "").replace("-", "").contains(input) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isMockMode = MutableStateFlow(false)
    val isMockMode: StateFlow<Boolean> = _isMockMode.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    fun updateSelectedLanguage(language: String) {
        _selectedLanguage.value = language
    }

    private val _defaultSim = MutableStateFlow("Automatic")
    val defaultSim: StateFlow<String> = _defaultSim.asStateFlow()

    fun updateDefaultSim(sim: String) {
        _defaultSim.value = sim
    }

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                val hasContacts = androidx.core.content.ContextCompat.checkSelfPermission(
                    getApplication(),
                    android.Manifest.permission.READ_CONTACTS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                val hasCallLogs = androidx.core.content.ContextCompat.checkSelfPermission(
                    getApplication(),
                    android.Manifest.permission.READ_CALL_LOG
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                _isMockMode.value = !hasContacts && !hasCallLogs

                if (hasContacts) {
                    _contacts.value = contactsRepo.getContacts()
                } else {
                    _contacts.value = com.phonex.app.data.MockData.contacts
                }

                if (hasCallLogs) {
                    _callLogs.value = callLogRepo.getCallLogs()
                } else {
                    _callLogs.value = com.phonex.app.data.MockData.callLogs
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isMockMode.value = true
                _contacts.value = com.phonex.app.data.MockData.contacts
                _callLogs.value = com.phonex.app.data.MockData.callLogs
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateDialerInput(input: String) {
        _dialerInput.value = input
    }
    
    fun appendDialerInput(char: Char) {
        _dialerInput.value += char
    }

    fun backspaceDialerInput() {
        if (_dialerInput.value.isNotEmpty()) {
            _dialerInput.value = _dialerInput.value.dropLast(1)
        }
    }

    fun clearDialerInput() {
        _dialerInput.value = ""
    }

    fun toggleFavorite(contact: ContactEntry) {
        viewModelScope.launch {
            // Check if already favorite. For simplicity, just checking current list.
            val isFav = favorites.value.any { it.phoneNumber == contact.number }
            if (isFav) {
                favoritesRepo.removeFavorite(contact.number)
            } else {
                favoritesRepo.addFavorite(
                    FavoriteContact(
                        phoneNumber = contact.number,
                        name = contact.name,
                        photoUri = contact.photoUri
                    )
                )
            }
        }
    }

    fun createNewContactInApp(name: String, number: String, email: String, notes: String, context: android.content.Context) {
        viewModelScope.launch {
            var savedToSystem = false
            val hasWriteContacts = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_CONTACTS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (hasWriteContacts) {
                savedToSystem = saveContactToSystem(context, name, number, email, notes)
            }
            
            // Generate temporary or actual ID
            val contactId = if (savedToSystem) System.currentTimeMillis().toString() else "custom_${System.currentTimeMillis()}"
            val newEntry = ContactEntry(
                id = contactId,
                name = name,
                number = number,
                photoUri = null
            )
            com.phonex.app.data.MockData.contacts.add(newEntry)
            loadData()
        }
    }

    fun savePremiumContact(
        context: android.content.Context,
        firstName: String,
        middleName: String,
        lastName: String,
        nickname: String,
        numbers: List<Pair<String, String>>,
        emails: List<Pair<String, String>>,
        country: String,
        city: String,
        street: String,
        postalCode: String,
        company: String,
        jobTitle: String,
        notes: String,
        birthday: String,
        groupName: String,
        addToFavorites: Boolean,
        photoUri: String?
    ) {
        viewModelScope.launch {
            val fullName = listOf(firstName, middleName, lastName).filter { it.isNotBlank() }.joinToString(" ")
            val displayName = if (fullName.isNotBlank()) fullName else if (nickname.isNotBlank()) nickname else "Unnamed"
            val primaryPhone = numbers.firstOrNull { it.first.isNotBlank() }?.first ?: ""

            val hasWriteContacts = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_CONTACTS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            var systemSaved = false
            if (hasWriteContacts) {
                systemSaved = savePremiumContactToSystem(
                    context, firstName, middleName, lastName, nickname,
                    numbers, emails, country, city, street, postalCode,
                    company, jobTitle, notes, birthday, photoUri
                )
            }

            val newContactId = if (systemSaved) System.currentTimeMillis().toString() else "mock_${System.currentTimeMillis()}"
            val newEntry = ContactEntry(
                id = newContactId,
                name = displayName,
                number = primaryPhone,
                photoUri = photoUri
            )
            
            com.phonex.app.data.MockData.contacts.add(newEntry)

            if (addToFavorites && primaryPhone.isNotEmpty()) {
                favoritesRepo.addFavorite(
                    FavoriteContact(
                        phoneNumber = primaryPhone,
                        name = displayName,
                        photoUri = photoUri
                    )
                )
            }

            loadData()
        }
    }

    private fun savePremiumContactToSystem(
        context: android.content.Context,
        firstName: String,
        middleName: String,
        lastName: String,
        nickname: String,
        numbers: List<Pair<String, String>>,
        emails: List<Pair<String, String>>,
        country: String,
        city: String,
        street: String,
        postalCode: String,
        company: String,
        jobTitle: String,
        notes: String,
        birthday: String,
        photoUri: String?
    ): Boolean {
        val ops = arrayListOf<android.content.ContentProviderOperation>()
        ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.RawContacts.CONTENT_URI)
            .withValue(android.provider.ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(android.provider.ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build())

        ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(android.provider.ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
            .withValue(android.provider.ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME, middleName)
            .withValue(android.provider.ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName)
            .build())

        if (nickname.isNotEmpty()) {
            ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
                .withValue(android.provider.ContactsContract.CommonDataKinds.Nickname.NAME, nickname)
                .build())
        }

        for (num in numbers) {
            if (num.first.isNotBlank()) {
                val type = when (num.second.lowercase()) {
                    "mobile" -> android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                    "home" -> android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_HOME
                    "work" -> android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_WORK
                    else -> android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_OTHER
                }
                ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER, num.first)
                    .withValue(android.provider.ContactsContract.CommonDataKinds.Phone.TYPE, type)
                    .build())
            }
        }

        for (em in emails) {
            if (em.first.isNotBlank()) {
                val type = when (em.second.lowercase()) {
                    "personal" -> android.provider.ContactsContract.CommonDataKinds.Email.TYPE_HOME
                    "work" -> android.provider.ContactsContract.CommonDataKinds.Email.TYPE_WORK
                    else -> android.provider.ContactsContract.CommonDataKinds.Email.TYPE_OTHER
                }
                ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(android.provider.ContactsContract.CommonDataKinds.Email.ADDRESS, em.first)
                    .withValue(android.provider.ContactsContract.CommonDataKinds.Email.TYPE, type)
                    .build())
            }
        }

        if (country.isNotEmpty() || city.isNotEmpty() || street.isNotEmpty() || postalCode.isNotEmpty()) {
            ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                .withValue(android.provider.ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, country)
                .withValue(android.provider.ContactsContract.CommonDataKinds.StructuredPostal.CITY, city)
                .withValue(android.provider.ContactsContract.CommonDataKinds.StructuredPostal.STREET, street)
                .withValue(android.provider.ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, postalCode)
                .build())
        }

        if (company.isNotEmpty() || jobTitle.isNotEmpty()) {
            ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                .withValue(android.provider.ContactsContract.CommonDataKinds.Organization.COMPANY, company)
                .withValue(android.provider.ContactsContract.CommonDataKinds.Organization.TITLE, jobTitle)
                .build())
        }

        if (notes.isNotEmpty()) {
            ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                .withValue(android.provider.ContactsContract.CommonDataKinds.Note.NOTE, notes)
                .build())
        }

        if (birthday.isNotEmpty()) {
            ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                .withValue(android.provider.ContactsContract.CommonDataKinds.Event.START_DATE, birthday)
                .withValue(android.provider.ContactsContract.CommonDataKinds.Event.TYPE, android.provider.ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
                .build())
        }

        if (photoUri != null) {
            try {
                val stream = context.contentResolver.openInputStream(android.net.Uri.parse(photoUri))
                val bytes = stream?.readBytes()
                stream?.close()
                if (bytes != null) {
                    ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        .withValue(android.provider.ContactsContract.CommonDataKinds.Photo.PHOTO, bytes)
                        .build())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return try {
            context.contentResolver.applyBatch(android.provider.ContactsContract.AUTHORITY, ops)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun appendNumberToExistingInApp(contactId: String, name: String, originalNumber: String, newNumber: String, context: android.content.Context) {
        viewModelScope.launch {
            var savedToSystem = false
            val hasWriteContacts = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_CONTACTS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (hasWriteContacts) {
                savedToSystem = appendNumberToContactSystem(context, contactId, newNumber)
            }
            
            com.phonex.app.data.MockData.contacts = com.phonex.app.data.MockData.contacts.map {
                if (it.id == contactId) {
                    it.copy(number = if (it.number.contains(newNumber)) it.number else "${it.number}, $newNumber")
                } else {
                    it
                }
            }.toMutableList()
            
            loadData()
        }
    }

    fun deleteContactInApp(contactId: String, number: String, context: android.content.Context) {
        viewModelScope.launch {
            val hasWriteContacts = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_CONTACTS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (hasWriteContacts) {
                try {
                    val ops = arrayListOf<android.content.ContentProviderOperation>()
                    ops.add(android.content.ContentProviderOperation.newDelete(android.provider.ContactsContract.RawContacts.CONTENT_URI)
                        .withSelection("${android.provider.ContactsContract.RawContacts.CONTACT_ID} = ?", arrayOf(contactId))
                        .build())
                    context.contentResolver.applyBatch(android.provider.ContactsContract.AUTHORITY, ops)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            favoritesRepo.removeFavorite(number)
            _contacts.value = _contacts.value.filter { it.id != contactId }
        }
    }

    fun editContactInApp(contactId: String, name: String, number: String, email: String, notes: String, context: android.content.Context) {
        viewModelScope.launch {
            val hasWriteContacts = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_CONTACTS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (hasWriteContacts) {
                try {
                    val ops = arrayListOf<android.content.ContentProviderOperation>()
                    ops.add(android.content.ContentProviderOperation.newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
                        .withSelection("${android.provider.ContactsContract.Data.CONTACT_ID} = ? AND ${android.provider.ContactsContract.Data.MIMETYPE} = ?", 
                            arrayOf(contactId, android.provider.ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE))
                        .withValue(android.provider.ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                        .build())
                    ops.add(android.content.ContentProviderOperation.newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
                        .withSelection("${android.provider.ContactsContract.Data.CONTACT_ID} = ? AND ${android.provider.ContactsContract.Data.MIMETYPE} = ?", 
                            arrayOf(contactId, android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE))
                        .withValue(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                        .build())
                    context.contentResolver.applyBatch(android.provider.ContactsContract.AUTHORITY, ops)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            _contacts.value = _contacts.value.map {
                if (it.id == contactId) {
                    it.copy(name = name, number = number)
                } else {
                    it
                }
            }
        }
    }

    private fun saveContactToSystem(context: android.content.Context, name: String, phone: String, email: String, notes: String): Boolean {
        val ops = arrayListOf<android.content.ContentProviderOperation>()
        ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.RawContacts.CONTENT_URI)
            .withValue(android.provider.ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(android.provider.ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build())
        ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(android.provider.ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
            .build())
        ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
            .withValue(android.provider.ContactsContract.CommonDataKinds.Phone.TYPE, android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
            .build())
        if (email.isNotEmpty()) {
            ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(android.provider.ContactsContract.CommonDataKinds.Email.ADDRESS, email)
                .withValue(android.provider.ContactsContract.CommonDataKinds.Email.TYPE, android.provider.ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                .build())
        }
        if (notes.isNotEmpty()) {
            ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                .withValue(android.provider.ContactsContract.CommonDataKinds.Note.NOTE, notes)
                .build())
        }
        return try {
            context.contentResolver.applyBatch(android.provider.ContactsContract.AUTHORITY, ops)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun appendNumberToContactSystem(context: android.content.Context, contactId: String, newNumber: String): Boolean {
        val rawContactId = getRawContactId(context, contactId) ?: return false
        val ops = arrayListOf<android.content.ContentProviderOperation>()
        ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
            .withValue(android.provider.ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber)
            .withValue(android.provider.ContactsContract.CommonDataKinds.Phone.TYPE, android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_OTHER)
            .build())
        return try {
            context.contentResolver.applyBatch(android.provider.ContactsContract.AUTHORITY, ops)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getRawContactId(context: android.content.Context, contactId: String): Long? {
        try {
            val cursor = context.contentResolver.query(
                android.provider.ContactsContract.RawContacts.CONTENT_URI,
                arrayOf(android.provider.ContactsContract.RawContacts._ID),
                "${android.provider.ContactsContract.RawContacts.CONTACT_ID} = ?",
                arrayOf(contactId),
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(android.provider.ContactsContract.RawContacts._ID)
                    if (idx != -1) return it.getLong(idx)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
