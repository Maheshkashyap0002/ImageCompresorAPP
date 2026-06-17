package com.maheshcompressor.data.repository

import android.content.Context
import com.google.firebase.database.DatabaseReference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: DatabaseReference
) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    val isPremiumUserFlow: Flow<Boolean> = callbackFlow {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "isPremium") {
                trySend(sharedPreferences.getBoolean("isPremium", false))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getBoolean("isPremium", false))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun isPremiumUser(): Boolean {
        return prefs.getBoolean("isPremium", false)
    }

    fun setPremiumStatus(isPremium: Boolean) {
        prefs.edit().putBoolean("isPremium", isPremium).apply()
    }

    fun clearPremiumStatus() {
        prefs.edit().clear().apply()
    }

    suspend fun validateCode(code: String): Result<Boolean> {
        return try {
            val snapshot = db.child("codes").child(code).get().await()
            if (snapshot.exists()) {
                val isUsed = snapshot.getValue(Boolean::class.java) ?: false
                if (!isUsed) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Code already used"))
                }
            } else {
                Result.failure(Exception("Invalid code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markCodeAsUsed(code: String) {
        db.child("codes").child(code).setValue(true).await()
    }
}
