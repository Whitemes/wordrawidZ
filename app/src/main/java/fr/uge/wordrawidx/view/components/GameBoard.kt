package fr.uge.wordrawidx.view.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.painter.Painter
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
        // Plateau de fond
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

            // Affichage de l'indice si case révélée
            if (revealedCell != null) {
                if (revealedCell.hintType == CaseHintType.SEMANTIC_WORD && revealedCell.hintContent.isNotBlank()) {
                    Text(
                        text = revealedCell.hintContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else if (revealedCell.hintType == CaseHintType.IMAGE && mysteryImageRes != null) {
                    // 🔥 NOUVELLE VERSION AVEC DÉCOUPAGE PRÉCIS
                    PortionOfImageInCellFromBitmap(
                        imageRes = mysteryImageRes,
                        portionIndex = cellIndex,
                        gridSize = boardSize,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    )
                }
                else {
                    Text(
                        text = "⚠️",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                Text(
                    text = visualNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                )
            }

            // Le pion toujours par-dessus tout
            if (isPlayerOnCell) {
                Image(
                    painter = painterResource(id = R.drawable.ic_player_pawn),
                    contentDescription = "Pion du joueur",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(pawnImageSize)
                        .align(Alignment.Center)
                        .graphicsLayer(
                            scaleX = scaleFactor,
                            scaleY = scaleFactor
                        )
                )
            }
        }
    }
}

/**
 * 🔥 NOUVELLE FONCTION - Version avec ressource d'image directement
 */
@Composable
fun PortionOfImageInCellFromBitmap(
    imageRes: Int,
    portionIndex: Int,
    gridSize: Int = 5,
    modifier: Modifier = Modifier
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
 * 🔥 NOUVELLE FONCTION - Version avec ImageBitmap
 */
@Composable
fun PortionOfImageInCellBitmap(
    imageBitmap: ImageBitmap,
    portionIndex: Int,
    gridSize: Int = 5,
    modifier: Modifier = Modifier
) {
    // Calcul des coordonnées de la cellule dans la grille
    val row = portionIndex / gridSize
    val col = portionIndex % gridSize

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Dimensions de l'image source
        val imageWidth = imageBitmap.width.toFloat()
        val imageHeight = imageBitmap.height.toFloat()

        // Calcul des dimensions d'une portion dans l'image source
        val portionWidth = imageWidth / gridSize
        val portionHeight = imageHeight / gridSize

        // Rectangle source : la portion à extraire de l'image
        val srcLeft = (col * portionWidth).toInt()
        val srcTop = (row * portionHeight).toInt()
        val srcRight = ((col + 1) * portionWidth).toInt().coerceAtMost(imageBitmap.width)
        val srcBottom = ((row + 1) * portionHeight).toInt().coerceAtMost(imageBitmap.height)

        // Rectangle de destination : où dessiner sur le canvas (toute la surface)
        val dstLeft = 0f
        val dstTop = 0f
        val dstRight = canvasWidth
        val dstBottom = canvasHeight

        // Dessin avec drawImage pour un découpage précis
        drawImage(
            image = imageBitmap,
            srcOffset = IntOffset(srcLeft, srcTop),
            srcSize = IntSize(srcRight - srcLeft, srcBottom - srcTop),
            dstOffset = IntOffset(dstLeft.toInt(), dstTop.toInt()),
            dstSize = IntSize((dstRight - dstLeft).toInt(), (dstBottom - dstTop).toInt())
        )
    }
}

/**
 * 📚 ANCIENNE FONCTION - Gardée pour référence/compatibilité
 * Peut être supprimée une fois que vous avez testé la nouvelle version
 */
@Composable
fun PortionOfImageInCell(
    painter: Painter,
    portionIndex: Int,
    gridSize: Int = 5,
    modifier: Modifier = Modifier
) {
    val row = portionIndex / gridSize
    val col = portionIndex % gridSize

    Box(modifier = modifier.clipToBounds()) {
        Image(
            painter = painter,
            contentDescription = "Portion de l'image mystère",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    // Décalage proportionnel en fonction de la grille
                    translationX = -col * size.width
                    translationY = -row * size.height

                    // Agrandit l'image pour qu'on voie la portion
                    scaleX = gridSize.toFloat()
                    scaleY = gridSize.toFloat()
                }
        )
    }
}