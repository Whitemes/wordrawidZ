package fr.uge.wordrawidx.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.uge.wordrawidx.model.GameState
import fr.uge.wordrawidx.controller.GameController
import fr.uge.wordrawidx.view.components.DiceButton
import fr.uge.wordrawidx.view.components.GameBoard
import fr.uge.wordrawidx.view.components.GameStatusCard

@Composable
fun GameScreen(
    onNavigateToVictory: () -> Unit, // Le nom du callback utilisé par AppNavigation
    modifier: Modifier = Modifier
) {
    val gameState = remember { GameState(boardSize = 5) }
    val coroutineScope = rememberCoroutineScope()
    val gameController = remember {
        GameController(
            gameState = gameState,
            coroutineScope = coroutineScope
        )
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.background
        )
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Drawid MVC",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(Modifier.weight(0.1f))

            GameBoard(
                gameState = gameState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            Spacer(Modifier.weight(0.1f))

            GameStatusCard(
                gameState = gameState,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(Modifier.height(16.dp))

            DiceButton(
                diceValue = gameState.lastDiceRoll,
                isRolling = gameState.isDiceRolling,
                // Passe le callback onNavigateToVictory à la méthode du contrôleur
                onRollClick = { gameController.rollDiceAndMove(onWin = onNavigateToVictory) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 16.dp)
            )
        }
    }
}