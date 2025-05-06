# Wordrawid Android Frontend

This repository contains the front-end for **Wordrawid**, an Android word-and-drawing game, built entirely using Jetpack Compose.  No XML layouts or Material Design components are used in this project.

## Project Structure

The project follows a standard Android application structure with Gradle as the build system.

```
wordrawidx/
├── .gradle/                        # Gradle internal files
├── .idea/                          # IDE settings
├── .kotlin/                        # Kotlin-specific config
├── app/
│   ├── build/                      # Build outputs
│   └── src/
│       ├── androidTest/            # Android test sources
│       └── main/
│           ├── AndroidManifest.xml
│           └── java/
│               └── fr/uge/wordrawidx/
│                   ├── MainActivity.kt               # App entry point
│                   ├── model/
│                   │   └── GameState.kt              # Data model for game state
│                   └── ui/
│                       ├── components/
│                       │   ├── DiceButton.kt         # Composable for dice roll button
│                       │   └── GameBoard.kt          # Composable representing the game board
│                       ├── screens/
│                       │   └── GameScreen.kt         # Main screen with board and controls
│                       └── theme/
│                           ├── Color.kt
│                           ├── Shape.kt
│                           ├── Theme.kt
│                           └── Type.kt
├── build.gradle.kts               # Project-level Gradle config
├── settings.gradle.kts            # Project module settings
└── gradlew                        # Gradle wrapper script
```

## 📄 Key Files and Descriptions

| File                                | Purpose                                                                                                                                       |
|-------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| **`settings.gradle.kts`**           | Defines project-wide settings and module inclusion.                                                                                           |
| **`build.gradle.kts` (Project)**    | Project-level configuration (plugin management, repositories, etc.).                                                                          |
| **`app/build.gradle.kts`**          | Module-specific Gradle config for the Android app, including Compose and dependencies.                                                        |
| **`MainActivity.kt`**               | App entry point. Sets up the UI and attaches the root composable.                                                                             |
| **`GameState.kt`**                  | Data class or state holder for managing game logic (e.g., dice value, player position).                                                       |
| **`GameScreen.kt`**                 | Main screen composable displaying the game board and interactive elements.                                                                    |
| **`DiceButton.kt`**                 | A reusable composable button used to trigger dice rolls.                                                                                      |
| **`GameBoard.kt`**                  | Composable that visually represents the grid/board where the game takes place.                                                                |
| **`Color.kt`, `Shape.kt`, `Type.kt`, `Theme.kt`** | Define the custom Material 3 theme (colors, typography, shapes) used across the app.                                               |

---

## ▶️ Running Locally

1. **Open the project in Android Studio (Electric Eel or newer).**
2. **Sync Gradle files** using the toolbar ("Sync Project with Gradle Files").
3. **Run the app** on an emulator or physical Android device using the green ▶️ button.
4. **Use Compose Previews** by opening any `@Preview`-annotated function (e.g. in `GameScreen.kt`) and viewing the design tab.
5. **No backend required** — this app runs standalone for now.

---

## 📜 License

This project is provided for **educational and coursework purposes**.  
You may freely reuse, modify, and adapt the code for learning or personal experimentation.
