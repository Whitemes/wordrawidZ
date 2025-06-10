// viewmodel/GameViewModel.kt (VERSION CORRIGÉE - TYPES COMPATIBLES)
package fr.uge.wordrawidx.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.uge.wordrawidx.data.repository.MysteryRepository
import fr.uge.wordrawidx.model.GameState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * GameViewModel moderne avec intégration Repository
 * Compatible avec GameState corrigé (utilise MysteryObject du Repository directement)
 */
class GameViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val mysteryRepository: MysteryRepository
) : ViewModel() {

    companion object {
        private const val KEY_PLAYER_POSITION_BEFORE_MINI_GAME = "player_pos_before_mini"
        private const val KEY_LAST_CHALLENGED_CELL = "last_challenged_cell"
        private const val KEY_PENDING_MINI_GAME_RESULT = "pending_mini_result"
        private const val KEY_CURRENT_MYSTERY_WORD = "current_mystery_word"
    }

    // ✅ État principal - Création moderne sans restoreMode
    private val _gameState = GameState(boardSize = 5).also {
        Log.i("GameViewModel", "GameState moderne créé")
    }

    val gameState: GameState get() = _gameState

    // ✅ Propriétés persistantes mini-jeux (inchangées)
    var playerPositionBeforeMiniGame: Int?
        get() = savedStateHandle.get<Int>(KEY_PLAYER_POSITION_BEFORE_MINI_GAME)
        set(value) {
            savedStateHandle[KEY_PLAYER_POSITION_BEFORE_MINI_GAME] = value
            Log.d("GameViewModel", "Position avant mini-jeu sauvegardée: $value")
        }

    var lastChallengedCell: Int?
        get() = savedStateHandle.get<Int>(KEY_LAST_CHALLENGED_CELL)
        set(value) {
            savedStateHandle[KEY_LAST_CHALLENGED_CELL] = value
            Log.d("GameViewModel", "Cellule challengée sauvegardée: $value")
        }

    var pendingMiniGameResult: Boolean?
        get() = savedStateHandle.get<Boolean>(KEY_PENDING_MINI_GAME_RESULT)
        set(value) {
            savedStateHandle[KEY_PENDING_MINI_GAME_RESULT] = value
            Log.d("GameViewModel", "Résultat mini-jeu en attente: $value")
        }

    init {
        Log.d("GameViewModel", "ViewModel initialisé avec Repository moderne")
        initializeGame()
    }

    // ✅ NOUVELLES MÉTHODES - Gestion Repository

    /**
     * Initialise une nouvelle partie avec objet aléatoire du repository
     */
    private fun initializeGame() {
        viewModelScope.launch {
            try {
                // Vérifier si une partie est en cours
                val savedWord = savedStateHandle.get<String>(KEY_CURRENT_MYSTERY_WORD)

                if (!savedWord.isNullOrBlank()) {
                    // Restaurer partie existante
                    Log.i("GameViewModel", "Restauration partie existante - Mot: '$savedWord'")
                    restoreGameFromSavedWord(savedWord)
                } else {
                    // Nouvelle partie
                    Log.i("GameViewModel", "Initialisation nouvelle partie")
                    startNewGameWithRepository()
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "Erreur initialisation: ${e.message}")
                // Fallback vers GameState moderne sans objets externes
                Log.w("GameViewModel", "GameState reste en attente de setMysteryObject()")
            }
        }
    }

    /**
     * Démarre une nouvelle partie avec un objet du repository
     */
    private suspend fun startNewGameWithRepository() {
        val repositoryMystery = mysteryRepository.getRandomMysteryObject()

        if (repositoryMystery != null) {
            // ✅ CORRECTION: Passer directement l'objet Repository à GameState
            // GameState se charge de la conversion interne vers LocalMysteryObject
            gameState.setMysteryObject(repositoryMystery)

            // Sauvegarder le mot actuel
            savedStateHandle[KEY_CURRENT_MYSTERY_WORD] = repositoryMystery.word

            Log.i("GameViewModel", "Nouvelle partie initialisée - Mot: '${repositoryMystery.word}', Source: repository, Indices: ${repositoryMystery.closeWords.size}")
        } else {
            Log.e("GameViewModel", "Repository n'a retourné aucun objet mystère")
        }
    }

    /**
     * Restaure une partie depuis un mot sauvegardé
     */
    private suspend fun restoreGameFromSavedWord(savedWord: String) {
        val repositoryMystery = mysteryRepository.findMysteryObjectByWord(savedWord)

        if (repositoryMystery != null) {
            // ✅ CORRECTION: Passer directement l'objet Repository à GameState
            gameState.setMysteryObject(repositoryMystery)
            Log.i("GameViewModel", "Partie restaurée - Mot: '${repositoryMystery.word}'")
        } else {
            Log.w("GameViewModel", "Mot sauvegardé '$savedWord' non trouvé - Nouvelle partie")
            startNewGameWithRepository()
        }
    }

    // ✅ MÉTHODES PUBLIQUES (inchangées)

    fun rollDiceAndMove(
        onChallengeRequired: (landedPosition: Int) -> Unit,
        onGameWin: () -> Unit
    ) {
        if (gameState.isDiceRolling || gameState.isPlayerMoving) {
            Log.w("GameViewModel", "Lancer ignoré - Mouvement en cours")
            return
        }

        gameState.isDiceRolling = true

        viewModelScope.launch {
            delay(800)
            val diceValue = (1..6).random()
            gameState.updateDiceValue(diceValue)
            Log.d("GameViewModel", "Dé lancé: $diceValue. Position: ${gameState.playerPosition}")

            gameState.isDiceRolling = false
            gameState.isPlayerMoving = true

            val newPosition = (gameState.playerPosition + diceValue) % gameState.totalCells
            movePlayerGradually(newPosition, onChallengeRequired, onGameWin)
        }
    }

    fun prepareMiniGameChallenge(cellIndex: Int): Boolean {
        if (gameState.isCellRevealed(cellIndex)) {
            Log.d("GameViewModel", "Cellule $cellIndex déjà révélée - Pas de challenge")
            return false
        }

        playerPositionBeforeMiniGame = gameState.playerPosition
        lastChallengedCell = cellIndex
        pendingMiniGameResult = null

        Log.i("GameViewModel", "Mini-jeu préparé - Position sauvée: ${gameState.playerPosition}, Cellule: $cellIndex")
        return true
    }

    fun processMiniGameResult(won: Boolean) {
        val challengedCell = lastChallengedCell
        val savedPosition = playerPositionBeforeMiniGame

        if (challengedCell == null) {
            Log.w("GameViewModel", "Aucune cellule challengée trouvée")
            return
        }

        Log.i("GameViewModel", "Traitement résultat mini-jeu - Cellule: $challengedCell, Gagné: $won, Position à restaurer: $savedPosition")

        if (savedPosition != null && savedPosition != gameState.playerPosition) {
            Log.i("GameViewModel", "Restauration position: ${gameState.playerPosition} → $savedPosition")
            gameState.updatePlayerPositionValue(savedPosition)
        }

        if (won) {
            gameState.revealCell(challengedCell)
            Log.i("GameViewModel", "Cellule $challengedCell révélée suite à victoire")
        } else {
            Log.d("GameViewModel", "Mini-jeu perdu - Cellule $challengedCell non révélée")
        }

        lastChallengedCell = null
        playerPositionBeforeMiniGame = null
        pendingMiniGameResult = null
    }

    fun tryToGuessWord(proposed: String): Boolean {
        val result = gameState.tryGuessMysteryWord(proposed)
        if (result) {
            Log.i("GameViewModel", "Mot mystère deviné correctement: '$proposed'")
        }
        return result
    }

    /**
     * Démarre une nouvelle partie (avec repository)
     */
    fun startNewGame() {
        Log.i("GameViewModel", "Démarrage nouvelle partie avec repository")

        // Nettoyage
        playerPositionBeforeMiniGame = null
        lastChallengedCell = null
        pendingMiniGameResult = null
        savedStateHandle.remove<String>(KEY_CURRENT_MYSTERY_WORD)

        // ✅ Reset du GameState moderne
        gameState.resetStateForNewGame()

        // Réinitialiser avec nouveau mystère
        viewModelScope.launch {
            startNewGameWithRepository()
        }
    }

    fun revealAllCellsForDebug() {
        for (i in 0 until gameState.totalCells) {
            gameState.revealCell(i)
        }
    }

    /**
     * Statistiques de la base de données (pour debug)
     */
    fun getDatabaseStats() {
        viewModelScope.launch {
            try {
                val stats = mysteryRepository.getDatabaseStats()
                val source = mysteryRepository.getDataSource()
                Log.i("GameViewModel", "Stats BDD: ${stats.totalObjects} objets, ${stats.totalWords} mots, ${stats.objectsWithImages} avec images")
                Log.i("GameViewModel", "Source données: $source")

                // Debug GameState
                Log.d("GameViewModel", gameState.debugState())
            } catch (e: Exception) {
                Log.e("GameViewModel", "Erreur stats BDD: ${e.message}")
            }
        }
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
            delay(300)
        }

        gameState.isPlayerMoving = false

        if (!gameState.isCellRevealed(gameState.playerPosition)) {
            Log.d("GameViewModel", "Case ${gameState.playerPosition} non révélée → Défi requis")
            onChallengeRequired(gameState.playerPosition)
        } else {
            Log.d("GameViewModel", "Case ${gameState.playerPosition} déjà révélée → Tour suivant")
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("GameViewModel", "ViewModel détruit")
    }
}

/**
 * Factory pour injection du Repository
 * Compatible avec GameState moderne
 */
class GameViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            val repository = MysteryRepository.getInstance(context)
            return GameViewModel(SavedStateHandle(), repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}