package fr.uge.wordrawidx.view.screens

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import fr.uge.wordrawidx.model.GameState
import fr.uge.wordrawidx.controller.GameController
import fr.uge.wordrawidx.controller.NavigationController
import fr.uge.wordrawidx.navigation.Screen
import fr.uge.wordrawidx.utils.MiniGameResultHolder
import fr.uge.wordrawidx.view.components.DiceButton
import fr.uge.wordrawidx.view.components.GameBoard
import fr.uge.wordrawidx.view.components.GameStatusCard

@Composable
fun GameScreen(
    navigationController: NavigationController,
    onNavigateToVictory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("GameScreen", "Composing GameScreen...")

    val gameState = rememberSaveable(saver = GameState.Saver) {
        Log.d("GameScreen", "Creating/Restoring GameState instance for GameScreen.")
        GameState(boardSize = 5)
    }
    val coroutineScope = rememberCoroutineScope()
    val gameController = remember(gameState, coroutineScope) {
        Log.d("GameScreen", "Creating/Recreating GameController for GameState id: ${System.identityHashCode(gameState)}")
        GameController(
            gameState = gameState,
            coroutineScope = coroutineScope
        )
    }

    // Nouveau : restauration de la position du pion si besoin (pour corriger le bug)
    LaunchedEffect(MiniGameResultHolder.playerPositionBeforeMiniGame) {
        val savedPos = MiniGameResultHolder.playerPositionBeforeMiniGame
        if (savedPos != null && gameState.playerPosition != savedPos) {
            Log.i("GameScreen", "Restoring pawn position after mini-game: $savedPos")
            gameState.updatePlayerPositionValue(savedPos)
            MiniGameResultHolder.playerPositionBeforeMiniGame = null
        }
    }

    // Gérer la demande de nouvelle partie
    LaunchedEffect(MiniGameResultHolder.newGameRequestedFromVictoryOrHome) {
        if (MiniGameResultHolder.newGameRequestedFromVictoryOrHome) {
            Log.i("GameScreen", "New game was explicitly requested. Resetting GameState.")
            gameController.startNewGame()
            MiniGameResultHolder.newGameRequestedFromVictoryOrHome = false
        }
    }

    // Traiter le résultat du mini-jeu
    LaunchedEffect(MiniGameResultHolder.lastResultWasWin, MiniGameResultHolder.lastChallengedCell) {
        val cell = MiniGameResultHolder.lastChallengedCell
        val won = MiniGameResultHolder.lastResultWasWin

        if (cell != null && won != null) {
            Log.i("GameScreen", "Processing MiniGameResult - Cell: $cell, Won: $won. CurrentPlayerPos in GameState: ${gameState.playerPosition}")
            if (cell == gameState.playerPosition) {
                gameController.processMiniGameResult(won, cell)
            } else {
                Log.e("GameScreen", "CRITICAL MISMATCH! MiniGameResult for cell $cell but GameState.playerPosition is ${gameState.playerPosition}. This indicates a state restoration issue or logic error.")
                gameController.processMiniGameResult(won, cell)
            }
            MiniGameResultHolder.lastChallengedCell = null
            MiniGameResultHolder.lastResultWasWin = null
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), MaterialTheme.colorScheme.background)
    )

    Log.d("GameScreen", "UI Rendering. PlayerPos in GameState: ${gameState.playerPosition}")

    Surface(modifier = modifier.fillMaxSize().background(backgroundBrush), color = Color.Transparent) {
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    GameBoard(gameState = gameState, modifier = Modifier.fillMaxWidth().weight(1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    GameStatusCard(gameState = gameState, modifier = Modifier.fillMaxWidth())
                }
                Column(
                    modifier = Modifier.weight(0.6f).fillMaxHeight().padding(start = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Drawid MVC", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 24.dp))
                    DiceButton(
                        diceValue = gameState.lastDiceRoll,
                        isRolling = gameState.isDiceRolling,
                        onRollClick = {
                            Log.d("GameScreen_Landscape", "DiceButton clicked. Player is at ${gameState.playerPosition}")
                            gameController.rollDiceAndMove(
                                onChallengeRequired = { landedPosition ->
                                    Log.d("GameScreen_Landscape", "Challenge for cell: $landedPosition. GS.playerPos: ${gameState.playerPosition}")
                                    // Correction : sauvegarder la position du pion AVANT de partir dans le mini-jeu
                                    MiniGameResultHolder.playerPositionBeforeMiniGame = gameState.playerPosition
                                    MiniGameResultHolder.lastChallengedCell = landedPosition
                                    MiniGameResultHolder.lastResultWasWin = null
                                    navigationController.navigateTo(Screen.AccelerometerMaze)
                                },
                                onGameWin = onNavigateToVictory
                            )
                        },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }
            }
        } else { // Portrait
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Drawid MVC", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
                Spacer(Modifier.weight(0.1f))
                GameBoard(gameState = gameState, modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp))
                Spacer(Modifier.weight(0.1f))
                GameStatusCard(gameState = gameState, modifier = Modifier.fillMaxWidth(0.9f))
                Spacer(Modifier.height(16.dp))
                DiceButton(
                    diceValue = gameState.lastDiceRoll,
                    isRolling = gameState.isDiceRolling,
                    onRollClick = {
                        Log.d("GameScreen_Portrait", "DiceButton clicked. Player is at ${gameState.playerPosition}")
                        gameController.rollDiceAndMove(
                            onChallengeRequired = { landedPosition ->
                                Log.d("GameScreen_Portrait", "Challenge for cell: $landedPosition. GS.playerPos: ${gameState.playerPosition}")
                                // Correction : sauvegarder la position du pion AVANT de partir dans le mini-jeu
                                MiniGameResultHolder.playerPositionBeforeMiniGame = gameState.playerPosition
                                MiniGameResultHolder.lastChallengedCell = landedPosition
                                MiniGameResultHolder.lastResultWasWin = null
                                navigationController.navigateTo(Screen.AccelerometerMaze)
                            },
                            onGameWin = onNavigateToVictory
                        )
                    },
                    modifier = Modifier.fillMaxWidth(0.8f).padding(bottom = 16.dp)
                )
            }
        }
    }
}
