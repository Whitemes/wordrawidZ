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
import fr.uge.wordrawidx.view.screens.AccelerometerMazeScreen
import fr.uge.wordrawidx.ui.theme.WordrawidTheme
import fr.uge.wordrawidx.utils.MiniGameResultHolder
import fr.uge.wordrawidx.view.screens.GameScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    Log.d("AppNavigation", "Current Screen: ${navController.currentScreen}. NewGameFlag: ${MiniGameResultHolder.newGameRequestedFromVictoryOrHome}")
    when (navController.currentScreen) {
        Screen.Home -> HomeScreen(
            onPlayClicked = {
                // Si le joueur clique sur "Jouer" depuis l'accueil,
                // on veut s'assurer que si une partie précédente s'est terminée par une victoire,
                // une NOUVELLE partie commence.
                // Le drapeau newGameRequestedFromVictoryOrHome est déjà mis par VictoryScreen.
                // GameScreen s'en occupera. Si on veut forcer un reset à chaque "Jouer" depuis Home :
                // MiniGameResultHolder.newGameRequestedFromVictoryOrHome = true; // Forcerait un reset
                Log.d("AppNavigation", "Play clicked from Home. Navigating to Game.")
                navController.navigateTo(Screen.Game)
            },
            onSettingsClicked = { /* TODO */ }
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
                Log.d("AppNavigation", "Play Again from Victory. Setting newGameRequested flag and navigating to Home.")
                MiniGameResultHolder.newGameRequestedFromVictoryOrHome = true // Important
                navController.navigateToHome()
            }
        )
        Screen.AccelerometerMaze -> AccelerometerMazeScreen(
            navigationController = navController,
            onGameFinished = { wasMiniGameWon ->
                Log.d("AppNavigation", "Mini-jeu AccelerometerMaze terminé. Gagné: $wasMiniGameWon. Storing result.")
                MiniGameResultHolder.lastResultWasWin = wasMiniGameWon
                // lastChallengedCell a été mis par GameScreen avant de naviguer vers le mini-jeu
                navController.navigateTo(Screen.Game) // Retourner à GameScreen
            }
        )
    }
}