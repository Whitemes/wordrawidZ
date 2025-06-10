// view/components/AssetImageLoader.kt
package fr.uge.wordrawidx.view.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
    modifier: Modifier = Modifier,
    imageName: String,
    imageResourceId: Int?,
    contentDescription: String? = null,
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
 * ✅ NOUVEAU : Découpage de portions pour images assets
 */
@Composable
fun AssetImagePortion(
    imageName: String,
    imageResourceId: Int?,
    portionIndex: Int,
    gridSize: Int = 5,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var imageBitmap by remember(imageName) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(imageName) { mutableStateOf(true) }
    var hasError by remember(imageName) { mutableStateOf(false) }

    // Chargement de l'image depuis assets
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

    // Affichage avec découpage de portion
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            hasError -> {
                // Fallback : Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🖼️",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            imageBitmap != null -> {
                // ✅ DÉCOUPAGE DE PORTION comme dans l'ancien code
                AssetImagePortionCanvas(
                    imageBitmap = imageBitmap!!,
                    portionIndex = portionIndex,
                    gridSize = gridSize,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * ✅ DÉCOUPAGE CANVAS pour images assets (identique à PortionOfImageInCellBitmap)
 */
@Composable
fun AssetImagePortionCanvas(
    modifier: Modifier = Modifier,
    imageBitmap: ImageBitmap,
    portionIndex: Int,
    gridSize: Int = 5
) {
    // Calcul des coordonnées de la portion dans la grille
    val row = portionIndex / gridSize
    val col = portionIndex % gridSize

    Canvas(modifier = modifier.fillMaxSize()) {
        // Dimensions de l'image source
        val imageWidth = imageBitmap.width.toFloat()
        val imageHeight = imageBitmap.height.toFloat()

        // Taille d'une portion dans l'image source
        val portionWidth = imageWidth / gridSize
        val portionHeight = imageHeight / gridSize

        // Rectangle source : quelle partie de l'image extraire
        val srcLeft = (col * portionWidth).toInt()
        val srcTop = (row * portionHeight).toInt()
        val srcRight = ((col + 1) * portionWidth).toInt().coerceAtMost(imageBitmap.width)
        val srcBottom = ((row + 1) * portionHeight).toInt().coerceAtMost(imageBitmap.height)

        // Dessiner la portion extraite sur tout le canvas de la cellule
        drawImage(
            image = imageBitmap,
            srcOffset = IntOffset(srcLeft, srcTop),
            srcSize = IntSize(srcRight - srcLeft, srcBottom - srcTop),
            dstOffset = IntOffset(0, 0),
            dstSize = IntSize(size.width.toInt(), size.height.toInt())
        )
    }
}