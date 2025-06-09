package fr.uge.wordrawidx.view.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment // Pour aligner les éléments dans la Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter // Optionnel: pour teinter l'image si besoin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.wordrawidx.R
import fr.uge.wordrawidx.ui.theme.WordrawidTheme

@Composable
fun HomeScreen(
    onPlayClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenHeightDp = configuration.screenHeightDp
    val screenWidthDp = configuration.screenWidthDp

    // Ajustements de taille et style pour la responsivité
    // La taille du logo et du texte du titre peuvent être ajustées ensemble
    val titleSectionHeight = if (isLandscape) screenHeightDp.dp * 0.25f else screenWidthDp.dp * 0.30f
    val logoImageSize = titleSectionHeight * 0.8f // L'image prend 80% de la hauteur allouée à la section titre/logo
    val titleStyle = if (screenWidthDp < 480) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge

    val buttonTextStyle = if (screenWidthDp < 480) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge
    val buttonHeight = if (screenHeightDp < 600) 52.dp else 60.dp
    val buttonMaxWidthFraction = if (isLandscape) 0.5f else 0.8f

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Image de fond (si vous l'utilisez toujours)
        Image(
            painter = painterResource(id = R.drawable.img_home_background), // VOTRE IMAGE DE FOND
            contentDescription = "Fond d'écran d'accueil",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Section Titre avec Logo et Texte
            Row(
                modifier = Modifier
                    .padding(bottom = if (isLandscape) 24.dp else 40.dp),
                verticalAlignment = Alignment.CenterVertically, // Aligner l'image et le texte verticalement
                horizontalArrangement = Arrangement.Center // Centrer la Row elle-même si elle ne prend pas toute la largeur
            ) {
                // Image du Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_wordrawid_logo), // REMPLACEZ PAR VOTRE LOGO
                    contentDescription = "Logo Wordrawid",
                    contentScale = ContentScale.Fit, // Ou .Inside si vous voulez qu'elle ne dépasse jamais sa taille naturelle
                    modifier = Modifier
                        .size(logoImageSize) // Taille ajustée pour le logo
                        .padding(end = 8.dp) // Espace entre le logo et le texte
                )
                // Texte du Titre
                Text(
                    text = "Wordrawid",
                    style = titleStyle,
                    color = MaterialTheme.colorScheme.onBackground, // Ajustez pour le contraste avec le fond
                    textAlign = TextAlign.Start // Ou TextAlign.Center si la Row est centrée
                )
            }

            Button(
                onClick = onPlayClicked,
                modifier = Modifier
                    .fillMaxWidth(buttonMaxWidthFraction)
                    .height(buttonHeight)
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Jouer",
                    style = buttonTextStyle
                )
            }

            Button(
                onClick = onSettingsClicked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier
                    .fillMaxWidth(buttonMaxWidthFraction)
                    .height(buttonHeight)
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Paramètres",
                    style = buttonTextStyle.copy(fontSize = buttonTextStyle.fontSize * 0.9)
                )
            }
        }
    }
}

@Preview(name = "Home Portrait", device = Devices.PHONE, showBackground = true)
@Preview(name = "Home Landscape", device = Devices.PHONE, widthDp = 720, heightDp = 360, showBackground = true)
@Preview(name = "Home Small Portrait", device = Devices.PHONE, widthDp = 360, heightDp = 600, showBackground = true)
@Composable
fun HomeScreenResponsivePreview() {
    WordrawidTheme {
        HomeScreen(
            onPlayClicked = {},
            onSettingsClicked = {}
        )
    }
}