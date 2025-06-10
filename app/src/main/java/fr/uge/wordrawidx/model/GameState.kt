package fr.uge.wordrawidx.model

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.util.concurrent.atomic.AtomicBoolean
import fr.uge.wordrawidx.data.model.MysteryObject
import fr.uge.wordrawidx.R

const val BOARD_COLS_MAIN = 5
const val BOARD_ROWS_MAIN = 5

enum class CaseHintType { IMAGE, SEMANTIC_WORD }

data class RevealedCell(
    val cellIndex: Int,
    val hintType: CaseHintType,
    val hintContent: String
)

/**
 * Objet mystère local pour GameState (converti depuis Repository)
 * Évite les conflits de types avec fr.uge.wordrawidx.data.model.MysteryObject
 */
data class LocalMysteryObject(
    val word: String,
    val closeWords: List<String>,
    val imageRes: Int,
    val imageName: String = "",
    val source: String = "repository"
)

data class GameCellHint(
    val type: CaseHintType,
    val value: String? = null,         // mot sémantique si SEMANTIC_WORD
    val imagePortionIndex: Int? = null // numéro portion si IMAGE
)

/**
 * État de jeu moderne géré par Repository + ViewModel
 * Compatible avec l'architecture Repository pattern
 */
class GameState(val boardSize: Int = BOARD_COLS_MAIN) {

    // ✅ États du jeu
    var playerPosition by mutableStateOf(0)
        internal set
    var lastDiceRoll by mutableStateOf(0)
        internal set
    var isDiceRolling by mutableStateOf(false)
        internal set
    var isPlayerMoving by mutableStateOf(false)
        internal set
    val totalCells = boardSize * boardSize

    // ✅ État du plateau
    val revealedCells: SnapshotStateList<RevealedCell> = mutableStateListOf()
    val cellHints: MutableList<GameCellHint> = mutableStateListOf()

    // ✅ Objet mystère local (converti depuis Repository)
    var mysteryObject by mutableStateOf<LocalMysteryObject?>(null)
        private set

    // ✅ État de fin de partie
    var isGameWon by mutableStateOf(false)
        internal set

    // ✅ Flag d'initialisation thread-safe
    private var isInitialized = AtomicBoolean(false)

    init {
        Log.d("GameState", "GameState moderne initialisé - En attente Repository")
    }

    // ✅ MÉTHODE PRINCIPALE : Définit l'objet mystère depuis Repository
    fun setMysteryObject(mystery: MysteryObject) {
        if (isInitialized.getAndSet(true)) {
            Log.w("GameState", "setMysteryObject IGNORÉ - déjà initialisé")
            return
        }

        Log.i("GameState", "setMysteryObject - Mot: '${mystery.word}', Source: Repository")

        // Convertir MysteryObject du Repository vers LocalMysteryObject
        val localMystery = LocalMysteryObject(
            word = mystery.word,
            closeWords = mystery.closeWords,
            imageRes = mystery.imageResourceId ?: R.drawable.img_souris,
            imageName = mystery.imageName,
            source = "repository"
        )

        mysteryObject = localMystery
        setupGameBoard(localMystery)
    }

    /**
     * Configure le plateau de jeu avec l'objet mystère local
     */
    private fun setupGameBoard(mystery: LocalMysteryObject) {
        cellHints.clear()

        val totalCells = boardSize * boardSize
        val imageCount = (totalCells * 0.6f).toInt() // 60% images, 40% mots
        val wordCount = totalCells - imageCount

        val allCellIndices = (0 until totalCells).shuffled()
        val imageCellIndices = allCellIndices.take(imageCount)
        val wordCellIndices = allCellIndices.drop(imageCount)

        val tempHints = MutableList<GameCellHint?>(totalCells) { null }

        // Distribution des portions d'image
        for ((portionIdx, cellIdx) in imageCellIndices.withIndex()) {
            tempHints[cellIdx] = GameCellHint(
                type = CaseHintType.IMAGE,
                imagePortionIndex = portionIdx
            )
        }

        // Distribution des mots sémantiques
        for ((i, cellIdx) in wordCellIndices.withIndex()) {
            val word = mystery.closeWords.getOrNull(i) ?: "[indice]"
            tempHints[cellIdx] = GameCellHint(
                type = CaseHintType.SEMANTIC_WORD,
                value = word
            )
        }

        // Remplir toutes les cases
        for (i in 0 until totalCells) {
            cellHints.add(
                tempHints[i] ?: GameCellHint(
                    type = CaseHintType.SEMANTIC_WORD,
                    value = "[indice]"
                )
            )
        }

        Log.i("GameState", "Plateau configuré: ${cellHints.size} cases, ${imageCount} images, ${wordCount} mots pour '${mystery.word}'")
    }

    // ✅ MÉTHODES DE CONTRÔLE DU JEU

    internal fun updateDiceValue(value: Int) {
        lastDiceRoll = value
        Log.d("GameState", "Dé lancé: $value")
    }

    internal fun updatePlayerPositionValue(newPosition: Int) {
        Log.d("GameState", "Position pion: $playerPosition → $newPosition")
        playerPosition = newPosition
    }

    // ✅ MÉTHODES D'ÉTAT DU PLATEAU

    fun isCellRevealed(cellIndex: Int): Boolean =
        revealedCells.any { it.cellIndex == cellIndex }

    fun getHintForCell(cellIndex: Int): RevealedCell? =
        revealedCells.find { it.cellIndex == cellIndex }

    fun getPlannedHintForCell(cellIndex: Int): GameCellHint? =
        cellHints.getOrNull(cellIndex)

    fun revealCell(cellIndex: Int) {
        if (isCellRevealed(cellIndex)) {
            Log.d("GameState", "Case $cellIndex déjà révélée - ignorée")
            return
        }

        val hint = getPlannedHintForCell(cellIndex)
        if (hint != null) {
            val content = when (hint.type) {
                CaseHintType.IMAGE -> hint.imagePortionIndex?.toString() ?: "0"
                CaseHintType.SEMANTIC_WORD -> hint.value ?: "[indice]"
            }

            revealedCells.add(RevealedCell(cellIndex, hint.type, content))
            Log.i("GameState", "Case $cellIndex révélée: ${hint.type} = '$content'")
        } else {
            Log.e("GameState", "Erreur: pas d'indice prévu pour case $cellIndex")
        }
    }

    // ✅ MÉTHODE DE VICTOIRE

    fun tryGuessMysteryWord(proposed: String): Boolean {
        val win = proposed.trim().equals(mysteryObject?.word?.trim(), ignoreCase = true)
        if (win) {
            isGameWon = true
            Log.i("GameState", "Victoire ! Mot '$proposed' deviné correctement")
        } else {
            Log.d("GameState", "Échec devinette: '$proposed' != '${mysteryObject?.word}'")
        }
        return win
    }

    // ✅ RESET POUR NOUVELLE PARTIE

    internal fun resetStateForNewGame() {
        Log.i("GameState", "RESET pour nouvelle partie")

        // Reset états de jeu
        playerPosition = 0
        lastDiceRoll = 0
        isDiceRolling = false
        isPlayerMoving = false
        isGameWon = false

        // Reset plateau
        revealedCells.clear()
        cellHints.clear()

        // Reset objet mystère
        mysteryObject = null
        isInitialized.set(false)

        Log.i("GameState", "Reset terminé - En attente nouveau mystère depuis Repository")
    }

    // ✅ MÉTHODES UTILITAIRES

    /**
     * Statistiques de l'état actuel
     */
    fun getGameStats(): GameStats {
        return GameStats(
            totalCells = totalCells,
            revealedCells = revealedCells.size,
            currentWord = mysteryObject?.word ?: "Aucun",
            hintsAvailable = mysteryObject?.closeWords?.size ?: 0,
            playerPosition = playerPosition,
            isGameWon = isGameWon,
            source = mysteryObject?.source ?: "Aucune"
        )
    }

    /**
     * Validation de l'état du jeu
     */
    fun isGameStateValid(): Boolean {
        return mysteryObject != null &&
                cellHints.isNotEmpty() &&
                cellHints.size == totalCells
    }

    /**
     * Debug : Affiche l'état complet
     */
    fun debugState(): String {
        val stats = getGameStats()
        return """
            GameState Debug:
            - Mot mystère: ${stats.currentWord} (${stats.source})
            - Position: ${stats.playerPosition}/${stats.totalCells}
            - Cases révélées: ${stats.revealedCells}/${stats.totalCells}
            - Indices disponibles: ${stats.hintsAvailable}
            - Jeu gagné: ${stats.isGameWon}
            - État valide: ${isGameStateValid()}
        """.trimIndent()
    }
}

/**
 * Statistiques de l'état de jeu
 */
data class GameStats(
    val totalCells: Int,
    val revealedCells: Int,
    val currentWord: String,
    val hintsAvailable: Int,
    val playerPosition: Int,
    val isGameWon: Boolean,
    val source: String
)