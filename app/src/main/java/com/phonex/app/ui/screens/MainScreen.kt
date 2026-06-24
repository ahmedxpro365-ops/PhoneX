package com.phonex.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.phonex.app.viewmodel.PhoneViewModel
import com.phonex.app.ui.localization.Locales

@Composable
fun MainScreen(viewModel: PhoneViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, viewModel) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dialer",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dialer") { 
                DialerScreen(
                    viewModel = viewModel,
                    onNavigateToCreateContact = { number -> navController.navigate("createContact?number=$number") }
                ) 
            }
            composable("contacts") { ContactsScreen(viewModel, onNavigateToDetails = { number -> navController.navigate("callDetails/$number") }) }
            composable("recents") { RecentsScreen(viewModel, onNavigateToDetails = { number -> navController.navigate("callDetails/$number") }) }
            composable("favorites") { FavoritesScreen(viewModel, onNavigateToDetails = { number -> navController.navigate("callDetails/$number") }) }
            composable("settings") { SettingsScreen(viewModel) }
            composable("callDetails/{number}") { backStackEntry ->
                val number = backStackEntry.arguments?.getString("number") ?: ""
                CallDetailsScreen(viewModel = viewModel, number = number, onBack = { navController.popBackStack() })
            }
            composable(
                route = "createContact?number={number}",
                arguments = listOf(
                    androidx.navigation.navArgument("number") {
                        type = androidx.navigation.NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val number = backStackEntry.arguments?.getString("number") ?: ""
                CreateContactScreen(
                    viewModel = viewModel,
                    initialPhoneNumber = number,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, viewModel: PhoneViewModel) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val appLang = selectedLanguage

    val items = listOf(
        Triple("favorites", Icons.Outlined.StarBorder, Locales.get("favorites", appLang)),
        Triple("recents", Icons.Default.Schedule, Locales.get("recents", appLang)),
        Triple("dialer", Icons.Default.Dialpad, Locales.get("keypad", appLang)),
        Triple("contacts", Icons.Outlined.PersonOutline, Locales.get("contacts", appLang)),
        Triple("settings", Icons.Default.Settings, Locales.get("settings", appLang))
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val isSelected = currentRoute == item.first
            NavigationBarItem(
                icon = {
                    if (item.first == "dialer") {
                        Box(
                            modifier = Modifier
                                .size(width = 64.dp, height = 32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.second, contentDescription = item.third, tint = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Icon(item.second, contentDescription = item.third)
                    }
                },
                label = { Text(item.third, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.first) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = if (item.first == "dialer") Color.Transparent else MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
