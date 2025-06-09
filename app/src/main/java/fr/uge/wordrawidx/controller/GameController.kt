//package fr.uge.wordrawidx.controller
//
//import fr.uge.wordrawidx.model.GameState
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//class GameController(
//    private val gameState: GameState,
//    private val coroutineScope: CoroutineScope
//) {
//    fun rollDiceAndMove(onWin: () -> Unit) {
//        if (gameState.isDiceRolling || gameState.isPlayerMoving) return
//
//        gameState.isDiceRolling = true
//
//        coroutineScope.launch {
//            delay(800) // Durée de l'animation du dé
//
//            val diceValue = (1..6).random()
//            gameState.updateDiceValue(diceValue)
//            gameState.isDiceRolling = false
//
//            gameState.isPlayerMoving = true
//            val newPosition = (gameState.playerPosition + diceValue).coerceAtMost(gameState.totalCells - 1)
//            movePlayerGradually(newPosition, onWin)
//        }
//    }
//
//    private suspend fun movePlayerGradually(targetPosition: Int, onWin: () -> Unit) {
//        var currentPos = gameState.playerPosition
//        while (currentPos < targetPosition) {
//            currentPos++
//            gameState.updatePlayerPositionValue(currentPos)
//            delay(300) // Vitesse de déplacement par case
//        }
//        gameState.isPlayerMoving = false
//
//        if (gameState.playerPosition == gameState.totalCells - 1) {
//            onWin()
//        }
//    }
//
//    // Cette méthode pourrait être appelée si un reset explicite est nécessaire
//    // en dehors du cycle de vie de remember { GameState() } dans GameScreen.
//    // Pour l'instant, la réinitialisation principale se fait par la recréation de
//    // GameState dans GameScreen lorsque l'on y navigue.
//    fun resetGameForExplicitAction() {
//        gameState.resetStateValues()
//    }
//}

//package fr.uge.wordrawidx.controller
//
//import android.util.Log
//import fr.uge.wordrawidx.model.GameState
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//class GameController(
//    private val gameState: GameState,
//    private val coroutineScope: CoroutineScope
//) {
//    fun rollDiceAndMove(
//        onChallengeRequired: (landedPosition: Int) -> Unit,
//        onGameWin: () -> Unit
//    ) {
//        if (gameState.isDiceRolling || gameState.isPlayerMoving) return
//        gameState.isDiceRolling = true
//
//        coroutineScope.launch {
//            delay(800)
//            val diceValue = (1..6).random()
//            gameState.updateDiceValue(diceValue)
//            Log.d("GameController_Main", "Dice rolled: $diceValue. Current pos: ${gameState.playerPosition}")
//            gameState.isDiceRolling = false
//            gameState.isPlayerMoving = true
//            val newPosition = (gameState.playerPosition + diceValue).coerceAtMost(gameState.totalCells - 1)
//            movePlayerGradually(newPosition, onChallengeRequired, onGameWin)
//        }
//    }
//
//    private suspend fun movePlayerGradually(
//        targetPosition: Int,
//        onChallengeRequired: (landedPosition: Int) -> Unit,
//        onGameWin: () -> Unit
//    ) {
//        var currentPos = gameState.playerPosition
//        Log.d("GameController_Main", "Moving from $currentPos to $targetPosition")
//        while (currentPos < targetPosition) {
//            currentPos++
//            gameState.updatePlayerPositionValue(currentPos) // Met à jour la position dans GameState
//            Log.d("GameController_Main", "Player now at $currentPos")
//            delay(300)
//        }
//        gameState.isPlayerMoving = false
//        Log.d("GameController_Main", "Player stopped. Final position in GameState: ${gameState.playerPosition}") // Doit être targetPosition
//
//        if (gameState.playerPosition == gameState.totalCells - 1) {
//            Log.d("GameController_Main", "Player reached the final cell (${gameState.playerPosition}). Calling onGameWin.")
//            onGameWin()
//        } else {
//            if (!gameState.revealedCells[gameState.playerPosition]) {
//                Log.d("GameController_Main", "Cell ${gameState.playerPosition} not revealed. Calling onChallengeRequired.")
//                onChallengeRequired(gameState.playerPosition) // Passe la position actuelle où le pion s'est arrêté
//            } else {
//                Log.d("GameController_Main", "Cell ${gameState.playerPosition} already revealed. No challenge.")
//                // TODO: Logique pour le tour suivant
//            }
//        }
//    }
//
//    fun processMiniGameResult(won: Boolean, challengedCellPosition: Int) {
//        Log.d("GameController_Main", "Processing mini-game result for cell $challengedCellPosition. Won: $won. Current player pos in GameState: ${gameState.playerPosition}")
//        // Normalement, gameState.playerPosition DEVRAIT être égal à challengedCellPosition si GameState est bien préservé.
//        if (won) {
//            if (challengedCellPosition == gameState.playerPosition) {
//                gameState.revealCell(challengedCellPosition)
//            } else {
//                // C'est un état inattendu si le GameState a été mal restauré ou modifié.
//                Log.e("GameController_Main", "Position mismatch! Challenged: $challengedCellPosition, Current Player Pos in GameState: ${gameState.playerPosition}. Will reveal challenged cell.")
//                gameState.revealCell(challengedCellPosition) // On révèle quand même la case du défi
//            }
//        } else {
//            Log.d("GameController_Main", "Mini-game lost for cell $challengedCellPosition.")
//        }
//        // Le pion reste sur `challengedCellPosition` (qui est `gameState.playerPosition`)
//        // TODO: Logique pour passer au tour suivant ou permettre de deviner le mot mystère
//    }
//
//    // Appelée par GameScreen quand une NOUVELLE partie doit commencer.
//    fun startNewGame() {
//        Log.i("GameController_Main", "startNewGame called. Resetting GameState.")
//        gameState.resetStateForNewGame() // Utilise la méthode renommée
//    }
//}


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

        // Proposer un défi seulement si la case n'est pas révélée
        if (!gameState.isCellRevealed(gameState.playerPosition)) {
            onChallengeRequired(gameState.playerPosition)
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
