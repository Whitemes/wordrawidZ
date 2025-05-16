package fr.uge.wordrawidx.model

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
// Rect n'est pas utilisé ici, mais dans MazeState pour le mini-jeu

const val BOARD_COLS_MAIN = 5
const val BOARD_ROWS_MAIN = 5

class GameState(val boardSize: Int = BOARD_COLS_MAIN) {
    var playerPosition by mutableStateOf(0)
        internal set
    var lastDiceRoll by mutableStateOf(0)
        internal set
    var isDiceRolling by mutableStateOf(false)
        internal set
    var isPlayerMoving by mutableStateOf(false)
        internal set
    val totalCells = boardSize * boardSize

    val revealedCells: SnapshotStateList<Boolean> = mutableStateListOf<Boolean>().also { list ->
        repeat(boardSize * boardSize) { list.add(false) }
    }

    init {
        Log.d("GameState_Main", "Instance GameState (main game) initialisée/créée. PlayerPos: $playerPosition")
    }

    internal fun updateDiceValue(value: Int) {
        lastDiceRoll = value
    }

    internal fun updatePlayerPositionValue(newPosition: Int) {
        Log.d("GameState_Main", "updatePlayerPositionValue: from $playerPosition to $newPosition")
        playerPosition = newPosition
    }

    internal fun revealCell(cellIndex: Int) {
        if (cellIndex >= 0 && cellIndex < revealedCells.size) {
            revealedCells[cellIndex] = true
            Log.d("GameState_Main", "Cell $cellIndex revealed.")
        }
    }

    // Cette méthode est pour une NOUVELLE PARTIE COMPLÈTE.
    internal fun resetStateForNewGame() {
        Log.d("GameState_Main", "resetStateForNewGame CALLED. Resetting all values.")
        playerPosition = 0
        lastDiceRoll = 0
        isDiceRolling = false
        isPlayerMoving = false
        revealedCells.indices.forEach { revealedCells[it] = false }
        Log.d("GameState_Main", "resetStateForNewGame FINISHED. PlayerPos: $playerPosition.")
    }

    companion object {
        private const val KEY_BOARD_SIZE = "boardSize"
        private const val KEY_PLAYER_POSITION = "playerPosition"
        private const val KEY_LAST_DICE_ROLL = "lastDiceRoll"
        private const val KEY_REVEALED_CELLS = "revealedCells"

        val Saver: Saver<GameState, Bundle> = Saver(
            save = { gameState ->
                Log.d("GameState_Main.Saver", "SAVING state. PlayerPos: ${gameState.playerPosition}")
                Bundle().apply {
                    putInt(KEY_BOARD_SIZE, gameState.boardSize)
                    putInt(KEY_PLAYER_POSITION, gameState.playerPosition)
                    putInt(KEY_LAST_DICE_ROLL, gameState.lastDiceRoll)
                    putBooleanArray(KEY_REVEALED_CELLS, gameState.revealedCells.toBooleanArray())
                }
            },
            restore = { bundle ->
                val boardSize = bundle.getInt(KEY_BOARD_SIZE)
                val restoredPlayerPosition = bundle.getInt(KEY_PLAYER_POSITION)
                Log.d("GameState_Main.Saver", "RESTORING state. PlayerPos from bundle: $restoredPlayerPosition")
                GameState(boardSize).apply {
                    this.playerPosition = restoredPlayerPosition
                    this.lastDiceRoll = bundle.getInt(KEY_LAST_DICE_ROLL)
                    bundle.getBooleanArray(KEY_REVEALED_CELLS)?.forEachIndexed { index, isRevealed ->
                        if (index < this.revealedCells.size) {
                            this.revealedCells[index] = isRevealed
                        }
                    }
                    Log.d("GameState_Main.Saver", "State RESTORED. Current PlayerPos: ${this.playerPosition}")
                }
            }
        )
    }
}