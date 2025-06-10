// data/local/AssetLoader.kt
package fr.uge.wordrawidx.data.local

import android.content.Context
import android.util.Log
import fr.uge.wordrawidx.data.model.BackendWordPack
import fr.uge.wordrawidx.data.model.MysteryObject
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * AssetLoader corrigé pour chercher les images dans assets/images/
 * Compatible avec la structure générée par votre script Python
 */
class AssetLoader(private val context: Context) {

    companion object {
        private const val OFFLINE_WORD_PACK_FILE = "offline_word_pack.json"
        private const val IMAGES_ASSETS_FOLDER = "images"  // ← NOUVEAU
        private const val TAG = "AssetLoader"
        // ID spécial pour indiquer qu'une image est disponible dans assets/
        const val ASSETS_IMAGE_PLACEHOLDER_ID = -1
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
     * ✅ NOUVELLE MÉTHODE : Vérifie si une image existe dans assets/images/
     */
    private fun checkImageInAssets(imageName: String): Boolean {
        val cleanName = cleanImageName(imageName)
        return try {
            // Essayer le nom original
            context.assets.open("$IMAGES_ASSETS_FOLDER/$imageName").close()
            Log.d(TAG, "✅ Image trouvée dans assets: '$imageName'")
            true
        } catch (e: IOException) {
            try {
                context.assets.open("$IMAGES_ASSETS_FOLDER/$cleanName").close()
                Log.d(TAG, "✅ Image trouvée dans assets (nom nettoyé): '$cleanName'")
                true
            } catch (e2: IOException) {
                Log.w(TAG, "❌ Image non trouvée dans assets: '$imageName' ni '$cleanName'")
                false
            }
        }
    }

    /**
     * ✅ MÉTHODE MISE À JOUR : Résout d'abord assets/, puis drawable/
     */
    fun resolveImageResource(imageName: String): Int? {
        return try {
            // ✅ STRATÉGIE 1 : Vérifier d'abord dans assets/images/
            if (checkImageInAssets(imageName)) {
                // Pour l'instant, on ne peut pas charger dynamiquement depuis assets/
                // Il faudrait utiliser AssetManager.open() dans les composants
                Log.d(TAG, "Image disponible dans assets: '$imageName' (chargement dynamique requis)")
                // Retourner un ID spécial pour indiquer qu'elle est dans assets
                return ASSETS_IMAGE_PLACEHOLDER_ID
            }

            // ✅ STRATÉGIE 2 : Chercher dans drawable/ (système Android classique)
            val cleanName = cleanImageName(imageName)
            val resourceId = context.resources.getIdentifier(
                cleanName,
                "drawable",
                context.packageName
            )

            if (resourceId != 0) {
                Log.d(TAG, "✅ Image trouvée dans drawable: '$imageName' -> '$cleanName' -> $resourceId")
                resourceId
            } else {
                Log.w(TAG, "⚠️ Image non trouvée dans drawable: '$imageName' -> '$cleanName'")

                // ✅ STRATÉGIE 3 : Essayer des variations
                val alternatives = listOf(
                    cleanName.replace("_01", ""),
                    cleanName.replace("01", ""),
                    "img_$cleanName",
                    cleanName.take(10)
                )

                for (alt in alternatives) {
                    val altId = context.resources.getIdentifier(alt, "drawable", context.packageName)
                    if (altId != 0) {
                        Log.d(TAG, "✅ Alternative trouvée: '$imageName' -> '$alt' -> $altId")
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

    /**
     * ✅ NOUVELLE MÉTHODE : Nettoie le nom d'image pour Android
     */
    private fun cleanImageName(imageName: String): String {
        return imageName
            .substringBeforeLast(".")           // Enlever extension (.png, .jpg)
            .replace(" ", "_")                  // Espaces -> underscores
            .replace("-", "_")                  // Tirets -> underscores
            .replace(Regex("[^a-zA-Z0-9_]"), "") // Enlever caractères spéciaux
            .lowercase()                        // Minuscules obligatoires
    }

    /**
     * ✅ MÉTHODE POUR CHARGER IMAGE DEPUIS ASSETS (à utiliser dans les Composables)
     */
    suspend fun loadImageFromAssets(imageName: String): ByteArray? {
        return try {
            val cleanName = cleanImageName(imageName)

            // Essayer nom original puis nom nettoyé
            val finalName = try {
                context.assets.open("$IMAGES_ASSETS_FOLDER/$imageName").close()
                imageName
            } catch (e: IOException) {
                context.assets.open("$IMAGES_ASSETS_FOLDER/$cleanName").close()
                cleanName
            }

            val inputStream = context.assets.open("$IMAGES_ASSETS_FOLDER/$finalName")
            val bytes = inputStream.readBytes()
            inputStream.close()

            Log.d(TAG, "✅ Image chargée depuis assets: '$finalName' (${bytes.size} bytes)")
            bytes

        } catch (e: IOException) {
            Log.w(TAG, "❌ Impossible de charger image depuis assets: '$imageName'")
            null
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
}