package com.phonex.app

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import com.phonex.app.ui.screens.InCallScreen
import com.phonex.app.ui.screens.MainScreen
import com.phonex.app.ui.screens.SplashScreen
import com.phonex.app.ui.theme.MyApplicationTheme
import com.phonex.app.viewmodel.CallViewModel
import com.phonex.app.viewmodel.PhoneViewModel

class MainActivity : ComponentActivity() {

    private val phoneViewModel: PhoneViewModel by viewModels()
    private val callViewModel: CallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup initial intent if needed
        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PhoneXApp(phoneViewModel, callViewModel)
                }
            }
        }
        
        requestPermissions()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_DIAL) {
            val uri = intent.data
            if (uri != null && uri.scheme == "tel") {
                val number = uri.schemeSpecificPart
                phoneViewModel.updateDialerInput(number)
            }
        }
    }

    private val roleRequestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Handle result if needed
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        phoneViewModel.loadData()
    }

    override fun onResume() {
        super.onResume()
        phoneViewModel.loadData()
    }

    private fun requestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(android.content.Context.ROLE_SERVICE) as? android.app.role.RoleManager
            if (roleManager != null && roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_DIALER) && 
                !roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_DIALER)) {
                val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_DIALER)
                roleRequestLauncher.launch(intent)
            }
        } else {
            val telecomManager = getSystemService(android.content.Context.TELECOM_SERVICE) as? TelecomManager
            if (telecomManager != null && packageName != telecomManager.defaultDialerPackage) {
                try {
                    val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                        putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                    }
                    roleRequestLauncher.launch(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.MANAGE_OWN_CALLS
            )
        )
    }
}

@Composable
fun PhoneXApp(phoneViewModel: PhoneViewModel, callViewModel: CallViewModel) {
    var showSplash by remember { mutableStateOf(true) }
    val currentCall by callViewModel.currentCall.collectAsState()
    val selectedLanguage by phoneViewModel.selectedLanguage.collectAsState()
    val layoutDirection = if (selectedLanguage == "Arabic") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (showSplash) {
                SplashScreen(onTimeout = { showSplash = false })
            } else {
                MainScreen(phoneViewModel)
            }
            
            if (currentCall != null) {
                InCallScreen(callViewModel)
            }
        }
    }
}
