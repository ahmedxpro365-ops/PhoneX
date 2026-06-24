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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phonex.app.viewmodel.PhoneViewModel

import com.phonex.app.ui.localization.Locales

@Composable
fun FavoritesScreen(viewModel: PhoneViewModel, onNavigateToDetails: (String) -> Unit) {
    val favorites by viewModel.favorites.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val appLang = selectedLanguage
    val context = LocalContext.current

    if (favorites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text(Locales.get("no_favorites", appLang), style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(favorites) { contact ->
                ListItem(
                    leadingContent = {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp))
                        }
                    },
                    headlineContent = { Text(contact.name, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text(contact.phoneNumber) },
                    trailingContent = {
                        IconButton(onClick = {
                            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                            val uri = Uri.fromParts("tel", contact.phoneNumber, null)
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
                    },
                    modifier = Modifier.clickable { onNavigateToDetails(contact.phoneNumber) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}
