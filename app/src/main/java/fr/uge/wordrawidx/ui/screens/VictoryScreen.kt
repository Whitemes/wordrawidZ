// VictoryScreen.kt
package fr.uge.wordrawidx.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit

/**
 * Version responsive de VictoryScreen adaptée à différentes tailles d'écran.
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun VictoryScreen(
    onPlayAgain: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isLargeScreen = screenWidthDp >= 600

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Effet confetti en arrière-plan
        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = listOf(
                Party(
                    emitter = Emitter(duration = 2000, TimeUnit.MILLISECONDS).max(200),
                    position = Position.Relative(0.5, 0.0),
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.toArgb(),
                        MaterialTheme.colorScheme.secondary.toArgb(),
                        MaterialTheme.colorScheme.error.toArgb()
                    ),
                    speed = 0f,
                    maxSpeed = 30f,
                    damping = 0.9f,
                    spread = 360,
                    size = listOf(Size.SMALL, Size.MEDIUM)
                )
            )
        )

        // Carte de message de victoire
        Card(
            shape = RoundedCornerShape(if (isLargeScreen) 24.dp else 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isLargeScreen) 12.dp else 8.dp),
            modifier = Modifier
                .fillMaxWidth(if (isLargeScreen) 0.6f else 0.9f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(if (isLargeScreen) 32.dp else 24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Félicitations !",
                    style = if (isLargeScreen) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(if (isLargeScreen) 24.dp else 16.dp))
                Text(
                    text = "Vous avez gagné la partie.",
                    style = if (isLargeScreen) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = if (isLargeScreen) 16.dp else 0.dp)
                )
                Spacer(modifier = Modifier.height(if (isLargeScreen) 32.dp else 24.dp))
                TextButton(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .defaultMinSize(minWidth = if (isLargeScreen) 200.dp else 150.dp)
                        .padding(vertical = if (isLargeScreen) 12.dp else 8.dp)
                ) {
                    Text(
                        text = "Rejouer",
                        style = if (isLargeScreen) MaterialTheme.typography.titleLarge else MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(name = "Petit écran", widthDp = 320, heightDp = 480, showBackground = true)
@Composable
fun VictoryScreenPreview_Small() {
    VictoryScreen(onPlayAgain = {})
}

@Preview(name = "Grand écran", widthDp = 700, heightDp = 600, showBackground = true)
@Composable
fun VictoryScreenPreview_Large() {
    VictoryScreen(onPlayAgain = {})
}
