package com.phonex.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import android.telecom.TelecomManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phonex.app.viewmodel.PhoneViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import android.provider.ContactsContract
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import com.phonex.app.ui.localization.Locales

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentsScreen(viewModel: PhoneViewModel, onNavigateToDetails: (String) -> Unit) {
    val callLogs by viewModel.callLogs.collectAsState()
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    // Language state from viewmodel
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val appLang = selectedLanguage

    var selectedFilter by remember { mutableStateOf("All Calls") }
    val filterItems = listOf(
        Pair("All Calls", Locales.get("all_calls", appLang)),
        Pair("Missed Calls", Locales.get("missed_calls", appLang)),
        Pair("Incoming Calls", Locales.get("incoming_calls", appLang)),
        Pair("Outgoing Calls", Locales.get("outgoing_calls", appLang))
    )

    val filteredLogs = remember(callLogs, selectedFilter) {
        when (selectedFilter) {
            "Missed Calls" -> callLogs.filter { it.type == CallLog.Calls.MISSED_TYPE }
            "Incoming Calls" -> callLogs.filter { it.type == CallLog.Calls.INCOMING_TYPE }
            "Outgoing Calls" -> callLogs.filter { it.type == CallLog.Calls.OUTGOING_TYPE }
            else -> callLogs
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterItems) { item ->
                FilterChip(
                    selected = selectedFilter == item.first,
                    onClick = { selectedFilter = item.first },
                    label = { Text(item.second) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredLogs) { log ->
                var showMenu by remember { mutableStateOf(false) }

                val icon = when (log.type) {
                    CallLog.Calls.INCOMING_TYPE -> Icons.AutoMirrored.Filled.CallReceived
                    CallLog.Calls.OUTGOING_TYPE -> Icons.AutoMirrored.Filled.CallMade
                    CallLog.Calls.MISSED_TYPE -> Icons.AutoMirrored.Filled.CallMissed
                    else -> Icons.Default.Call
                }
                val typeName = when (log.type) {
                    CallLog.Calls.INCOMING_TYPE -> Locales.get("incoming", appLang)
                    CallLog.Calls.OUTGOING_TYPE -> Locales.get("outgoing", appLang)
                    CallLog.Calls.MISSED_TYPE -> Locales.get("missed", appLang)
                    else -> Locales.get("call", appLang)
                }
                val iconColor = if (log.type == CallLog.Calls.MISSED_TYPE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                
                val durationText = if (log.duration > 0) "${log.duration}s" else ""

                ListItem(
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    headlineContent = { Text(log.name ?: log.number, fontWeight = FontWeight.SemiBold) },
                    supportingContent = {
                        Text("${log.number}\n$typeName • ${dateFormat.format(Date(log.date))} ${Locales.get("at", appLang)} ${timeFormat.format(Date(log.date))} $durationText")
                    },
                    trailingContent = {
                        IconButton(onClick = {
                            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                            val uri = Uri.fromParts("tel", log.number, null)
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
                    modifier = Modifier.combinedClickable(
                        onClick = { onNavigateToDetails(log.number) },
                        onLongClick = { showMenu = true }
                    )
                )
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(Locales.get("call", appLang)) },
                        leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                            val uri = Uri.fromParts("tel", log.number, null)
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
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(Locales.get("message", appLang)) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${log.number}"))
                            context.startActivity(intent)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(Locales.get("copy_number", appLang)) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("phone number", log.number)
                            clipboard.setPrimaryClip(clip)
                        }
                    )
                    if (log.name == null) {
                        DropdownMenuItem(
                            text = { Text(Locales.get("add_contact", appLang)) },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                val intent = Intent(Intent.ACTION_INSERT_OR_EDIT).apply {
                                    type = ContactsContract.Contacts.CONTENT_ITEM_TYPE
                                    putExtra(ContactsContract.Intents.Insert.PHONE, log.number)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(Locales.get("block_number", appLang)) },
                        leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            // Done
                        }
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}
