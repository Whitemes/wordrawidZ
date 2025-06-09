package fr.uge.wordrawidx.utils

object MiniGameResultHolder {
    // Position du pion avant le mini-jeu
    var playerPositionBeforeMiniGame: Int? = null

    // Cellule pour laquelle le mini-jeu a été lancé
    var lastChallengedCell: Int? = null

    // Résultat du mini-jeu : true (gagné), false (perdu), null (non joué)
    var lastResultWasWin: Boolean? = null

    // Indique si une nouvelle partie a été explicitement demandée
    var newGameRequestedFromVictoryOrHome: Boolean = false
}
