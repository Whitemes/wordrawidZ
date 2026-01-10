package fr.uge.wordrawidx.view.screens

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.uge.wordrawidx.controller.NavigationController
import fr.uge.wordrawidx.navigation.Screen
import fr.uge.wordrawidx.utils.MiniGameResultHolder
import fr.uge.wordrawidx.view.components.DiceButton
import fr.uge.wordrawidx.view.components.GameBoard
import fr.uge.wordrawidx.view.components.GameStatusCard
import fr.uge.wordrawidx.viewmodel.GameViewModel
import fr.uge.wordrawidx.viewmodel.GameViewModelFactory
import nl.dionsegijn.konfetti.compose.BuildConfig
import kotlin.random.Random

@Composable
fun GameScreen(
    navigationController: NavigationController,
    onNavigateToVictory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("GameScreen", "Recomposition GameScreen")

    val context = LocalContext.current

    // ✅ NOUVEAU : ViewModel avec Factory pour injection Repository
    val gameViewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(context)
    )
    val gameState = gameViewModel.gameState

    // États locaux pour l'interface de devinette (inchangés)
    var guessText by remember { mutableStateOf("") }
    var guessResult by remember { mutableStateOf<Boolean?>(null) }

    // ✅ LaunchedEffect pour statistiques repository (debug)
    LaunchedEffect(Unit) {
        gameViewModel.getDatabaseStats()
    }

    // ✅ GESTION RÉSULTAT MINI-JEU (inchangé)
    LaunchedEffect(MiniGameResultHolder.lastResultWasWin) {
        val won = MiniGameResultHolder.lastResultWasWin

        if (won != null) {
            Log.i("GameScreen", "Traitement résultat mini-jeu via ViewModel: won=$won")
            gameViewModel.processMiniGameResult(won)
            MiniGameResultHolder.lastResultWasWin = null
        }
    }

    // ✅ GESTION NOUVELLE PARTIE (inchangé)
    LaunchedEffect(MiniGameResultHolder.newGameRequestedFromVictoryOrHome) {
        if (MiniGameResultHolder.newGameRequestedFromVictoryOrHome) {
            Log.i("GameScreen", "Nouvelle partie demandée via ViewModel")
            gameViewModel.startNewGame()
            MiniGameResultHolder.newGameRequestedFromVictoryOrHome = false

            guessText = ""
            guessResult = null
        }
    }

    // Configuration d'écran et style (inchangé)
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

            // ✅ MULTIJOUEUR : Indicateur de tour
            gameState.currentPlayer?.let { currentPlayer ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = currentPlayer.color.copy(alpha = 0.2f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Pion coloré du joueur actuel
                        Canvas(modifier = Modifier.size(24.dp)) {
                            drawCircle(color = currentPlayer.color)
                            drawCircle(
                                color = Color.White,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Tour de ${currentPlayer.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Score: ${currentPlayer.score}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Plateau de jeu principal
            GameBoard(
                gameState = gameState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ BOUTON DÉ (inchangé)
            DiceButton(
                diceValue = gameState.lastDiceRoll,
                isRolling = gameState.isDiceRolling,
                onRollClick = {
                    Log.d("GameScreen", "Lancer dé - ViewModel Repository - Mot: '${gameState.mysteryObject?.word}', Position: ${gameState.playerPosition}")

                    gameViewModel.rollDiceAndMove(
                        onChallengeRequired = { cellIdx ->
                            if (gameViewModel.prepareMiniGameChallenge(cellIdx)) {
                                Log.i("GameScreen", "Mini-jeu préparé pour cellule $cellIdx - Position sauvegardée: ${gameState.playerPosition}")

                                // ✅ TEMPORAIRE : Seulement ShakeGame activé
//                                Log.d("GameScreen", "Navigation vers ShakeGame (labyrinthe désactivé)")
//                                navigationController.navigateTo(Screen.ShakeGame)
                                // Choix aléatoire du mini-jeu (Shake ou Labyrinthe)
                                val nextScreen = if (Random.nextBoolean()) {
                                    Screen.ShakeGame
                                } else {
                                    Screen.AccelerometerMaze
                                }
                                Log.d("GameScreen", "Navigation vers $nextScreen (choix aléatoire)")
                                navigationController.navigateTo(nextScreen)

                            } else {
                                Log.d("GameScreen", "Case $cellIdx déjà révélée - Pas de challenge nécessaire")
                            }
                        },
                        onGameWin = {
                            Log.d("GameScreen", "Partie gagnée via ViewModel Repository !")
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

            // ✅ DEBUG CARD - Avec informations Repository
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
                            text = "🔍 Debug ViewModel + Repository",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Mot mystère : ${gameState.mysteryObject?.word ?: "Non défini"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Source : ${if (gameState.mysteryObject != null) "Repository Backend" else "Fallback"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (gameState.mysteryObject != null) Color.Green else Color.Yellow
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
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ INTERFACE DE DEVINETTE (inchangée)
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
                            Log.d("GameScreen", "Mot mystère deviné correctement via ViewModel Repository !")
                            onNavigateToVictory()
                        }
                    },
                    enabled = guessText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Proposer")
                }

                guessResult?.let { result ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (result) "🎉 Bravo, c'est gagné !" else "❌ Mauvaise réponse, essayez encore…",
                        color = if (result) Color.Green else Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    text = "🏆 Partie terminée ! Félicitations !",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Green
                )
            }

            // ✅ BOUTONS DEBUG (avec nouvelles fonctionnalités Repository)
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
                            Log.d("GameScreen", "DEBUG: Nouvelle partie forcée avec Repository")
                        }
                    ) {
                        Text("🔄 Nouveau Jeu")
                    }
                }
            }
        }
    }
}