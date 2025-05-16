package fr.uge.wordrawidx.view.components // MODIFIÉ: Package

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DiceButton(
    diceValue: Int,
    isRolling: Boolean,
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "diceTransition") // AJOUTÉ: label
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "diceRotationAngle" // AJOUTÉ: label
    )

    Button(
        onClick = onRollClick,
        enabled = !isRolling,
        modifier = modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(32.dp)), // Maintenir l'ombre externe ici
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent // Pour que le Box en dessous soit la source du fond
        ),
        contentPadding = ButtonDefaults.ContentPadding // Important pour la taille du clic
    ) {
        Box( // Ce Box porte le dégradé et le contenu interne
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    shape = RoundedCornerShape(32.dp) // Assurer la cohérence des coins arrondis
                )
                // .then(Modifier.shadow(4.dp, RoundedCornerShape(32.dp))) // Ombre interne peut être redondante ou créer un effet double
                .padding(horizontal = 24.dp, vertical = 12.dp) // Padding pour le contenu à l'intérieur du Box dégradé
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(2.dp, RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = if (diceValue > 0) diceValue.toString() else "-", // Afficher "-" si 0
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Filled.Casino,
                    contentDescription = "Dice Icon",
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(if (isRolling) rotation else 0f),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Roll Dice",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }
    }
}