package fr.uge.wordrawidx.controller

import android.util.Log
import fr.uge.wordrawidx.model.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameController(
    private val gameState: GameState,
    private val coroutineScope: CoroutineScope
) {
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
            Log.d("GameController_Main", "Dice rolled: $diceValue. Current pos: ${gameState.playerPosition}")
            gameState.isDiceRolling = false
            gameState.isPlayerMoving = true

            // --- Wrap-around logique (modulo pour boucler le plateau)
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

        // ✅ LOGIQUE CORRIGÉE : défi seulement si la case n'est PAS révélée
        if (!gameState.isCellRevealed(gameState.playerPosition)) {
            Log.d("GameController", "Case ${gameState.playerPosition} non révélée -> Défi requis")
            onChallengeRequired(gameState.playerPosition)
        } else {
            Log.d("GameController", "Case ${gameState.playerPosition} déjà révélée -> Aucun défi, tour suivant")
            // La case est déjà révélée : rien ne se passe, le joueur peut relancer le dé
        }
    }


    // Appelé après succès au mini-jeu pour révéler la case
//    fun revealHintForCell(cellIndex: Int) {
//        gameState.revealCell(cellIndex)
//    }

    //CI DESSOUS DEBUG A SUPPRIMER
    fun revealAllCellsForDebug() {
        for (i in 0 until gameState.totalCells) {
            gameState.revealCell(i)
        }
    }



    // Deviner le mot mystère
    fun tryToGuessWord(proposed: String): Boolean = gameState.tryGuessMysteryWord(proposed)

    fun startNewGame() {
        gameState.resetStateForNewGame() // Correction : sans paramètre
    }
}
