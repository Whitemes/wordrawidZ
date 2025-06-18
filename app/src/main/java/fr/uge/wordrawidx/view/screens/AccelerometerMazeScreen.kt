package fr.uge.wordrawidx.view.screens

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import fr.uge.wordrawidx.R
import fr.uge.wordrawidx.controller.AccelerometerMazeController
import fr.uge.wordrawidx.controller.NavigationController
import fr.uge.wordrawidx.model.*
import androidx.lifecycle.viewmodel.compose.viewModel;
import fr.uge.wordrawidx.viewmodel.MazeGameViewModel;

// Constantes pour le mini-jeu labyrinthe
val MAZE_BALL_IMAGE_SIZE_DP: Dp = 50.dp
val TARGET_RADIUS_DP_MAZE: Dp = 15.dp

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
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

    // État du labyrinthe (peut être sauvegardé avec Saver si nécessaire)
//    val mazeState = remember { MazeState() }
    // ViewModel qui survit aux changements de configuration
    val mazeViewModel: MazeGameViewModel = viewModel()
    val mazeState = mazeViewModel.mazeState

    // Contrôleur pour gérer l'accéléromètre et la logique de jeu
    val controller = remember(mazeState, coroutineScope) {
        AccelerometerMazeController(
            mazeState = mazeState,
            context = context,
            onGameEnd = { gameWon -> onGameFinished(gameWon) },
            scope = coroutineScope
        )
    }

    // Gestion du cycle de vie pour l'accéléromètre
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
            controller.unregisterListener()
        }
    }

    // Démarrage du timer de jeu
    LaunchedEffect(mazeState.currentMiniGameState) {
        Log.d("AccelerometerMazeScreen", "LaunchedEffect for timer - currentMiniGameState: ${mazeState.currentMiniGameState}")
        if (mazeState.currentMiniGameState == MiniGameState.PLAYING) {
            controller.startGameTimer()
        }
    }

    // Calculs de dimensions et ressources
    val density = LocalDensity.current
    val ballImageSizePx = remember(density) { with(density) { MAZE_BALL_IMAGE_SIZE_DP.toPx() } }
    val ballCollisionRadiusPx = ballImageSizePx / 2f
    val targetRadiusPx = remember(density) { with(density) { TARGET_RADIUS_DP_MAZE.toPx() } }

    val ballImageBitmap = ImageBitmap.imageResource(id = R.drawable.ic_maze_ball)

    // Couleurs du thème
    val surfaceBrightColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.9f)
    val targetColorInCanvas = Color.Green
    val wallColorInCanvas = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    Box(modifier = Modifier.fillMaxSize()) {
        // Image de fond
        Image(
            painter = painterResource(id = R.drawable.img_maze_background),
            contentDescription = "Fond du mini-jeu labyrinthe",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titre du mini-jeu
            Text(
                "Guidez la Bille !",
                style = MaterialTheme.typography.headlineMedium,
                color = onSurfaceColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Affichage du temps restant
            Text(
                "Temps Restant: ${mazeState.timeLeftSeconds}s",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (mazeState.timeLeftSeconds <= 10) errorColor else primaryColor,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Zone de jeu principale
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(surfaceBrightColor)
                    .aspectRatio(
                        ratio = MAZE_COLS.toFloat() / MAZE_ROWS.toFloat(),
                        matchHeightConstraintsFirst = true
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
            ) {
                // Configuration des dimensions lors des changements
                LaunchedEffect(
                    maxWidth, maxHeight, density, ballImageSizePx, targetRadiusPx, orientation
                ) {
                    Log.d(
                        "MazeScreen",
                        "setScreenDimensions triggered! orientation=$orientation, w=${maxWidth}, h=${maxHeight}"
                    )

                    if (maxWidth > 0.dp && maxHeight > 0.dp) {
                        controller.setScreenDimensions(
                            widthPx = with(density) { maxWidth.toPx() },
                            heightPx = with(density) { maxHeight.toPx() },
                            ballRpx = ballCollisionRadiusPx,
                            targetRpx = targetRadiusPx
                        )
                    }
                }

                // Canvas principal pour le rendu du labyrinthe
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

                    val currentCellWidthPx = canvasWidth / MAZE_COLS
                    val currentCellHeightPx = canvasHeight / MAZE_ROWS

                    if (currentCellWidthPx <= 0f || currentCellHeightPx <= 0f) return@Canvas

                    // Dessiner les murs du labyrinthe
                    mazeState.walls.forEach { wallInGridUnits ->
                        val wallPx = Rect(
                            left = wallInGridUnits.left * currentCellWidthPx,
                            top = wallInGridUnits.top * currentCellHeightPx,
                            right = wallInGridUnits.right * currentCellWidthPx,
                            bottom = wallInGridUnits.bottom * currentCellHeightPx
                        )
                        drawRect(
                            color = wallColorInCanvas,
                            topLeft = wallPx.topLeft,
                            size = wallPx.size
                        )
                    }

                    // Dessiner la cible (zone d'arrivée)
                    val targetCenterPx = Offset(
                        mazeState.targetPositionInGridUnits.x * currentCellWidthPx,
                        mazeState.targetPositionInGridUnits.y * currentCellHeightPx
                    )
                    drawCircle(
                        color = targetColorInCanvas,
                        radius = targetRadiusPx,
                        center = targetCenterPx
                    )

                    // Dessiner la bille (image)
                    if (mazeState.ballPosition != Offset.Zero ||
                        (mazeState.ballPosition.x == 0f && mazeState.ballPosition.y == 0f &&
                                DEFAULT_BALL_START_POSITION_GRID_UNITS == Offset(0f, 0f))) {

                        val topLeftImage = Offset(
                            x = mazeState.ballPosition.x - ballImageSizePx / 2f,
                            y = mazeState.ballPosition.y - ballImageSizePx / 2f
                        )

                        val imageDisplaySize = IntSize(
                            ballImageSizePx.toInt(),
                            ballImageSizePx.toInt()
                        )

                        drawImage(
                            image = ballImageBitmap,
                            dstOffset = IntOffset(topLeftImage.x.toInt(), topLeftImage.y.toInt()),
                            dstSize = imageDisplaySize
                        )
                    }
                }

                // Overlays d'état de jeu
                when (mazeState.currentMiniGameState) {
                    MiniGameState.WON -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Color(0xCC00C853),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Gagné !",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                    MiniGameState.LOST_TIME_UP -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Color(0xCCD32F2F),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Temps écoulé !",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                    else -> {} // Jeu en cours
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bouton pour recommencer (seulement si jeu terminé)
            if (mazeState.currentMiniGameState != MiniGameState.PLAYING) {
                Button(
                    onClick = {
                        controller.resetGame()
                        controller.registerListener()
                    }
                ) {
                    Text("Recommencer")
                }
            }
        }
    }
}