package com.maheshcompressor.ui.screen.premiumscreen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.maheshcompressor.R
import com.google.firebase.database.FirebaseDatabase
import com.maheshcompressor.nofication.showNotification
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(navController: NavController) {

    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var codeInput by remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(
        topBar = {
            PremiumScreenTopBar(navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = Color.White)
                .verticalScroll(rememberScrollState()),

            ) {


            Spacer(Modifier.height(25.dp))

            Image(
                painter = painterResource(id = R.drawable.premium_banner),
                contentDescription = "Premium Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth().size(330.dp)
                    .padding(horizontal = 24.dp)
                    .border(
                        1.dp, Color.LightGray,
                        RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "Enter Premium Code",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black, fontSize = 16.sp
                    )

                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it },
                        label = { Text("Premium Code" ) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))

                    // ✅ ACTIVATE BUTTON
                    Button(
                        enabled = codeInput.text.isNotBlank(),
                        onClick = {
                            scope.launch {
                                isLoading = true
                                delay(1000)

                                val db =
                                    FirebaseDatabase.getInstance().reference
                                val code = codeInput.text.trim()

                                db.child("codes").child(code).get()
                                    .addOnSuccessListener { snapshot ->

                                        if (snapshot.exists()) {

                                            val isUsed =
                                                snapshot.getValue(Boolean::class.java) ?: false

                                            if (!isUsed) {

                                                // ✅ Mark code as used
                                                db.child("codes").child(code).setValue(true)

                                                setPremium(context, true)

                                                showNotification(
                                                    context,
                                                    "Premium Upgrade 🚀",
                                                    "You are now a Premium user"
                                                )

                                                Toast.makeText(
                                                    context,
                                                    "Premium Activated ✅",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                navController.popBackStack()

                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Code already used ❌",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Invalid Code ❌",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }


                                    }.addOnFailureListener {
                                    isLoading = false
                                    Toast.makeText(
                                        context,
                                        "Error: ${it.message}",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }



                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().size(35.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkGray,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(10.dp)
                    ) {
                        Text("Activate Premium 💎")
                    }

                    Spacer(Modifier.height(15.dp))


                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                delay(1000)

                                clearPremium(context)

                                showNotification(
                                    context,
                                    "Premium Removed ♻️",
                                    "You are now a Free user"
                                )

                                isLoading = false

                                Toast.makeText(
                                    context,
                                    "Premium Reset ✅",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().size(35.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkGray,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(10.dp)
                    ) {
                        Text("Reset Premium ♻️")
                    }

                    Spacer(Modifier.height(15.dp))

                    // ✅ BACK BUTTON
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth().size(35.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkGray,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(10.dp)
                    ) {
                        Text("Back 🏠")
                    }
                    Spacer(Modifier.height(15.dp))

                }
            }
        }

        // ✅ LOADING ANIMATION (CENTER)
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


// ✅ CLEAR PREMIUM
fun clearPremium(context: Context) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit().clear().apply()
}