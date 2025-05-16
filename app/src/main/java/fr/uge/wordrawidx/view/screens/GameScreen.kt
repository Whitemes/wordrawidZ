package fr.uge.wordrawidx.view.screens

import android.content.res.Configuration // IMPORT POUR Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember // MODIFIÉ: remember simple pour gameController
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable // IMPORT POUR rememberSaveable (utilisé pour GameState)
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration // IMPORT POUR LocalConfiguration
import androidx.compose.ui.unit.dp
import fr.uge.wordrawidx.model.GameState
import fr.uge.wordrawidx.controller.GameController
import fr.uge.wordrawidx.view.components.DiceButton
import fr.uge.wordrawidx.view.components.GameBoard
import fr.uge.wordrawidx.view.components.GameStatusCard

@Composable
fun GameScreen(
    onNavigateToVictory: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Utiliser rememberSaveable pour que l'état du jeu persiste lors des rotations
    val gameState = rememberSaveable(saver = GameState.Saver) { GameState(boardSize = 5) }
    val coroutineScope = rememberCoroutineScope()

    // GameController est sans état propre. Un remember simple suffit.
    // Il sera recréé si gameState ou coroutineScope change d'instance,
    // ce qui est le comportement attendu.
    val gameController = remember(gameState, coroutineScope) { // MODIFIÉ: Utilisation de remember avec clés
        GameController(
            gameState = gameState,
            coroutineScope = coroutineScope
        )
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
        if (isLandscape) {
            // Disposition pour le mode PAYSAGE
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    GameBoard(
                        gameState = gameState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GameStatusCard(
                        gameState = gameState,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                        .padding(start = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Drawid MVC",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    DiceButton(
                        diceValue = gameState.lastDiceRoll,
                        isRolling = gameState.isDiceRolling,
                        onRollClick = { gameController.rollDiceAndMove(onWin = onNavigateToVictory) },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }
            }
        } else {
            // Disposition pour le mode PORTRAIT
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
                    onRollClick = { gameController.rollDiceAndMove(onWin = onNavigateToVictory) },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}