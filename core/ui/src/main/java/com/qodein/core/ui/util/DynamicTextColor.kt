package com.qodein.core.ui.util

import android.graphics.Bitmap
import android.graphics.Color.blue
import android.graphics.Color.green
import android.graphics.Color.red
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.pow

/**
 * Enterprise-level dynamic text color utility that automatically determines
 * optimal text color (white/black) based on image brightness analysis.
 *
 * Uses Palette API for professional-grade color analysis with intelligent fallbacks.
 */

/**
 * Composable that dynamically determines text color based on image brightness.
 *
 * @param imageUrl URL of the banner image to analyze
 * @param onTextColorChanged Callback when optimal text color is determined
 */
@Composable
fun DynamicTextColor(
    imageUrl: String,
    onTextColorChanged: (Color) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(imageUrl) {
        if (imageUrl.isBlank()) {
            onTextColorChanged(Color.White) // Default fallback
            return@LaunchedEffect
        }

        try {
            val textColor = withContext(Dispatchers.IO) {
                analyzeImageBrightness(context, imageUrl)
            }
            onTextColorChanged(textColor)
        } catch (e: Exception) {
            // Graceful fallback to white text
            onTextColorChanged(Color.White)
        }
    }
}

/**
 * Analyzes image brightness and returns optimal text color.
 *
 * @param context Android context for image loading
 * @param imageUrl URL of the image to analyze
 * @return Color.Black for bright backgrounds, Color.White for dark backgrounds
 */
private suspend fun analyzeImageBrightness(
    context: android.content.Context,
    imageUrl: String
): Color {
    val imageLoader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .allowHardware(false) // Disable hardware bitmaps for Palette analysis
        .build()

    val drawable = imageLoader.execute(request).drawable
        ?: return Color.White // Fallback if image fails to load

    val bitmap = (drawable as? BitmapDrawable)?.bitmap
        ?: return Color.White // Fallback if not a bitmap

    return analyzeBitmapBrightness(bitmap)
}

/**
 * Analyzes bitmap using Palette API for professional color analysis.
 *
 * @param bitmap The bitmap to analyze
 * @return Optimal text color based on dominant colors and brightness
 */
private fun analyzeBitmapBrightness(bitmap: Bitmap): Color {
    // Use Palette to extract dominant colors from image
    val palette = Palette.from(bitmap)
        .maximumColorCount(16) // Efficient color analysis
        .generate()

    // Get the most vibrant or dominant color from the palette
    val dominantColor = palette.dominantSwatch?.rgb
        ?: palette.vibrantSwatch?.rgb
        ?: palette.mutedSwatch?.rgb
        ?: run {
            // If no palette colors found, analyze average bitmap color directly
            return analyzeBitmapAverageColor(bitmap)
        }

    // Calculate luminance using standard formula (ITU-R BT.709)
    val luminance = calculateLuminance(dominantColor)

    // Return white text for dark backgrounds, black text for bright backgrounds
    // Using lower threshold (0.3) to be more aggressive with black text on bright backgrounds
    return if (luminance > 0.3f) {
        Color.Black // Bright background = dark text
    } else {
        Color.White // Dark background = light text
    }
}

/**
 * Calculates relative luminance using ITU-R BT.709 standard.
 *
 * @param color RGB color value
 * @return Luminance value between 0.0 (darkest) and 1.0 (brightest)
 */
private fun calculateLuminance(color: Int): Float {
    val red = red(color) / 255f
    val green = green(color) / 255f
    val blue = blue(color) / 255f

    // Apply gamma correction and luminance weights
    val linearRed = if (red <= 0.03928f) red / 12.92f else ((red + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
    val linearGreen = if (green <= 0.03928f) green / 12.92f else ((green + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
    val linearBlue = if (blue <= 0.03928f) blue / 12.92f else ((blue + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()

    // ITU-R BT.709 luminance coefficients
    return 0.2126f * linearRed + 0.7152f * linearGreen + 0.0722f * linearBlue
}

/**
 * Fallback method to analyze bitmap average color when Palette fails.
 *
 * @param bitmap The bitmap to analyze
 * @return Optimal text color based on average brightness
 */
private fun analyzeBitmapAverageColor(bitmap: Bitmap): Color {
    // Sample pixels from center region to avoid edge artifacts
    val centerX = bitmap.width / 2
    val centerY = bitmap.height / 2
    val sampleSize = kotlin.math.min(bitmap.width, bitmap.height) / 4

    var totalRed = 0L
    var totalGreen = 0L
    var totalBlue = 0L
    var pixelCount = 0

    // Sample pixels in a grid pattern for efficiency
    val step = kotlin.math.max(1, sampleSize / 20)
    for (x in (centerX - sampleSize / 2) until (centerX + sampleSize / 2) step step) {
        for (y in (centerY - sampleSize / 2) until (centerY + sampleSize / 2) step step) {
            if (x >= 0 && x < bitmap.width && y >= 0 && y < bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                totalRed += red(pixel)
                totalGreen += green(pixel)
                totalBlue += blue(pixel)
                pixelCount++
            }
        }
    }

    if (pixelCount == 0) return Color.Black // Safe fallback

    val avgRed = (totalRed / pixelCount).toInt()
    val avgGreen = (totalGreen / pixelCount).toInt()
    val avgBlue = (totalBlue / pixelCount).toInt()

    val averageColor = android.graphics.Color.rgb(avgRed, avgGreen, avgBlue)
    val luminance = calculateLuminance(averageColor)

    return if (luminance > 0.3f) {
        Color.Black // Bright background = dark text
    } else {
        Color.White // Dark background = light text
    }
}

/**
 * Remember-based API for dynamic text color state management.
 *
 * @param imageUrl URL of the banner image to analyze
 * @return Mutable state containing the optimal text color
 */
@Composable
fun rememberDynamicTextColor(imageUrl: String): androidx.compose.runtime.MutableState<Color> {
    val textColor = remember { mutableStateOf(Color.White) }

    DynamicTextColor(imageUrl = imageUrl) { color ->
        textColor.value = color
    }

    return textColor
}
