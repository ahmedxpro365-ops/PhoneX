package com.phonex.app.ui.screens

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.phonex.app.viewmodel.PhoneViewModel
import com.phonex.app.ui.localization.Locales
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateContactScreen(
    viewModel: PhoneViewModel,
    initialPhoneNumber: String = "",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Language state from viewmodel
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val appLang = selectedLanguage

    // 1. Photo URI State
    var photoUri by remember { mutableStateOf<String?>(null) }

    // 2. Name Section States
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

    // 3. Dynamic Phone Number List (Type Pair)
    var phoneNumbers by remember {
        mutableStateOf(
            listOf(
                Pair(initialPhoneNumber, "Mobile"),
                Pair("", "Home"),
                Pair("", "Work")
            )
        )
    }

    // 4. Dynamic Email List (Type Pair)
    var emails by remember {
        mutableStateOf(
            listOf(
                Pair("", "Personal"),
                Pair("", "Work")
            )
        )
    }

    // 5. Address Section
    var country by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }

    // 6. Work Info
    var company by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }

    // 7. Notes
    var notes by remember { mutableStateOf("") }

    // 8. Birthday
    var birthday by remember { mutableStateOf("") }

    // 9. Groups
    val groupsList = listOf("Family", "Friends", "Work", "Custom Group")
    var selectedGroup by remember { mutableStateOf("Friends") }

    // 10. Favorites Toggle
    var addToFavorites by remember { mutableStateOf(false) }

    // Image Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
            Toast.makeText(context, Locales.get("photo_loaded", appLang), Toast.LENGTH_SHORT).show()
        }
    }

    // Camera Capture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val file = java.io.File(context.cacheDir, "contact_temp_${System.currentTimeMillis()}.jpg")
            try {
                val out = java.io.FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()
                photoUri = Uri.fromFile(file).toString()
                Toast.makeText(context, Locales.get("photo_captured", appLang), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, Locales.get("photo_failed", appLang), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Save Action Helper
    val saveContactAction = {
        val activeNumbers = phoneNumbers.filter { it.first.isNotBlank() }
        val activeName = firstName.trim()
        if (activeName.isEmpty()) {
            Toast.makeText(context, Locales.get("validation_name", appLang), Toast.LENGTH_SHORT).show()
        } else if (activeNumbers.isEmpty()) {
            Toast.makeText(context, Locales.get("validation_phone", appLang), Toast.LENGTH_SHORT).show()
        } else {
            viewModel.savePremiumContact(
                context = context,
                firstName = firstName.trim(),
                middleName = middleName.trim(),
                lastName = lastName.trim(),
                nickname = nickname.trim(),
                numbers = activeNumbers,
                emails = emails.filter { it.first.isNotBlank() },
                country = country.trim(),
                city = city.trim(),
                street = street.trim(),
                postalCode = postalCode.trim(),
                company = company.trim(),
                jobTitle = jobTitle.trim(),
                notes = notes.trim(),
                birthday = birthday.trim(),
                groupName = selectedGroup,
                addToFavorites = addToFavorites,
                photoUri = photoUri
            )

            Toast.makeText(context, Locales.get("contact_saved", appLang), Toast.LENGTH_LONG).show()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = Locales.get("create_new_contact", appLang), 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Locales.get("cancel", appLang))
                    }
                },
                actions = {
                    // Save Button moved to top-right corner, Samsung and Google Contacts style
                    TextButton(
                        onClick = { saveContactAction() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = Locales.get("save", appLang),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = Locales.get("save", appLang),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 600.dp), // Tablet friendly width limits
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // PHOTO SECTION CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            tonalElevation = 4.dp,
                            modifier = Modifier.size(110.dp)
                        ) {
                            if (photoUri != null) {
                                AsyncImage(
                                    model = photoUri,
                                    contentDescription = Locales.get("contact_photo", appLang),
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

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { cameraLauncher.launch(null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = Locales.get("camera", appLang), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Locales.get("camera", appLang), fontSize = 13.sp)
                            }

                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = Locales.get("gallery", appLang), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Locales.get("gallery", appLang), fontSize = 13.sp)
                            }

                            if (photoUri != null) {
                                Button(
                                    onClick = { photoUri = null },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = Locales.get("remove", appLang), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                // NAME SECTION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Locales.get("name_details", appLang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text(Locales.get("first_name", appLang)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = middleName,
                            onValueChange = { middleName = it },
                            label = { Text(Locales.get("middle_name", appLang)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text(Locales.get("last_name", appLang)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = { Text(Locales.get("nickname", appLang)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // PHONE NUMBERS SECTION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Locales.get("phone_numbers", appLang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        phoneNumbers.forEachIndexed { index, pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val labelKey = when(pair.second) {
                                    "Mobile" -> "mobile"
                                    "Home" -> "home"
                                    "Work" -> "work"
                                    else -> "other"
                                }
                                val localizedType = Locales.get(labelKey, appLang)
                                OutlinedTextField(
                                    value = pair.first,
                                    onValueChange = { newVal ->
                                        phoneNumbers = phoneNumbers.mapIndexed { idx, p ->
                                            if (idx == index) Pair(newVal, p.second) else p
                                        }
                                    },
                                    label = { Text("$localizedType (${Locales.get("phone", appLang)})") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                IconButton(
                                    onClick = {
                                        phoneNumbers = phoneNumbers.filterIndexed { idx, _ -> idx != index }
                                    },
                                    enabled = phoneNumbers.size > 1
                                ) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = Locales.get("remove", appLang), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        TextButton(
                            onClick = {
                                phoneNumbers = phoneNumbers + Pair("", "Other")
                            },
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Locales.get("add_another_number", appLang))
                        }
                    }
                }

                // EMAILS SECTION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Locales.get("emails", appLang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        emails.forEachIndexed { index, pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val labelKey = when(pair.second) {
                                    "Personal" -> "personal"
                                    "Work" -> "work"
                                    else -> "other"
                                }
                                val localizedType = Locales.get(labelKey, appLang)
                                OutlinedTextField(
                                    value = pair.first,
                                    onValueChange = { newVal ->
                                        emails = emails.mapIndexed { idx, p ->
                                            if (idx == index) Pair(newVal, p.second) else p
                                        }
                                    },
                                    label = { Text(localizedType) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                IconButton(
                                    onClick = {
                                        emails = emails.filterIndexed { idx, _ -> idx != index }
                                    },
                                    enabled = emails.size > 1
                                ) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = Locales.get("remove", appLang), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        TextButton(
                            onClick = {
                                emails = emails + Pair("", "Other")
                            },
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Locales.get("add_another_email", appLang))
                        }
                    }
                }

                // ADDRESS SECTION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Locales.get("postal_address", appLang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        OutlinedTextField(
                            value = street,
                            onValueChange = { street = it },
                            label = { Text(Locales.get("street_address", appLang)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text(Locales.get("city", appLang)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = postalCode,
                                onValueChange = { postalCode = it },
                                label = { Text(Locales.get("postal_code", appLang)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text(Locales.get("country", appLang)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // WORK INFO SECTION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Locales.get("work_info", appLang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        OutlinedTextField(
                            value = company,
                            onValueChange = { company = it },
                            label = { Text(Locales.get("company", appLang)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = jobTitle,
                            onValueChange = { jobTitle = it },
                            label = { Text(Locales.get("job_title", appLang)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // BIRTHDAY SECTION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cake, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Locales.get("birthday", appLang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val calendar = Calendar.getInstance()
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            birthday = "${month + 1}/$dayOfMonth/$year"
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            OutlinedTextField(
                                value = birthday,
                                onValueChange = {},
                                label = { Text(Locales.get("choose_date", appLang)) },
                                enabled = false,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    Icon(Icons.Default.CalendarToday, contentDescription = Locales.get("choose_date", appLang))
                                }
                            )
                        }
                    }
                }

                // GROUPS & PRIORITIES SECTION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Locales.get("groups_priority", appLang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Text(Locales.get("group_categorization", appLang), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            groupsList.forEach { grp ->
                                FilterChip(
                                    selected = selectedGroup == grp,
                                    onClick = { selectedGroup = grp },
                                    label = { Text(grp, fontSize = 11.sp) }
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(Locales.get("add_to_favorites", appLang), fontWeight = FontWeight.SemiBold)
                                Text(Locales.get("pin_favorites_desc", appLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = addToFavorites, onCheckedChange = { addToFavorites = it })
                        }
                    }
                }

                // NOTES SECTION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Note, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Locales.get("personal_notes", appLang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text(Locales.get("notes_details", appLang)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
