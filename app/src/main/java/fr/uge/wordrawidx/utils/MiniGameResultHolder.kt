package fr.uge.wordrawidx.utils

/**
 * Holder simplifié pour la communication entre GameScreen et les mini-jeux.
 *
 * MIGRATION VERS VIEWMODEL :
 * - playerPositionBeforeMiniGame → GameViewModel.playerPositionBeforeMiniGame
 * - lastChallengedCell → GameViewModel.lastChallengedCell
 *
 * Ce holder ne conserve que les propriétés nécessaires à la communication
 * entre écrans pour maintenir la compatibilité avec les mini-jeux existants.
 */
object MiniGameResultHolder {

    /**
     * Résultat du dernier mini-jeu joué
     * - true : Mini-jeu gagné
     * - false : Mini-jeu perdu
     * - null : Aucun mini-jeu joué ou résultat déjà traité
     *
     * ✅ GARDÉ : Utilisé pour la communication GameScreen ↔ Mini-jeux
     */
    var lastResultWasWin: Boolean? = null

    /**
     * Indique si une nouvelle partie a été explicitement demandée
     * depuis l'écran de victoire ou l'écran d'accueil
     *
     * ✅ GARDÉ : Signal pour déclencher une nouvelle partie
     */
    var newGameRequestedFromVictoryOrHome: Boolean = false

    // ❌ SUPPRIMÉ : Migré vers GameViewModel.playerPositionBeforeMiniGame
    // var playerPositionBeforeMiniGame: Int? = null

    // ❌ SUPPRIMÉ : Migré vers GameViewModel.lastChallengedCell
    // var lastChallengedCell: Int? = null

    /**
     * Utilitaire pour nettoyer tous les états
     * Utilisé lors du reset complet de l'application
     */
    fun clearAll() {
        lastResultWasWin = null
        newGameRequestedFromVictoryOrHome = false
    }

    /**
     * Debug : Affiche l'état actuel du holder
     */
    fun debugState(): String {
        return """
            MiniGameResultHolder State:
            - lastResultWasWin: $lastResultWasWin
            - newGameRequestedFromVictoryOrHome: $newGameRequestedFromVictoryOrHome
        """.trimIndent()
    }
}