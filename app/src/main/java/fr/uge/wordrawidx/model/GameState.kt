package fr.uge.wordrawidx.model

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import fr.uge.wordrawidx.R
import java.util.concurrent.atomic.AtomicBoolean

const val BOARD_COLS_MAIN = 5
const val BOARD_ROWS_MAIN = 5

enum class CaseHintType { IMAGE, SEMANTIC_WORD }

data class RevealedCell(
    val cellIndex: Int,
    val hintType: CaseHintType,
    val hintContent: String
)

data class MysteryObject(
    val word: String,
    val closeWords: List<String>,
    val imageRes: Int
)

object MysteryBank {
    val objects = listOf(
        MysteryObject(
            word = "souris",
            closeWords = listOf(
                "clavier", "pointeur", "USB", "ordinateur", "molette", "écran", "clic", "tapis", "filaire", "Bluetooth",
                "trackpad", "mulot", "roulette", "rongeur", "rat", "cordon", "dongle", "infra-rouge", "sans fil", "batterie", "mac",
                "windows", "scroll", "gamer", "LED", "logiciel"
            ),
            imageRes = R.drawable.img_souris
        ),
        MysteryObject(
            word = "guitare",
            closeWords = listOf(
                "corde", "instrument", "musique", "électrique", "acoustique", "amplificateur", "mélodie", "basse", "accord", "piano",
                "rythme", "solo", "classique", "rock", "jazz", "folk", "manche", "table", "pique", "note",
                "batterie", "scène", "concert", "chanson", "main"
            ),
            imageRes = R.drawable.img_guitare
        ),
        MysteryObject(
            word = "bouteille",
            closeWords = listOf(
                "eau", "verre", "bouchon", "plastique", "boisson", "liquide", "vin", "conteneur", "canette", "verrerie",
                "gourde", "cristal", "soda", "capsule", "lait", "jus", "bulle", "pétillant", "remplir", "ouvrir",
                "laver", "capsuleuse", "usage", "étiquette", "recyclage"
            ),
            imageRes = R.drawable.img_bouteille
        )
    )
}

data class GameCellHint(
    val type: CaseHintType,
    val value: String? = null,         // mot sémantique si SEMANTIC_WORD
    val imagePortionIndex: Int? = null // numéro portion si IMAGE
)

class GameState(val boardSize: Int = BOARD_COLS_MAIN, restoreMode: Boolean = false) {
    var playerPosition by mutableStateOf(0)
        internal set
    var lastDiceRoll by mutableStateOf(0)
        internal set
    var isDiceRolling by mutableStateOf(false)
        internal set
    var isPlayerMoving by mutableStateOf(false)
        internal set
    val totalCells = boardSize * boardSize

    val revealedCells: SnapshotStateList<RevealedCell> = mutableStateListOf()

    var mysteryObject by mutableStateOf<MysteryObject?>(null)
        private set // ✅ CRITIQUE : Empêcher modification externe

    val cellHints: MutableList<GameCellHint> = mutableStateListOf()
    var isGameWon by mutableStateOf(false)
        internal set

    // ✅ CRITIQUE : Flag d'initialisation thread-safe
    private var isInitialized = AtomicBoolean(false)

    init {
        Log.d("GameState", "GameState INIT - restoreMode: $restoreMode")
        if (!restoreMode) {
            setupMysteryAndHintsOnce()
        } else {
            isInitialized.set(true)
        }
    }

    // ✅ CRITIQUE : Setup thread-safe qui ne peut être appelé qu'une fois
    private fun setupMysteryAndHintsOnce() {
        if (isInitialized.getAndSet(true)) {
            Log.w("GameState", "setupMysteryAndHints IGNORÉ - déjà initialisé")
            return
        }

        Log.i("GameState", "setupMysteryAndHints DÉMARRAGE - Premier setup")

        // Tirage aléatoire UNE SEULE FOIS
        mysteryObject = MysteryBank.objects.random()
        cellHints.clear()

        val obj = mysteryObject ?: run {
            Log.e("GameState", "Erreur: mysteryObject est null")
            return
        }

        Log.i("GameState", "Mot mystère choisi: '${obj.word}' (ne changera plus)")

        val totalCells = boardSize * boardSize
        val imageCount = (totalCells * 0.6f).toInt()
        val wordCount = totalCells - imageCount

        val allCellIndices = (0 until totalCells).shuffled()
        val imageCellIndices = allCellIndices.take(imageCount)
        val wordCellIndices = allCellIndices.drop(imageCount)

        val tempHints = MutableList<GameCellHint?>(totalCells) { null }

        // Distribution fixe des indices
        for ((portionIdx, cellIdx) in imageCellIndices.withIndex()) {
            tempHints[cellIdx] = GameCellHint(
                type = CaseHintType.IMAGE,
                imagePortionIndex = portionIdx
            )
        }

        for ((i, cellIdx) in wordCellIndices.withIndex()) {
            val word = obj.closeWords.getOrNull(i) ?: "[mot]"
            tempHints[cellIdx] = GameCellHint(
                type = CaseHintType.SEMANTIC_WORD,
                value = word
            )
        }

        for (i in 0 until totalCells) {
            cellHints.add(tempHints[i] ?: GameCellHint(type = CaseHintType.SEMANTIC_WORD, value = "[mot]"))
        }

        Log.i("GameState", "Setup terminé: ${cellHints.size} indices créés")
    }

    // ✅ MÉTHODES PUBLIQUES
    internal fun updateDiceValue(value: Int) {
        lastDiceRoll = value
    }

    internal fun updatePlayerPositionValue(newPosition: Int) {
        Log.d("GameState", "Position pion: $playerPosition → $newPosition")
        playerPosition = newPosition
    }

    fun isCellRevealed(cellIndex: Int) = revealedCells.any { it.cellIndex == cellIndex }
    fun getHintForCell(cellIndex: Int): RevealedCell? = revealedCells.find { it.cellIndex == cellIndex }
    fun getPlannedHintForCell(cellIndex: Int): GameCellHint? = cellHints.getOrNull(cellIndex)

    // ✅ CRITIQUE : revealCell ne doit JAMAIS modifier le setup
    fun revealCell(cellIndex: Int) {
        if (isCellRevealed(cellIndex)) {
            Log.d("GameState", "Case $cellIndex déjà révélée - ignorée")
            return
        }

        val hint = getPlannedHintForCell(cellIndex)
        if (hint != null) {
            val content = when (hint.type) {
                CaseHintType.IMAGE -> hint.imagePortionIndex?.toString() ?: "0"
                CaseHintType.SEMANTIC_WORD -> hint.value ?: "[mot]"
            }
            revealedCells.add(RevealedCell(cellIndex, hint.type, content))
            Log.i("GameState", "Case $cellIndex révélée: ${hint.type} = '$content'")
        } else {
            Log.e("GameState", "Erreur: pas d'indice prévu pour case $cellIndex")
        }
    }

    fun tryGuessMysteryWord(proposed: String): Boolean {
        val win = proposed.trim().equals(mysteryObject?.word?.trim(), ignoreCase = true)
        if (win) isGameWon = true
        return win
    }

    // ✅ CRITIQUE : Reset complet pour nouvelle partie SEULEMENT
    internal fun resetStateForNewGame() {
        Log.i("GameState", "RESET COMPLET pour nouvelle partie")

        // Reset de tous les états
        playerPosition = 0
        lastDiceRoll = 0
        isDiceRolling = false
        isPlayerMoving = false
        revealedCells.clear()
        isGameWon = false

        // CRITIQUE : Reset complet du contenu
        mysteryObject = null
        cellHints.clear()
        isInitialized.set(false)

        // Nouveau setup complet
        setupMysteryAndHintsOnce()
        Log.i("GameState", "Reset terminé - Nouveau mot: '${mysteryObject?.word}'")
    }

    companion object {
        // Clés de sauvegarde
        private const val KEY_BOARD_SIZE = "boardSize"
        private const val KEY_PLAYER_POSITION = "playerPosition"
        private const val KEY_LAST_DICE_ROLL = "lastDiceRoll"
        private const val KEY_IS_GAME_WON = "isGameWon"
        private const val KEY_MYSTERY_OBJ_IDX = "mysteryObjectIdx"
        private const val KEY_REVEALED_CELLS = "revealedCells"
        private const val KEY_CELL_HINTS = "cellHints"
        private const val KEY_HINT_TYPE = "hintType"
        private const val KEY_HINT_VALUE = "hintValue"
        private const val KEY_HINT_IMAGE_IDX = "hintImageIdx"

        val Saver: Saver<GameState, Bundle> = Saver(
            save = { gameState ->
                Log.d("GameState.Saver", "SAUVEGARDE état - Mot: '${gameState.mysteryObject?.word}'")
                Bundle().apply {
                    putInt(KEY_BOARD_SIZE, gameState.boardSize)
                    putInt(KEY_PLAYER_POSITION, gameState.playerPosition)
                    putInt(KEY_LAST_DICE_ROLL, gameState.lastDiceRoll)
                    putBoolean(KEY_IS_GAME_WON, gameState.isGameWon)
                    putInt(KEY_MYSTERY_OBJ_IDX, MysteryBank.objects.indexOf(gameState.mysteryObject))

                    // Sauvegarder cellHints pour préserver la distribution
                    putParcelableArray(KEY_CELL_HINTS, gameState.cellHints.map {
                        Bundle().apply {
                            putString(KEY_HINT_TYPE, it.type.name)
                            putString(KEY_HINT_VALUE, it.value)
                            if (it.imagePortionIndex != null) putInt(KEY_HINT_IMAGE_IDX, it.imagePortionIndex)
                        }
                    }.toTypedArray())

                    // Sauvegarder cases révélées
                    putParcelableArray(KEY_REVEALED_CELLS, gameState.revealedCells.map {
                        Bundle().apply {
                            putInt("cellIndex", it.cellIndex)
                            putString("hintType", it.hintType.name)
                            putString("hintContent", it.hintContent)
                        }
                    }.toTypedArray())
                }
            },
            restore = { bundle ->
                val boardSize = bundle.getInt(KEY_BOARD_SIZE)
                val mysteryIdx = bundle.getInt(KEY_MYSTERY_OBJ_IDX, 0)
                val obj = MysteryBank.objects.getOrNull(mysteryIdx)

                Log.d("GameState.Saver", "RESTAURATION état - Mot: '${obj?.word}'")

                GameState(boardSize, restoreMode = true).apply {
                    this.playerPosition = bundle.getInt(KEY_PLAYER_POSITION)
                    this.lastDiceRoll = bundle.getInt(KEY_LAST_DICE_ROLL)
                    this.isGameWon = bundle.getBoolean(KEY_IS_GAME_WON, false)
                    this.mysteryObject = obj

                    // Restaurer la distribution des indices
                    cellHints.clear()
                    val hintsArr = bundle.getParcelableArray(KEY_CELL_HINTS)
                    if (hintsArr != null) {
                        for (el in hintsArr) {
                            val b = el as Bundle
                            val type = CaseHintType.valueOf(b.getString(KEY_HINT_TYPE)!!)
                            val value = b.getString(KEY_HINT_VALUE)
                            val imageIdx = if (b.containsKey(KEY_HINT_IMAGE_IDX)) b.getInt(KEY_HINT_IMAGE_IDX) else null
                            cellHints.add(GameCellHint(type, value, imageIdx))
                        }
                    }

                    // Restaurer cases révélées
                    val array = bundle.getParcelableArray(KEY_REVEALED_CELLS)
                    if (array != null) {
                        for (el in array) {
                            val b = el as Bundle
                            val idx = b.getInt("cellIndex")
                            val type = CaseHintType.valueOf(b.getString("hintType")!!)
                            val content = b.getString("hintContent")!!
                            revealedCells.add(RevealedCell(idx, type, content))
                        }
                    }
                }
            }
        )
    }
}