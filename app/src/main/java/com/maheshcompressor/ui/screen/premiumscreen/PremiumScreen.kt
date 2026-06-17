package com.maheshcompressor.ui.screen.premiumscreen

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.maheshcompressor.R
import com.maheshcompressor.nofication.showNotification
import com.maheshcompressor.ui.screen.premiumscreen.viewmodel.PremiumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun PremiumScreen(
    navController: NavController,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

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
                    .fillMaxWidth()
                    .size(330.dp)
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
                        value = viewModel.codeInput,
                        onValueChange = { viewModel.onCodeInputChange(it) },
                        label = { Text("Premium Code") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(
                        enabled = viewModel.codeInput.text.isNotBlank(),
                        onClick = {
                            viewModel.activatePremium(
                                onSuccess = {
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
                                },
                                onError = { message ->
                                    Toast.makeText(
                                        context,
                                        message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
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
                            viewModel.resetPremium(
                                onSuccess = {
                                    showNotification(
                                        context,
                                        "Premium Removed ♻️",
                                        "You are now a Free user"
                                    )
                                    Toast.makeText(
                                        context,
                                        "Premium Reset ✅",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
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

    }

    if (uiState.isLoading) {
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
