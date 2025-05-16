//package fr.uge.wordrawidx.view.components
//
//import android.annotation.SuppressLint
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.* // Import pour BoxWithConstraints
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.grid.itemsIndexed
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.ElevatedCard
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.min // Pour min(dp, dp)
//import fr.uge.wordrawidx.model.GameState
//
//@Composable
//fun GameBoard(
//    gameState: GameState,
//    modifier: Modifier = Modifier
//) {
//    val boardSize = gameState.boardSize
//    val cellIndicesSnakeOrder = remember(boardSize) { getCellIndicesSnakeOrder(boardSize) }
//    val cornerRadius = 24.dp
//
//    ElevatedCard(
//        modifier = modifier
//            .fillMaxWidth()
//            .aspectRatio(1f)
//            .shadow(8.dp, RoundedCornerShape(cornerRadius), clip = false)
//            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(cornerRadius)),
//        shape = RoundedCornerShape(cornerRadius),
//        colors = CardDefaults.elevatedCardColors(
//            containerColor = MaterialTheme.colorScheme.surface
//        ),
//        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
//    ) {
//        LazyVerticalGrid(
//            columns = GridCells.Fixed(boardSize),
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(4.dp),
//            verticalArrangement = Arrangement.spacedBy(2.dp),
//            horizontalArrangement = Arrangement.spacedBy(2.dp)
//        ) {
//            itemsIndexed(cellIndicesSnakeOrder) { visualGridIndex, gameBoardCellIndex ->
//                BoardCell(
//                    visualNumber = visualGridIndex + 1,
//                    isPlayerOnCell = gameBoardCellIndex == gameState.playerPosition,
//                    isPlayerMoving = gameState.isPlayerMoving && gameBoardCellIndex == gameState.playerPosition,
//                    isEvenCell = run {
//                        val logicalRow = gameBoardCellIndex / boardSize
//                        val logicalCol = gameBoardCellIndex % boardSize
//                        (logicalRow + logicalCol) % 2 == 0
//                    }
//                )
//            }
//        }
//    }
//}
//
//@SuppressLint("UnusedBoxWithConstraintsScope")
//@Composable
//fun BoardCell(
//    visualNumber: Int,
//    isPlayerOnCell: Boolean,
//    isPlayerMoving: Boolean,
//    isEvenCell: Boolean,
//    modifier: Modifier = Modifier
//) {
//    val cellColor = if (isEvenCell) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainerHighest
//
//    val scale by animateFloatAsState(
//        targetValue = if (isPlayerMoving && isPlayerOnCell) 1.2f else 1.0f,
//        label = "playerScaleAnimation"
//    )
//
//    Surface(
//        modifier = modifier.aspectRatio(1f), // Cellule carrée
//        color = cellColor,
//        tonalElevation = 1.dp
//    ) {
//        BoxWithConstraints( // Pour obtenir la taille de la cellule pour le pion
//            modifier = Modifier.fillMaxSize(),
//        ) {
//            // Le `this` ici est `BoxWithConstraintsScope` qui donne `constraints.maxWidth` et `maxHeight`
//            val cellSize = min(constraints.maxWidth.dp, constraints.maxHeight.dp) // La cellule est carrée, donc maxWidth devrait suffire
//            val pionDiameter = cellSize * 0.5f // Pion prend 50% de la taille de la cellule
//
//            Text(
//                text = visualNumber.toString(),
//                style = MaterialTheme.typography.labelSmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                modifier = Modifier
//                    .align(Alignment.TopStart)
//                    .padding(4.dp)
//            )
//            if (isPlayerOnCell) {
//                Surface(
//                    modifier = Modifier
//                        .size(pionDiameter * scale) // Taille adaptative du pion
//                        .align(Alignment.Center), // S'assurer qu'il est centré
//                    shape = CircleShape,
//                    color = MaterialTheme.colorScheme.primary,
//                    tonalElevation = 4.dp
//                ) {}
//            }
//        }
//    }
//}
//
//private fun getCellIndicesSnakeOrder(boardSize: Int): List<Int> =
//    List(boardSize * boardSize) { it }
//        .chunked(boardSize)
//        .mapIndexed { rowIndex, row ->
//            if (rowIndex % 2 == 0) row else row.reversed()
//        }
//        .flatten()


package fr.uge.wordrawidx.view.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image // IMPORT POUR IMAGE
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
// CircleShape n'est plus nécessaire si l'image est déjà ronde ou si vous ne voulez pas la clipper en cercle
// import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow // Si vous voulez garder une ombre
import androidx.compose.ui.graphics.graphicsLayer // IMPORT POUR LA MISE À L'ÉCHELLE AVEC graphicsLayer
import androidx.compose.ui.layout.ContentScale // IMPORT POUR ContentScale
import androidx.compose.ui.res.painterResource // IMPORT POUR painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import fr.uge.wordrawidx.model.GameState
import fr.uge.wordrawidx.R // IMPORT POUR ACCÉDER À R.drawable

@Composable
fun GameBoard(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val boardSize = gameState.boardSize
    val cellIndicesSnakeOrder = remember(boardSize) { getCellIndicesSnakeOrder(boardSize) }
    val cornerRadius = 24.dp

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(8.dp, RoundedCornerShape(cornerRadius), clip = false)
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(boardSize),
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemsIndexed(cellIndicesSnakeOrder) { visualGridIndex, gameBoardCellIndex ->
                BoardCell(
                    visualNumber = visualGridIndex + 1,
                    isPlayerOnCell = gameBoardCellIndex == gameState.playerPosition,
                    isPlayerMoving = gameState.isPlayerMoving && gameBoardCellIndex == gameState.playerPosition,
                    isEvenCell = run {
                        val logicalRow = gameBoardCellIndex / boardSize
                        val logicalCol = gameBoardCellIndex % boardSize
                        (logicalRow + logicalCol) % 2 == 0
                    }
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
    modifier: Modifier = Modifier
) {
    val cellColor = if (isEvenCell) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainerHighest

    val scaleFactor by animateFloatAsState(
        targetValue = if (isPlayerMoving && isPlayerOnCell) 1.2f else 1.0f,
        label = "playerScaleAnimation"
    )

    Surface(
        modifier = modifier.aspectRatio(1f),
        color = cellColor,
        tonalElevation = 1.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            val cellSize = min(constraints.maxWidth.dp, constraints.maxHeight.dp)
            // Ajustez ce ratio pour la taille de votre image de pion
            val pawnImageSize = cellSize * 0.7f // Pion prend 70% de la taille de la cellule

            Text(
                text = visualNumber.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            )
            if (isPlayerOnCell) {
                Image(
                    painter = painterResource(id = R.drawable.ic_player_pawn), // REMPLACEZ par votre ressource
                    contentDescription = "Pion du joueur",
                    contentScale = ContentScale.Fit, // Ou Crop, FillBounds, etc. selon votre image
                    modifier = Modifier
                        .size(pawnImageSize) // Taille adaptative de l'image
                        .align(Alignment.Center) // Centrer l'image dans la cellule
                        // Appliquer la mise à l'échelle avec graphicsLayer pour une meilleure performance
                        .graphicsLayer(
                            scaleX = scaleFactor,
                            scaleY = scaleFactor
                        )
                    // Si vous voulez une ombre sous l'image (optionnel) :
                    // .shadow(2.dp, CircleShape) // Attention, l'ombre sur une image transparente peut être étrange
                )
            }
        }
    }
}

private fun getCellIndicesSnakeOrder(boardSize: Int): List<Int> =
    List(boardSize * boardSize) { it }
        .chunked(boardSize)
        .mapIndexed { rowIndex, row ->
            if (rowIndex % 2 == 0) row else row.reversed()
        }
        .flatten()