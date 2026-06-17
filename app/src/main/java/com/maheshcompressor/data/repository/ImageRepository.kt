package com.maheshcompressor.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class ImageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun loadCorrectBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val input = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bitmap = BitmapFactory.decodeStream(input)
            input.close()

            val exifInput = context.contentResolver.openInputStream(uri) ?: return@withContext bitmap
            val exif = ExifInterface(exifInput)

            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            exifInput.close()

            val matrix = Matrix()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            if (orientation == ExifInterface.ORIENTATION_NORMAL) return@withContext bitmap

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun compressExactKB(bitmap: Bitmap, targetKB: Int): ByteArray = withContext(Dispatchers.Default) {
        var low = 5
        var high = 100
        var bestBytes = byteArrayOf()
        var bestDiff = Int.MAX_VALUE

        var bmp = bitmap

        if (bmp.width > 2000 || bmp.height > 2000) {
            bmp = Bitmap.createScaledBitmap(bmp, bmp.width / 2, bmp.height / 2, true)
        }

        while (low <= high) {
            val mid = (low + high) / 2
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, mid, stream)
            val bytes = stream.toByteArray()
            val sizeKB = bytes.size / 1024
            val diff = abs(sizeKB - targetKB)

            if (diff < bestDiff) {
                bestDiff = diff
                bestBytes = bytes
            }

            if (sizeKB > targetKB) high = mid - 1 else low = mid + 1
        }
        bestBytes
    }

    suspend fun saveImageToMediaStore(bytes: ByteArray): Uri? = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        uri?.let {
            val out = context.contentResolver.openOutputStream(it)
            out?.write(bytes)
            out?.close()
        }
        uri
    }
}
