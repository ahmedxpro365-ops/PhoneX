package com.phonex.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telecom.TelecomManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phonex.app.viewmodel.PhoneViewModel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background

import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

import com.phonex.app.ui.localization.Locales

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ContactsScreen(viewModel: PhoneViewModel, onNavigateToDetails: (String) -> Unit) {
    val contacts by viewModel.filteredContacts.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val appLang = selectedLanguage
    val context = LocalContext.current
    
    val groupedContacts = remember(contacts) {
        contacts.filter { it.name.isNotBlank() }
                .groupBy { it.name.first().uppercaseChar() }
                .toSortedMap()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onSearch = {},
            active = false,
            onActiveChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text(Locales.get("search_placeholder", appLang), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                dividerColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {}

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            groupedContacts.forEach { (initial, contactsForInitial) ->
                stickyHeader {
                    Text(
                        text = initial.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
                items(contactsForInitial) { contact ->
                    val isFavorite = favorites.any { it.phoneNumber == contact.number }
                    ListItem(
                        leadingContent = {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                if (contact.photoUri != null) {
                                    AsyncImage(
                                        model = contact.photoUri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp))
                                }
                            }
                        },
                        headlineContent = { Text(contact.name, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(contact.number) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { viewModel.toggleFavorite(contact) }) {
                                    Icon(
                                        if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = {
                                    val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                                    val uri = Uri.fromParts("tel", contact.number, null)
                                    try {
                                        telecomManager.placeCall(uri, null)
                                    } catch (e: Exception) {
                                        try {
                                            val intent = Intent(Intent.ACTION_CALL, uri)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            context.startActivity(intent)
                                        } catch (ex: Exception) {
                                            ex.printStackTrace()
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        modifier = Modifier.clickable { onNavigateToDetails(contact.number) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}
