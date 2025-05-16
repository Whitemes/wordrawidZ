package fr.uge.wordrawidx.view.screens

import android.content.res.Configuration // IMPORT
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration // IMPORT
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices // IMPORT POUR DEVICES
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

    // Ajustements de taille en fonction de l'orientation ou de la taille de l'écran
    val logoSize = if (isLandscape) screenHeightDp.dp * 0.25f else screenWidthDp.dp * 0.35f
    val titleStyle = if (screenWidthDp < 360) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall
    val buttonTextStyle = if (screenWidthDp < 360) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge
    val buttonHeight = if (screenHeightDp < 480) 48.dp else 56.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Image(
        //     painter = painterResource(id = R.drawable.ic_wordrawid_logo),
        //     contentDescription = "Wordrawid Logo",
        //     modifier = Modifier
        //         .size(logoSize)
        //         .padding(bottom = if (isLandscape) 16.dp else 24.dp)
        // )
        Text(
            text = "Wordrawid",
            style = titleStyle,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = if (isLandscape) 24.dp else 32.dp)
        )

        Button(
            onClick = onPlayClicked,
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.6f else 1f) // Moins large en paysage
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
                .fillMaxWidth(if (isLandscape) 0.6f else 1f) // Moins large en paysage
                .height(buttonHeight)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Paramètres",
                style = MaterialTheme.typography.titleMedium // Garder une taille cohérente
            )
        }
    }
}

@Preview(name = "Home Portrait", device = Devices.PHONE)
@Preview(name = "Home Landscape", device = Devices.PHONE, widthDp = 640, heightDp = 360)
@Preview(name = "Home Small Portrait", device = Devices.PHONE, widthDp = 320, heightDp = 480)
@Composable
fun HomeScreenResponsivePreview() {
    WordrawidTheme {
        HomeScreen(
            onPlayClicked = {},
            onSettingsClicked = {}
        )
    }
}