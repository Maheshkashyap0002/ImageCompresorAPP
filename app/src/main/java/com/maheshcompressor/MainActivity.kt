package com.maheshcompressor

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import androidx.core.app.ActivityCompat
import com.google.firebase.FirebaseApp
import com.maheshcompressor.navigation.AppNavigation

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }

        setContent {
            AppNavigation()
        }


    }

    override fun onResume() {
        super.onResume()

        //(application as MyApp).appOpenAdManager.showAdIfAvailable(this)
    }
}


fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
/* ===================== EXACT KB COMPRESSOR ===================== */

fun compressExactKB(bitmap: Bitmap, targetKB: Int): ByteArray {

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

    return bestBytes

}

/* ===================== FIX IMAGE ROTATION ===================== */

fun loadCorrectBitmap(context: Context, uri: Uri): Bitmap? {

    val input = context.contentResolver.openInputStream(uri) ?: return null
    val bitmap = BitmapFactory.decodeStream(input)
    input.close()

    val exifInput = context.contentResolver.openInputStream(uri) ?: return bitmap
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

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

