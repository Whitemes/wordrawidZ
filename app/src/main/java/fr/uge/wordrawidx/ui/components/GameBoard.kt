package fr.uge.wordrawidx.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, BoardBorder, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(boardSize),
            modifier = Modifier.fillMaxSize()
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
    // Calculate cell number (1-based for display)
    val cellNumber = index + 1

    // Determine cell color based on position (checkerboard pattern)
    val rowIndex = index / boardSize
    val colIndex = index % boardSize
    val isEvenCell = (rowIndex + colIndex) % 2 == 0
    val cellColor = if (isEvenCell) CellEven else CellOdd

    // Animation for player token when moving
    val playerScale by animateFloatAsState(
        targetValue = if (isMoving && hasPlayer) 1.2f else 1f,
        label = "playerScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(cellColor)
    ) {
        // Cell number
        Text(
            text = cellNumber.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
        )

        // Player token
        if (hasPlayer) {
            Box(
                modifier = Modifier
                    .size((24 * playerScale).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f), CircleShape)
            )
        }
    }
}

/**
 * Generate cell indices in snake-like pattern (left to right, then right to left, etc.)
 */
private fun getCellIndices(boardSize: Int): List<Int> {
    val totalCells = boardSize * boardSize
    val indices = mutableListOf<Int>()

    for (row in 0 until boardSize) {
        val rowStart = row * boardSize

        // Alternate row direction (left-to-right, right-to-left)
        if (row % 2 == 0) {
            // Left to right
            for (col in 0 until boardSize) {
                indices.add(rowStart + col)
            }
        } else {
            // Right to left
            for (col in boardSize - 1 downTo 0) {
                indices.add(rowStart + col)
            }
        }
    }

    return indices
}