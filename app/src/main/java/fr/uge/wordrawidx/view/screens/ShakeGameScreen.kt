package fr.uge.wordrawidx.view.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import fr.uge.wordrawidx.controller.NavigationController
import fr.uge.wordrawidx.controller.ShakeGameController

import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.*
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ShakeGameScreen(
    navigationController: NavigationController,
    onGameFinished: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // Le nouveau GameState adapté aux nouveaux états !
    var gameState by remember {
        mutableStateOf(
            ShakeGameController.GameState(
                0f, 70, 1, 0, 1, false, false, false, false,
                0, 0, 0, 0, false, "",
                fatigueActive = false, poisonActive = false, fakeBonusActive = false, wave = 1
            )
        )
    }
    var isGameFinished by remember { mutableStateOf(false) }
    var didWin by remember { mutableStateOf(false) }

    // --- STYLISH BACKGROUND ANIMATION ---
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val animatedGradientAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientAngle"
    )
    // Dégradés par "stages" et FEVER
    val colorsStage1 = listOf(Color(0xFF90CAF9), Color(0xFF1976D2), Color(0xFFB3E5FC))
    val colorsStage2 = listOf(Color(0xFFC8E6C9), Color(0xFF388E3C), Color(0xFFF4FF81))
    val colorsStage3 = listOf(Color(0xFFFFCDD2), Color(0xFFD32F2F), Color(0xFFFFF59D))
    val feverColors = listOf(Color(0xFFFFFF00), Color(0xFFFFC107), Color(0xFFFFA000), Color(0xFFFF8A65))
    val bgColors = when {
        gameState.fever -> feverColors
        gameState.stage == 1 -> colorsStage1
        gameState.stage == 2 -> colorsStage2
        else -> colorsStage3
    }
    val backgroundBrush = Brush.sweepGradient(bgColors)

    val feverGlow = if (gameState.fever) Modifier.blur(28.dp) else Modifier

    // Konfetti (Combo/Fever, victoire, boss)
    val konfettiParties = remember { mutableStateListOf<Party>() }
    LaunchedEffect(gameState.combo, gameState.fever) {
        if (gameState.comboMultiplier >= 3 || gameState.fever) {
            konfettiParties += Party(
                emitter = Emitter(duration = 800, TimeUnit.MILLISECONDS).perSecond(90),
                position = Position.Relative(0.5, 0.1),
                spread = 60,
                speed = 7f,
                maxSpeed = 16f,
                size = listOf(Size.SMALL, Size.MEDIUM),
                timeToLive = 1500,
                colors = listOf(0xFFFFD600.toInt(), 0xFFF50057.toInt(), 0xFF00E676.toInt(), 0xFF2979FF.toInt())
            )
        }
    }
    LaunchedEffect(isGameFinished, didWin, gameState.bossSuccess) {
        if (isGameFinished && (didWin || gameState.bossSuccess)) {
            konfettiParties += Party(
                emitter = Emitter(duration = 2200, TimeUnit.MILLISECONDS).perSecond(180),
                position = Position.Relative(0.5, 0.05),
                spread = 120,
                speed = 13f,
                maxSpeed = 26f,
                size = listOf(Size.MEDIUM, Size.LARGE),
                timeToLive = 3500,
                colors = listOf(0xFFE040FB.toInt(), 0xFF00B8D4.toInt(), 0xFFFFEA00.toInt(), 0xFFD50000.toInt())
            )
        }
    }

    val stageColor by animateColorAsState(
        when (gameState.stage) {
            1 -> Color(0xFF1976D2)
            2 -> Color(0xFF388E3C)
            else -> Color(0xFFD32F2F)
        }, tween(400)
    )
    val feverColor by animateColorAsState(
        if (gameState.fever) Color.Yellow else stageColor, tween(400)
    )

    val controller = remember {
        ShakeGameController(
            context,
            coroutineScope,
            onUpdate = { gs -> gameState = gs },
            onGameEnd = { win, finalState -> isGameFinished = true; didWin = win; gameState = finalState; onGameFinished(win) }
        )
    }

    DisposableEffect(lifecycleOwner, controller) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> controller.registerListener()
                Lifecycle.Event.ON_PAUSE -> controller.unregisterListener()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            controller.unregisterListener()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .clip(RoundedCornerShape(20.dp))
    ) {
        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = konfettiParties
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Quête du Totem !", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            if (gameState.message.isNotBlank()) {
                Text(
                    gameState.message,
                    color = Color.Magenta,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(6.dp))
            }
            // --- Progress Glow Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(36.dp)
                    .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
                    .then(if (gameState.fever) Modifier.blur(18.dp) else Modifier)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                LinearProgressIndicator(
                    progress = gameState.progress,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(feverGlow),
                    color = feverColor,
                    trackColor = Color(0x33FFFFFF)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row {
                repeat(gameState.stage) {
                    Box(
                        modifier = Modifier
                            .padding(3.dp)
                            .size(16.dp)
                            .background(stageColor, shape = RoundedCornerShape(3.dp))
                            .blur(if (gameState.fever) 6.dp else 0.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Progression : ${(gameState.progress * 100).toInt()}%",
                color = Color.White
            )
            AnimatedComboText(combo = gameState.combo, comboMultiplier = gameState.comboMultiplier, fever = gameState.fever)
            if (gameState.fever) Text("🔥 FEVER ! 🔥", color = Color.Yellow, style = MaterialTheme.typography.titleLarge)
            if (gameState.bonusActive) Text("🎁 Bonus Totem ! Double shake pour l’attraper", color = Color(0xFF7B1FA2))
            if (gameState.freezeActive) Text("❄️ Freeze ! Secoue pour briser la glace !", color = Color.Cyan)
            if (gameState.bossActive) Text("💀 Boss final ! Shake super vite !", color = Color.Red, style = MaterialTheme.typography.titleLarge)
            // ----------- NOUVELLE PARTIE : affichage des malus, obstacles et vagues ------------
            if (gameState.fatigueActive) Text("💤 Fatigue active : shakes moins efficaces !", color = Color.Gray)
            if (gameState.poisonActive) Text("☠️ Poison : NE SECOUE PAS !", color = Color(0xFFB71C1C))
            if (gameState.fakeBonusActive) Text("😈 Faux Totem : Ne secoue pas !", color = Color(0xFFFFA000))
            if (gameState.wave > 1) Text("🌊 Vague ${gameState.wave} !", color = Color(0xFF00E676), style = MaterialTheme.typography.titleMedium)
            // -------------------------------------------------------------------------
            Spacer(modifier = Modifier.height(12.dp))
            Text("Shakes : ${gameState.shakes}  |  Bonus attrapés : ${gameState.bonusCaught}  |  Glace brisée : ${gameState.freezeBreaks}", color = Color.White)
            Text("Best Combo : ${gameState.bestCombo}", color = Color(0xFFE1FF3C))
            Text("Temps restant : ${gameState.timeLeft}s", color = if (gameState.timeLeft < 10) Color.Red else Color.White)
            Spacer(modifier = Modifier.height(18.dp))
            if (isGameFinished) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    if (didWin) "🎉 Tu as gagné la Quête du Totem ! 🎉"
                    else "Raté… Le Totem t’échappe, réessaie !",
                    color = if (didWin) Color.Green else Color.Red,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text("Score : ${gameState.shakes} shakes", color = Color.White)
                Text("Bonus : ${gameState.bonusCaught}", color = Color(0xFF7B1FA2))
                Text("Glace cassée : ${gameState.freezeBreaks}", color = Color.Cyan)
                Text("Best combo : ${gameState.bestCombo}", color = Color(0xFFE1FF3C))
                Button(
                    onClick = {
                        isGameFinished = false
                        controller.registerListener()
                    },
                    Modifier.padding(top = 12.dp)
                ) {
                    Text("Rejouer")
                }
            }
        }
    }
}

@Composable
fun AnimatedComboText(combo: Int, comboMultiplier: Int, fever: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "comboAnim")
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (fever) 1.25f else 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "comboScale"
    )
    if (combo > 1) {
        Text(
            "Combo : $combo (x$comboMultiplier)",
            color = Color(0xFFFFEB3B),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.16f))
                .padding(horizontal = 14.dp, vertical = 3.dp)
                .size(width = 24.dp * animatedScale, height = 26.dp * animatedScale)
        )
    }
}
