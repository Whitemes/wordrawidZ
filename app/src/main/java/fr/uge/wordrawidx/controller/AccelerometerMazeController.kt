package fr.uge.wordrawidx.controller

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import fr.uge.wordrawidx.model.DEFAULT_BALL_START_POSITION_GRID_UNITS
import fr.uge.wordrawidx.model.MAZE_COLS
import fr.uge.wordrawidx.model.MAZE_ROWS
import fr.uge.wordrawidx.model.MazeState
import fr.uge.wordrawidx.model.MiniGameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

class AccelerometerMazeController(
    private val mazeState: MazeState,
    private val context: Context,
    private val onGameEnd: (Boolean) -> Unit,
    private val scope: CoroutineScope
) : SensorEventListener {

    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val baseMoveFactor = 2.8f

    private var screenWidthPx: Float = 0f
    private var screenHeightPx: Float = 0f
    private var ballRadiusPx: Float = 0f
    private var targetRadiusPx: Float = 0f
    private var cellWidthPx: Float = 0f
    private var cellHeightPx: Float = 0f
    private var dimensionsInitialized = false

    fun registerListener() {
        accelerometer?.also { accel ->
            val registered = sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
            Log.d("MazeController", "Accelerometer listener registered: $registered")
        }
    }

    fun unregisterListener() {
        sensorManager.unregisterListener(this)
        Log.d("MazeController", "Accelerometer listener unregistered")
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun setScreenDimensions(widthPx: Float, heightPx: Float, ballRpx: Float, targetRpx: Float) {
        if (widthPx <=0 || heightPx <=0) {
            Log.e("MazeController", "setScreenDimensions: Invalid screen dimensions ($widthPx, $heightPx). Aborting.")
            return
        }
        screenWidthPx = widthPx
        val oldCellWidth = cellWidthPx
        val oldCellHeight = cellHeightPx
        screenHeightPx = heightPx
        ballRadiusPx = ballRpx
        targetRadiusPx = targetRpx

        if (MAZE_COLS <= 0 || MAZE_ROWS <= 0) {
            Log.e("MazeController", "MAZE_COLS or MAZE_ROWS is invalid. Using full screen as cell.")
            cellWidthPx = screenWidthPx
            cellHeightPx = screenHeightPx
        } else {
            cellWidthPx = screenWidthPx / MAZE_COLS
            cellHeightPx = screenHeightPx / MAZE_ROWS
        }

        if (cellWidthPx <= 0 || cellHeightPx <= 0) {
            Log.e("MazeController", "setScreenDimensions: Calculated cell dimensions are zero or negative. cellW: $cellWidthPx, cellH: $cellHeightPx. Screen (w,h): ($screenWidthPx, $screenHeightPx)")
            // Fallback to avoid crash, but drawing will be incorrect
            cellWidthPx = if (cellWidthPx <= 0) 1f else cellWidthPx
            cellHeightPx = if (cellHeightPx <= 0) 1f else cellHeightPx
        }

//        val initialBallX = DEFAULT_BALL_START_POSITION_GRID_UNITS.x * cellWidthPx
//        val initialBallY = DEFAULT_BALL_START_POSITION_GRID_UNITS.y * cellHeightPx
//        mazeState.ballPosition = Offset(initialBallX, initialBallY)
//        Log.d("MazeController", "Dimensions set. Initial Ball Pos (px): ${mazeState.ballPosition}, cellW: $cellWidthPx, cellH: $cellHeightPx")
          if (dimensionsInitialized && oldCellWidth > 0f && oldCellHeight > 0f) {
                        // On convertit l’ancienne position en unités de grille, puis on la
                        // reprojette dans les nouvelles dimensions d’écran.
                        val gridX = mazeState.ballPosition.x / oldCellWidth
                        val gridY = mazeState.ballPosition.y / oldCellHeight
                        mazeState.ballPosition = Offset(gridX * cellWidthPx, gridY * cellHeightPx)
                        Log.d("MazeController", "Rotation : bille recalculée (grid=[$gridX,$gridY]) => px=${mazeState.ballPosition}")
          } else {
             // Première initialisation ou valeurs invalides : on part du départ.
             val initialBallX = DEFAULT_BALL_START_POSITION_GRID_UNITS.x * cellWidthPx
             val initialBallY = DEFAULT_BALL_START_POSITION_GRID_UNITS.y * cellHeightPx
             mazeState.ballPosition = Offset(initialBallX, initialBallY)
             dimensionsInitialized = true
             Log.d("MazeController","Init : bille placée au départ px=${mazeState.ballPosition}")
          }
    }

//    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
//    override fun onSensorChanged(event: SensorEvent?) {
//        if (mazeState.currentMiniGameState != MiniGameState.PLAYING) return
//        if (screenWidthPx == 0f || screenHeightPx == 0f || cellWidthPx <=0f || cellHeightPx <=0f) {
//            // Log.w("MazeController", "SensorChanged: Dimensions not ready.") // Peut être trop bruyant
//            return
//        }
//
//        event?.let {
//            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
//                val rawAccelX = it.values[0]
//                val rawAccelY = it.values[1]
//
//                val displayRotation = windowManager.defaultDisplay.rotation
//                var deviceRelativeAccelX = 0f
//                var deviceRelativeAccelY = 0f
//
//                when (displayRotation) {
//                    Surface.ROTATION_0 -> { deviceRelativeAccelX = -rawAccelX; deviceRelativeAccelY = rawAccelY }
//                    Surface.ROTATION_90 -> { deviceRelativeAccelX = rawAccelY; deviceRelativeAccelY = rawAccelX }
//                    Surface.ROTATION_180 -> { deviceRelativeAccelX = rawAccelX; deviceRelativeAccelY = -rawAccelY }
//                    Surface.ROTATION_270 -> { deviceRelativeAccelX = -rawAccelY; deviceRelativeAccelY = -rawAccelX }
//                }
//
//                val currentBallPos = mazeState.ballPosition
//                val newX = currentBallPos.x + deviceRelativeAccelX * baseMoveFactor
//                val newY = currentBallPos.y + deviceRelativeAccelY * baseMoveFactor
//
//                mazeState.updateBallPosition(
//                    newX, newY,
//                    screenWidthPx, screenHeightPx, ballRadiusPx,
//                    cellWidthPx, cellHeightPx
//                )
//                checkWinCondition()
//            }
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onSensorChanged(event: SensorEvent?) {
        if (mazeState.currentMiniGameState != MiniGameState.PLAYING) return
        if (screenWidthPx == 0f || screenHeightPx == 0f || cellWidthPx <= 0f || cellHeightPx <= 0f) {
            return
        }

        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val rawAccelX = it.values[0]
                val rawAccelY = it.values[1]

                // Utilisation moderne pour obtenir la rotation (API 30+)
                val displayRotation = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        context.display?.rotation ?: Surface.ROTATION_0
                    } else {
                        @Suppress("DEPRECATION")
                        windowManager.defaultDisplay.rotation
                    }
                } catch (e: Exception) {
                    Surface.ROTATION_0
                }

                Log.d(
                    "MazeController",
                    "displayRotation=$displayRotation, rawX=$rawAccelX, rawY=$rawAccelY"
                )

                // Vérifie sur ton device : ce mapping fonctionne sur la majorité des tablettes/phones modernes
                val (deviceRelativeAccelX, deviceRelativeAccelY) = when (displayRotation) {
                    Surface.ROTATION_0 -> Pair(-rawAccelX, rawAccelY)         // Portrait "normal"
                    Surface.ROTATION_90 -> Pair(rawAccelY, rawAccelX)         // Paysage home à droite
                    Surface.ROTATION_180 -> Pair(rawAccelX, -rawAccelY)       // Portrait inversé
                    Surface.ROTATION_270 -> Pair(-rawAccelY, -rawAccelX)      // Paysage home à gauche
                    else -> Pair(-rawAccelX, rawAccelY)
                }

                val currentBallPos = mazeState.ballPosition
                val newX = currentBallPos.x + deviceRelativeAccelX * baseMoveFactor
                val newY = currentBallPos.y + deviceRelativeAccelY * baseMoveFactor

                mazeState.updateBallPosition(
                    newX, newY,
                    screenWidthPx, screenHeightPx, ballRadiusPx,
                    cellWidthPx, cellHeightPx
                )
                checkWinCondition()
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* Non utilisé */ }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun checkWinCondition() {
        if (mazeState.currentMiniGameState != MiniGameState.PLAYING) return
        if (cellWidthPx <= 0f || cellHeightPx <= 0f) return

        val targetCenterPx = Offset(
            mazeState.targetPositionInGridUnits.x * cellWidthPx,
            mazeState.targetPositionInGridUnits.y * cellHeightPx
        )
        val distance = sqrt(
            (mazeState.ballPosition.x - targetCenterPx.x).pow(2) +
                    (mazeState.ballPosition.y - targetCenterPx.y).pow(2)
        )
        if (distance < ballRadiusPx + targetRadiusPx * 0.8f) {
            mazeState.currentMiniGameState = MiniGameState.WON
            onGameEnd(true)
            unregisterListener()
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun startGameTimer() {
        if (mazeState.currentMiniGameState != MiniGameState.PLAYING) return
        scope.launch {
            Log.d("MazeController", "Timer starting for ${mazeState.timeLeftSeconds}s")
            while (mazeState.timeLeftSeconds > 0 && mazeState.currentMiniGameState == MiniGameState.PLAYING && isActive) {
                delay(1000)
                if (mazeState.currentMiniGameState == MiniGameState.PLAYING && isActive) {
                    mazeState.timeLeftSeconds--
                }
            }
            if (isActive && mazeState.timeLeftSeconds <= 0 && mazeState.currentMiniGameState == MiniGameState.PLAYING) {
                Log.d("MazeController", "Timer ended - TIME UP")
                mazeState.currentMiniGameState = MiniGameState.LOST_TIME_UP
                onGameEnd(false)
                unregisterListener()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun resetGame() {
        if (screenWidthPx > 0f && screenHeightPx > 0f && cellWidthPx > 0f && cellHeightPx > 0f) {
            mazeState.reset(screenWidthPx, screenHeightPx, cellWidthPx, cellHeightPx)
            Log.d("MazeController", "MazeState reset with dimensions. New ball pos: ${mazeState.ballPosition}")
        } else {
            Log.w("MazeController", "ResetGame called, but screen/cell dimensions not fully set. Attempting reset with (0,0).")
            mazeState.reset(0f,0f,0f,0f)
        }
    }
}
