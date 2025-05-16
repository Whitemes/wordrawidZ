package fr.uge.wordrawidx.controller

import fr.uge.wordrawidx.model.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameController(
    private val gameState: GameState,
    private val coroutineScope: CoroutineScope
) {
    fun rollDiceAndMove(onWin: () -> Unit) {
        if (gameState.isDiceRolling || gameState.isPlayerMoving) return

        gameState.isDiceRolling = true

        coroutineScope.launch {
            delay(800) // Durée de l'animation du dé

            val diceValue = (1..6).random()
            gameState.updateDiceValue(diceValue)
            gameState.isDiceRolling = false

            gameState.isPlayerMoving = true
            val newPosition = (gameState.playerPosition + diceValue).coerceAtMost(gameState.totalCells - 1)
            movePlayerGradually(newPosition, onWin)
        }
    }

    private suspend fun movePlayerGradually(targetPosition: Int, onWin: () -> Unit) {
        var currentPos = gameState.playerPosition
        while (currentPos < targetPosition) {
            currentPos++
            gameState.updatePlayerPositionValue(currentPos)
            delay(300) // Vitesse de déplacement par case
        }
        gameState.isPlayerMoving = false

        if (gameState.playerPosition == gameState.totalCells - 1) {
            onWin()
        }
    }

    // Cette méthode pourrait être appelée si un reset explicite est nécessaire
    // en dehors du cycle de vie de remember { GameState() } dans GameScreen.
    // Pour l'instant, la réinitialisation principale se fait par la recréation de
    // GameState dans GameScreen lorsque l'on y navigue.
    fun resetGameForExplicitAction() {
        gameState.resetStateValues()
    }
}