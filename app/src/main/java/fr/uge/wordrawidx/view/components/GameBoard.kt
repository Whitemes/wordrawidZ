package fr.uge.wordrawidx.view.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
// import androidx.compose.foundation.border // Peut ne plus être nécessaire si l'image de fond a des bordures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape // Peut encore être utile pour clipper les coins du Box global
import androidx.compose.material3.MaterialTheme // Pour les couleurs et la typographie
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // Pour clipper le Box global
import androidx.compose.ui.graphics.Color // Pour des couleurs de fond de cellule transparentes ou semi-transparentes
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import fr.uge.wordrawidx.model.GameState
import fr.uge.wordrawidx.R // Pour R.drawable

@Composable
fun GameBoard(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val boardSize = gameState.boardSize
    val cellIndicesSnakeOrder = remember(boardSize) { getCellIndicesSnakeOrder(boardSize) }
    val cornerRadius = 24.dp // Rayon pour les coins du plateau si vous voulez les arrondir

    // Box principal pour superposer l'image de fond et la grille
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Garder le plateau carré
            .clip(RoundedCornerShape(cornerRadius)) // Arrondir les coins de l'ensemble du plateau
        // .shadow(8.dp, RoundedCornerShape(cornerRadius)) // Optionnel: ombre globale
    ) {
        // 1. Image de fond pour le plateau
        Image(
            painter = painterResource(id = R.drawable.img_maze_background), // VOTRE IMAGE DE FOND
            contentDescription = "Fond du plateau de jeu",
            contentScale = ContentScale.Crop, // ou FillBounds, selon l'image et l'effet désiré
            modifier = Modifier.fillMaxSize()
        )

        // 2. Grille des cellules par-dessus l'image de fond
        LazyVerticalGrid(
            columns = GridCells.Fixed(boardSize),
            modifier = Modifier
                .fillMaxSize()
                // Padding pour que les cellules ne touchent pas les bords de l'image de fond
                // Ajustez ce padding en fonction du design de votre image de fond
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp), // Espacement entre les lignes de cellules
            horizontalArrangement = Arrangement.spacedBy(4.dp) // Espacement entre les cellules d'une ligne
        ) {
            itemsIndexed(cellIndicesSnakeOrder) { visualGridIndex, gameBoardCellIndex ->
                BoardCell(
                    visualNumber = visualGridIndex + 1,
                    isPlayerOnCell = gameBoardCellIndex == gameState.playerPosition,
                    isPlayerMoving = gameState.isPlayerMoving && gameBoardCellIndex == gameState.playerPosition,
                    isEvenCell = run { // Vous pouvez changer cette logique si les couleurs de cellules ne sont plus utiles
                        val logicalRow = gameBoardCellIndex / boardSize
                        val logicalCol = gameBoardCellIndex % boardSize
                        (logicalRow + logicalCol) % 2 == 0
                    },
                    // Potentiellement passer une image spécifique pour la case si besoin
                    // cellBackgroundImageId = R.drawable.some_cell_image (si la case a sa propre image)
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
    isEvenCell: Boolean, // Peut être utilisé pour une légère variation de teinte ou ignoré
    modifier: Modifier = Modifier
    // cellBackgroundImageId: Int? = null // Optionnel: pour une image de fond par cellule
) {
    // Couleur de fond des cellules:
    // Si vous avez une image de fond pour le plateau, les cellules pourraient être transparentes
    // ou avoir une couleur semi-transparente pour laisser voir l'image de fond du plateau.
    val cellBackgroundColor = if (isEvenCell) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) // Semi-transparent
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f) // Semi-transparent
    }
    // Ou complètement transparent si l'image de fond du plateau a déjà des délimitations de cases
    // val cellBackgroundColor = Color.Transparent

    val scaleFactor by animateFloatAsState(
        targetValue = if (isPlayerMoving && isPlayerOnCell) 1.2f else 1.0f,
        label = "playerScaleAnimation"
    )

    Surface( // Surface peut toujours être utile pour le regroupement et l'élévation
        modifier = modifier
            .aspectRatio(1f)
            // Optionnel: légère bordure pour délimiter les cellules si le fond est transparent
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
        color = cellBackgroundColor, // Appliquer la couleur de fond (potentiellement (semi-)transparente)
        shape = RoundedCornerShape(4.dp), // Légers coins arrondis pour les cellules
        tonalElevation = 0.dp // Peut-être pas d'élévation si le style est plus plat
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            val cellSize = min(constraints.maxWidth.dp, constraints.maxHeight.dp)
            val pawnImageSize = cellSize * 0.7f // Ajustez selon la taille de votre pion

            // Optionnel: Si chaque cellule a sa propre image de fond
            // cellBackgroundImageId?.let { imageId ->
            //     Image(
            //         painter = painterResource(id = imageId),
            //         contentDescription = "Fond de la case",
            //         contentScale = ContentScale.Crop,
            //         modifier = Modifier.fillMaxSize()
            //     )
            // }

            Text(
                text = visualNumber.toString(),
                style = MaterialTheme.typography.labelMedium, // Ajustez la taille/style
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), // Couleur harmonisée
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            )

            if (isPlayerOnCell) {
                Image(
                    painter = painterResource(id = R.drawable.ic_player_pawn), // Votre image de pion
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

// getCellIndicesSnakeOrder reste la même
private fun getCellIndicesSnakeOrder(boardSize: Int): List<Int> =
    List(boardSize * boardSize) { it }
        .chunked(boardSize)
        .mapIndexed { rowIndex, row ->
            if (rowIndex % 2 == 0) row else row.reversed()
        }
        .flatten()