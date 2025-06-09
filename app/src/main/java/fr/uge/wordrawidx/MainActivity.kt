package fr.uge.wordrawidx

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import fr.uge.wordrawidx.controller.NavigationController
import fr.uge.wordrawidx.navigation.Screen
import fr.uge.wordrawidx.view.screens.HomeScreen
import fr.uge.wordrawidx.view.screens.VictoryScreen
// import fr.uge.wordrawidx.view.screens.AccelerometerMazeScreen // ❌ DÉSACTIVÉ
import fr.uge.wordrawidx.ui.theme.WordrawidTheme
import fr.uge.wordrawidx.utils.MiniGameResultHolder
import fr.uge.wordrawidx.view.screens.GameScreen
import fr.uge.wordrawidx.view.screens.ShakeGameScreen

/**
 * MainActivity avec mini-jeu labyrinthe DÉSACTIVÉ
 * Seul le ShakeGame est disponible pour les tests
 */
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "Application WordrawidZ démarrée - Labyrinthe DÉSACTIVÉ")

        setContent {
            WordrawidTheme {
                val navigationController = rememberSaveable(saver = NavigationController.Saver) {
                    NavigationController()
                }
                AppNavigation(navController = navigationController)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AppNavigation(navController: NavigationController) {
    Log.d("AppNavigation", "Current Screen: ${navController.currentScreen}")

    when (navController.currentScreen) {
        Screen.Home -> HomeScreen(
            onPlayClicked = {
                Log.d("AppNavigation", "Play clicked from Home. Navigating to Game.")
                navController.navigateTo(Screen.Game)
            },
            onSettingsClicked = {
                Log.d("AppNavigation", "Settings clicked - TODO")
            }
        )

        Screen.Game -> GameScreen(
            navigationController = navController,
            onNavigateToVictory = {
                Log.d("AppNavigation", "Game Won. Navigating to Victory.")
                navController.navigateTo(Screen.Victory)
            }
        )

        Screen.Victory -> VictoryScreen(
            onPlayAgain = {
                Log.d("AppNavigation", "Play Again from Victory.")
                MiniGameResultHolder.newGameRequestedFromVictoryOrHome = true
                navController.navigateToHome()
            }
        )

        // ❌ DÉSACTIVÉ : AccelerometerMaze
        Screen.AccelerometerMaze -> {
            Log.w("AppNavigation", "AccelerometerMaze DÉSACTIVÉ - Redirection vers ShakeGame")
            // Redirection automatique vers ShakeGame
            MiniGameResultHolder.lastResultWasWin = false // Éviter le blocage
            navController.navigateTo(Screen.Game)
        }

        Screen.ShakeGame -> ShakeGameScreen(
            navigationController = navController,
            onGameFinished = { won ->
                Log.d("AppNavigation", "ShakeGame finished. Won: $won")
                MiniGameResultHolder.lastResultWasWin = won
                navController.navigateTo(Screen.Game)
            }
        )
    }
}