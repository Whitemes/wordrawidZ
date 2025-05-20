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

class GameState(val boardSize: Int = BOARD_COLS_MAIN,  restoreMode: Boolean = false) {
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
    val cellHints: MutableList<GameCellHint> = mutableStateListOf() // must be mutableStateListOf for restoration
    var isGameWon by mutableStateOf(false)
        internal set

    init {
        if (!restoreMode && mysteryObject == null) {
            //Log.d("GameState_Main", "Instance GameState initialisée/créée. PlayerPos: $playerPosition")
            setupMysteryAndHints()
        }
    }

//    fun setupMysteryAndHints() {
//        // ✅ Blocage complet : si tout est déjà prêt, on ne refait rien
//        if (mysteryObject != null && cellHints.isNotEmpty()) {
//            Log.d("GameState_Debug", "setupMysteryAndHints SKIPPED — already initialized")
//            return
//        }
//
//        // ✅ Tirage du mot mystère si pas encore fait
//        if (mysteryObject == null) {
//            mysteryObject = MysteryBank.objects.random()
//            Log.d("GameState_Debug", "Mystery object set: ${mysteryObject?.word}")
//        }
//
//        cellHints.clear()
//
//        val obj = mysteryObject ?: return
//        val totalCells = boardSize * boardSize
//        val wordCount = minOf(10, obj.closeWords.size)
//        val imageCount = totalCells - wordCount
//
//        val allCellIndices = (0 until totalCells).shuffled()
//        val imageCellIndices = allCellIndices.take(imageCount)
//        val wordCellIndices = allCellIndices.drop(imageCount)
//
//        val tempHints = MutableList<GameCellHint?>(totalCells) { null }
//        for ((portionIdx, cellIdx) in imageCellIndices.withIndex()) {
//            tempHints[cellIdx] = GameCellHint(type = CaseHintType.IMAGE, imagePortionIndex = portionIdx)
//        }
//        for ((i, cellIdx) in wordCellIndices.withIndex()) {
//            val word = obj.closeWords.getOrNull(i) ?: "[mot]"
//            tempHints[cellIdx] = GameCellHint(type = CaseHintType.SEMANTIC_WORD, value = word)
//        }
//
//        for (i in 0 until totalCells) {
//            cellHints.add(tempHints[i] ?: GameCellHint(type = CaseHintType.SEMANTIC_WORD, value = "[mot]"))
//        }
//
//        Log.d("GameState_Debug", "setupMysteryAndHints DONE with ${cellHints.size} hints")
//    }

    //CI DESSOUS DEBUG A SUPPRIMER
    fun setupMysteryAndHints() {
        if (mysteryObject != null && cellHints.isNotEmpty()) return

        mysteryObject = MysteryBank.objects.first { it.word == "souris" } // ou "guitare", "bouteille"
        cellHints.clear()

        val totalCells = boardSize * boardSize

        for (i in 0 until totalCells) {
            cellHints.add(
                GameCellHint(
                    type = CaseHintType.IMAGE,
                    imagePortionIndex = i // chaque cellule aura une portion unique
                )
            )
        }
    }


    internal fun updateDiceValue(value: Int) {
        lastDiceRoll = value
    }

    internal fun updatePlayerPositionValue(newPosition: Int) {
        Log.d("GameState_Main", "updatePlayerPositionValue: from $playerPosition to $newPosition")
        playerPosition = newPosition
    }

    fun isCellRevealed(cellIndex: Int) = revealedCells.any { it.cellIndex == cellIndex }
    fun getHintForCell(cellIndex: Int): RevealedCell? = revealedCells.find { it.cellIndex == cellIndex }
    fun getPlannedHintForCell(cellIndex: Int): GameCellHint? = cellHints.getOrNull(cellIndex)

    fun revealCell(cellIndex: Int) {
        if (!isCellRevealed(cellIndex)) {
            val hint = getPlannedHintForCell(cellIndex)
            if (hint != null) {
                val content = when (hint.type) {
                    CaseHintType.IMAGE -> hint.imagePortionIndex?.toString() ?: "0"
                    CaseHintType.SEMANTIC_WORD -> hint.value ?: "[mot]"
                }
                revealedCells.add(
                    RevealedCell(cellIndex, hint.type, content)
                )
            }
        }
    }


    fun tryGuessMysteryWord(proposed: String): Boolean {
        val win = proposed.trim().equals(mysteryObject?.word?.trim(), ignoreCase = true)
        if (win) isGameWon = true
        return win
    }

    internal fun resetStateForNewGame() {
        Log.d("GameState_Main", "resetStateForNewGame CALLED. Resetting all values.")
        playerPosition = 0
        lastDiceRoll = 0
        isDiceRolling = false
        isPlayerMoving = false
        revealedCells.clear()
        isGameWon = false
        setupMysteryAndHints()
        Log.d("GameState_Main", "resetStateForNewGame FINISHED. PlayerPos: $playerPosition.")
    }

    companion object {
        private const val KEY_BOARD_SIZE = "boardSize"
        private const val KEY_PLAYER_POSITION = "playerPosition"
        private const val KEY_LAST_DICE_ROLL = "lastDiceRoll"
        private const val KEY_REVEALED_CELLS = "revealedCells"
        private const val KEY_IS_GAME_WON = "isGameWon"
        private const val KEY_HINT_TYPE = "hintType"
        private const val KEY_HINT_VALUE = "hintValue"
        private const val KEY_HINT_IMAGE_IDX = "hintImageIdx"
        private const val KEY_CELL_HINTS = "cellHints"
        private const val KEY_MYSTERY_OBJ_IDX = "mysteryObjectIdx"

        val Saver: Saver<GameState, Bundle> = Saver(
            save = { gameState ->
                Log.d("GameState_Main.Saver", "SAVING state. PlayerPos: ${gameState.playerPosition}")
                Bundle().apply {
                    putInt(KEY_BOARD_SIZE, gameState.boardSize)
                    putInt(KEY_PLAYER_POSITION, gameState.playerPosition)
                    putInt(KEY_LAST_DICE_ROLL, gameState.lastDiceRoll)
                    putBoolean(KEY_IS_GAME_WON, gameState.isGameWon)
                    // Sauve l'index du mystère utilisé
                    putInt(KEY_MYSTERY_OBJ_IDX, MysteryBank.objects.indexOf(gameState.mysteryObject))

                    // Serialisation des revealedCells
                    putParcelableArray(KEY_REVEALED_CELLS, gameState.revealedCells.map {
                        Bundle().apply {
                            putInt("cellIndex", it.cellIndex)
                            putString("hintType", it.hintType.name)
                            putString("hintContent", it.hintContent)
                        }
                    }.toTypedArray())

                    // Serialisation des hints de chaque case
                    putParcelableArray(KEY_CELL_HINTS, gameState.cellHints.map {
                        Bundle().apply {
                            putString(KEY_HINT_TYPE, it.type.name)
                            putString(KEY_HINT_VALUE, it.value)
                            if (it.imagePortionIndex != null) putInt(KEY_HINT_IMAGE_IDX, it.imagePortionIndex)
                        }
                    }.toTypedArray())
                }
            },
            restore = { bundle ->
                val boardSize = bundle.getInt(KEY_BOARD_SIZE)
                val restoredPlayerPosition = bundle.getInt(KEY_PLAYER_POSITION)
                val mysteryIdx = bundle.getInt(KEY_MYSTERY_OBJ_IDX, 0)
                val obj = MysteryBank.objects.getOrNull(mysteryIdx)

                GameState(boardSize, restoreMode = true).apply {
                    this.playerPosition = restoredPlayerPosition
                    this.lastDiceRoll = bundle.getInt(KEY_LAST_DICE_ROLL)
                    this.isGameWon = bundle.getBoolean(KEY_IS_GAME_WON, false)
                    this.mysteryObject = obj
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
                    // Charger la répartition originale des indices
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
                }
            }
        )
    }
}
