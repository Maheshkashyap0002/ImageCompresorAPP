package com.maheshcompressor.ui.screen.homesccreen

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.maheshcompressor.R
import com.maheshcompressor.compressExactKB
import com.maheshcompressor.findActivity
import com.maheshcompressor.loadCorrectBitmap
import com.maheshcompressor.nofication.showNotification
import com.maheshcompressor.ui.screen.premiumscreen.isPremiumUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {

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

    Scaffold(
        topBar = {
            HomeScreenTopBar(text = "Compress Your Image")
        }
    ) { innerPadding->

        Box(modifier = Modifier.fillMaxSize()
            .background(Color.White)){




            Box(modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {



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


    }




}