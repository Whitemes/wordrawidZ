package fr.uge.wordrawidx.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.uge.wordrawidx.navigation.Screen

class NavigationController(initialScreen: Screen = Screen.Home) {
    var currentScreen by mutableStateOf(initialScreen)
        private set

    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }

    fun navigateToGame() {
        navigateTo(Screen.Game)
    }

    fun navigateToVictory() {
        navigateTo(Screen.Victory)
    }

    fun navigateToHome() {
        navigateTo(Screen.Home)
        // La réinitialisation du GameState se fera naturellement lorsque GameScreen
        // sera recomposé avec un nouveau 'remember { GameState() }'.
        // Si un GameController de plus longue durée existait (ex: ViewModel),
        // on appellerait ici gameController.resetGame().
    }
}