package com.example.timemanager.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.timemanager.presentation.screen.home.HomeScreen
import com.example.timemanager.presentation.screen.settings.SettingsScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf("home", "settings")) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = { navController.navigate("home") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Home, contentDescription = "首页") },
                        label = { Text("首页") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "settings",
                        onClick = { navController.navigate("settings") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
                        label = { Text("设置") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
