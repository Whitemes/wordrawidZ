# Wordrawid Android Frontend

This repository holds the front‑end skeleton for **Wordrawid**, an Android word‑and‑drawing game built purely with Jetpack Compose (no XML or MaterialTheme). It’s based on the “Empty Activity (Compose)” template in Android Studio.

## Project Structure

```
app/
├── build.gradle
├── settings.gradle
├── src/
│   └── main/
│       ├── AndroidManifest.xml
│       ├── java/
│       │   └── fr/esiee/wordrawid/
│       │       ├── MainActivity.kt       # Entrypoint: sets up NavHost
│       │       ├── GameViewModel.kt      # Holds diceValue state and rollDice() logic
│       │       ├── HomeScreen.kt         # Composable with "Start Game" button
│       │       ├── GameScreen.kt         # Composable showing DiceRoller & HintCard
│       │       ├── DiceRoller.kt         # DiceFace + DiceRoller composables (Canvas‑drawn die)
│       │       └── HintCard.kt           # Simple box that displays a text hint
│       └── res/
│           ├── drawable/   (unused)
│           ├── layout/     (empty)
│           └── values/
│               ├── colors.xml
│               ├── strings.xml
│               └── themes.xml
```

## File Descriptions

| File                 | Purpose                                                                                                              |
| -------------------- | -------------------------------------------------------------------------------------------------------------------- |
| **MainActivity.kt**  | Sets up the `NavHostController`, declares `home` and `game` routes, injects `GameViewModel`.                         |
| **GameViewModel.kt** | Holds the `diceValue` Compose state and exposes `rollDice()` which randomises from 1‑6.                              |
| **HomeScreen.kt**    | Welcome screen with a clickable `Box` “Start Game” button. Includes `@Preview`.                                      |
| **GameScreen.kt**    | Observes `diceValue`, displays `DiceRoller` + `HintCard`, and a return button. Previewable.                          |
| **DiceRoller.kt**    | `DiceFace` draws pips via `Canvas`; `DiceRoller` arranges the face plus a clickable Roll button. Both have previews. |
| **HintCard.kt**      | Bordered `Box` showing any hint string. Includes its own preview.                                                    |

## Running Locally

1. Open in Android Studio (Electric Eel or newer).
2. Run on an emulator or real device.
3. Or open a `@Preview` and enable **Interactive** mode to click **Roll**.
4. No backend required – the frontend runs standalone.

## License

Distributed for educational purposes — adapt freely for coursework or personal projects.
