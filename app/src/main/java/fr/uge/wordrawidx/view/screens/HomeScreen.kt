package fr.uge.wordrawidx.view.screens

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
import fr.uge.wordrawidx.R // Assurez-vous que ce package est correct pour votre R.drawable
import fr.uge.wordrawidx.ui.theme.WordrawidTheme // Pour la Preview

@Composable
fun HomeScreen(
    onPlayClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Décommentez et ajustez si R.drawable.ic_wordrawid_logo existe
        // Image(
        //     painter = painterResource(id = R.drawable.ic_wordrawid_logo),
        //     contentDescription = "Wordrawid Logo",
        //     modifier = Modifier
        //         .size(150.dp)
        //         .padding(bottom = 24.dp)
        // )
        Text(
            text = "Wordrawid",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onPlayClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Jouer",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Button(
            onClick = onSettingsClicked,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Paramètres",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    WordrawidTheme {
        HomeScreen(
            onPlayClicked = {},
            onSettingsClicked = {}
        )
    }
}