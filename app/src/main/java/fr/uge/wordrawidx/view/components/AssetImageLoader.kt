// view/components/AssetImageLoader.kt
package fr.uge.wordrawidx.view.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.uge.wordrawidx.data.local.AssetLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Composable pour charger des images depuis assets/images/ ou drawable/
 * Compatible avec l'architecture Repository backend
 */
@Composable
fun AssetImageLoader(
    imageName: String,
    imageResourceId: Int?,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current
    var imageBitmap by remember(imageName) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(imageName) { mutableStateOf(true) }
    var hasError by remember(imageName) { mutableStateOf(false) }

    // Chargement de l'image depuis assets ou drawable
    LaunchedEffect(imageName, imageResourceId) {
        isLoading = true
        hasError = false

        try {
            when {
                // Cas 1: Image dans drawable/ (ID fourni)
                imageResourceId != null && imageResourceId != AssetLoader.ASSETS_IMAGE_PLACEHOLDER_ID -> {
                    imageBitmap = ImageBitmap.imageResource(context.resources, imageResourceId)
                    isLoading = false
                }

                // Cas 2: Image dans assets/images/ (chargement dynamique)
                else -> {
                    val assetLoader = AssetLoader(context)
                    val imageBytes = withContext(Dispatchers.IO) {
                        assetLoader.loadImageFromAssets(imageName)
                    }

                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        imageBitmap = bitmap.asImageBitmap()
                    } else {
                        hasError = true
                    }
                    isLoading = false
                }
            }
        } catch (e: Exception) {
            hasError = true
            isLoading = false
        }
    }

    // Affichage conditionnel
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            hasError -> {
                // Fallback : Image par défaut ou placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🖼️",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            imageBitmap != null -> {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
        }
    }
}

/**
 * Version simplifiée pour les portions d'image (utilisée dans GameBoard)
 */
@Composable
fun AssetImagePortion(
    imageName: String,
    imageResourceId: Int?,
    portionIndex: Int,
    gridSize: Int = 5,
    modifier: Modifier = Modifier
) {
    // Pour l'instant, utiliser l'image complète
    // TODO: Implémenter le découpage de portions pour les images assets
    AssetImageLoader(
        imageName = imageName,
        imageResourceId = imageResourceId,
        contentDescription = "Portion $portionIndex de $imageName",
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}