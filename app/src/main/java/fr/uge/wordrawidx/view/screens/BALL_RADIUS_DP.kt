package fr.uge.wordrawidx.view.screens

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image // Nécessaire pour l'image de fond
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap // POUR DESSINER L'IMAGE DANS LE CANVAS
import androidx.compose.ui.graphics.drawscope.DrawScope // Pour la fonction d'extension
import androidx.compose.ui.graphics.drawscope.withTransform // Pour la rotation/mise à l'échelle de l'image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.imageResource // POUR CHARGER L'IMAGE DANS LE CANVAS
import androidx.compose.ui.res.painterResource // Pour l'image de fond
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset // Pour drawImage
import androidx.compose.ui.unit.IntSize // Pour drawImage
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import fr.uge.wordrawidx.R // IMPORTANT pour R.drawable.votre_image
import fr.uge.wordrawidx.controller.AccelerometerMazeController
import fr.uge.wordrawidx.model.MAZE_COLS
import fr.uge.wordrawidx.model.MAZE_ROWS
import fr.uge.wordrawidx.model.MazeState
import fr.uge.wordrawidx.model.MiniGameState
import fr.uge.wordrawidx.navigation.Screen
import fr.uge.wordrawidx.controller.NavigationController
import fr.uge.wordrawidx.model.DEFAULT_BALL_START_POSITION_GRID_UNITS

// Constantes pour la taille (peuvent être ajustées)
val MAZE_BALL_IMAGE_SIZE_DP: Dp = 50.dp // Taille souhaitée pour votre image de bille
val TARGET_RADIUS_DP_MAZE: Dp = 15.dp // Cible reste un cercle pour l'instant

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AccelerometerMazeScreen(
    navigationController: NavigationController,
    onGameFinished: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val mazeState = remember { MazeState() }
    val controller = remember(mazeState, coroutineScope) {
        AccelerometerMazeController(
            mazeState = mazeState, context = context,
            onGameEnd = { gameWon -> onGameFinished(gameWon) },
            scope = coroutineScope
        )
    }

    DisposableEffect(lifecycleOwner, controller) {
        Log.d("AccelerometerMazeScreen", "DisposableEffect: Attaching lifecycle observer.")
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.d("AccelerometerMazeScreen", "Lifecycle ON_RESUME: Registering sensor listener.")
                    controller.registerListener()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d("AccelerometerMazeScreen", "Lifecycle ON_PAUSE: Unregistering sensor listener.")
                    controller.unregisterListener()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            Log.d("AccelerometerMazeScreen", "DisposableEffect ON_DISPOSE: Removing observer and unregistering listener.")
            lifecycleOwner.lifecycle.removeObserver(observer)
            controller.unregisterListener() // S'assurer qu'il est désenregistré
        }
    }

    LaunchedEffect(mazeState.currentMiniGameState) {
        Log.d("AccelerometerMazeScreen", "LaunchedEffect for timer - currentMiniGameState: ${mazeState.currentMiniGameState}")
        if (mazeState.currentMiniGameState == MiniGameState.PLAYING) {
            controller.startGameTimer()
        }
    }

    val density = LocalDensity.current
    // Convertir la taille de l'image de bille en pixels
    val ballImageSizePx = remember(density) { with(density) { MAZE_BALL_IMAGE_SIZE_DP.toPx() } }
    // Le "rayon" pour la logique de collision de la bille sera la moitié de sa taille
    val ballCollisionRadiusPx = ballImageSizePx / 2f
    val targetRadiusPx = remember(density) { with(density) { TARGET_RADIUS_DP_MAZE.toPx() } }


    // Charger l'ImageBitmap pour la bille une seule fois
    val ballImageBitmap = ImageBitmap.imageResource(id = R.drawable.ic_maze_ball) // REMPLACEZ ic_maze_ball

    // Couleurs
    val surfaceDimColor = MaterialTheme.colorScheme.surfaceDim
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceBrightColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.9f)
    val targetColorInCanvas = Color.Green
    val wallColorInCanvas = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_maze_background), // VOTRE IMAGE DE FOND
            contentDescription = "Fond du mini-jeu labyrinthe",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Guidez la Bille !", style = MaterialTheme.typography.headlineMedium, color = onSurfaceColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Temps Restant: ${mazeState.timeLeftSeconds}s",
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                color = if (mazeState.timeLeftSeconds <= 10) errorColor else primaryColor,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            BoxWithConstraints(
                modifier = Modifier.weight(1f).fillMaxWidth().background(surfaceBrightColor)
                    .aspectRatio(MAZE_COLS.toFloat() / MAZE_ROWS.toFloat(), matchHeightConstraintsFirst = true)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            ) {
                LaunchedEffect(maxWidth, maxHeight, density, ballImageSizePx, targetRadiusPx, mazeState) {
                    if (maxWidth > 0.dp && maxHeight > 0.dp) {
                        controller.setScreenDimensions(
                            with(density) { maxWidth.toPx() },
                            with(density) { maxHeight.toPx() },
                            ballCollisionRadiusPx, // Passer le rayon de collision
                            targetRadiusPx
                        )
                    }
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas
                    val currentCellWidthPx = canvasWidth / MAZE_COLS
                    val currentCellHeightPx = canvasHeight / MAZE_ROWS
                    if (currentCellWidthPx <= 0f || currentCellHeightPx <= 0f) return@Canvas

                    // Dessiner les murs
                    mazeState.walls.forEach { wallInGridUnits ->
                        val wallPx = Rect(
                            left = wallInGridUnits.left * currentCellWidthPx,
                            top = wallInGridUnits.top * currentCellHeightPx,
                            right = wallInGridUnits.right * currentCellWidthPx,
                            bottom = wallInGridUnits.bottom * currentCellHeightPx
                        )
                        drawRect(color = wallColorInCanvas, topLeft = wallPx.topLeft, size = wallPx.size)
                    }

                    // Dessiner la cible
                    val targetCenterPx = Offset(
                        mazeState.targetPositionInGridUnits.x * currentCellWidthPx,
                        mazeState.targetPositionInGridUnits.y * currentCellHeightPx
                    )
                    drawCircle(color = targetColorInCanvas, radius = targetRadiusPx, center = targetCenterPx)

                    // Dessiner l'IMAGE de la bille
                    if (mazeState.ballPosition != Offset.Zero || (mazeState.ballPosition.x ==0f && mazeState.ballPosition.y == 0f && DEFAULT_BALL_START_POSITION_GRID_UNITS == Offset(0f,0f) )) {
                        // La position de la bille est le centre de l'image
                        // Pour drawImage, topLeft est requis.
                        val topLeftImage = Offset(
                            x = mazeState.ballPosition.x - ballImageSizePx / 2f,
                            y = mazeState.ballPosition.y - ballImageSizePx / 2f
                        )
                        // La taille de l'image en pixels entiers
                        val imageDisplaySize = IntSize(ballImageSizePx.toInt(), ballImageSizePx.toInt())

                        // Appliquer la transformation (mise à l'échelle si isPlayerMoving et animation de scale)
                        // Pour l'instant, on ne met pas de scale sur l'image, mais c'est possible avec withTransform.
                        // val scaleFactor = if (mazeState.isPlayerMoving) 1.2f else 1.0f // Si vous aviez un état isPlayerMoving dans MazeState

                        drawImage(
                            image = ballImageBitmap,
                            dstOffset = IntOffset(topLeftImage.x.toInt(), topLeftImage.y.toInt()),
                            dstSize = imageDisplaySize
                            // Vous pouvez ajouter srcOffset et srcSize si vous voulez dessiner une portion de l'ImageBitmap
                            // alpha = ..., colorFilter = ... sont aussi disponibles
                        )
                    }
                }

                // Messages de victoire/défaite
                if (mazeState.currentMiniGameState == MiniGameState.WON) { /* ... (comme avant) ... */ }
                else if (mazeState.currentMiniGameState == MiniGameState.LOST_TIME_UP) { /* ... (comme avant) ... */ }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (mazeState.currentMiniGameState != MiniGameState.PLAYING) { /* ... (bouton Recommencer comme avant) ... */ }
        }
    }
}