package fr.uge.wordrawidx

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable // IMPORT POUR rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration // IMPORT POUR LocalConfiguration
import fr.uge.wordrawidx.controller.NavigationController
import fr.uge.wordrawidx.navigation.Screen
import fr.uge.wordrawidx.view.screens.GameScreen
import fr.uge.wordrawidx.view.screens.HomeScreen
import fr.uge.wordrawidx.view.screens.VictoryScreen
import fr.uge.wordrawidx.ui.theme.WordrawidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordrawidTheme {
                // Utiliser rememberSaveable pour que l'écran actuel persiste lors des rotations
                val navigationController = rememberSaveable(saver = NavigationController.Saver) {
                    NavigationController()
                }
                AppNavigation(navController = navigationController)
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavigationController) {
    // Vous pourriez aussi passer la configuration d'ici si plusieurs écrans en ont besoin
    // val configuration = LocalConfiguration.current
    // val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    when (navController.currentScreen) {
        Screen.Home -> HomeScreen(
            onPlayClicked = { navController.navigateToGame() },
            onSettingsClicked = { /* TODO */ }
            // modifier = Modifier, // Passez un modifier si HomeScreen l'accepte
        )
        Screen.Game -> GameScreen(
            onNavigateToVictory = { navController.navigateToVictory() }
            // modifier = Modifier, // Passez un modifier si GameScreen l'accepte
        )
        Screen.Victory -> VictoryScreen(
            onPlayAgain = { navController.navigateToHome() }
            // modifier = Modifier, // Passez un modifier si VictoryScreen l'accepte
        )
    }
}