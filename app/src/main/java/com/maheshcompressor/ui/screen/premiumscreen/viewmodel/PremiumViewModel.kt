package com.maheshcompressor.ui.screen.premiumscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maheshcompressor.data.repository.PremiumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumRepository: PremiumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    var codeInput by mutableStateOf(TextFieldValue(""))
        private set

    fun onCodeInputChange(newValue: TextFieldValue) {
        codeInput = newValue
    }

    fun activatePremium(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val code = codeInput.text.trim()
        if (code.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(1000)

            premiumRepository.validateCode(code)
                .onSuccess {
                    premiumRepository.markCodeAsUsed(code)
                    premiumRepository.setPremiumStatus(true)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError(it.message ?: "Error activating premium")
                }
        }
    }

    fun resetPremium(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(1000)
            premiumRepository.clearPremiumStatus()
            _uiState.value = _uiState.value.copy(isLoading = false)
            onSuccess()
        }
    }
}

data class PremiumUiState(
    val isLoading: Boolean = false
)
