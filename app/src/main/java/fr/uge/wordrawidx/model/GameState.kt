package fr.uge.wordrawidx.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
}