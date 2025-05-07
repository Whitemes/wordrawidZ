// GameScreen.kt
package fr.uge.wordrawidx.ui.screens

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.uge.wordrawidx.model.GameState
import fr.uge.wordrawidx.ui.components.DiceButton
import fr.uge.wordrawidx.ui.components.GameBoard
import fr.uge.wordrawidx.ui.components.GameStatusCard

/**
 * Écran de jeu principal.
 * @param onWin Callback appelé lorsqu'un joueur atteint la dernière case.
 */
@Composable
fun GameScreen(
    onWin: () -> Unit
) {
    val gameState = remember { GameState(boardSize = 5) }

    // Dégradé d'arrière-plan pour l'écran
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.background
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        color = Color.Transparent // on utilise le brush, pas la couleur solide
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ▶ Titre
            Text(
                text = "Drawid",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // ▶ Plateau de jeu
            GameBoard(
                gameState = gameState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
            )

            // ▶ Statut du jeu
            GameStatusCard(gameState)

            // ▶ Bouton lancer le dé
            DiceButton(
                diceValue = gameState.lastDiceRoll,
                isRolling = gameState.isDiceRolling,
                onRollClick = { gameState.rollDiceAndMove() },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(vertical = 16.dp)
            )

            // ▶ Détection de victoire
            if (gameState.playerPosition == gameState.totalCells - 1) {
                // Appelle le callback onWin plutôt que de reset interne
                onWin()
            }
        }
    }
}
