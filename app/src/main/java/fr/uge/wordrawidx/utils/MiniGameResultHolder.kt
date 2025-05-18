package fr.uge.wordrawidx.utils

object MiniGameResultHolder {
    var lastChallengedCell: Int? = null
    var lastResultWasWin: Boolean? = null
    var newGameRequestedFromVictoryOrHome: Boolean = false
    var playerPositionBeforeMiniGame: Int? = null
}
