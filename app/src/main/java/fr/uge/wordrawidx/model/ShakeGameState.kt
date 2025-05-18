package fr.uge.wordrawidx.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ShakeGameState(
    var targetShakes: Int = 30,    // Par défaut, nombre de secousses à faire
    duration: Int = 45             // Temps max en secondes
) {
    var shakeCount by mutableStateOf(0)
    var timeLeft by mutableStateOf(duration)
    var gameState by mutableStateOf(GameState.PLAYING)

    enum class GameState { PLAYING, WON, LOST }

    fun reset(newTarget: Int = 30, duration: Int = 45) {
        shakeCount = 0
        targetShakes = newTarget
        timeLeft = duration
        gameState = GameState.PLAYING
    }
}
