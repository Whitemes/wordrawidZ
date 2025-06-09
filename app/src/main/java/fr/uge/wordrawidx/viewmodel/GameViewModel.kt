package fr.uge.wordrawidx.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.uge.wordrawidx.model.GameState
import fr.uge.wordrawidx.model.MysteryBank
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_PLAYER_POSITION = "player_position"
        private const val KEY_LAST_DICE_ROLL = "last_dice_roll"
        private const val KEY_IS_GAME_WON = "is_game_won"
        private const val KEY_MYSTERY_WORD = "mystery_word"
        private const val KEY_REVEALED_CELLS_COUNT = "revealed_cells_count"
        private const val KEY_REVEALED_CELLS_DATA = "revealed_cells_data"
        private const val KEY_PLAYER_POSITION_BEFORE_MINI_GAME = "player_pos_before_mini"
        private const val KEY_LAST_CHALLENGED_CELL = "last_challenged_cell"
        private const val KEY_PENDING_MINI_GAME_RESULT = "pending_mini_result"
    }

    // ✅ État principal - Création unique avec persistance manuelle
    private val _gameState: GameState

    val gameState: GameState get() = _gameState

    // ✅ Position sauvegardée AVANT mini-jeu (thread-safe)
    var playerPositionBeforeMiniGame: Int?
        get() = savedStateHandle.get<Int>(KEY_PLAYER_POSITION_BEFORE_MINI_GAME)
        set(value) {
            savedStateHandle[KEY_PLAYER_POSITION_BEFORE_MINI_GAME] = value
            Log.d("GameViewModel", "Position avant mini-jeu sauvegardée: $value")
        }

    // ✅ Cellule challengée (persistante)
    var lastChallengedCell: Int?
        get() = savedStateHandle.get<Int>(KEY_LAST_CHALLENGED_CELL)
        set(value) {
            savedStateHandle[KEY_LAST_CHALLENGED_CELL] = value
            Log.d("GameViewModel", "Cellule challengée sauvegardée: $value")
        }

    // ✅ Résultat du mini-jeu en attente
    var pendingMiniGameResult: Boolean?
        get() = savedStateHandle.get<Boolean>(KEY_PENDING_MINI_GAME_RESULT)
        set(value) {
            savedStateHandle[KEY_PENDING_MINI_GAME_RESULT] = value
            Log.d("GameViewModel", "Résultat mini-jeu en attente: $value")
        }

    init {
        // ✅ Initialisation du GameState avec restauration manuelle
        _gameState = restoreGameStateFromSavedState() ?: createNewGameState()
        Log.d("GameViewModel", "ViewModel initialisé - Mot: '${gameState.mysteryObject?.word}', Position: ${gameState.playerPosition}")
    }

    // ✅ MÉTHODES PUBLIQUES

    /**
     * Lance le dé et déplace le pion
     */
    fun rollDiceAndMove(
        onChallengeRequired: (landedPosition: Int) -> Unit,
        onGameWin: () -> Unit
    ) {
        if (gameState.isDiceRolling || gameState.isPlayerMoving) {
            Log.w("GameViewModel", "Lancer ignoré - Mouvement en cours")
            return
        }

        gameState.isDiceRolling = true
        saveGameState()

        viewModelScope.launch {
            delay(800)
            val diceValue = (1..6).random()
            gameState.updateDiceValue(diceValue)
            Log.d("GameViewModel", "Dé lancé: $diceValue. Position: ${gameState.playerPosition}")

            gameState.isDiceRolling = false
            gameState.isPlayerMoving = true
            saveGameState()

            // Mouvement avec wrap-around
            val newPosition = (gameState.playerPosition + diceValue) % gameState.totalCells
            movePlayerGradually(newPosition, onChallengeRequired, onGameWin)
        }
    }

    /**
     * Prépare un mini-jeu pour une cellule
     */
    fun prepareMiniGameChallenge(cellIndex: Int): Boolean {
        if (gameState.isCellRevealed(cellIndex)) {
            Log.d("GameViewModel", "Cellule $cellIndex déjà révélée - Pas de challenge")
            return false
        }

        // ✅ CRITIQUE : Sauvegarder la position ACTUELLE avant le mini-jeu
        playerPositionBeforeMiniGame = gameState.playerPosition
        lastChallengedCell = cellIndex
        pendingMiniGameResult = null

        Log.i("GameViewModel", "Mini-jeu préparé - Position sauvée: ${gameState.playerPosition}, Cellule: $cellIndex")
        return true
    }

    /**
     * Traite le résultat d'un mini-jeu
     */
    fun processMiniGameResult(won: Boolean) {
        val challengedCell = lastChallengedCell
        val savedPosition = playerPositionBeforeMiniGame

        if (challengedCell == null) {
            Log.w("GameViewModel", "Aucune cellule challengée trouvée")
            return
        }

        Log.i("GameViewModel", "Traitement résultat mini-jeu - Cellule: $challengedCell, Gagné: $won, Position à restaurer: $savedPosition")

        // ✅ RESTAURER LA POSITION (critique pour éviter la téléportation)
        if (savedPosition != null && savedPosition != gameState.playerPosition) {
            Log.i("GameViewModel", "Restauration position: ${gameState.playerPosition} → $savedPosition")
            gameState.updatePlayerPositionValue(savedPosition)
        }

        // ✅ RÉVÉLER LA CELLULE si victoire
        if (won) {
            gameState.revealCell(challengedCell)
            Log.i("GameViewModel", "Cellule $challengedCell révélée suite à victoire")
        } else {
            Log.d("GameViewModel", "Mini-jeu perdu - Cellule $challengedCell non révélée")
        }

        // ✅ NETTOYAGE
        lastChallengedCell = null
        playerPositionBeforeMiniGame = null
        pendingMiniGameResult = null

        saveGameState()
    }

    /**
     * Teste une proposition de mot mystère
     */
    fun tryToGuessWord(proposed: String): Boolean {
        val result = gameState.tryGuessMysteryWord(proposed)
        if (result) {
            Log.i("GameViewModel", "Mot mystère deviné correctement: '$proposed'")
        }
        saveGameState()
        return result
    }

    /**
     * Démarre une nouvelle partie
     */
    fun startNewGame() {
        Log.i("GameViewModel", "Démarrage nouvelle partie")
        gameState.resetStateForNewGame()

        // Nettoyage complet des états de mini-jeu
        playerPositionBeforeMiniGame = null
        lastChallengedCell = null
        pendingMiniGameResult = null

        saveGameState()
        Log.i("GameViewModel", "Nouvelle partie initialisée - Nouveau mot: '${gameState.mysteryObject?.word}'")
    }

    /**
     * Force la révélation de toutes les cellules (debug)
     */
    fun revealAllCellsForDebug() {
        for (i in 0 until gameState.totalCells) {
            gameState.revealCell(i)
        }
        saveGameState()
    }

    // ✅ MÉTHODES PRIVÉES

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
            saveGameState()
            delay(300)
        }

        gameState.isPlayerMoving = false
        saveGameState()

        // Vérifier si un défi est nécessaire
        if (!gameState.isCellRevealed(gameState.playerPosition)) {
            Log.d("GameViewModel", "Case ${gameState.playerPosition} non révélée → Défi requis")
            onChallengeRequired(gameState.playerPosition)
        } else {
            Log.d("GameViewModel", "Case ${gameState.playerPosition} déjà révélée → Tour suivant")
        }
    }

    private fun saveGameState() {
        // ✅ Sauvegarde manuelle des propriétés critiques dans SavedStateHandle
        try {
            savedStateHandle[KEY_PLAYER_POSITION] = gameState.playerPosition
            savedStateHandle[KEY_LAST_DICE_ROLL] = gameState.lastDiceRoll
            savedStateHandle[KEY_IS_GAME_WON] = gameState.isGameWon
            savedStateHandle[KEY_MYSTERY_WORD] = gameState.mysteryObject?.word ?: ""
            savedStateHandle[KEY_REVEALED_CELLS_COUNT] = gameState.revealedCells.size

            // Sauvegarder les cellules révélées (format simple)
            val revealedCellsData = gameState.revealedCells.map { cell ->
                "${cell.cellIndex},${cell.hintType.name},${cell.hintContent}"
            }
            savedStateHandle[KEY_REVEALED_CELLS_DATA] = revealedCellsData.toTypedArray()

            Log.d("GameViewModel", "État sauvegardé - Position: ${gameState.playerPosition}, Mot: '${gameState.mysteryObject?.word}'")
        } catch (e: Exception) {
            Log.e("GameViewModel", "Erreur sauvegarde état: ${e.message}")
        }
    }

    private fun restoreGameStateFromSavedState(): GameState? {
        return try {
            val savedPosition = savedStateHandle.get<Int>(KEY_PLAYER_POSITION) ?: return null
            val savedWord = savedStateHandle.get<String>(KEY_MYSTERY_WORD) ?: return null

            if (savedWord.isBlank()) return null

            Log.i("GameViewModel", "Restauration état - Position: $savedPosition, Mot: '$savedWord'")

            // Créer GameState avec restauration
            val restoredState = GameState(boardSize = 5, restoreMode = true)

            // Restaurer les propriétés de base
            restoredState.updatePlayerPositionValue(savedPosition)
            restoredState.updateDiceValue(savedStateHandle.get<Int>(KEY_LAST_DICE_ROLL) ?: 0)

            // Restaurer le mot mystère (recherche dans MysteryBank)
            val mysteryObject = MysteryBank.objects.find { it.word == savedWord }
            if (mysteryObject != null) {
                // ⚠️ Accès réflexion pour restaurer mysteryObject (hack temporaire)
                val field = restoredState::class.java.getDeclaredField("mysteryObject")
                field.isAccessible = true
                field.set(restoredState, mysteryObject)

                // Restaurer les cellules révélées
                val revealedCellsData = savedStateHandle.get<Array<String>>(KEY_REVEALED_CELLS_DATA)
                revealedCellsData?.forEach { cellData ->
                    val parts = cellData.split(",")
                    if (parts.size >= 3) {
                        val cellIndex = parts[0].toIntOrNull()
                        if (cellIndex != null) {
                            restoredState.revealCell(cellIndex)
                        }
                    }
                }

                Log.i("GameViewModel", "État restauré avec succès - ${restoredState.revealedCells.size} cellules révélées")
                return restoredState
            }

            null
        } catch (e: Exception) {
            Log.w("GameViewModel", "Impossible de restaurer l'état: ${e.message}")
            null
        }
    }

    private fun createNewGameState(): GameState {
        Log.i("GameViewModel", "Création nouveau GameState")
        return GameState(boardSize = 5, restoreMode = false).also {
            Log.i("GameViewModel", "Nouveau GameState créé - Mot: '${it.mysteryObject?.word}'")
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("GameViewModel", "ViewModel détruit")
    }
}