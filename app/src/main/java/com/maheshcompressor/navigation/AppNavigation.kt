package com.maheshcompressor.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.maheshcompressor.ui.screen.homesccreen.HomeScreen
import com.maheshcompressor.ui.screen.premiumscreen.PremiumScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {

        composable("home") {
            HomeScreen(navController)
        }

        composable("premium") {
            PremiumScreen(navController)
        }
    }
}