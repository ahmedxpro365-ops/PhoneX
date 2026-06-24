package com.phonex.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telecom.TelecomManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonex.app.viewmodel.PhoneViewModel
import com.phonex.app.ui.theme.DialButtonBg
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.phonex.app.ui.localization.Locales

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialerScreen(
    viewModel: PhoneViewModel,
    onNavigateToCreateContact: (String) -> Unit
) {
    val context = LocalContext.current
    val dialerInput by viewModel.dialerInput.collectAsState()
    val suggestedContacts by viewModel.suggestedContacts.collectAsState()
    val allContacts by viewModel.contacts.collectAsState()
    
    // Language State
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val appLang = selectedLanguage

    var showDialogOptions by remember { mutableStateOf(false) }
    var showAddToExistingDialog by remember { mutableStateOf(false) }

    // Contact search inside "Add to Existing" dialog
    var existingSearchQuery by remember { mutableStateOf("") }

    // 1. Selection Dialog (Options Menu)
    if (showDialogOptions) {
        AlertDialog(
            onDismissRequest = { showDialogOptions = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.ContactPage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(Locales.get("contact_options", appLang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (dialerInput.isNotEmpty()) "${Locales.get("dialer_save_prompt", appLang)} $dialerInput:" else Locales.get("contact_actions_title", appLang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Surface(
                        onClick = {
                            showDialogOptions = false
                            onNavigateToCreateContact(dialerInput)
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(Locales.get("create_new_contact", appLang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(Locales.get("create_new_desc", appLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Surface(
                        onClick = {
                            showDialogOptions = false
                            existingSearchQuery = ""
                            showAddToExistingDialog = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.secondary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(20.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(Locales.get("add_to_existing_contact", appLang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(Locales.get("add_to_existing_desc", appLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDialogOptions = false }) {
                    Text(Locales.get("cancel", appLang), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // 2. Add to Existing Contact Dialog (List of existing contacts)
    if (showAddToExistingDialog) {
        val filteredList = remember(existingSearchQuery, allContacts) {
            if (existingSearchQuery.isBlank()) {
                allContacts
            } else {
                allContacts.filter {
                    it.name.contains(existingSearchQuery, ignoreCase = true) ||
                    it.number.contains(existingSearchQuery)
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showAddToExistingDialog = false },
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ContactPhone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(Locales.get("select_contact", appLang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    if (dialerInput.isNotEmpty()) {
                        Text(
                            "${Locales.get("appending", appLang)} $dialerInput",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = existingSearchQuery,
                        onValueChange = { existingSearchQuery = it },
                        placeholder = { Text(Locales.get("search_placeholder", appLang)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (existingSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { existingSearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Box(modifier = Modifier.height(300.dp)) {
                        if (filteredList.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Contacts,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        Locales.get("no_contacts", appLang),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredList) { contact ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.appendNumberToExistingInApp(
                                                    contactId = contact.id,
                                                    name = contact.name,
                                                    originalNumber = contact.number,
                                                    newNumber = dialerInput,
                                                    context = context
                                                )
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "${Locales.get("appended_success", appLang)} ${contact.name}",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                                showAddToExistingDialog = false
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (contact.photoUri != null) {
                                                    coil.compose.AsyncImage(
                                                        model = contact.photoUri,
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                    )
                                                } else {
                                                    val initial = contact.name.take(1).uppercase()
                                                    Text(
                                                        text = if (initial.isNotBlank()) initial else "?",
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                            
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(contact.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                Text(contact.number, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            
                                            Icon(
                                                Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddToExistingDialog = false }) {
                    Text(Locales.get("close", appLang), color = MaterialTheme.colorScheme.primary)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Suggested Contacts area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            items(suggestedContacts) { contact ->
                ListItem(
                    headlineContent = { Text(contact.number, fontSize = 24.sp, fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onBackground) },
                    supportingContent = { Text(contact.name, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.clickable {
                        viewModel.updateDialerInput(contact.number)
                    }
                )
            }
        }

        // Input display and Add Contact button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dialerInput,
                fontSize = 36.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                maxLines = 1
            )
            if (dialerInput.isNotEmpty()) {
                TextButton(onClick = { showDialogOptions = true }) {
                    Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Locales.get("add_contact_short", appLang))
                }
            }
        }

        // Keypad
        val keys = listOf(
            listOf("1" to " ", "2" to "ABC", "3" to "DEF"),
            listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
            listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
            listOf("*" to "", "0" to "+", "#" to "")
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 16.dp, start = 32.dp, end = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in keys) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (key in row) {
                            DialPadButton(
                                number = key.first,
                                letters = key.second,
                                onClick = { viewModel.appendDialerInput(key.first.first()) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Bottom row: Call, Backspace
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showDialogOptions = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Contact", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    FloatingActionButton(
                        onClick = {
                            if (dialerInput.isNotEmpty()) {
                                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                                val uri = Uri.fromParts("tel", dialerInput, null)
                                try {
                                    telecomManager.placeCall(uri, null)
                                } catch (e: Exception) {
                                    try {
                                        val intent = Intent(Intent.ACTION_CALL, uri)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(intent)
                                    } catch (ex: Exception) {
                                        ex.printStackTrace() // e.g. ActivityNotFoundException or SecurityException
                                    }
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(32.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .combinedClickable(
                                onClick = { viewModel.backspaceDialerInput() },
                                onLongClick = { viewModel.clearDialerInput() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun DialPadButton(number: String, letters: String, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(DialButtonBg)
            .clickable(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = number, fontSize = 24.sp, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onBackground)
            if (letters.isNotEmpty()) {
                Text(text = letters, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
