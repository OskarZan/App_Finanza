package com.example.financeszan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.financeszan.ui.screens.AddTransactionScreen
import com.example.financeszan.ui.screens.CalendarScreen
import com.example.financeszan.ui.screens.MainScreen
import com.example.financeszan.ui.screens.ProfileScreen
import com.example.financeszan.ui.screens.SettingsScreen
import com.example.financeszan.ui.theme.FinancesZanTheme
import com.example.financeszan.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val settingsViewModel = SettingsViewModel.getInstance(application)
        
        setContent {
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            // Aplicar el tema oscuro según la configuración del usuario
            FinancesZanTheme(
                darkTheme = isDarkMode
            ) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        MainScreen(
                            onAddTransactionClick = {
                                navController.navigate("add_transaction")
                            },
                            onNavigateToProfile = {
                                navController.navigate("profile")
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings")
                            },
                            onNavigateToCalendar = {
                                navController.navigate("calendar")
                            }
                        )
                    }
                    composable("add_transaction") {
                        AddTransactionScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("profile") {
                        ProfileScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("calendar") {
                        CalendarScreen(
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}