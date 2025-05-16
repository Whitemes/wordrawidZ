package fr.uge.wordrawidx.utils

import android.os.Build
import androidx.annotation.RequiresApi
import fr.uge.wordrawidx.model.MazeCell
import fr.uge.wordrawidx.model.MAZE_COLS // Assurez-vous que ces constantes sont accessibles
import fr.uge.wordrawidx.model.MAZE_ROWS // ou passez-les en paramètres
import androidx.compose.ui.geometry.Rect // Pour la conversion
import kotlin.random.Random

object MazeGenerator {

    /**
     * Génère une grille de labyrinthe en utilisant l'algorithme de Recursive Backtracking (DFS).
     * @param rows Nombre de lignes dans la grille.
     * @param cols Nombre de colonnes dans la grille.
     * @param startRow Ligne de la cellule de départ pour la génération.
     * @param startCol Colonne de la cellule de départ pour la génération.
     * @return Une List<List<MazeCell>> représentant le labyrinthe généré.
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun generateMazeGrid(rows: Int, cols: Int, startRow: Int = 0, startCol: Int = 0): List<List<MazeCell>> {
        val grid = List(rows) { r ->
            List(cols) { c -> MazeCell(row = r, col = c) }
        }
        val stack = mutableListOf<MazeCell>()

        val initialCell = grid[startRow.coerceIn(0, rows -1)][startCol.coerceIn(0,cols-1)]
        initialCell.isVisited = true
        stack.add(initialCell)

        while (stack.isNotEmpty()) {
            val current = stack.last()
            val unvisitedNeighbors = mutableListOf<Pair<MazeCell, Char>>() // Voisin, Direction DEPUIS current VERS voisin

            // Nord
            if (current.row > 0 && !grid[current.row - 1][current.col].isVisited) {
                unvisitedNeighbors.add(grid[current.row - 1][current.col] to 'N')
            }
            // Sud
            if (current.row < rows - 1 && !grid[current.row + 1][current.col].isVisited) {
                unvisitedNeighbors.add(grid[current.row + 1][current.col] to 'S')
            }
            // Est
            if (current.col < cols - 1 && !grid[current.row][current.col + 1].isVisited) {
                unvisitedNeighbors.add(grid[current.row][current.col + 1] to 'E')
            }
            // Ouest
            if (current.col > 0 && !grid[current.row][current.col - 1].isVisited) {
                unvisitedNeighbors.add(grid[current.row][current.col - 1] to 'O')
            }

            if (unvisitedNeighbors.isNotEmpty()) {
                val (nextCell, direction) = unvisitedNeighbors.random(Random.Default)
                when (direction) {
                    'N' -> {
                        current.northWall = false
                        nextCell.southWall = false
                    }
                    'S' -> {
                        current.southWall = false
                        nextCell.northWall = false
                    }
                    'E' -> {
                        current.eastWall = false
                        nextCell.westWall = false
                    }
                    'O' -> {
                        current.westWall = false
                        nextCell.eastWall = false
                    }
                }
                nextCell.isVisited = true
                stack.add(nextCell)
            } else {
                stack.removeLast()
            }
        }
        return grid
    }

    /**
     * Convertit la grille de MazeCell générée en une liste de Rect pour les murs QUI SONT TOUJOURS PRÉSENTS.
     * Ces Rects sont en unités de grille et représentent des lignes fines entre les cellules.
     * C'est une façon de représenter les murs pour le dessin et la collision.
     */
    fun convertMazeGridToWallRects(grid: List<List<MazeCell>>): List<Rect> {
        val wallRects = mutableListOf<Rect>()
        val rows = grid.size
        if (rows == 0) return emptyList()
        val cols = grid[0].size
        if (cols == 0) return emptyList()

        // Épaisseur des murs dessinés (en fraction d'unité de grille)
        val wallThickness = 0.1f

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = grid[r][c]

                // Mur Nord de la cellule (r,c) est une ligne horizontale en haut de la cellule
                // Sauf pour la première ligne, qui est une bordure externe.
                if (cell.northWall && r == 0) { // Bordure externe HAUT
                    wallRects.add(Rect(c.toFloat(), r.toFloat() - wallThickness, (c + 1f), r.toFloat()))
                }

                // Mur Ouest de la cellule (r,c) est une ligne verticale à gauche de la cellule
                // Sauf pour la première colonne, qui est une bordure externe.
                if (cell.westWall && c == 0) { // Bordure externe GAUCHE
                     wallRects.add(Rect(c.toFloat() - wallThickness, r.toFloat(), c.toFloat(), (r + 1f)))
                }

                // Mur Est (entre (r,c) et (r,c+1))
                // On ne le dessine que si on est la cellule de gauche, pour éviter doublons.
                if (cell.eastWall && c < cols) { // c < cols est toujours vrai, cols-1 pour le voisin
                    wallRects.add(Rect((c + 1f) - wallThickness / 2f, r.toFloat(), (c + 1f) + wallThickness / 2f, (r + 1f)))
                }

                // Mur Sud (entre (r,c) et (r+1,c))
                // On ne le dessine que si on est la cellule du haut, pour éviter doublons.
                if (cell.southWall && r < rows) { // r < rows est toujours vrai, rows-1 pour le voisin
                    wallRects.add(Rect(c.toFloat(), (r + 1f) - wallThickness / 2f, (c + 1f), (r + 1f) + wallThickness / 2f))
                }
            }
        }
        // Ajouter les bordures externes explicitement pour être sûr (en fonction de la logique ci-dessus, elles pourraient déjà y être)
        // Haut
        wallRects.add(Rect(0f, -wallThickness, cols.toFloat(), 0f))
        // Bas
        wallRects.add(Rect(0f, rows.toFloat(), cols.toFloat(), rows.toFloat() + wallThickness))
        // Gauche
        wallRects.add(Rect(-wallThickness, 0f, 0f, rows.toFloat()))
        // Droite
        wallRects.add(Rect(cols.toFloat(), 0f, cols.toFloat() + wallThickness, rows.toFloat()))


        return wallRects.distinct() // Pour enlever les doublons potentiels si la logique ci-dessus en crée
    }
}