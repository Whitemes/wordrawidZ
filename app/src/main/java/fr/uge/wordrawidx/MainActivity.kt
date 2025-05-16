package fr.uge.wordrawidx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import fr.uge.wordrawidx.controller.NavigationController
import fr.uge.wordrawidx.navigation.Screen
import fr.uge.wordrawidx.view.screens.GameScreen
import fr.uge.wordrawidx.view.screens.HomeScreen
import fr.uge.wordrawidx.view.screens.VictoryScreen // Assurez-vous que ce fichier existe et est correctement importé
import fr.uge.wordrawidx.ui.theme.WordrawidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordrawidTheme {
                val navigationController = remember { NavigationController() }
                AppNavigation(navController = navigationController)
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavigationController) {
    when (navController.currentScreen) {
        Screen.Home -> HomeScreen(
            onPlayClicked = { navController.navigateToGame() },
            onSettingsClicked = { /* TODO: Implémenter la navigation vers les paramètres si besoin */ }
        )
        Screen.Game -> GameScreen(
            // Le nom du paramètre correspond à celui défini dans GameScreen
            onNavigateToVictory = { navController.navigateToVictory() }
        )
        Screen.Victory -> VictoryScreen(
            onPlayAgain = { navController.navigateToHome() }
        )
    }
}