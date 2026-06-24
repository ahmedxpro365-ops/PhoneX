package com.phonex.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import android.telecom.TelecomManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.phonex.app.domain.ContactEntry
import com.phonex.app.viewmodel.PhoneViewModel
import com.phonex.app.ui.localization.Locales
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallDetailsScreen(viewModel: PhoneViewModel, number: String, onBack: () -> Unit) {
    val callLogs by viewModel.callLogs.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val appLang = selectedLanguage

    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    // Look up matching contact by number
    val cleanNumber = number.replace(" ", "").replace("-", "")
    val matchedContact = remember(contacts, number) {
        contacts.find { it.number.replace(" ", "").replace("-", "") == cleanNumber }
    }

    val contactHistory = remember(callLogs, number) {
        callLogs.filter { it.number.replace(" ", "").replace("-", "") == cleanNumber }
    }

    val name = matchedContact?.name ?: (contactHistory.firstOrNull()?.name ?: number)
    val isFavorite = favorites.any { it.phoneNumber.replace(" ", "").replace("-", "") == cleanNumber }

    // Dialog state for Edit Contact
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(name) }
    var editPhone by remember { mutableStateOf(number) }
    var editEmail by remember { mutableStateOf("") }
    var editNotes by remember { mutableStateOf("") }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(Locales.get("edit_contact", appLang), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(Locales.get("name", appLang)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text(Locales.get("phone_number", appLang)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text(Locales.get("email", appLang)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text(Locales.get("notes", appLang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(Locales.get("cancel", appLang))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank() && editPhone.isNotBlank()) {
                            if (matchedContact != null) {
                                viewModel.editContactInApp(
                                    contactId = matchedContact.id,
                                    name = editName,
                                    number = editPhone,
                                    email = editEmail,
                                    notes = editNotes,
                                    context = context
                                )
                            } else {
                                // If contact didn't exist, create it!
                                viewModel.createNewContactInApp(
                                    name = editName,
                                    number = editPhone,
                                    email = editEmail,
                                    notes = editNotes,
                                    context = context
                                )
                            }
                            showEditDialog = false
                        }
                    },
                    enabled = editName.isNotBlank() && editPhone.isNotBlank()
                ) {
                    Text(Locales.get("save", appLang))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Locales.get("contact_profile", appLang), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Profile Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 4.dp,
                        modifier = Modifier.size(110.dp)
                    ) {
                        if (matchedContact?.photoUri != null) {
                            AsyncImage(
                                model = matchedContact.photoUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = number,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Flagship Action Buttons Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Call
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                                val uri = Uri.fromParts("tel", number, null)
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
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(Locales.get("call", appLang), style = MaterialTheme.typography.labelSmall)
                    }

                    // Message
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number"))
                                context.startActivity(intent)
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Message")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(Locales.get("message", appLang), style = MaterialTheme.typography.labelSmall)
                    }

                    // Edit
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                editName = name
                                editPhone = number
                                editEmail = ""
                                editNotes = ""
                                showEditDialog = true
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(Locales.get("edit_contact", appLang), style = MaterialTheme.typography.labelSmall)
                    }

                    // Share
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Contact: $name")
                                    putExtra(Intent.EXTRA_TEXT, "Name: $name\nPhone: $number")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Contact"))
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(Locales.get("share", appLang), style = MaterialTheme.typography.labelSmall)
                    }

                    // Favorite
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                val entry = ContactEntry(
                                    id = matchedContact?.id ?: System.currentTimeMillis().toString(),
                                    name = name,
                                    number = number,
                                    photoUri = matchedContact?.photoUri
                                )
                                viewModel.toggleFavorite(entry)
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isFavorite) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(
                                if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favorite"
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(Locales.get("favorite", appLang), style = MaterialTheme.typography.labelSmall)
                    }

                    // Delete
                    if (matchedContact != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = {
                                    viewModel.deleteContactInApp(matchedContact.id, matchedContact.number, context)
                                    onBack()
                                },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                modifier = Modifier.size(50.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(Locales.get("delete", appLang), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Contact Info Details
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(Locales.get("contacts_options", appLang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        
                        // Phone number detail row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(number, fontWeight = FontWeight.SemiBold)
                                Text("Mobile", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                        // Email detail row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(if (editEmail.isNotEmpty()) editEmail else Locales.get("add_email", appLang), color = if (editEmail.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                Text(Locales.get("email", appLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                        // Notes detail row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(if (editNotes.isNotEmpty()) editNotes else Locales.get("no_notes", appLang), color = if (editNotes.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                Text(Locales.get("notes", appLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // History Header
            item {
                Text(
                    Locales.get("call_history_timeline", appLang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // History List
            if (contactHistory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(Locales.get("no_recent_logs", appLang), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }
            } else {
                items(contactHistory) { log ->
                    val typeName = when (log.type) {
                        CallLog.Calls.INCOMING_TYPE -> Locales.get("incoming_call_detail", appLang)
                        CallLog.Calls.OUTGOING_TYPE -> Locales.get("outgoing_call_detail", appLang)
                        CallLog.Calls.MISSED_TYPE -> Locales.get("missed_call_detail", appLang)
                        else -> Locales.get("call", appLang)
                    }
                    val typeColor = when (log.type) {
                        CallLog.Calls.MISSED_TYPE -> MaterialTheme.colorScheme.error
                        CallLog.Calls.INCOMING_TYPE -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    val durationText = if (log.duration > 0) {
                        val mins = log.duration / 60
                        val secs = log.duration % 60
                        if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
                    } else Locales.get("no_answer", appLang)

                    ListItem(
                        headlineContent = { Text(typeName, fontWeight = FontWeight.SemiBold, color = typeColor) },
                        supportingContent = { Text("${dateFormat.format(Date(log.date))} • ${timeFormat.format(Date(log.date))}") },
                        trailingContent = { Text(durationText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f))
                }
            }
        }
    }
}
