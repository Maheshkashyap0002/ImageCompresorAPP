package com.maheshcompressor.ui.screen.premiumscreen

import android.content.Context


fun setPremium(context: Context, value: Boolean) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("isPremium", value).apply()
}

fun isPremiumUser(context: Context): Boolean {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("isPremium", false)
}