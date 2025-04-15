package com.example.electricianappnew.ui.calculators.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

// UI State for Luminaire Layout (Zonal Cavity / Lumen Method)
data class LuminaireLayoutUiState(
    // Inputs
    val roomLengthStr: String = "",
    val roomWidthStr: String = "",
    val roomHeightStr: String = "", // Ceiling height
    val workPlaneHeightStr: String = "2.5", // Default work plane (e.g., desk height in feet)
    val targetIlluminanceStr: String = "50", // Target foot-candles (fc)
    val lumensPerLuminaireStr: String = "",
    val coefficientOfUtilizationStr: String = "0.7", // CU - User input or estimated
    val lightLossFactorStr: String = "0.8", // LLF - User input or estimated

    // Results
    val roomArea: Double? = null,
    val cavityHeight: Double? = null,
    val roomCavityRatio: Double? = null,
    val requiredLumens: Double? = null,
    val numLuminairesExact: Double? = null,
    val numLuminairesActual: Int? = null, // Rounded up
    // TODO: Add layout spacing calculation results later
    val errorMessage: String? = null
)

@HiltViewModel
class LuminaireLayoutViewModel @Inject constructor() : ViewModel() {

    var uiState by mutableStateOf(LuminaireLayoutUiState())
        private set

    // --- Input Change Handlers ---
    fun onRoomLengthChanged(value: String) {
        uiState = uiState.copy(roomLengthStr = value, errorMessage = null)
        calculateLayout()
    }
    fun onRoomWidthChanged(value: String) {
        uiState = uiState.copy(roomWidthStr = value, errorMessage = null)
        calculateLayout()
    }
     fun onRoomHeightChanged(value: String) {
        uiState = uiState.copy(roomHeightStr = value, errorMessage = null)
        calculateLayout()
    }
     fun onWorkPlaneHeightChanged(value: String) {
        uiState = uiState.copy(workPlaneHeightStr = value, errorMessage = null)
        calculateLayout()
    }
     fun onTargetIlluminanceChanged(value: String) {
        uiState = uiState.copy(targetIlluminanceStr = value, errorMessage = null)
        calculateLayout()
    }
     fun onLumensPerLuminaireChanged(value: String) {
        uiState = uiState.copy(lumensPerLuminaireStr = value, errorMessage = null)
        calculateLayout()
    }
     fun onCoefficientOfUtilizationChanged(value: String) {
        uiState = uiState.copy(coefficientOfUtilizationStr = value, errorMessage = null)
        calculateLayout()
    }
     fun onLightLossFactorChanged(value: String) {
        uiState = uiState.copy(lightLossFactorStr = value, errorMessage = null)
        calculateLayout()
    }

    fun clearInputs() {
        uiState = LuminaireLayoutUiState() // Reset to defaults
    }

    // --- Calculation Logic ---
    fun calculateLayout() {
        viewModelScope.launch {
            val length = uiState.roomLengthStr.toDoubleOrNull()
            val width = uiState.roomWidthStr.toDoubleOrNull()
            val roomHeight = uiState.roomHeightStr.toDoubleOrNull()
            val workPlaneHeight = uiState.workPlaneHeightStr.toDoubleOrNull()
            val targetIlluminance = uiState.targetIlluminanceStr.toDoubleOrNull()
            val lumensPerLuminaire = uiState.lumensPerLuminaireStr.toDoubleOrNull()
            val cu = uiState.coefficientOfUtilizationStr.toDoubleOrNull()
            val llf = uiState.lightLossFactorStr.toDoubleOrNull()

            // Clear results if inputs are invalid
            if (listOf(length, width, roomHeight, workPlaneHeight, targetIlluminance, lumensPerLuminaire, cu, llf).any { it == null || it <= 0 } || roomHeight!! <= workPlaneHeight!!) {
                val error = if (uiState.roomLengthStr.isBlank() && uiState.roomWidthStr.isBlank()) {
                    null // Don't show error on initial blank state
                } else {
                    "Enter valid positive numbers. Room height must be greater than work plane height."
                }
                clearResults(error) // Pass the potentially null error message
                return@launch
            }

            var errorMsg: String? = null
            var rcr: Double? = null
            var reqLumens: Double? = null
            var numExact: Double? = null
            var numActual: Int? = null
            var cavityH: Double? = null
            var area: Double? = null


            try {
                // 1. Calculate Room Area
                area = length!! * width!!

                // 2. Calculate Cavity Height (Luminaire to Work Plane)
                // Assuming luminaires are mounted at ceiling height for simplicity
                // TODO: Add input for luminaire mounting height if different from ceiling
                cavityH = roomHeight!! - workPlaneHeight!!
                if (cavityH <= 0) throw CalculationException("Cavity height must be positive.")

                // 3. Calculate Room Cavity Ratio (RCR)
                rcr = (5 * cavityH * (length + width)) / area

                // 4. Calculate Total Required Lumens on Work Plane
                reqLumens = targetIlluminance!! * area

                // 5. Calculate Number of Luminaires Needed
                val denominator = lumensPerLuminaire!! * cu!! * llf!!
                if (denominator <= 0) throw CalculationException("Lumens, CU, and LLF must be positive.")
                numExact = reqLumens / denominator
                numActual = ceil(numExact).toInt() // Round up to nearest whole luminaire

            } catch (e: CalculationException) {
                errorMsg = e.message
            } catch (e: Exception) {
                errorMsg = "Calculation error: ${e.message}"
            }

            // Update UI State
            uiState = uiState.copy(
                roomArea = area,
                cavityHeight = cavityH,
                roomCavityRatio = rcr,
                requiredLumens = reqLumens,
                numLuminairesExact = numExact,
                numLuminairesActual = numActual,
                errorMessage = errorMsg
            )
        }
    }

     private fun clearResults(errorMessage: String?) {
         uiState = uiState.copy(
            roomArea = null, cavityHeight = null, roomCavityRatio = null,
            requiredLumens = null, numLuminairesExact = null, numLuminairesActual = null,
            errorMessage = errorMessage
        )
    }

    // Custom exception
    class CalculationException(message: String): Exception(message)

    // Helper to format results nicely
    private fun Double.formatResult(decimals: Int = 1): String {
        return String.format("%.${decimals}f", this).trimEnd('0').trimEnd('.')
    }
}
