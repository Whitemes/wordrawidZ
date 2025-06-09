package fr.uge.wordrawidx.controller

import android.util.Log
import fr.uge.wordrawidx.model.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * OPTIONNEL : GameController simplifié car la logique métier
 * est maintenant dans GameViewModel.
 *
 * Ce controller peut être utilisé pour des opérations
 * qui ne nécessitent pas de persistance d'état.
 */
class GameController(
    private val gameState: GameState,
    private val coroutineScope: CoroutineScope
) {

    /**
     * Lance le dé et déplace le pion (version simplifiée)
     * ⚠️ DEPRECATED : Utilisez GameViewModel.rollDiceAndMove() à la place
     */
    @Deprecated("Utilisez GameViewModel.rollDiceAndMove() pour une gestion d'état persistante")
    fun rollDiceAndMove(
        onChallengeRequired: (landedPosition: Int) -> Unit,
        onGameWin: () -> Unit
    ) {
        if (gameState.isDiceRolling || gameState.isPlayerMoving) return
        gameState.isDiceRolling = true

        coroutineScope.launch {
            delay(800)
            val diceValue = (1..6).random()
            gameState.updateDiceValue(diceValue)
            Log.d("GameController_Legacy", "Dice rolled: $diceValue. Current pos: ${gameState.playerPosition}")
            gameState.isDiceRolling = false
            gameState.isPlayerMoving = true

            val newPosition = (gameState.playerPosition + diceValue) % gameState.totalCells
            movePlayerGradually(newPosition, onChallengeRequired, onGameWin)
        }
    }

    private suspend fun movePlayerGradually(
        targetPosition: Int,
        onChallengeRequired: (landedPosition: Int) -> Unit,
        onGameWin: () -> Unit
    ) {
        var currentPos = gameState.playerPosition
        val total = gameState.totalCells

        while (currentPos != targetPosition) {
            currentPos = (currentPos + 1) % total
            gameState.updatePlayerPositionValue(currentPos)
            delay(300)
        }
        gameState.isPlayerMoving = false

        if (!gameState.isCellRevealed(gameState.playerPosition)) {
            Log.d("GameController_Legacy", "Case ${gameState.playerPosition} non révélée → Défi requis")
            onChallengeRequired(gameState.playerPosition)
        } else {
            Log.d("GameController_Legacy", "Case ${gameState.playerPosition} déjà révélée → Tour suivant")
        }
    }

    /**
     * Révèle toutes les cases (debug uniquement)
     */
    fun revealAllCellsForDebug() {
        for (i in 0 until gameState.totalCells) {
            gameState.revealCell(i)
        }
    }

    /**
     * Test de devinette (version simplifiée)
     * ⚠️ DEPRECATED : Utilisez GameViewModel.tryToGuessWord() à la place
     */
    @Deprecated("Utilisez GameViewModel.tryToGuessWord() pour une gestion d'état persistante")
    fun tryToGuessWord(proposed: String): Boolean = gameState.tryGuessMysteryWord(proposed)

    /**
     * Nouvelle partie (version simplifiée)
     * ⚠️ DEPRECATED : Utilisez GameViewModel.startNewGame() à la place
     */
    @Deprecated("Utilisez GameViewModel.startNewGame() pour une gestion d'état persistante")
    fun startNewGame() {
        gameState.resetStateForNewGame()
    }
}