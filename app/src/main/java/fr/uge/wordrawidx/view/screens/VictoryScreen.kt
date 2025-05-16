package fr.uge.wordrawidx.view.screens // MODIFIÉ: Package

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb // Nécessaire pour Konfetti
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.dionsegijn.konfetti.compose.KonfettiView // Assurez-vous que l'import est correct
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit
import fr.uge.wordrawidx.ui.theme.WordrawidTheme // Pour la Preview

@SuppressLint("ConfigurationScreenWidthHeight") // Gardé si vous utilisez explicitement width/height
@Composable
fun VictoryScreen(
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier // Bonne pratique
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isLargeScreen = screenWidthDp >= 600 // Seuil pour grand écran

    Box(
        modifier = modifier // Utilisation du modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)), // Léger fond pour voir les confettis
        contentAlignment = Alignment.Center
    ) {
        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = rememberVictoryConfettiParty(), // Externalisé pour la lisibilité
        )
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // Pour centrer le contenu si la carte est grande
            ) {
                Text(
                    text = "Félicitations !",
                    style = if (isLargeScreen) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineLarge, // Styles ajustés
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(if (isLargeScreen) 24.dp else 16.dp))
                Text(
                    text = "Vous avez gagné la partie.",
                    style = if (isLargeScreen) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium, // Styles ajustés
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = if (isLargeScreen) 16.dp else 0.dp)
                )
                Spacer(modifier = Modifier.height(if (isLargeScreen) 32.dp else 24.dp))
                TextButton(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        // .defaultMinSize(minWidth = if (isLargeScreen) 200.dp else 150.dp) // Peut être géré par le padding
                        .height(56.dp) // Hauteur standard
                        .fillMaxWidth(0.8f) // Largeur du bouton
                ) {
                    Text(
                        text = "Rejouer",
                        style = if (isLargeScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall // Styles ajustés
                        // La couleur est gérée par TextButton et MaterialTheme
                    )
                }
            }
        }
    }
}

// Externalisation de la configuration des confettis pour la lisibilité
@Composable
private fun rememberVictoryConfettiParty(): List<Party> {
    // Utiliser les couleurs du thème actuel pour les confettis
    val primaryColorArgb = MaterialTheme.colorScheme.primary.toArgb()
    val secondaryColorArgb = MaterialTheme.colorScheme.secondary.toArgb()
    val tertiaryColorArgb = MaterialTheme.colorScheme.tertiary.toArgb() // Autre couleur pour la variété

    return remember { // `remember` pour éviter la recréation à chaque recomposition
        listOf(
            Party(
                emitter = Emitter(duration = 3000, TimeUnit.MILLISECONDS).perSecond(100), // Plus longue durée, plus de confettis
                position = Position.Relative(0.5, 0.0), // Commence en haut au centre
                // Angle de diffusion pour couvrir l'écran
                angle = 270, // Vers le bas
                spread = 90,  // Étalement horizontal
                speed = 15f,   // Vitesse initiale
                maxSpeed = 40f,
                damping = 0.9f,
                size = listOf(Size.SMALL, Size.MEDIUM),
                colors = listOf(primaryColorArgb, secondaryColorArgb, tertiaryColorArgb),
                fadeOutEnabled = true, // Pour une disparition en douceur
                timeToLive = 3000L
            )
        )
    }
}


@Preview(name = "Victory Screen Small", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun VictoryScreenPreview_Small() {
    WordrawidTheme {
        VictoryScreen(onPlayAgain = {})
    }
}

@Preview(name = "Victory Screen Large", widthDp = 800, heightDp = 600, showBackground = true)
@Composable
fun VictoryScreenPreview_Large() {
    WordrawidTheme {
        VictoryScreen(onPlayAgain = {})
    }
}