// data/local/AssetLoader.kt
package fr.uge.wordrawidx.data.local

import android.content.Context
import android.util.Log
import fr.uge.wordrawidx.data.model.BackendWordPack
import fr.uge.wordrawidx.data.model.MysteryObject
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Utilitaire pour charger les assets depuis le backend
 */
class AssetLoader(private val context: Context) {

    companion object {
        private const val OFFLINE_WORD_PACK_FILE = "offline_word_pack.json"  // ← Nom exact du backend
        private const val TAG = "AssetLoader"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Charge la base de données depuis le fichier backend offline_word_pack.json
     */
    suspend fun loadBackendWordPack(): Result<BackendWordPack> {
        return try {
            Log.d(TAG, "Chargement depuis assets/$OFFLINE_WORD_PACK_FILE")

            val jsonString = context.assets.open(OFFLINE_WORD_PACK_FILE)
                .bufferedReader()
                .use { it.readText() }

            val wordPack = json.decodeFromString<BackendWordPack>(jsonString)

            Log.i(TAG, "Word pack chargé: ${wordPack.pack_info.word_count} objets, version ${wordPack.pack_info.version}")
            Result.success(wordPack)

        } catch (e: IOException) {
            Log.e(TAG, "Erreur lecture fichier assets: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur parsing JSON backend: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Convertit le WordPack backend vers une liste d'objets utilisables
     */
    suspend fun getAllMysteryObjects(): List<MysteryObject> {
        return try {
            val wordPack = loadBackendWordPack().getOrThrow()

            val objects = wordPack.words.map { (key, wordData) ->
                MysteryObject.fromBackendData(key, wordData) { imageName ->
                    resolveImageResource(imageName)
                }
            }

            Log.i(TAG, "Conversion réussie: ${objects.size} objets mystères")
            objects

        } catch (e: Exception) {
            Log.w(TAG, "Fallback vers objets hardcodés: ${e.message}")
            getHardcodedFallback()
        }
    }

    /**
     * Fallback hardcodé si backend non disponible
     */
    private fun getHardcodedFallback(): List<MysteryObject> {
        Log.w(TAG, "Utilisation du fallback hardcodé")
        return listOf(
            MysteryObject(
                word = "souris",
                closeWords = listOf(
                    "clavier", "pointeur", "USB", "ordinateur", "molette",
                    "écran", "clic", "tapis", "filaire", "bluetooth"
                ),
                imageName = "img_souris.png",
                imageResourceId = null
            ),
            MysteryObject(
                word = "guitare",
                closeWords = listOf(
                    "corde", "instrument", "musique", "électrique", "acoustique",
                    "amplificateur", "mélodie", "basse", "accord", "piano"
                ),
                imageName = "img_guitare.png",
                imageResourceId = null
            ),
            MysteryObject(
                word = "bouteille",
                closeWords = listOf(
                    "eau", "verre", "bouchon", "plastique", "boisson",
                    "liquide", "vin", "conteneur", "canette", "verrerie"
                ),
                imageName = "img_bouteille.png",
                imageResourceId = null
            )
        )
    }

    /**
     * Résout le nom d'image vers un resource ID Android
     * Adapté pour les noms d'images du backend (ex: "Karukan 01.jpg")
     */
    fun resolveImageResource(imageName: String): Int? {
        return try {
            // Nettoyer le nom pour Android (enlever extension, espaces, caractères spéciaux)
            val cleanName = imageName
                .substringBeforeLast(".")           // Enlever extension
                .replace(" ", "_")                  // Espaces -> underscores
                .replace("-", "_")                  // Tirets -> underscores
                .replace(Regex("[^a-zA-Z0-9_]"), "") // Enlever caractères spéciaux
                .lowercase()                        // Minuscules obligatoires

            // Chercher dans les drawables
            val resourceId = context.resources.getIdentifier(
                cleanName,
                "drawable",
                context.packageName
            )

            if (resourceId != 0) {
                Log.d(TAG, "Image trouvée: '$imageName' -> '$cleanName' -> $resourceId")
                resourceId
            } else {
                Log.w(TAG, "Image non trouvée: '$imageName' -> '$cleanName'")

                // Essayer des variations courantes
                val alternatives = listOf(
                    cleanName.replace("_01", ""),     // "karukan_01" -> "karukan"
                    cleanName.replace("01", ""),      // "karukan01" -> "karukan"
                    "img_$cleanName",                 // "karukan" -> "img_karukan"
                    cleanName.take(10)                // Limiter à 10 caractères
                )

                for (alt in alternatives) {
                    val altId = context.resources.getIdentifier(alt, "drawable", context.packageName)
                    if (altId != 0) {
                        Log.d(TAG, "Alternative trouvée: '$imageName' -> '$alt' -> $altId")
                        return altId
                    }
                }

                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur résolution image '$imageName': ${e.message}")
            null
        }
    }
}