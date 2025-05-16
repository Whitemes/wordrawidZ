package fr.uge.wordrawidx.view.components // MODIFIÉ: Package

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed // Utiliser itemsIndexed pour plus de flexibilité
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember // Pour getCellIndices
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import fr.uge.wordrawidx.model.GameState

@Composable
fun GameBoard(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val boardSize = gameState.boardSize // Utiliser la taille du gameState
    val cellIndicesSnakeOrder = remember(boardSize) { getCellIndicesSnakeOrder(boardSize) }
    val cornerRadius = 24.dp

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(8.dp, RoundedCornerShape(cornerRadius), clip = false) // Ombre externe
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(cornerRadius)), // Bordure thématique
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface // Fond de la carte
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp) // L'ombre est déjà gérée par le modificateur .shadow
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(boardSize),
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp), // Padding interne pour les cellules
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // cellIndicesSnakeOrder contient les indices de case (0 à N-1) dans l'ordre visuel du serpentin
            itemsIndexed(cellIndicesSnakeOrder) { visualGridIndex, gameBoardCellIndex ->
                BoardCell(
                    visualNumber = visualGridIndex + 1, // Le numéro affiché (1 à N)
                    isPlayerOnCell = gameBoardCellIndex == gameState.playerPosition,
                    isPlayerMoving = gameState.isPlayerMoving && gameBoardCellIndex == gameState.playerPosition,
                    // Pour la couleur, on se base sur la position logique (non-serpentin)
                    // Ou on peut simplifier en utilisant visualGridIndex si l'alternance est purement visuelle
                    isEvenCell = run { // Calcul de la parité pour la couleur de fond
                        val logicalRow = gameBoardCellIndex / boardSize
                        val logicalCol = gameBoardCellIndex % boardSize
                        (logicalRow + logicalCol) % 2 == 0
                    }
                )
            }
        }
    }
}

@Composable
fun BoardCell(
    visualNumber: Int,
    isPlayerOnCell: Boolean,
    isPlayerMoving: Boolean,
    isEvenCell: Boolean,
    modifier: Modifier = Modifier
) {
    val cellColor = if (isEvenCell) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainerHighest // Couleurs thématiques

    val scale by animateFloatAsState(
        targetValue = if (isPlayerMoving && isPlayerOnCell) 1.2f else 1.0f, // Animation uniquement si le joueur est sur la case et bouge
        label = "playerScaleAnimation"
    )

    Surface(
        modifier = modifier.aspectRatio(1f),
        color = cellColor,
        tonalElevation = 1.dp // Légère élévation pour chaque cellule
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center // Centre le pion
        ) {
            Text(
                text = visualNumber.toString(),
                style = MaterialTheme.typography.labelSmall, // Plus petit pour ne pas surcharger
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            )
            if (isPlayerOnCell) {
                Surface(
                    modifier = Modifier.size((24 * scale).dp), // Taille du pion, animé
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary, // Couleur du pion
                    tonalElevation = 4.dp // Ombre pour le pion
                ) {}
            }
        }
    }
}

// Renommée pour clarifier qu'elle retourne les indices dans l'ordre du serpentin
private fun getCellIndicesSnakeOrder(boardSize: Int): List<Int> =
    List(boardSize * boardSize) { it } // Crée une liste de 0 à (N*N - 1)
        .chunked(boardSize) // Sépare en lignes
        .mapIndexed { rowIndex, row ->
            if (rowIndex % 2 == 0) row else row.reversed() // Inverse les lignes impaires
        }
        .flatten() // Remet tout dans une seule liste