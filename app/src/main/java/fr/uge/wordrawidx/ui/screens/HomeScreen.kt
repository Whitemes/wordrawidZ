package fr.uge.wordrawidx.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.wordrawidx.R

/**
 * Écran d'accueil de Wordrawid.
 * Affiche le logo, un bouton Jouer et un bouton Paramètres.
 */
@Composable
fun HomeScreen(
    onPlayClicked: () -> Unit,
    onSettingsClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo du jeu
        Image(
            painter = painterResource(id = R.drawable.ic_wordrawid_logo),
            contentDescription = "Wordrawid Logo",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 24.dp)
        )

        // Bouton "Jouer"
        Button(
            onClick = onPlayClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Jouer",
                style = MaterialTheme.typography.titleLarge
            )
        }

        // Bouton "Paramètres"
        Button(
            onClick = onSettingsClicked,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Paramètres",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onPlayClicked = {},
        onSettingsClicked = {}
    )
}