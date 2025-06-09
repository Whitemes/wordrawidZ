// data/local/SimpleMysteryLoader.kt
package fr.uge.wordrawidx.data.local

import android.content.Context
import android.util.Log
import fr.uge.wordrawidx.data.model.MysteryObject
import fr.uge.wordrawidx.R

/**
 * Version simplifiée pour transition sans backend JSON
 * À utiliser si vous voulez tester l'architecture repository sans le JSON backend
 */
class SimpleMysteryLoader(private val context: Context) {

    companion object {
        private const val TAG = "SimpleMysteryLoader"
    }

    /**
     * Retourne une liste étendue d'objets mystères (version hardcodée étendue)
     */
    fun getExtendedMysteryObjects(): List<MysteryObject> {
        Log.i(TAG, "Chargement liste étendue hardcodée")

        return listOf(
            MysteryObject(
                word = "souris",
                closeWords = listOf(
                    "clavier", "pointeur", "USB", "ordinateur", "molette",
                    "écran", "clic", "tapis", "filaire", "bluetooth", "mulot", "rongeur"
                ),
                imageName = "img_souris.png",
                imageResourceId = R.drawable.img_souris
            ),
            MysteryObject(
                word = "guitare",
                closeWords = listOf(
                    "corde", "instrument", "musique", "électrique", "acoustique",
                    "amplificateur", "mélodie", "basse", "accord", "piano", "rock", "jazz"
                ),
                imageName = "img_guitare.png",
                imageResourceId = R.drawable.img_guitare
            ),
            MysteryObject(
                word = "bouteille",
                closeWords = listOf(
                    "eau", "verre", "bouchon", "plastique", "boisson",
                    "liquide", "vin", "conteneur", "canette", "verrerie", "capsule", "étiquette"
                ),
                imageName = "img_bouteille.png",
                imageResourceId = R.drawable.img_bouteille
            ),
            // ✨ NOUVEAUX OBJETS HARDCODÉS pour tester l'architecture
            MysteryObject(
                word = "voiture",
                closeWords = listOf(
                    "automobile", "véhicule", "moteur", "roue", "essence",
                    "conduite", "route", "transport", "vitesse", "garage", "parking", "freins"
                ),
                imageName = "img_voiture.png",
                imageResourceId = null // Pas d'image pour l'instant
            ),
            MysteryObject(
                word = "ordinateur",
                closeWords = listOf(
                    "clavier", "écran", "souris", "processeur", "mémoire",
                    "logiciel", "internet", "programme", "données", "fichier", "windows", "mac"
                ),
                imageName = "img_ordinateur.png",
                imageResourceId = null
            ),
            MysteryObject(
                word = "livre",
                closeWords = listOf(
                    "page", "lecture", "histoire", "auteur", "bibliothèque",
                    "roman", "papier", "texte", "chapitre", "écriture", "littérature", "savoir"
                ),
                imageName = "img_livre.png",
                imageResourceId = null
            ),
            MysteryObject(
                word = "téléphone",
                closeWords = listOf(
                    "appel", "communication", "portable", "smartphone", "écran",
                    "application", "message", "contact", "sonnerie", "réseau", "wifi", "bluetooth"
                ),
                imageName = "img_telephone.png",
                imageResourceId = null
            ),
            MysteryObject(
                word = "chat",
                closeWords = listOf(
                    "animal", "domestique", "félin", "miaou", "ronronnement",
                    "poils", "griffes", "moustaches", "queue", "litière", "croquettes", "compagnie"
                ),
                imageName = "img_chat.png",
                imageResourceId = null
            )
        )
    }

    /**
     * Simule le chargement avec délai (pour tester les coroutines)
     */
    suspend fun loadMysteryObjectsAsync(): List<MysteryObject> {
        Log.d(TAG, "Simulation chargement asynchrone...")
        kotlinx.coroutines.delay(500) // Simuler temps de chargement
        return getExtendedMysteryObjects()
    }

    /**
     * Stats de la version étendue
     */
    fun getStats(): String {
        val objects = getExtendedMysteryObjects()
        val totalWords = objects.sumOf { it.closeWords.size }
        val withImages = objects.count { it.imageResourceId != null }

        return "SimpleMysteryLoader: ${objects.size} objets, $totalWords mots, $withImages avec images"
    }
}