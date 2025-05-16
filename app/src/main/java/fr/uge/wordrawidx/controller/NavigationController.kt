package fr.uge.wordrawidx.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver // IMPORT POUR Saver
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
    }

    // Compagnon object pour le Saver
    companion object {
        val Saver: Saver<NavigationController, String> = Saver(
            save = { it.currentScreen.name }, // Sauvegarde le nom de l'enum
            restore = { NavigationController(Screen.valueOf(it)) } // Restaure en retrouvant l'enum par son nom
        )
    }
}