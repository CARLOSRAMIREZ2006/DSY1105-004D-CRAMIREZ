package com.example.insertcode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.insertcode.navigation.NavigationEvent
import com.example.insertcode.navigation.Screen
import com.example.insertcode.ui.screens.HomeScreen
import com.example.insertcode.ui.screens.ProfileScreen
import com.example.insertcode.ui.screens.SettingsScreen
import com.example.insertcode.ui.theme.InsertcodeTheme
import com.example.insertcode.viewmodels.MainViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InsertcodeTheme {
                val viewModel: MainViewModel = viewModel()
                val navController = rememberNavController()

                // Escuchar eventos de navegación emitidos por el ViewModel
                LaunchedEffect(Unit) {
                    viewModel.navigationEvents.collectLatest { event ->
                        when (event) {
                            is NavigationEvent.NavigateTo -> {
                                navController.navigate(route = event.route.route) {
                                    event.popupToRoute?.let { popRoute ->
                                        popUpTo(popRoute.route) {
                                            inclusive = event.inclusive
                                        }
                                    }
                                    launchSingleTop = event.singleTop
                                    restoreState = true
                                }
                            }
                            is NavigationEvent.PopBackStack -> navController.popBackStack()
                            is NavigationEvent.NavigateUp -> navController.navigateUp()
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(route = Screen.Home.route) {
                            HomeScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(route = Screen.Profile.route) {
                            ProfileScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(route = Screen.Settings.route) {
                            SettingsScreen(navController = navController, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}