// data/model/MysteryObjectData.kt
package fr.uge.wordrawidx.data.model

import kotlinx.serialization.Serializable

/**
 * Structure exacte du JSON backend généré par votre binôme
 */
@Serializable
data class BackendWordPack(
    val pack_info: PackInfo,
    val words: Map<String, WordData>
)

@Serializable
data class PackInfo(
    val version: String,
    val word_count: Int,
    val selection_method: String,
    val generation_date: String,
    val max_hints_per_word: Int,
    val max_words_processed: Int
)

@Serializable
data class WordData(
    val french_name: String,
    val english_name: String,
    val image_name: String,
    val local_image_filename: String,
    val hints: List<HintData>
)

@Serializable
data class HintData(
    val word: String,
    val similarity_score: Double
)

/**
 * Modèle converti pour l'usage dans l'app (compatible avec votre GameState existant)
 */
data class MysteryObject(
    val word: String,                    // french_name du backend
    val closeWords: List<String>,        // Liste des hints.word
    val imageName: String,               // local_image_filename
    val imageResourceId: Int?,           // ID Android drawable (résolu)
    val englishName: String = "",        // english_name (optionnel)
    val similarityScores: List<Double> = emptyList()  // hints.similarity_score (pour debug)
) {
    companion object {
        /**
         * Convertit WordData (backend) vers MysteryObject (app)
         */
        fun fromBackendData(
            key: String,
            data: WordData,
            resourceResolver: (String) -> Int?
        ): MysteryObject {
            return MysteryObject(
                word = data.french_name,
                closeWords = data.hints.map { it.word },
                imageName = data.local_image_filename,
                imageResourceId = resourceResolver(data.local_image_filename),
                englishName = data.english_name,
                similarityScores = data.hints.map { it.similarity_score }
            )
        }
    }
}