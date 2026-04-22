package com.example.imagecompressor

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.io.ByteArrayOutputStream
import java.nio.file.WatchEvent
import kotlin.math.abs
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.activity.compose.BackHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageCompressorApp()
        }
    }
}

@Composable
fun ImageCompressorApp() {

    val context = LocalContext.current

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var compressedUri by remember { mutableStateOf<Uri?>(null) }
    var targetKB by remember { mutableStateOf(TextFieldValue("50")) }
    var resultText by remember { mutableStateOf("") }
    var showExitDialog by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            bitmap = loadCorrectBitmap(context, it)   // ✅ FIXED ROTATION HERE
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Spacer(Modifier.height(50.dp))
        Text(
            "Compress Your Image",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(Modifier.height(15.dp))

        Box(
            modifier = Modifier
                .size(250.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(androidx.compose.ui.graphics.Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } ?: Text("No Image")
        }

        Spacer(Modifier.height(20.dp))
        BackHandler {
            showExitDialog = true
        }
        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 110.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue,
                contentColor = Color.White
            ),

            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp,
                pressedElevation = 15.dp,
                disabledElevation = 0.dp
            )
        ) {
            Text("Select Image 📂")

        }

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = targetKB,
            onValueChange = { targetKB = it },
            label = { Text("Target KB") }
        )

        Spacer(Modifier.height(20.dp))
        Text(
            resultText,
            textAlign = TextAlign.Center,
            color = Color.DarkGray,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(10.dp))
        Button(
            enabled = bitmap != null,
            onClick = {

                val bmp = bitmap ?: return@Button
                val target = targetKB.text.toIntOrNull() ?: 50

                val bytes = compressExactKB(bmp, target)

                val values = ContentValues().apply {
                    put(
                        MediaStore.Images.Media.DISPLAY_NAME,
                        "IMG_${System.currentTimeMillis()}.jpg"
                    )
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
                    compressedUri = it
                }

                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                Toast.makeText(context, "Image Downloding...", Toast.LENGTH_SHORT).show()

                resultText = "Target: ${target}KB | Final: ${bytes.size / 1024}KB ✔"
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue,
                contentColor = Color.White
            ),

            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp,
                pressedElevation = 15.dp,
                disabledElevation = 0.dp
            )
        ) {
            Text("Compress 🧠")
        }

        Spacer(Modifier.height(10.dp))

        Button(
            enabled = compressedUri != null,
            onClick = {
                compressedUri?.let {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/jpeg"
                        putExtra(Intent.EXTRA_STREAM, it)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share"))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp,
                pressedElevation = 15.dp,
                disabledElevation = 0.dp
            ),
        ) {
            Text("Share 📤")
        }

        Spacer(Modifier.height(50.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "DESIGN BY MAHESH",
                color = androidx.compose.ui.graphics.Color.Gray,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Exit") },
                text = { Text("Are you sure you want to exit?") },

                confirmButton = {
                    TextButton(
                        onClick = {
                            showExitDialog = false
                            (context as? ComponentActivity)?.finish()
                        }
                    ) {
                        Text("Exit")
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = { showExitDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
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

fun loadCorrectBitmap(context: android.content.Context, uri: Uri): Bitmap? {

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