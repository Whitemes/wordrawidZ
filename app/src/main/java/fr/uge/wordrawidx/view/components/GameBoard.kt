package fr.uge.wordrawidx.view.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import fr.uge.wordrawidx.model.GameState
import fr.uge.wordrawidx.model.CaseHintType
import fr.uge.wordrawidx.R

@Composable
fun GameBoard(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val boardSize = gameState.boardSize
    val cellIndices = remember(boardSize) { List(boardSize * boardSize) { it } }
    val cornerRadius = 24.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        // Plateau de fond (motif de plateau, toujours visible)
        Image(
            painter = painterResource(id = R.drawable.img_maze_background),
            contentDescription = "Fond du plateau de jeu",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(boardSize),
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            itemsIndexed(cellIndices) { visualGridIndex, gameBoardCellIndex ->
                val revealedCell = gameState.getHintForCell(gameBoardCellIndex)
                BoardCell(
                    visualNumber = visualGridIndex + 1,
                    isPlayerOnCell = gameBoardCellIndex == gameState.playerPosition,
                    isPlayerMoving = gameState.isPlayerMoving && gameBoardCellIndex == gameState.playerPosition,
                    isEvenCell = run {
                        val logicalRow = gameBoardCellIndex / boardSize
                        val logicalCol = gameBoardCellIndex % boardSize
                        (logicalRow + logicalCol) % 2 == 0
                    },
                    revealedCell = revealedCell,
                    mysteryImageRes = gameState.mysteryObject?.imageRes,
                    cellIndex = gameBoardCellIndex,
                    boardSize = boardSize
                )
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BoardCell(
    visualNumber: Int,
    isPlayerOnCell: Boolean,
    isPlayerMoving: Boolean,
    isEvenCell: Boolean,
    revealedCell: fr.uge.wordrawidx.model.RevealedCell?,
    mysteryImageRes: Int?,
    cellIndex: Int,
    boardSize: Int,
    modifier: Modifier = Modifier
) {
    val cellBackgroundColor = if (isEvenCell) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.22f)
    }

    val scaleFactor by animateFloatAsState(
        targetValue = if (isPlayerMoving && isPlayerOnCell) 1.15f else 1.0f,
        label = "playerScaleAnimation"
    )

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(4.dp)),
        color = cellBackgroundColor,
        shape = RoundedCornerShape(4.dp),
        tonalElevation = 0.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().clipToBounds(),
        ) {
            val cellSize = min(constraints.maxWidth.dp, constraints.maxHeight.dp)
            val pawnImageSize = cellSize * 0.75f

            // ✅ COUCHE 1 : Contenu de la case (derrière le pion)
            if (revealedCell != null) {
                when (revealedCell.hintType) {
                    CaseHintType.SEMANTIC_WORD -> {
                        // Mot sémantique avec fond coloré
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.9f)
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = revealedCell.hintContent,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    CaseHintType.IMAGE -> {
                        if (mysteryImageRes != null) {
                            // ✅ Portion d'image en pleine taille (derrière le pion)
                            PortionOfImageInCellFromBitmap(
                                imageRes = mysteryImageRes,
                                portionIndex = cellIndex,
                                gridSize = boardSize,
                                modifier = Modifier
                                    .fillMaxSize() // Pleine taille
                                    .align(Alignment.Center)
                            )

                            // Bordure subtile pour délimiter la portion
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
            } else {
                // Numéro de case si non révélée
                Text(
                    text = visualNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                )
            }

            // ✅ COUCHE 2 : Pion TRANSPARENT au-dessus de tout
            if (isPlayerOnCell) {
                // Ombre du pion pour maintenir la visibilité de position
                Image(
                    painter = painterResource(id = R.drawable.ic_player_pawn),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(pawnImageSize)
                        .align(Alignment.Center)
                        .graphicsLayer(
                            scaleX = scaleFactor,
                            scaleY = scaleFactor,
                            alpha = 0.3f, // Ombre semi-transparente
                            translationX = 2f,
                            translationY = 2f
                        )
                )

                // Pion principal transparent
                Image(
                    painter = painterResource(id = R.drawable.ic_player_pawn),
                    contentDescription = "Pion du joueur",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(pawnImageSize)
                        .align(Alignment.Center)
                        .graphicsLayer(
                            scaleX = scaleFactor,
                            scaleY = scaleFactor,
                            alpha = 0.7f // ✅ PION TRANSPARENT (70% opacité)
                        )
                )
            }
        }
    }
}

/**
 * ✅ FONCTION OPTIMISÉE - Découpage précis d'image avec Canvas
 */
@Composable
fun PortionOfImageInCellFromBitmap(
    modifier: Modifier = Modifier,
    imageRes: Int,
    portionIndex: Int,
    gridSize: Int = 5
) {
    val context = LocalContext.current
    val imageBitmap = remember(imageRes) {
        ImageBitmap.imageResource(context.resources, imageRes)
    }
    PortionOfImageInCellBitmap(
        imageBitmap = imageBitmap,
        portionIndex = portionIndex,
        gridSize = gridSize,
        modifier = modifier
    )
}

/**
 * ✅ FONCTION CORE - Découpage mathématique précis avec Canvas
 */
@Composable
fun PortionOfImageInCellBitmap(
    modifier: Modifier = Modifier,
    imageBitmap: ImageBitmap,
    portionIndex: Int,
    gridSize: Int = 5
) {
    // Calcul des coordonnées de la portion dans la grille
    val row = portionIndex / gridSize
    val col = portionIndex % gridSize

    Canvas(modifier = modifier.fillMaxSize()) {
        // Dimensions de l'image source
        val imageWidth = imageBitmap.width.toFloat()
        val imageHeight = imageBitmap.height.toFloat()

        // Taille d'une portion dans l'image source
        val portionWidth = imageWidth / gridSize
        val portionHeight = imageHeight / gridSize

        // Rectangle source : quelle partie de l'image extraire
        val srcLeft = (col * portionWidth).toInt()
        val srcTop = (row * portionHeight).toInt()
        val srcRight = ((col + 1) * portionWidth).toInt().coerceAtMost(imageBitmap.width)
        val srcBottom = ((row + 1) * portionHeight).toInt().coerceAtMost(imageBitmap.height)

        // Dessiner la portion extraite sur tout le canvas de la cellule
        drawImage(
            image = imageBitmap,
            srcOffset = IntOffset(srcLeft, srcTop),
            srcSize = IntSize(srcRight - srcLeft, srcBottom - srcTop),
            dstOffset = IntOffset(0, 0),
            dstSize = IntSize(size.width.toInt(), size.height.toInt())
        )
    }
}