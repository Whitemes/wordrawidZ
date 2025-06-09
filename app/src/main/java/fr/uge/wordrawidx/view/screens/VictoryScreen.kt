package fr.uge.wordrawidx.view.screens

import android.annotation.SuppressLint
import android.content.res.Configuration // IMPORT
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember // Pour la Party des confettis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration // IMPORT
import androidx.compose.ui.tooling.preview.Devices // IMPORT
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.wordrawidx.ui.theme.WordrawidTheme // Pour la Preview
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun VictoryScreen(
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp
    // Déterminer si c'est un grand écran (tablette) ou un écran plus petit
    val isLargeScreen = screenWidthDp >= 600 // Seuil pour "grand écran"

    // Ajustements en fonction de la taille et de l'orientation
    val cardMaxWidthFraction = if (isLargeScreen) 0.6f else if (isLandscape) 0.7f else 0.9f
    val cardCornerRadius = if (isLargeScreen) 24.dp else 16.dp
    val cardElevation = if (isLargeScreen) 12.dp else 8.dp
    val contentPadding = if (isLargeScreen) 32.dp else 24.dp
    val titleTextStyle = if (isLargeScreen) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineMedium
    val bodyTextStyle = if (isLargeScreen) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium
    val buttonTextStyle = if (isLargeScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall
    val buttonHeight = if (isLargeScreen) 60.dp else 56.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            // Un fond subtil pour que les confettis soient plus visibles
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = rememberVictoryConfettiParty(), // Configuration des confettis
        )
        Card(
            shape = RoundedCornerShape(cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
            modifier = Modifier
                .fillMaxWidth(cardMaxWidthFraction)
                .wrapContentHeight() // S'adapte à la hauteur du contenu
        ) {
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Félicitations !",
                    style = titleTextStyle,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(if (isLargeScreen) 24.dp else 16.dp))
                Text(
                    text = "Vous avez gagné la partie.",
                    style = bodyTextStyle,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = if (isLargeScreen) 16.dp else 0.dp)
                )
                Spacer(modifier = Modifier.height(if (isLargeScreen) 32.dp else 24.dp))
                TextButton(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .fillMaxWidth(0.8f) // Bouton un peu moins large que la carte
                        .height(buttonHeight)
                ) {
                    Text(
                        text = "Rejouer",
                        style = buttonTextStyle
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberVictoryConfettiParty(): List<Party> {
    val primaryColorArgb = MaterialTheme.colorScheme.primary.toArgb()
    val secondaryColorArgb = MaterialTheme.colorScheme.secondary.toArgb()
    val tertiaryColorArgb = MaterialTheme.colorScheme.tertiary.toArgb()

    return remember {
        listOf(
            Party(
                emitter = Emitter(duration = 3000, TimeUnit.MILLISECONDS).perSecond(100),
                position = Position.Relative(0.5, 0.0),
                angle = 270,
                spread = 90,
                speed = 15f,
                maxSpeed = 40f,
                damping = 0.9f,
                size = listOf(Size.SMALL, Size.MEDIUM),
                colors = listOf(primaryColorArgb, secondaryColorArgb, tertiaryColorArgb),
                fadeOutEnabled = true,
                timeToLive = 3000L
            )
        )
    }
}

@Preview(name = "Victory Portrait", device = Devices.PHONE)
@Preview(name = "Victory Landscape", device = Devices.PHONE, widthDp = 720, heightDp = 360)
@Preview(name = "Victory Tablet Portrait", device = Devices.TABLET, widthDp = 768, heightDp = 1024)
@Composable
fun VictoryScreenResponsivePreview() {
    WordrawidTheme {
        VictoryScreen(onPlayAgain = {})
    }
}