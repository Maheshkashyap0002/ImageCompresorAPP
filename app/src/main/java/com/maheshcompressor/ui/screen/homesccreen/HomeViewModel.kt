package com.maheshcompressor.ui.screen.homesccreen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maheshcompressor.data.repository.ImageRepository
import com.maheshcompressor.data.repository.PremiumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val imageRepository: ImageRepository,
    private val premiumRepository: PremiumRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            premiumRepository.isPremiumUserFlow.collect { isPremium ->
                _uiState.value = _uiState.value.copy(isPremium = isPremium)
            }
        }
    }

    // We can also use simple state variables if preferred, but the prompt asks for StateFlow
    var targetKB by mutableStateOf(TextFieldValue("50"))
        private set

    fun onTargetKBChange(newValue: TextFieldValue) {
        targetKB = newValue
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            val bitmap = imageRepository.loadCorrectBitmap(uri)
            _uiState.value = _uiState.value.copy(bitmap = bitmap)
        }
    }

    fun compressImage() {
        val bitmap = _uiState.value.bitmap ?: return
        val target = targetKB.text.toIntOrNull() ?: 50

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Simulating delay as in original code
            delay(1000)

            val bytes = imageRepository.compressExactKB(bitmap, target)
            val uri = imageRepository.saveImageToMediaStore(bytes)
            
            val compressedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                compressedUri = uri,
                bitmap = compressedBitmap,
                resultText = "Target: ${target}KB | Final: ${bytes.size / 1024}KB ✔"
            )
        }
    }

    fun setShowExitDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showExitDialog = show)
    }
}

data class HomeUiState(
    val bitmap: Bitmap? = null,
    val compressedUri: Uri? = null,
    val resultText: String = "",
    val isLoading: Boolean = false,
    val showExitDialog: Boolean = false,
    val isPremium: Boolean = false
)
