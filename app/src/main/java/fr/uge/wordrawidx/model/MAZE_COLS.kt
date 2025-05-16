package fr.uge.wordrawidx.model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import fr.uge.wordrawidx.utils.MazeGenerator // IMPORT DU GÉNÉRATEUR

const val MAZE_COLS = 10
const val MAZE_ROWS = 15

val DEFAULT_BALL_START_POSITION_GRID_UNITS = Offset(1.5f, 1.5f)
val DEFAULT_TARGET_POSITION_GRID_UNITS = Offset(MAZE_COLS - 1.5f, MAZE_ROWS - 1.5f)

enum class MiniGameState { PLAYING, WON, LOST_TIME_UP }

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class MazeState {
    var ballPosition by mutableStateOf(Offset.Zero)
        internal set
    val targetPositionInGridUnits by mutableStateOf(DEFAULT_TARGET_POSITION_GRID_UNITS)

    // La grille générée
    private var mazeGridInternal: List<List<MazeCell>> = emptyList()

    // Les murs pour la collision et le dessin, dérivés de mazeGridInternal
    val walls: SnapshotStateList<Rect> = mutableStateListOf()

    var timeLeftSeconds by mutableStateOf(45)
        internal set
    var currentMiniGameState by mutableStateOf(MiniGameState.PLAYING)
        internal set

    init {
        Log.d("MazeState", "INIT - Generating new maze.")
        generateNewMazeAndWalls()
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun generateNewMazeAndWalls() {
        // Utiliser MAZE_ROWS et MAZE_COLS ici
        mazeGridInternal = MazeGenerator.generateMazeGrid(MAZE_ROWS, MAZE_COLS, startRow = 1, startCol = 1)
        val wallRects = MazeGenerator.convertMazeGridToWallRects(mazeGridInternal)
        walls.clear()
        walls.addAll(wallRects)
        Log.d("MazeState", "Generated ${wallRects.size} wall Rects.")
    }

    internal fun updateBallPosition(
        newXInPixels: Float, newYInPixels: Float,
        screenWidthPx: Float, screenHeightPx: Float, ballRadiusPx: Float,
        cellWidthPx: Float, cellHeightPx: Float
    ) {
        // ... (la logique de collision utilise mazeState.walls, qui est maintenant générée) ...
        // La logique de collision existante avec List<Rect> peut rester pour l'instant.
        // Elle va maintenant utiliser les murs générés par le backtracking.
        var proposedX = newXInPixels
        var proposedY = newYInPixels

        proposedX = proposedX.coerceIn(ballRadiusPx, screenWidthPx - ballRadiusPx)
        proposedY = proposedY.coerceIn(ballRadiusPx, screenHeightPx - ballRadiusPx)

        if (cellWidthPx > 0f && cellHeightPx > 0f) {
            val originalBallX = ballPosition.x
            val originalBallY = ballPosition.y

            var currentProposedX = proposedX
            var ballRectX = Rect(currentProposedX - ballRadiusPx, originalBallY - ballRadiusPx, currentProposedX + ballRadiusPx, originalBallY + ballRadiusPx)
            var collisionX = false
            for (wallInGridUnits in walls) { // walls contient maintenant des Rects en unités de grille
                // La conversion en pixels doit se faire ici ou la liste walls doit déjà être en pixels
                // DANS CETTE VERSION, convertGridToWallRects retourne des Rects en UNITÉS DE GRILLE.
                val wallPx = Rect(
                    left = wallInGridUnits.left * cellWidthPx,
                    top = wallInGridUnits.top * cellHeightPx,
                    right = wallInGridUnits.right * cellWidthPx,
                    bottom = wallInGridUnits.bottom * cellHeightPx
                )
                if (ballRectX.overlaps(wallPx)) { collisionX = true; break }
            }
            if (collisionX) proposedX = originalBallX

            var currentProposedY = proposedY
            var ballRectY = Rect(proposedX - ballRadiusPx, currentProposedY - ballRadiusPx, proposedX + ballRadiusPx, currentProposedY + ballRadiusPx)
            var collisionY = false
            for (wallInGridUnits in walls) {
                val wallPx = Rect(
                    left = wallInGridUnits.left * cellWidthPx,
                    top = wallInGridUnits.top * cellHeightPx,
                    right = wallInGridUnits.right * cellWidthPx,
                    bottom = wallInGridUnits.bottom * cellHeightPx
                )
                if (ballRectY.overlaps(wallPx)) { collisionY = true; break }
            }
            if (collisionY) proposedY = originalBallY
        }
        ballPosition = Offset(proposedX, proposedY)
    }

    internal fun reset(screenWidthPx: Float, screenHeightPx: Float, cellWidthPx: Float, cellHeightPx: Float) {
        Log.d("MazeState", "RESETTING MazeState - Generating new maze and walls.")
        generateNewMazeAndWalls() // Regénère le labyrinthe
        if (cellWidthPx > 0f && cellHeightPx > 0f) {
            ballPosition = Offset(
                DEFAULT_BALL_START_POSITION_GRID_UNITS.x * cellWidthPx,
                DEFAULT_BALL_START_POSITION_GRID_UNITS.y * cellHeightPx
            )
        } else if (screenWidthPx > 0f && screenHeightPx > 0f) {
            ballPosition = Offset(screenWidthPx * 0.1f, screenHeightPx * 0.1f)
        } else {
            ballPosition = Offset.Zero
        }
        timeLeftSeconds = 45
        currentMiniGameState = MiniGameState.PLAYING
        Log.d("MazeState", "MazeState RESET complete. Ball at ${ballPosition}")
    }
}