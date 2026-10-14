package com.example.juegoreaccion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.juegoreaccion.ui.*
import com.example.juegoreaccion.ui.theme.JuegoReaccionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JuegoReaccionTheme {
                val navController = rememberNavController()
                val gameViewModel: GameViewModel = viewModel()
                
                NavHost(navController = navController, startDestination = "menu") {
                    composable("menu") {
                        val settings by gameViewModel.settings
                        MainMenuScreen(
                            settings = settings,
                            onStartGame = {
                                navController.navigate("instructions")
                            },
                            onViewStats = {
                                navController.navigate("stats")
                            },
                            onSettingsChange = { gameViewModel.updateSettings(it) }
                        )
                    }
                    composable("instructions") {
                        val settings by gameViewModel.settings
                        InstructionsScreen(
                            isReverseMode = settings.isReverseMode,
                            onStart = {
                                gameViewModel.startGame()
                                navController.navigate("game") {
                                    popUpTo("menu") { inclusive = false }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("game") {
                        GameScreen(
                            viewModel = gameViewModel,
                            onExit = { navController.popBackStack("menu", inclusive = false) }
                        )
                    }
                    composable("stats") {
                        val results by gameViewModel.getTopResults().collectAsState(initial = emptyList())
                        StatsScreen(
                            results = results,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
