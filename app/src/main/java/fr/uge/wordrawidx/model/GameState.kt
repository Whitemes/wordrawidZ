package fr.uge.wordrawidx.model

import android.os.Bundle // IMPORT POUR Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver // IMPORT POUR Saver
import androidx.compose.runtime.setValue

class GameState(val boardSize: Int = 5) {
    var playerPosition by mutableStateOf(0)
        internal set
    var lastDiceRoll by mutableStateOf(0)
        internal set
    var isDiceRolling by mutableStateOf(false)
        internal set
    var isPlayerMoving by mutableStateOf(false)
        internal set
    val totalCells = boardSize * boardSize

    internal fun updateDiceValue(value: Int) {
        lastDiceRoll = value
    }

    internal fun updatePlayerPositionValue(newPosition: Int) {
        playerPosition = newPosition
    }

    internal fun resetStateValues() {
        playerPosition = 0
        lastDiceRoll = 0
        isDiceRolling = false
        isPlayerMoving = false
    }

    // Méthodes pour la restauration (utilisées par le Saver)
    // Ces méthodes permettent de modifier l'état même avec les setters internal,
    // car elles sont appelées depuis l'intérieur de la classe (via le companion object).
    private fun restorePlayerPosition(position: Int) {
        playerPosition = position
    }
    private fun restoreLastDiceRoll(roll: Int) {
        lastDiceRoll = roll
    }


    companion object {
        // Clés pour le Bundle du Saver
        private const val KEY_BOARD_SIZE = "boardSize"
        private const val KEY_PLAYER_POSITION = "playerPosition"
        private const val KEY_LAST_DICE_ROLL = "lastDiceRoll"
        // isDiceRolling et isPlayerMoving sont des états transitoires d'animation,
        // il n'est généralement pas nécessaire de les sauvegarder/restaurer.
        // S'ils l'étaient, ils seraient réinitialisés à false.

        val Saver: Saver<GameState, Bundle> = Saver(
            save = { gameState ->
                Bundle().apply {
                    putInt(KEY_BOARD_SIZE, gameState.boardSize)
                    putInt(KEY_PLAYER_POSITION, gameState.playerPosition)
                    putInt(KEY_LAST_DICE_ROLL, gameState.lastDiceRoll)
                }
            },
            restore = { bundle ->
                val boardSize = bundle.getInt(KEY_BOARD_SIZE)
                GameState(boardSize).apply {
                    // Utiliser les méthodes de restauration ou rendre les setters temporairement accessibles
                    // Pour garder les setters `internal`, nous devons appeler des méthodes internes.
                    // Le plus simple est d'avoir des méthodes `internal` pour la restauration
                    // ou d'accéder directement aux backing fields si possible (non recommandé avec by mutableStateOf).
                    // Ici, on va supposer qu'on peut les initialiser puis les modifier via des méthodes.
                    this.restorePlayerPosition(bundle.getInt(KEY_PLAYER_POSITION))
                    this.restoreLastDiceRoll(bundle.getInt(KEY_LAST_DICE_ROLL))
                    // isDiceRolling et isPlayerMoving seront false par défaut, ce qui est correct.
                }
            }
        )
    }
}