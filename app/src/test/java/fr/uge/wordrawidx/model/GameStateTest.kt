package fr.uge.wordrawidx.model

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameStateTest {

    private lateinit var gameState: GameState

    @Before
    fun setUp() {
        // boardSize = 3 pour tester totalCells = 9
        gameState = GameState(boardSize = 3)
    }

    @Test
    fun initialState_isCorrect() {
        // au démarrage
        assertEquals(0, gameState.playerPosition)
        assertEquals(0, gameState.lastDiceRoll)
        assertFalse(gameState.isDiceRolling)
        assertFalse(gameState.isPlayerMoving)
        assertEquals(9, gameState.totalCells)
    }

    @Test
    fun resetGame_clearsState() {
        // on modifie l'état
        gameState.apply {
            playerPosition = 5
            lastDiceRoll = 4
        }
        // on reset
        gameState.resetGame()

        assertEquals(0, gameState.playerPosition)
        assertEquals(0, gameState.lastDiceRoll)
    }

}
