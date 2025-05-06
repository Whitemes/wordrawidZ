package fr.uge.wordrawidx.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import fr.uge.wordrawidx.model.GameState
import fr.uge.wordrawidx.ui.theme.BoardBorder
import fr.uge.wordrawidx.ui.theme.CellEven
import fr.uge.wordrawidx.ui.theme.CellOdd

@Composable
fun GameBoard(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val boardSize = 5
    val cellIndices = getCellIndices(boardSize)
    val cornerRadius = 24.dp

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            // 1) shadow et shape
            .shadow(8.dp, RoundedCornerShape(cornerRadius), clip = false)
            .border(2.dp, BoardBorder, RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(boardSize),
            modifier = Modifier
                .fillMaxSize()
                // 2) même couleur de fond pour éviter le liseré
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(cellIndices) { index ->
                BoardCell(
                    index = index,
                    boardSize = boardSize,
                    hasPlayer = index == gameState.playerPosition,
                    isMoving = gameState.isPlayerMoving
                )
            }
        }
    }
}

@Composable
fun BoardCell(
    index: Int,
    boardSize: Int,
    hasPlayer: Boolean,
    isMoving: Boolean
) {
    val cellNumber = index + 1
    val row = index / boardSize
    val col = index % boardSize
    val cellColor = if ((row + col) % 2 == 0) CellEven else CellOdd

    val scale by animateFloatAsState(targetValue = if (isMoving && hasPlayer) 1.2f else 1f)

    Surface(
        modifier = Modifier.aspectRatio(1f),
        color = cellColor,
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cellNumber.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            )
            if (hasPlayer) {
                Surface(
                    modifier = Modifier.size((24 * scale).dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 4.dp
                ) {}
            }
        }
    }
}

private fun getCellIndices(boardSize: Int): List<Int> =
    List(boardSize * boardSize) { it }
        .chunked(boardSize)
        .flatMapIndexed { row, list ->
            if (row % 2 == 0) list else list.reversed()
        }
