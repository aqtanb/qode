package com.qodein.core.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import co.touchlab.kermit.Logger
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.StorageError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

/**
 * Utility for compressing images with proper orientation handling.
 * Compresses images to JPEG format with configurable quality and max dimensions.
 */
object ImageCompressor {
    private const val TAG = "ImageCompressor"
    private const val DEFAULT_QUALITY = 85
    private const val DEFAULT_MAX_DIMENSION = 2048
    private const val COMPRESSED_IMAGE_PREFIX = "compressed_"

    /**
     * Compress an image from URI and save to app cache directory.
     *
     * @param context Android context
     * @param uri Source image URI
     * @param quality JPEG quality (0-100), default 85
     * @param maxDimension Maximum width/height, default 2048px
     * @return Result with URI of compressed image or compression error
     */
    suspend fun compressImage(
        context: Context,
        uri: Uri,
        quality: Int = DEFAULT_QUALITY,
        maxDimension: Int = DEFAULT_MAX_DIMENSION
    ): Result<Uri, StorageError.CompressionFailure> =
        withContext(Dispatchers.IO) {
            try {
                Logger.d(TAG) { "Starting compression for: $uri" }

                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: run {
                        Logger.e(TAG) { "Failed to open input stream for: $uri" }
                        return@withContext Result.Error(StorageError.CompressionFailure.CannotReadImage)
                    }

                val originalBitmap = inputStream.use { stream ->
                    BitmapFactory.decodeStream(stream)
                } ?: run {
                    Logger.e(TAG) { "Failed to decode bitmap from: $uri" }
                    return@withContext Result.Error(StorageError.CompressionFailure.InvalidImageFormat)
                }

                Logger.d(TAG) { "Original dimensions: ${originalBitmap.width}x${originalBitmap.height}" }

                val rotatedBitmap = rotateImageIfRequired(context, uri, originalBitmap)
                val scaledBitmap = scaleImage(rotatedBitmap, maxDimension)

                if (scaledBitmap != rotatedBitmap) {
                    rotatedBitmap.recycle()
                }
                if (rotatedBitmap != originalBitmap) {
                    originalBitmap.recycle()
                }

                Logger.d(TAG) { "Compressed dimensions: ${scaledBitmap.width}x${scaledBitmap.height}" }

                val compressedFile = File(
                    context.cacheDir,
                    "$COMPRESSED_IMAGE_PREFIX${System.currentTimeMillis()}.jpg",
                )

                FileOutputStream(compressedFile).use { outputStream ->
                    val compressed = scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                    if (!compressed) {
                        Logger.e(TAG) { "Bitmap compress returned false" }
                        return@withContext Result.Error(StorageError.CompressionFailure.CompressionFailed)
                    }
                }

                scaledBitmap.recycle()

                val compressedUri = Uri.fromFile(compressedFile)
                Logger.i(TAG) { "Compression successful: $compressedUri (${compressedFile.length() / 1024}KB)" }
                Result.Success(compressedUri)
            } catch (e: IOException) {
                Logger.e(TAG, e) { "IO error during compression" }
                Result.Error(StorageError.CompressionFailure.CannotReadImage)
            } catch (e: OutOfMemoryError) {
                Logger.e(TAG, e) { "Out of memory during compression" }
                Result.Error(StorageError.CompressionFailure.OutOfMemory)
            } catch (e: Exception) {
                Logger.e(TAG, e) { "Unexpected error during compression" }
                Result.Error(StorageError.CompressionFailure.CompressionFailed)
            }
        }

    /**
     * Compress multiple images sequentially.
     *
     * @param context Android context
     * @param uris List of source image URIs
     * @param quality JPEG quality (0-100), default 85
     * @param maxDimension Maximum width/height, default 2048px
     * @param onProgress Callback with (current, total) progress
     * @return Result with list of successfully compressed image URIs
     */
    suspend fun compressImages(
        context: Context,
        uris: List<Uri>,
        quality: Int = DEFAULT_QUALITY,
        maxDimension: Int = DEFAULT_MAX_DIMENSION,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<List<Uri>, StorageError.CompressionFailure> =
        withContext(Dispatchers.IO) {
            val compressedUris = mutableListOf<Uri>()

            uris.forEachIndexed { index, uri ->
                onProgress(index + 1, uris.size)
                when (val result = compressImage(context, uri, quality, maxDimension)) {
                    is Result.Success -> {
                        compressedUris.add(result.data)
                    }
                    is Result.Error -> {
                        Logger.w(TAG) { "Failed to compress image $uri: ${result.error}" }
                        return@withContext Result.Error(result.error)
                    }
                }
            }

            Logger.i(TAG) { "Compressed ${compressedUris.size}/${uris.size} images successfully" }
            Result.Success(compressedUris)
        }

    /**
     * Clear compressed images from cache directory.
     */
    fun clearCompressedImages(context: Context) {
        try {
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith(COMPRESSED_IMAGE_PREFIX)) {
                    file.delete()
                    Logger.d(TAG) { "Deleted cached compressed image: ${file.name}" }
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Error clearing compressed images" }
        }
    }

    private fun rotateImageIfRequired(
        context: Context,
        uri: Uri,
        bitmap: Bitmap
    ): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap

        return try {
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: IOException) {
            Logger.w(TAG) { "Failed to read EXIF data, using original orientation" }
            bitmap
        } finally {
            inputStream.close()
        }
    }

    private fun rotateBitmap(
        bitmap: Bitmap,
        degrees: Float
    ): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        return rotatedBitmap
    }

    private fun scaleImage(
        bitmap: Bitmap,
        maxDimension: Int
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }

        val scale = min(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true).also {
            if (it != bitmap) {
                bitmap.recycle()
            }
        }
    }
}
