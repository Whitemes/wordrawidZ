package fr.uge.wordrawidx.model


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Represents the state of the game
 * @param boardSize The size of the board (n x n)
 */
class GameState(private val boardSize: Int = 5) {
    // Current position of the player (0-based index)
    var playerPosition by mutableStateOf(0)

    // Last dice roll value
    var lastDiceRoll by mutableStateOf(0)

    // Animation state for dice roll
    var isDiceRolling by mutableStateOf(false)
        private set

    // Animation state for player movement
    var isPlayerMoving by mutableStateOf(false)
        private set

    // Total number of cells on the board
    val totalCells = boardSize * boardSize

    /**
     * Roll the dice and move the player
     */
    fun rollDiceAndMove() {
        if (isDiceRolling || isPlayerMoving) return

        // Start dice rolling animation
        isDiceRolling = true

        // Simulate dice roll delay and then move player
        // In a real app, you'd use coroutines here
        android.os.Handler().postDelayed({
            // Roll the dice (1-6)
            val diceValue = (1..6).random()
            lastDiceRoll = diceValue
            isDiceRolling = false

            // Start player movement
            isPlayerMoving = true

            // Calculate new position
            val newPosition = (playerPosition + diceValue).coerceAtMost(totalCells - 1)

            // Simulate gradual movement
            movePlayerGradually(newPosition)
        }, 800) // Dice roll animation duration
    }

    /**
     * Simulate gradual movement of the player token
     */
    private fun movePlayerGradually(targetPosition: Int) {
        if (playerPosition >= targetPosition) {
            isPlayerMoving = false
            return
        }

        // Move one step at a time with delay
        android.os.Handler().postDelayed({
            playerPosition++
            movePlayerGradually(targetPosition)
        }, 300) // Movement speed per cell
    }

    /**
     * Reset the game
     */
    fun resetGame() {
        playerPosition = 0
        lastDiceRoll = 0
    }
}