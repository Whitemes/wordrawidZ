package fr.uge.wordrawidx.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import fr.uge.wordrawidx.model.MazeState

/**
 * ViewModel dédié au mini-jeu du labyrinthe.
 * Conserve l’instance de [MazeState] à travers les changements
 * de configuration (rotation de l’écran, changement de langue, etc.).
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class MazeGameViewModel : ViewModel() {

    /** État courant du labyrinthe, persistant tant que le ViewModel vit. */
    val mazeState: MazeState = MazeState()

    /** Relance une partie : génère un nouveau labyrinthe et remet le timer. */
    fun resetGame() {
        // Il suffit de recréer l’état; Compose observera les nouvelles valeurs.
        // (On pourrait aussi ajouter une méthode `reset()` dans MazeState.)
        val newState = MazeState()
        // Copier les références pour que les Composables observent le nouvel objet
        // sans recréer le ViewModel.
        mazeState.walls.clear()
        mazeState.walls.addAll(newState.walls)
        mazeState.ballPosition = newState.ballPosition
        mazeState.timeLeftSeconds = newState.timeLeftSeconds
        mazeState.currentMiniGameState = newState.currentMiniGameState
    }
}
