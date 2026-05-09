package com.maheshcompressor

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import kotlin.math.abs
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.activity.compose.BackHandler
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.maheshcompressor.R
import com.maheshcompressor.nofication.showNotification
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        // ✅ ASK PERMISSION
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

@Composable
fun ImageCompressorApp(navController: NavHostController) {

    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var compressedUri by remember { mutableStateOf<Uri?>(null) }
    var targetKB by remember { mutableStateOf(TextFieldValue("50")) }
    var resultText by remember { mutableStateOf("") }
    var showExitDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            bitmap = loadCorrectBitmap(context, it)   // ✅ FIXED ROTATION HERE
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Spacer(Modifier.height(30.dp))
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
                    .background(Color.LightGray),
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
                    .padding(horizontal = 80.dp),
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

                    scope.launch {

                        isLoading = true

                        delay(1000)

                        isLoading = false

                        val bmp = bitmap ?: return@launch
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

                        resultText = "Target: ${target}KB | Final: ${bytes.size / 1024}KB ✔"
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

            Spacer(Modifier.height(30.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DESIGN BY MAHESH",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )

            }
            Spacer(Modifier.height(20.dp))



            Button(onClick = {
                navController.navigate("premium")
            },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 10.dp,
                    pressedElevation = 15.dp,
                    disabledElevation = 0.dp
                )
                ) {
                Text("Go Premium 🚀")
            }

            Spacer(Modifier.height(20.dp))

            // For Banner Ads
            val context = LocalContext.current
            val activity = context.findActivity()

            val isPremium = isPremiumUser(context)

            if (!isPremium && activity != null) {

                val adView = remember {
                    AdView(activity).apply {   // ✅ use activity NOT context
                        setAdSize(AdSize.BANNER)
                        adUnitId = "ca-app-pub-6111799346544791/4637625914"
                    }
                }

                DisposableEffect(Unit) {
                    adView.loadAd(AdRequest.Builder().build())

                    onDispose {
                        adView.destroy()
                    }
                }

                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { adView }
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
                                showNotification(
                                    context,
                                    "Please Rate us ❤\uFE0F",
                                    "Thanks for using Image Compressor App",
                                    openPlayStore = true
                                )

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
        // for Lottie Animation
        if (isLoading) {

            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.loading1)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)), // 🔥 optional dim background
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(150.dp)
                )
            }
        }
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

