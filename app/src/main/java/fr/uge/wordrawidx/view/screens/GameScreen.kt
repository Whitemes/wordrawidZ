package fr.uge.wordrawidx.view.screens

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    navigationController: NavigationController,
    onNavigateToVictory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("GameScreen", "Recomposition GameScreen")

    // ✅ CRITIQUE : State avec clé stable pour éviter recréation
    val gameState = rememberSaveable(
        saver = GameState.Saver,
        key = "game_state_main" // Clé stable
    ) {
        Log.i("GameScreen", "Création NOUVEAU GameState")
        GameState(boardSize = 5)
    }

    val coroutineScope = rememberCoroutineScope()
    val gameController = remember(gameState) {
        Log.d("GameScreen", "Création GameController")
        GameController(gameState = gameState, coroutineScope = coroutineScope)
    }

    var guessText by remember { mutableStateOf("") }
    var guessResult by remember { mutableStateOf<Boolean?>(null) }

    // ✅ SUPPRIMÉ : Section qui causait la téléportation du pion
    /*
    LaunchedEffect(MiniGameResultHolder.playerPositionBeforeMiniGame) {
        val savedPos = MiniGameResultHolder.playerPositionBeforeMiniGame
        if (savedPos != null && gameState.playerPosition != savedPos) {
            gameState.updatePlayerPositionValue(savedPos)  // ← CAUSAIT LE TÉLÉPORTATION
            MiniGameResultHolder.playerPositionBeforeMiniGame = null
        }
    }
    */

    // ✅ CRITIQUE : Traitement résultat avec clé pour éviter re-exécution
    LaunchedEffect(
        key1 = MiniGameResultHolder.lastChallengedCell,
        key2 = MiniGameResultHolder.lastResultWasWin
    ) {
        val cell = MiniGameResultHolder.lastChallengedCell
        val won = MiniGameResultHolder.lastResultWasWin

        if (cell != null && won != null) {
            Log.i("GameScreen", "Traitement résultat: cell=$cell, won=$won, motActuel='${gameState.mysteryObject?.word}'")

            if (won) {
                gameState.revealCell(cell)
                Log.i("GameScreen", "Case révélée - Mot toujours: '${gameState.mysteryObject?.word}'")
            }

            // Nettoyage immédiat
            MiniGameResultHolder.lastChallengedCell = null
            MiniGameResultHolder.lastResultWasWin = null
        }
    }

    // Gestion nouvelle partie
    LaunchedEffect(MiniGameResultHolder.newGameRequestedFromVictoryOrHome) {
        if (MiniGameResultHolder.newGameRequestedFromVictoryOrHome) {
            Log.i("GameScreen", "Nouvelle partie demandée")
            gameController.startNewGame()
            MiniGameResultHolder.newGameRequestedFromVictoryOrHome = false
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), MaterialTheme.colorScheme.background)
    )

    Surface(modifier = modifier.fillMaxSize().background(backgroundBrush), color = Color.Transparent) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Wordrawid", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))

            GameBoard(
                gameState = gameState,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )

            Spacer(Modifier.height(16.dp))

            DiceButton(
                diceValue = gameState.lastDiceRoll,
                isRolling = gameState.isDiceRolling,
                onRollClick = {
                    Log.d("GameScreen", "Lancer dé - Mot: '${gameState.mysteryObject?.word}', Position: ${gameState.playerPosition}")
                    gameController.rollDiceAndMove(
                        onChallengeRequired = { cellIdx ->
                            if (!gameState.isCellRevealed(cellIdx)) {
                                // ✅ CORRECTION : Sauvegarder la position AVANT le mini-jeu
                                Log.d("GameScreen", "Sauvegarde position avant mini-jeu: ${gameState.playerPosition}")
                                MiniGameResultHolder.playerPositionBeforeMiniGame = gameState.playerPosition
                                MiniGameResultHolder.lastChallengedCell = cellIdx
                                MiniGameResultHolder.lastResultWasWin = null

                                // Choix du mini-jeu (alternez selon vos préférences)
                                if (cellIdx % 2 == 0) {
                                    navigationController.navigateTo(Screen.ShakeGame)
                                } else {
                                    navigationController.navigateTo(Screen.AccelerometerMaze)
                                }
                            } else {
                                Log.d("GameScreen", "Case $cellIdx déjà révélée - Pas de challenge")
                            }
                        },
                        onGameWin = {
                            Log.d("GameScreen", "Partie gagnée !")
                            onNavigateToVictory()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(Modifier.height(16.dp))
            GameStatusCard(gameState = gameState, modifier = Modifier.fillMaxWidth())

            // ✅ DEBUG : Affichage pour vérifier stabilité
            Text("🔍 Mot mystère : ${gameState.mysteryObject?.word ?: "?"}")
            Text("Cases révélées : ${gameState.revealedCells.size}/${gameState.totalCells}")

            Spacer(Modifier.height(16.dp))

            // Interface de devinette
            if (!gameState.isGameWon) {
                OutlinedTextField(
                    value = guessText,
                    onValueChange = { guessText = it },
                    label = { Text("Deviner le mot mystère") }
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val res = gameController.tryToGuessWord(guessText)
                        guessResult = res
                        if (res) {
                            Log.d("GameScreen", "Mot mystère deviné correctement !")
                            onNavigateToVictory()
                        }
                    },
                    enabled = guessText.isNotBlank()
                ) {
                    Text("Proposer")
                }

                guessResult?.let { result ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (result) "Bravo, c'est gagné !" else "Mauvaise réponse…",
                        color = if (result) Color.Green else Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}