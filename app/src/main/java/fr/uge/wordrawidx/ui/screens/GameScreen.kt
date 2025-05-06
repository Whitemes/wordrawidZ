package fr.uge.wordrawidx.ui.screens


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.uge.wordrawidx.model.GameState
import fr.uge.wordrawidx.ui.components.DiceButton
import fr.uge.wordrawidx.ui.components.GameBoard

@Composable
fun GameScreen() {
    // Remember the game state
    val gameState = remember { GameState(boardSize = 5) }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Game title
            Text(
                text = "VodyDrawid",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Game board
            GameBoard(
                gameState = gameState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Game status
            GameStatusCard(gameState)

            Spacer(modifier = Modifier.height(16.dp))

            // Dice roll button
            DiceButton(
                diceValue = gameState.lastDiceRoll,
                isRolling = gameState.isDiceRolling,
                onRollClick = { gameState.rollDiceAndMove() },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            // Win message
            if (gameState.playerPosition == gameState.totalCells - 1) {
                WinMessage(
                    onPlayAgain = { gameState.resetGame() }
                )
            }
        }
    }
}

@Composable
fun GameStatusCard(gameState: GameState) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Position status
            Text(
                text = "Position: ${gameState.playerPosition + 1} / ${gameState.totalCells}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Last roll
            if (gameState.lastDiceRoll > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Last roll: ${gameState.lastDiceRoll}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WinMessage(onPlayAgain: () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "You Win!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onPlayAgain) {
                        Text(
                            text = "Play Again",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}