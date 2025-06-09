// data/repository/MysteryRepository.kt
package fr.uge.wordrawidx.data.repository

import android.content.Context
import android.util.Log
import fr.uge.wordrawidx.data.local.AssetLoader
import fr.uge.wordrawidx.data.local.SimpleMysteryLoader
import fr.uge.wordrawidx.data.model.MysteryObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository centralisé pour la gestion des objets mystères
 * Supporte à la fois le backend JSON et le fallback étendu
 */
class MysteryRepository(context: Context) {

    private val assetLoader = AssetLoader(context)
    private val simpleMysteryLoader = SimpleMysteryLoader(context)
    private var _mysteryObjects: List<MysteryObject>? = null

    companion object {
        private const val TAG = "MysteryRepository"

        @Volatile
        private var INSTANCE: MysteryRepository? = null

        fun getInstance(context: Context): MysteryRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MysteryRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    /**
     * Charge tous les objets mystères avec stratégie de fallback
     * 1. Essayer backend JSON
     * 2. Si échec -> version hardcodée étendue
     * 3. Si échec -> version hardcodée minimale
     */
    suspend fun getAllMysteryObjects(): List<MysteryObject> {
        // Cache déjà chargé
        _mysteryObjects?.let {
            Log.d(TAG, "Retour cache: ${it.size} objets")
            return it
        }

        return withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Tentative chargement backend JSON...")

                // Stratégie 1 : Backend JSON
                val backendObjects = assetLoader.getAllMysteryObjects()

                // Vérifier si c'est vraiment le backend ou le fallback
                val isRealBackend = backendObjects.size > 5 &&
                        backendObjects.any { obj ->
                            obj.word !in listOf("souris", "guitare", "bouteille")
                        }

                if (isRealBackend) {
                    _mysteryObjects = backendObjects
                    Log.i(TAG, "✅ Backend JSON chargé: ${backendObjects.size} objets")
                    backendObjects
                } else {
                    throw Exception("Backend JSON non disponible - fallback détecté")
                }

            } catch (e: Exception) {
                Log.w(TAG, "Backend JSON échec: ${e.message}")

                try {
                    // Stratégie 2 : Version hardcodée étendue
                    Log.i(TAG, "Utilisation version hardcodée étendue...")
                    val extendedObjects = simpleMysteryLoader.loadMysteryObjectsAsync()
                    _mysteryObjects = extendedObjects
                    Log.i(TAG, "✅ Version étendue chargée: ${extendedObjects.size} objets")
                    extendedObjects

                } catch (e2: Exception) {
                    Log.e(TAG, "Version étendue échec: ${e2.message}")

                    // Stratégie 3 : Fallback minimal hardcodé
                    Log.w(TAG, "Utilisation fallback minimal...")
                    val minimalObjects = listOf(
                        MysteryObject(
                            word = "souris",
                            closeWords = listOf("clavier", "pointeur", "USB", "ordinateur", "molette"),
                            imageName = "img_souris.png",
                            imageResourceId = null
                        ),
                        MysteryObject(
                            word = "guitare",
                            closeWords = listOf("corde", "instrument", "musique", "électrique", "acoustique"),
                            imageName = "img_guitare.png",
                            imageResourceId = null
                        ),
                        MysteryObject(
                            word = "bouteille",
                            closeWords = listOf("eau", "verre", "bouchon", "plastique", "boisson"),
                            imageName = "img_bouteille.png",
                            imageResourceId = null
                        )
                    )
                    _mysteryObjects = minimalObjects
                    Log.w(TAG, "⚠️ Fallback minimal: ${minimalObjects.size} objets")
                    minimalObjects
                }
            }
        }
    }

    /**
     * Sélectionne un objet mystère aléatoire pour une nouvelle partie
     */
    suspend fun getRandomMysteryObject(): MysteryObject? {
        val allObjects = getAllMysteryObjects()

        return if (allObjects.isNotEmpty()) {
            val selected = allObjects.random()
            Log.d(TAG, "Objet mystère sélectionné: '${selected.word}' (${selected.closeWords.size} indices)")
            selected
        } else {
            Log.e(TAG, "Aucun objet mystère disponible!")
            null
        }
    }

    /**
     * Recherche un objet par son mot
     */
    suspend fun findMysteryObjectByWord(word: String): MysteryObject? {
        val allObjects = getAllMysteryObjects()
        return allObjects.find { it.word.equals(word, ignoreCase = true) }
    }

    /**
     * Statistiques de la base de données
     */
    suspend fun getDatabaseStats(): DatabaseStats {
        val objects = getAllMysteryObjects()
        return DatabaseStats(
            totalObjects = objects.size,
            totalWords = objects.sumOf { it.closeWords.size },
            objectsWithImages = objects.count { it.imageResourceId != null },
            averageWordsPerObject = if (objects.isNotEmpty()) {
                objects.sumOf { it.closeWords.size } / objects.size
            } else 0
        )
    }

    /**
     * Force le rechargement de la base (pour debug)
     */
    suspend fun refreshDatabase() {
        Log.i(TAG, "Rechargement forcé de la base de données")
        _mysteryObjects = null
        getAllMysteryObjects()
    }

    /**
     * Identifie la source des données actuelles
     */
    suspend fun getDataSource(): String {
        val objects = getAllMysteryObjects()
        return when {
            objects.isEmpty() -> "Aucune source"
            objects.size >= 15 -> "Backend JSON"
            objects.size >= 5 -> "Version étendue hardcodée"
            else -> "Fallback minimal"
        }
    }
}

/**
 * Statistiques de la base de données
 */
data class DatabaseStats(
    val totalObjects: Int,
    val totalWords: Int,
    val objectsWithImages: Int,
    val averageWordsPerObject: Int
)