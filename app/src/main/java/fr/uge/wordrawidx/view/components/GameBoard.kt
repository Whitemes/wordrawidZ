package fr.uge.wordrawidx.view.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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

            // Affichage de l’indice si case révélée
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
                    PortionOfImageInCell(
                        painter = painterResource(id = mysteryImageRes),
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
 * Découpe et affiche la portion de l'image mystère correspondant à la cellule du plateau.
 */
@Composable
fun PortionOfImageInCell(
    painter: Painter,
    portionIndex: Int,
    gridSize: Int = 5,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val row = portionIndex / gridSize
    val col = portionIndex % gridSize
    val portionSize = 1f / gridSize

    Box(
        modifier = modifier.clipToBounds()
    ) {
        Image(
            painter = painter,
            contentDescription = "Portion de l'image mystère",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    translationX = -col * size.width * portionSize
                    translationY = -row * size.height * portionSize
                    scaleX = gridSize.toFloat()
                    scaleY = gridSize.toFloat()
                }
        )
    }
}


//private fun getCellIndicesSnakeOrder(boardSize: Int): List<Int> =
//    List(boardSize * boardSize) { it }
//        .chunked(boardSize)
//        .mapIndexed { rowIndex, row ->
//            if (rowIndex % 2 == 0) row else row.reversed()
//        }
//        .flatten()
