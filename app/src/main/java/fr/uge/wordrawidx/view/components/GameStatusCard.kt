package fr.uge.wordrawidx.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape // Import correct
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.uge.wordrawidx.model.GameState

@Composable
fun GameStatusCard(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier // Le modifier principal de la Card
            .fillMaxWidth()
            .padding(horizontal = 16.dp), // Appliquer le padding au modifier de la Card
        shape = RoundedCornerShape(16.dp), // CORRIGÉ: shape est un paramètre nommé de Card
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Padding interne de la Column
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Position : ${gameState.playerPosition + 1} / ${gameState.totalCells}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (gameState.lastDiceRoll > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dernier lancer : ${gameState.lastDiceRoll}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}