package fr.uge.wordrawidx.controller

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

class ShakeGameController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onUpdate: (GameState) -> Unit,
    private val onGameEnd: (Boolean, GameState) -> Unit
) : SensorEventListener {

    data class GameState(
        val progress: Float,
        val timeLeft: Int,
        val stage: Int,
        val combo: Int,
        val comboMultiplier: Int,
        val bonusActive: Boolean,
        val freezeActive: Boolean,
        val bossActive: Boolean,
        val fever: Boolean,
        val shakes: Int,
        val bonusCaught: Int,
        val freezeBreaks: Int,
        val bestCombo: Int,
        val bossSuccess: Boolean,
        val message: String,
        val fatigueActive: Boolean = false,
        val poisonActive: Boolean = false,
        val fakeBonusActive: Boolean = false,
        val wave: Int = 1
    )

    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Model
    private var progress = 0f
    private var timeLeft = 70 // plus long !
    private var stage = 1
    private var combo = 0
    private var comboMultiplier = 1
    private var fever = false
    private var shakes = 0
    private var bonusActive = false
    private var bonusCaught = 0
    private var freezeActive = false
    private var freezeBreaks = 0
    private var bossActive = false
    private var bossSuccess = false
    private var bestCombo = 0
    private var message = ""
    private var isGameActive = false
    private var wave = 1

    // Nouveaux états pour les malus
    private var fatigueActive = false
    private var poisonActive = false
    private var fakeBonusActive = false

    // Shake timing
    private var lastShakeTimestamp = 0L
    private val shakeTimes = mutableListOf<Long>()

    // Boss
    private var bossShakeCount = 0
    private var bossStartTime = 0L

    // Jobs
    private var timerJob: Job? = null
    private var bonusJob: Job? = null
    private var freezeJob: Job? = null
    private var bossJob: Job? = null
    private var poisonJob: Job? = null
    private var fatigueJob: Job? = null
    private var fakeBonusJob: Job? = null

    fun registerListener() {
        resetState()
        isGameActive = true
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        startGameTimer()
        launchRandomBonus()
        launchRandomFreeze()
        launchRandomMalus()
        onUpdate(buildGameState())
    }

    fun unregisterListener() {
        isGameActive = false
        sensorManager.unregisterListener(this)
        timerJob?.cancel()
        bonusJob?.cancel()
        freezeJob?.cancel()
        bossJob?.cancel()
        poisonJob?.cancel()
        fatigueJob?.cancel()
        fakeBonusJob?.cancel()
    }

    private fun resetState() {
        progress = 0f
        timeLeft = 70
        stage = 1
        combo = 0
        comboMultiplier = 1
        fever = false
        shakes = 0
        bonusActive = false
        bonusCaught = 0
        freezeActive = false
        freezeBreaks = 0
        bossActive = false
        bossSuccess = false
        bestCombo = 0
        message = ""
        wave = 1
        fatigueActive = false
        poisonActive = false
        fakeBonusActive = false
        shakeTimes.clear()
        bossShakeCount = 0
        bossStartTime = 0L
    }

    private fun buildGameState() = GameState(
        progress, timeLeft, stage, combo, comboMultiplier, bonusActive, freezeActive,
        bossActive, fever, shakes, bonusCaught, freezeBreaks, bestCombo, bossSuccess, message,
        fatigueActive, poisonActive, fakeBonusActive, wave
    )

    private fun setMessage(msg: String) {
        message = msg
        onUpdate(buildGameState())
        scope.launch { delay(1200); if (message == msg) { message = ""; onUpdate(buildGameState()) } }
    }

    // Timer & decay logic
    private fun startGameTimer() {
        timerJob = scope.launch {
            while (timeLeft > 0 && progress < 1.0f && isActive) {
                delay(1000)
                timeLeft--
                onUpdate(buildGameState())
            }
            if (isGameActive) finishGame(progress >= 1.0f)
        }
        scope.launch {
            while (isGameActive && progress < 1.0f) {
                delay(350)
                val now = System.currentTimeMillis()
                // Difficulté progressive : la jauge descend plus vite si on avance
                val decay = when {
                    progress > 0.80f -> 0.04f * wave
                    progress > 0.66f -> 0.03f * wave
                    progress > 0.33f -> 0.02f * wave
                    else -> 0.012f * wave
                }
                if (!bossActive && !freezeActive && !poisonActive) {
                    if (now - lastShakeTimestamp > 1600) {
                        progress = (progress - decay).coerceAtLeast(0f)
                        onUpdate(buildGameState())
                    }
                }
            }
        }
    }

    // Random bonus
    private fun launchRandomBonus() {
        bonusJob = scope.launch {
            delay((Random.nextLong(7_000, 16_000)))
            while (isGameActive && !bossActive && !bonusActive && progress < 0.95f) {
                bonusActive = true; setMessage("Un Totem apparaît ! Double shake rapide pour l’attraper !")
                var caught = false
                val bonusWindow = System.currentTimeMillis()
                while (isActive && bonusActive && System.currentTimeMillis() - bonusWindow < 3000) {
                    delay(60)
                    if (caught) break
                }
                bonusActive = false
                onUpdate(buildGameState())
                delay((Random.nextLong(10_000, 20_000)))
            }
        }
    }

    // Random freeze
    private fun launchRandomFreeze() {
        freezeJob = scope.launch {
            delay(Random.nextLong(10_000, 19_000))
            while (isGameActive && !bossActive && !freezeActive && progress < 0.95f) {
                freezeActive = true; setMessage("Freeze ! Secoue vite pour casser la glace !")
                var unfreezeCount = 0
                val freezeStart = System.currentTimeMillis()
                while (isActive && freezeActive && System.currentTimeMillis() - freezeStart < 2500) {
                    delay(30)
                    if (unfreezeCount >= 3) break
                }
                if (unfreezeCount >= 3) {
                    freezeBreaks++
                    freezeActive = false
                    setMessage("Glace brisée !")
                } else {
                    freezeActive = false
                }
                onUpdate(buildGameState())
                delay(Random.nextLong(13_000, 22_000))
            }
        }
    }

    // Nouveaux malus avancés
    private fun launchRandomMalus() {
        // Fatigue
        fatigueJob = scope.launch {
            while (isGameActive && progress < 1.0f) {
                delay(Random.nextLong(15000, 25000))
                if (!bossActive && !fatigueActive && !poisonActive && Random.nextBoolean()) {
                    fatigueActive = true
                    setMessage("💤 Fatigue ! Tes shakes sont moins puissants...")
                    delay(5000)
                    fatigueActive = false
                    setMessage("La fatigue disparaît.")
                }
            }
        }
        // Poison: il ne faut PAS secouer
        poisonJob = scope.launch {
            while (isGameActive && progress < 1.0f) {
                delay(Random.nextLong(18000, 30000))
                if (!bossActive && !fatigueActive && !poisonActive && Random.nextBoolean()) {
                    poisonActive = true
                    setMessage("☠️ Poison ! NE SECOUE PAS pendant 3s sinon tu recules !")
                    val oldShakes = shakes
                    delay(3000)
                    if (shakes > oldShakes) {
                        progress = (progress - 0.11f).coerceAtLeast(0f)
                        setMessage("Aïe, tu as secoué sous poison ! -11%")
                    }
                    poisonActive = false
                }
            }
        }
        // Fake bonus: piège
        fakeBonusJob = scope.launch {
            while (isGameActive && progress < 1.0f) {
                delay(Random.nextLong(20000, 35000))
                if (!bossActive && !fakeBonusActive && Random.nextBoolean()) {
                    fakeBonusActive = true
                    setMessage("😈 Faux Totem ! Ne secoue surtout pas !")
                    val oldShakes = shakes
                    delay(2200)
                    if (shakes > oldShakes) {
                        progress = (progress - 0.15f).coerceAtLeast(0f)
                        setMessage("Raté, c'était un piège ! -15%")
                    }
                    fakeBonusActive = false
                }
            }
        }
    }

    // Boss
    private fun startBossStage() {
        bossActive = true
        bossShakeCount = 0
        bossStartTime = System.currentTimeMillis()
        setMessage("Boss final ! Secoue 10x en 3s ou tu retombes à 80% !")
        bossJob = scope.launch {
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < 3000 && bossShakeCount < 10 && isActive) {
                delay(40)
            }
            if (bossShakeCount >= 10) {
                progress = 1.0f; bossSuccess = true; bossActive = false
                setMessage("Victoire contre le Boss !")
                finishGame(true)
            } else {
                progress = 0.80f; bossActive = false; setMessage("Raté… Le Boss t’a repoussé !")
                onUpdate(buildGameState())
            }
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun addShake() {
        val now = System.currentTimeMillis()
        if (!isGameActive || progress >= 1.0f) return
        // Malus bloquants
        if (poisonActive || fakeBonusActive) {
            // Ils sont gérés dans leurs jobs
            shakes++
            return
        }
        lastShakeTimestamp = now
        shakes++
        shakeTimes.add(now)
        shakeTimes.removeAll { it < now - 1500 }
        // Bonus
        if (bonusActive && shakeTimes.size >= 2 && shakeTimes[shakeTimes.size - 1] - shakeTimes[shakeTimes.size - 2] < 400) {
            bonusActive = false; bonusCaught++; progress += 0.14f; setMessage("Totem capturé ! +14% !")
        }
        // Freeze (il faut secouer vite pour "casser")
        if (freezeActive) {
            if (shakeTimes.size >= 1) {
                if (shakeTimes.size >= 3 && shakeTimes[shakeTimes.size - 1] - shakeTimes[shakeTimes.size - 3] < 900) {
                    freezeActive = false; freezeBreaks++; setMessage("Glace brisée !")
                }
            }
            return
        }
        // Combo logique
        if (shakeTimes.size >= 4 && shakeTimes[shakeTimes.size - 1] - shakeTimes[shakeTimes.size - 4] < 1100) {
            combo++
            comboMultiplier = (comboMultiplier + 1).coerceAtMost(5)
            fever = comboMultiplier >= 3
            setMessage("Combo ! x$comboMultiplier")
        } else if (now - lastShakeTimestamp > 1300) {
            combo = 0; comboMultiplier = 1; fever = false
        }
        if (combo > bestCombo) bestCombo = combo

        // Paliers de difficultés supplémentaires
        val progressionCoef =
            when {
                fatigueActive -> 0.017f * comboMultiplier // Fatigue : shakes très faibles
                progress > 0.88f -> 0.019f * comboMultiplier // Endgame : très difficile
                progress > 0.66f -> 0.027f * comboMultiplier
                fever -> 0.08f
                else -> 0.037f * comboMultiplier
            }
        progress = (progress + progressionCoef).coerceAtMost(1.0f)

        if (progress > 0.33f && stage == 1) { stage = 2; wave = 2; setMessage("Niveau 2 franchi ! Nouvelle vague !") }
        if (progress > 0.66f && stage == 2) { stage = 3; wave = 3; setMessage("Niveau 3 franchi !") }
        if (!bossActive && progress >= 0.95f) {
            startBossStage(); return
        }
        onUpdate(buildGameState())
        // Vibration
        try {
            val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION") vib.vibrate(40)
            }
        } catch (_: Exception) { }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isGameActive || progress >= 1.0f) return
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val accel = sqrt(it.values[0]*it.values[0]+it.values[1]*it.values[1]+it.values[2]*it.values[2])
                val delta = abs(accel - SensorManager.GRAVITY_EARTH)
                if (delta > 2.6f) {
                    // Boss count
                    if (bossActive) bossShakeCount++
                    addShake()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
    private fun finishGame(win: Boolean) {
        isGameActive = false
        unregisterListener()
        onGameEnd(win, buildGameState())
    }
}
