# Wordrawid Android Frontend

This repository contains the front-end for **Wordrawid**, an Android word-and-drawing game, built entirely using Jetpack Compose.  No XML layouts or Material Design components are used in this project.

## Project Structure

The project follows a standard Android application structure with Gradle as the build system.

```
wordrawid/
├── .gradle/                       # Gradle internal files
├── app/                           # Application module
│   ├── build.gradle.kts           # Module-level build file
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/
│           │   └── fr/esiee/wordrawid/
│           │       ├── MainActivity.kt       # Entry point: sets up NavHost
│           │       ├── GameViewModel.kt      # Holds diceValue state and rollDice() logic
│           │       ├── HomeScreen.kt         # Composable with "Start Game" button
│           │       ├── GameScreen.kt         # Composable showing DiceRoller & HintCard
│           │       ├── DiceRoller.kt         # DiceFace + DiceRoller composables (Canvas-drawn die)
│           │       └── HintCard.kt           # Simple box that displays a text hint
│           └── res/
│               ├── drawable/                 # (unused)
│               ├── layout/                   # (empty)
│               └── values/
│                   ├── colors.xml
│                   ├── strings.xml
│                   └── themes.xml
├── gradle/                        # Gradle wrapper
│   └── wrapper/
│       └── gradle-wrapper.properties
├── build.gradle.kts              # Project-level build file
├── settings.gradle.kts           # Project settings
└── gradlew                       # Gradle wrapper script
```

## 📁 Key Files and Descriptions

| File                              | Purpose                                                                                                                                                       |
|-----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`settings.gradle.kts`**         | Defines project-wide settings, includes modules, and configures dependency resolution.                                                                        |
| **`build.gradle.kts` (Project)**  | Defines project-level configurations and applies plugins shared across modules.                                                                               |
| **`build.gradle.kts` (Module)**   | Defines module-specific configurations, including dependencies, build types, and Android settings for the `app` module.                                       |
| **`MainActivity.kt`**             | Sets up the `NavHostController`, defines the navigation routes (`home` and `game`), and injects the `GameViewModel`.                                          |
| **`GameViewModel.kt`**            | Holds the `diceValue` Compose state and exposes the `rollDice()` function, which generates a random number between 1 and 6.                                   |
| **`HomeScreen.kt`**               | The welcome screen, featuring a clickable "Start Game" button. Includes the `@Preview` annotation for UI previews.                                           |
| **`GameScreen.kt`**               | Observes the `diceValue`, displays the `DiceRoller` and `HintCard`, and provides a return button. This composable is also previewable.                       |
| **`DiceRoller.kt`**               | Contains `DiceFace`, which draws the die pips using `Canvas`, and `DiceRoller`, which arranges the die face and a clickable "Roll" button. Both are previewable. |
| **`HintCard.kt`**                 | A composable `Box` with a border that displays a text hint. Includes its own `@Preview` for easy testing.                                                    |

---

## ▶️ Running Locally

1. **Open in Android Studio**  
   Open the project in Android Studio (Electric Eel or newer recommended).

2. **Sync Project**  
   Ensure the project syncs correctly with Gradle by clicking on **"Sync Project with Gradle Files"** (elephant icon with a green arrow).

3. **Run on Emulator or Device**  
   Run the app on an emulator or real Android device using the green play button.

4. **Use Interactive Previews (Optional)**  
   Open any composable annotated with `@Preview` and enable **Interactive Mode** to directly test UI elements like the dice roller.

5. **No Backend Required**  
   This is a standalone frontend prototype. No backend configuration is needed.

---

## 📜 License

This project is provided for **educational purposes**.  
You are free to adapt, modify, or reuse the code for coursework, demos, or personal projects.
