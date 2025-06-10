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
import fr.uge.wordrawidx.utils.MazeGenerator

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

    // La grille générée par l'algorithme de backtracking
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
        // Utiliser le générateur de labyrinthe avec backtracking
        mazeGridInternal = MazeGenerator.generateMazeGrid(
            rows = MAZE_ROWS,
            cols = MAZE_COLS,
            startRow = 1,
            startCol = 1
        )

        // Convertir la grille en rectangles de murs pour le rendu/collision
        val wallRects = MazeGenerator.convertMazeGridToWallRects(mazeGridInternal)
        walls.clear()
        walls.addAll(wallRects)

        Log.d("MazeState", "Generated ${wallRects.size} wall Rects from ${MAZE_ROWS}x${MAZE_COLS} maze grid.")
    }

    internal fun updateBallPosition(
        newXInPixels: Float,
        newYInPixels: Float,
        screenWidthPx: Float,
        screenHeightPx: Float,
        ballRadiusPx: Float,
        cellWidthPx: Float,
        cellHeightPx: Float
    ) {
        var proposedX = newXInPixels
        var proposedY = newYInPixels

        // Contraintes des bordures de l'écran
        proposedX = proposedX.coerceIn(ballRadiusPx, screenWidthPx - ballRadiusPx)
        proposedY = proposedY.coerceIn(ballRadiusPx, screenHeightPx - ballRadiusPx)

        if (cellWidthPx > 0f && cellHeightPx > 0f) {
            val originalBallX = ballPosition.x
            val originalBallY = ballPosition.y

            // Test collision X (mouvement horizontal)
            val ballRectX = Rect(
                left = proposedX - ballRadiusPx,
                top = originalBallY - ballRadiusPx,
                right = proposedX + ballRadiusPx,
                bottom = originalBallY + ballRadiusPx
            )

            var collisionX = false
            for (wallInGridUnits in walls) {
                // Convertir mur de unités de grille vers pixels
                val wallPx = Rect(
                    left = wallInGridUnits.left * cellWidthPx,
                    top = wallInGridUnits.top * cellHeightPx,
                    right = wallInGridUnits.right * cellWidthPx,
                    bottom = wallInGridUnits.bottom * cellHeightPx
                )

                if (ballRectX.overlaps(wallPx)) {
                    collisionX = true
                    break
                }
            }

            if (collisionX) proposedX = originalBallX

            // Test collision Y (mouvement vertical)
            val ballRectY = Rect(
                left = proposedX - ballRadiusPx,
                top = proposedY - ballRadiusPx,
                right = proposedX + ballRadiusPx,
                bottom = proposedY + ballRadiusPx
            )

            var collisionY = false
            for (wallInGridUnits in walls) {
                val wallPx = Rect(
                    left = wallInGridUnits.left * cellWidthPx,
                    top = wallInGridUnits.top * cellHeightPx,
                    right = wallInGridUnits.right * cellWidthPx,
                    bottom = wallInGridUnits.bottom * cellHeightPx
                )

                if (ballRectY.overlaps(wallPx)) {
                    collisionY = true
                    break
                }
            }

            if (collisionY) proposedY = originalBallY
        }

        ballPosition = Offset(proposedX, proposedY)
    }

    internal fun reset(
        screenWidthPx: Float,
        screenHeightPx: Float,
        cellWidthPx: Float,
        cellHeightPx: Float
    ) {
        Log.d("MazeState", "RESETTING MazeState - Generating new maze and walls.")

        // Regénérer un nouveau labyrinthe
        generateNewMazeAndWalls()

        // Repositionner la bille au point de départ
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

        // Reset du timer et de l'état
        timeLeftSeconds = 45
        currentMiniGameState = MiniGameState.PLAYING

        Log.d("MazeState", "MazeState RESET complete. Ball at ${ballPosition}")
    }

    companion object {
        /**
         * Saver pour la persistance d'état lors des changements de configuration
         */
        val Saver: androidx.compose.runtime.saveable.Saver<MazeState, android.os.Bundle> =
            androidx.compose.runtime.saveable.Saver(
                save = { state ->
                    android.os.Bundle().apply {
                        putFloat("ballX", state.ballPosition.x)
                        putFloat("ballY", state.ballPosition.y)
                        putInt("timeLeft", state.timeLeftSeconds)
                        putString("miniGameState", state.currentMiniGameState.name)
                    }
                },
                restore = { bundle ->
                    MazeState().apply {
                        ballPosition = Offset(
                            bundle.getFloat("ballX"),
                            bundle.getFloat("ballY")
                        )
                        timeLeftSeconds = bundle.getInt("timeLeft", 45)
                        currentMiniGameState = MiniGameState.valueOf(
                            bundle.getString("miniGameState") ?: MiniGameState.PLAYING.name
                        )
                    }
                }
            )
    }
}