package com.maheshcompressor

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {

        composable("home") {
            ImageCompressorApp(navController)
        }

        composable("premium") {
            PremiumScreen(navController)
        }
    }
}