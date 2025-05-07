package fr.uge.wordrawidx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import fr.uge.wordrawidx.ui.screens.HomeScreen
import fr.uge.wordrawidx.ui.screens.GameScreen
import fr.uge.wordrawidx.ui.screens.VictoryScreen
import fr.uge.wordrawidx.ui.theme.WordrawidTheme

/**
 * Navigation simple entre Home, Game et Victory sans XML ni NavController.
 */
enum class Screen {
    Home,
    Game,
    Victory
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordrawidTheme {
                // État de navigation
                var currentScreen by remember { mutableStateOf(Screen.Home) }

                // Affichage selon l'écran courant
                when (currentScreen) {
                    Screen.Home -> HomeScreen(
                        onPlayClicked = { currentScreen = Screen.Game },
                        onSettingsClicked = { /* TODO: ouvrir Settings */ }
                    )

                    Screen.Game -> GameScreen(
                        onWin = { currentScreen = Screen.Victory }
                    )

                    Screen.Victory -> VictoryScreen(
                        onPlayAgain = { currentScreen = Screen.Home }
                    )
                }
            }
        }
    }
}
