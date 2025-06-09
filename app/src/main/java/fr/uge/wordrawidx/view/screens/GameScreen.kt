package fr.uge.wordrawidx.view.screens

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.uge.wordrawidx.controller.NavigationController
import fr.uge.wordrawidx.navigation.Screen
import fr.uge.wordrawidx.utils.MiniGameResultHolder
import fr.uge.wordrawidx.view.components.DiceButton
import fr.uge.wordrawidx.view.components.GameBoard
import fr.uge.wordrawidx.view.components.GameStatusCard
import fr.uge.wordrawidx.viewmodel.GameViewModel
import nl.dionsegijn.konfetti.compose.BuildConfig

@Composable
fun GameScreen(
    navigationController: NavigationController,
    onNavigateToVictory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("GameScreen", "Recomposition GameScreen")

    // ✅ MIGRATION : Utilisation du ViewModel au lieu de rememberSaveable
    val gameViewModel: GameViewModel = viewModel()
    val gameState = gameViewModel.gameState

    // États locaux pour l'interface de devinette
    var guessText by remember { mutableStateOf("") }
    var guessResult by remember { mutableStateOf<Boolean?>(null) }

    // ✅ GESTION RÉSULTAT MINI-JEU - Version simplifiée avec ViewModel
    LaunchedEffect(MiniGameResultHolder.lastResultWasWin) {
        val won = MiniGameResultHolder.lastResultWasWin

        if (won != null) {
            Log.i("GameScreen", "Traitement résultat mini-jeu via ViewModel: won=$won")

            // ✨ Le ViewModel gère automatiquement la cellule challengée et la position
            gameViewModel.processMiniGameResult(won)

            // Nettoyage MiniGameResultHolder
            MiniGameResultHolder.lastResultWasWin = null

            Log.i("GameScreen", "Résultat traité - État actuel: Mot='${gameState.mysteryObject?.word}', Position=${gameState.playerPosition}, Cases révélées=${gameState.revealedCells.size}")
        }
    }

    // ✅ GESTION NOUVELLE PARTIE
    LaunchedEffect(MiniGameResultHolder.newGameRequestedFromVictoryOrHome) {
        if (MiniGameResultHolder.newGameRequestedFromVictoryOrHome) {
            Log.i("GameScreen", "Nouvelle partie demandée via ViewModel")
            gameViewModel.startNewGame()
            MiniGameResultHolder.newGameRequestedFromVictoryOrHome = false

            // Reset des états locaux de l'écran
            guessText = ""
            guessResult = null

            Log.i("GameScreen", "Nouvelle partie initialisée - Nouveau mot: '${gameState.mysteryObject?.word}'")
        }
    }

    // Configuration d'écran et style
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.background
        )
    )

    Surface(
        modifier = modifier.fillMaxSize().background(backgroundBrush),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titre du jeu
            Text(
                text = "Wordrawid",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Plateau de jeu principal
            GameBoard(
                gameState = gameState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ BOUTON DÉ - Utilisation du ViewModel
            DiceButton(
                diceValue = gameState.lastDiceRoll,
                isRolling = gameState.isDiceRolling,
                onRollClick = {
                    Log.d("GameScreen", "Lancer dé - ViewModel - Mot: '${gameState.mysteryObject?.word}', Position: ${gameState.playerPosition}")

                    gameViewModel.rollDiceAndMove(
                        onChallengeRequired = { cellIdx ->
                            // ✅ PRÉPARATION MINI-JEU via ViewModel
                            if (gameViewModel.prepareMiniGameChallenge(cellIdx)) {
                                Log.i("GameScreen", "Mini-jeu préparé pour cellule $cellIdx - Position sauvegardée: ${gameState.playerPosition}")

                                // ✅ TEMPORAIRE : Seulement ShakeGame activé
                                Log.d("GameScreen", "Navigation vers ShakeGame (labyrinthe désactivé)")
                                navigationController.navigateTo(Screen.ShakeGame)

                                // ❌ DÉSACTIVÉ TEMPORAIREMENT : AccelerometerMaze
                                // if (cellIdx % 2 == 0) {
                                //     Log.d("GameScreen", "Navigation vers ShakeGame (cellule paire)")
                                //     navigationController.navigateTo(Screen.ShakeGame)
                                // } else {
                                //     Log.d("GameScreen", "Navigation vers AccelerometerMaze (cellule impaire)")
                                //     navigationController.navigateTo(Screen.AccelerometerMaze)
                                // }
                            } else {
                                Log.d("GameScreen", "Case $cellIdx déjà révélée - Pas de challenge nécessaire")
                            }
                        },
                        onGameWin = {
                            Log.d("GameScreen", "Partie gagnée via ViewModel !")
                            onNavigateToVictory()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Carte de statut du jeu
            GameStatusCard(
                gameState = gameState,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ DEBUG CARD - Affichage de l'état ViewModel (removable en production)
            if (BuildConfig.DEBUG) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "🔍 Debug ViewModel",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Mot mystère : ${gameState.mysteryObject?.word ?: "Non défini"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Cases révélées : ${gameState.revealedCells.size}/${gameState.totalCells}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Position sauvée : ${gameViewModel.playerPositionBeforeMiniGame ?: "Aucune"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Cellule en attente : ${gameViewModel.lastChallengedCell ?: "Aucune"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "État jeu gagné : ${gameState.isGameWon}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ INTERFACE DE DEVINETTE - Utilisation du ViewModel
            if (!gameState.isGameWon) {
                OutlinedTextField(
                    value = guessText,
                    onValueChange = { guessText = it },
                    label = { Text("Deviner le mot mystère") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val result = gameViewModel.tryToGuessWord(guessText)
                        guessResult = result

                        Log.d("GameScreen", "Tentative de devinette: '$guessText' -> $result")

                        if (result) {
                            Log.d("GameScreen", "Mot mystère deviné correctement via ViewModel !")
                            onNavigateToVictory()
                        }
                    },
                    enabled = guessText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Proposer")
                }

                // Affichage du résultat de la devinette
                guessResult?.let { result ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (result) "🎉 Bravo, c'est gagné !" else "❌ Mauvaise réponse, essayez encore…",
                        color = if (result) Color.Green else Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // Message de victoire (ne devrait pas apparaître car navigation vers VictoryScreen)
                Text(
                    text = "🏆 Partie terminée ! Félicitations !",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Green
                )
            }

            // ✅ BOUTONS DEBUG (seulement en mode développement)
            if (BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = {
                            gameViewModel.revealAllCellsForDebug()
                            Log.d("GameScreen", "DEBUG: Toutes les cases révélées")
                        }
                    ) {
                        Text("🔧 Révéler Tout")
                    }

                    TextButton(
                        onClick = {
                            gameViewModel.startNewGame()
                            guessText = ""
                            guessResult = null
                            Log.d("GameScreen", "DEBUG: Nouvelle partie forcée")
                        }
                    ) {
                        Text("🔄 Nouveau Jeu")
                    }
                }
            }
        }
    }
}