package com.example.imagecompressor

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun PremiumScreen(navController: NavController) {

    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var codeInput by remember { mutableStateOf(TextFieldValue("")) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ✅ MAIN UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text("Enter Premium Code", fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = codeInput,
                onValueChange = { codeInput = it },
                label = { Text("Code") }
            )

            Spacer(Modifier.height(20.dp))

            Button(
                enabled = codeInput.text.isNotBlank(),
                onClick = {
                    scope.launch {
                        isLoading = true
                        delay(1000)

                        if (codeInput.text == "Mahesh") {
                            setPremium(context, true)
                            Toast.makeText(context, "Premium Activated ✅", Toast.LENGTH_SHORT)
                                .show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Invalid Code ❌", Toast.LENGTH_SHORT).show()
                        }

                        isLoading = false
                    }
                }
            ) {
                Text("Activate Premium")
            }

            Spacer(Modifier.height(20.dp))

            Button(onClick = {
                scope.launch {
                    isLoading = true
                    delay(1000)

                    clearPremium(context)

                    isLoading = false
                    Toast.makeText(context, "Premium Reset ✅", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Reset Premium")
            }

            Spacer(Modifier.height(20.dp))

            Button(onClick = {
                navController.popBackStack()
            }) {
                Text("Back")
            }
        }

        // ✅ FULL SCREEN LOADING (CENTERED PERFECTLY)
        if (isLoading) {

            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.loading1)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
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

fun clearPremium(context: Context) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit().clear().apply()
}