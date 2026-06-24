package com.phonex.app.ui.screens

import android.content.Context
import android.content.Intent
import android.telecom.TelecomManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonex.app.viewmodel.PhoneViewModel
import com.phonex.app.ui.localization.Locales
import com.phonex.app.ui.localization.stringLoc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: PhoneViewModel) {
    val context = LocalContext.current
    val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
    val isDefault = telecomManager != null && context.packageName == telecomManager.defaultDialerPackage

    // ViewModel states
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val defaultSim by viewModel.defaultSim.collectAsState()

    // Calls switches
    var isRecordingEnabled by remember { mutableStateOf(true) }
    var dialPadSounds by remember { mutableStateOf(true) }
    var autoSpeaker by remember { mutableStateOf(false) }
    var vibrationOnAnswer by remember { mutableStateOf(true) }

    // Contacts switches
    var showFavsAtTop by remember { mutableStateOf(true) }
    var firstLastDisplay by remember { mutableStateOf("First Name First") }

    // Blocked numbers & Spam
    var spamBlockerEnabled by remember { mutableStateOf(true) }
    var showBlockedListDialog by remember { mutableStateOf(false) }
    var blockedNumbers by remember { mutableStateOf(listOf("+1 (555) 019-2831", "+1 (555) 043-9912")) }
    var newBlockNumber by remember { mutableStateOf("") }

    // Application details dialogs
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }

    // Localized labels
    val appLang = selectedLanguage

    // Blocked Numbers Dialog
    if (showBlockedListDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedListDialog = false },
            title = { Text(Locales.get("view_blocked", appLang), fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    Text(Locales.get("blocked_num_desc", appLang), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newBlockNumber,
                            onValueChange = { newBlockNumber = it },
                            label = { Text(Locales.get("block_new_num", appLang)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newBlockNumber.isNotBlank()) {
                                    blockedNumbers = blockedNumbers + newBlockNumber.trim()
                                    newBlockNumber = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Block Number", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (blockedNumbers.isEmpty()) {
                        Text(Locales.get("no_blocked_yet", appLang), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            blockedNumbers.forEach { number ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(number, fontWeight = FontWeight.SemiBold)
                                    IconButton(
                                        onClick = {
                                            blockedNumbers = blockedNumbers.filter { it != number }
                                        }
                                    ) {
                                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Unblock", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBlockedListDialog = false }) {
                    Text(Locales.get("done", appLang), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(Locales.get("about_title", appLang), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(Locales.get("about_title", appLang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                    Text(Locales.get("about_desc", appLang), style = MaterialTheme.typography.bodyMedium)
                    Text(Locales.get("about_footer", appLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(Locales.get("dismiss", appLang), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(Locales.get("privacy_policy", appLang), fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        Locales.get("privacy_desc", appLang),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text(Locales.get("accept_close", appLang), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Licenses Dialog
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text(Locales.get("open_source", appLang), fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(Locales.get("licenses_title", appLang), style = MaterialTheme.typography.titleSmall)
                    
                    Column {
                        Text("Jetpack Compose Suite", fontWeight = FontWeight.Bold)
                        Text("Licensed under Apache License 2.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column {
                        Text("Kotlin Coroutines & Flow", fontWeight = FontWeight.Bold)
                        Text("Licensed under Apache License 2.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column {
                        Text("Room Database Engine", fontWeight = FontWeight.Bold)
                        Text("Licensed under Apache License 2.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column {
                        Text("Coil Image Loading", fontWeight = FontWeight.Bold)
                        Text("Licensed under MIT License", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text(Locales.get("close", appLang), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = Locales.get("settings", appLang),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 0. Default Dialer Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(Locales.get("default_phone_service", appLang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (isDefault) {
                    Text(Locales.get("default_phone_desc", appLang), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(Locales.get("default_phone_enable_desc", appLang), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? android.app.role.RoleManager
                                if (roleManager != null && roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_DIALER)) {
                                    val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_DIALER)
                                    context.startActivity(intent)
                                }
                            } else {
                                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                                intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
                                context.startActivity(intent)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Locales.get("set_as_default", appLang))
                    }
                }
            }
        }

        // 1. Appearance Section (Follow Device Theme Automatically)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(Locales.get("appearance_theme", appLang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Column {
                            Text(Locales.get("theme_auto", appLang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                if (selectedLanguage == "Arabic") "يتكيف التطبيق تلقائيًا مع وضع المظهر الفاتح أو الداكن لجهازك." else "PhoneX automatically syncs with your device light or dark mode settings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. Language Section (Full Dynamic Arabic switcher)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(Locales.get("language_section", appLang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                listOf("English", "Arabic").forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateSelectedLanguage(lang) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "Arabic") Locales.get("arabic_native", appLang) else Locales.get("english_us", appLang),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selectedLanguage == lang) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedLanguage == lang) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        RadioButton(
                            selected = selectedLanguage == lang,
                            onClick = { viewModel.updateSelectedLanguage(lang) }
                        )
                    }
                }
            }
        }

        // 3. Dedicated SIM Section (SIM Settings Improvements)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SimCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(Locales.get("sim_section", appLang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                Text(
                    Locales.get("preferred_sim", appLang),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                // Sim selection list
                listOf("SIM 1", "SIM 2", "Automatic").forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateDefaultSim(option) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val displayName = when(option) {
                                "SIM 1" -> Locales.get("sim1_name", appLang)
                                "SIM 2" -> Locales.get("sim2_name", appLang)
                                else -> Locales.get("sim_automatic", appLang)
                            }
                            val subtext = when(option) {
                                "SIM 1" -> Locales.get("carrier_verizon", appLang)
                                "SIM 2" -> Locales.get("carrier_tmobile", appLang)
                                else -> Locales.get("sim_status_info", appLang)
                            }
                            Text(displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = if (defaultSim == option) FontWeight.Bold else FontWeight.Normal)
                            Text(subtext, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        RadioButton(
                            selected = defaultSim == option,
                            onClick = { viewModel.updateDefaultSim(option) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

                // Active SIM Status
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(Locales.get("active_sims", appLang), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("1", modifier = Modifier.padding(2.dp)) }
                        Column {
                            Text(Locales.get("sim1_active", appLang), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(Locales.get("carrier_verizon", appLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Badge(containerColor = MaterialTheme.colorScheme.secondary) { Text("2", modifier = Modifier.padding(2.dp)) }
                        Column {
                            Text(Locales.get("sim2_active", appLang), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(Locales.get("carrier_tmobile", appLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // 4. Calls Preferences Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(Locales.get("call_preferences", appLang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Vibration settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(Locales.get("vibration_feedback", appLang), fontWeight = FontWeight.SemiBold)
                        Text(Locales.get("vibration_desc", appLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = vibrationOnAnswer, onCheckedChange = { vibrationOnAnswer = it })
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

                // Auto Speaker settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(Locales.get("auto_speaker", appLang), fontWeight = FontWeight.SemiBold)
                        Text(Locales.get("auto_speaker_desc", appLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = autoSpeaker, onCheckedChange = { autoSpeaker = it })
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

                // Call Recording Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(Locales.get("call_recording", appLang), fontWeight = FontWeight.SemiBold)
                        Text(Locales.get("call_recording_desc", appLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isRecordingEnabled, onCheckedChange = { isRecordingEnabled = it })
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

                // Dial Pad Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(Locales.get("dialpad_sounds", appLang), fontWeight = FontWeight.SemiBold)
                        Text(Locales.get("dialpad_sounds_desc", appLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = dialPadSounds, onCheckedChange = { dialPadSounds = it })
                }
            }
        }

        // 5. Contacts Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Contacts, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(Locales.get("contacts_options", appLang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(Locales.get("favorites_display", appLang), fontWeight = FontWeight.SemiBold)
                        Text(Locales.get("favorites_display_desc", appLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = showFavsAtTop, onCheckedChange = { showFavsAtTop = it })
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(Locales.get("contact_display_format", appLang), fontWeight = FontWeight.SemiBold)
                        Text("${Locales.get("format_btn", appLang)}: $firstLastDisplay", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            firstLastDisplay = if (firstLastDisplay == "First Name First") "Last Name First" else "First Name First"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(Locales.get("format_btn", appLang))
                    }
                }
            }
        }

        // 6. Blocked Numbers Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(Locales.get("view_blocked", appLang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(Locales.get("smart_spam_blocker", appLang), fontWeight = FontWeight.SemiBold)
                        Text(Locales.get("spam_blocker_desc", appLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = spamBlockerEnabled, onCheckedChange = { spamBlockerEnabled = it })
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBlockedListDialog = true }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(Locales.get("view_blocked", appLang), fontWeight = FontWeight.SemiBold)
                        Text(Locales.get("view_blocked_desc", appLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // 7. Application Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(Locales.get("app_info", appLang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAboutDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(Locales.get("about_phonex", appLang), fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(Locales.get("app_version", appLang), fontWeight = FontWeight.SemiBold)
                    Text("v2.5.0 (Flagship)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPrivacyDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(Locales.get("privacy_policy", appLang), fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLicensesDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(Locales.get("open_source", appLang), fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
