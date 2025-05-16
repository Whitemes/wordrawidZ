package fr.uge.wordrawidx.model

// Pas d'imports Composable ici, c'est une classe de données pure.

data class MazeCell(
    val row: Int,
    val col: Int,
    var northWall: Boolean = true, // true si le mur existe
    var southWall: Boolean = true,
    var eastWall: Boolean = true,
    var westWall: Boolean = true,
    var isVisited: Boolean = false // Utilisé par l'algorithme de génération
)