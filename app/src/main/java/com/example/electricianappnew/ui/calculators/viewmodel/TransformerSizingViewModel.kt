package com.example.electricianappnew.ui.calculators.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.repository.NecDataRepository // Keep for consistency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.sqrt

// UI State Data Class
data class TransformerSizingUiState(
    // Inputs
    val loadKvaStr: String = "",
    val loadAmpsStr: String = "",
    val primaryVoltageStr: String = "480",
    val secondaryVoltageStr: String = "208",
    val selectedPhase: String = "Three Phase",
    val inputMode: String = "kVA", // "kVA" or "Amps"
    val primaryProtectionOnly: Boolean = true, // Assume primary only unless specified

    // Results
    val calculatedKva: Double? = null,
    val standardKva: Double? = null,
    val primaryFla: Double? = null,
    val secondaryFla: Double? = null,
    val primaryOcpdMaxAmps: Double? = null,
    val primaryOcpdStandardSize: Int? = null,
    val secondaryOcpdMaxAmps: Double? = null, // Null if primary only protection <= 125%
    val secondaryOcpdStandardSize: Int? = null, // Null if primary only protection <= 125%
    val errorMessage: String? = null,

    // Dropdown Options
    val phases: List<String> = listOf("Single Phase", "Three Phase"),
    val inputModes: List<String> = listOf("kVA", "Amps")
)

@HiltViewModel
class TransformerSizingViewModel @Inject constructor(
    private val necDataRepository: NecDataRepository // Inject repository
) : ViewModel() {

    var uiState by mutableStateOf(TransformerSizingUiState())
        private set

    // TODO: Move standardKvaSizes and standardOcpdSizes to a shared constants file or fetch if needed
    private val standardKvaSizes = listOf(
        1.0, 1.5, 2.0, 3.0, 5.0, 7.5, 10.0, 15.0, 25.0, 30.0, 37.5, 45.0, 50.0,
        75.0, 100.0, 112.5, 150.0, 200.0, 225.0, 250.0, 300.0, 400.0, 500.0,
        750.0, 1000.0 // Add more as needed
    )
     private val standardOcpdSizes: List<Int> = listOf(
        15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100, 110, 125, 150,
        175, 200, 225, 250, 300, 350, 400, 450, 500, 600, 700, 800, 1000,
        1200, 1600, 2000, 2500, 3000, 4000, 5000, 6000 // NEC 240.6(A)
    )

    // --- Input Change Handlers ---
    fun onInputModeChanged(newMode: String) {
        uiState = uiState.copy(inputMode = newMode, loadKvaStr = if (newMode == "Amps") "" else uiState.loadKvaStr, loadAmpsStr = if (newMode == "kVA") "" else uiState.loadAmpsStr, errorMessage = null)
        calculateTransformer()
    }
    fun onLoadKvaChanged(value: String) {
        uiState = uiState.copy(loadKvaStr = value, errorMessage = null)
        if (uiState.inputMode == "kVA") calculateTransformer()
    }
     fun onLoadAmpsChanged(value: String) {
        uiState = uiState.copy(loadAmpsStr = value, errorMessage = null)
         if (uiState.inputMode == "Amps") calculateTransformer()
    }
    fun onPrimaryVoltageChanged(value: String) {
        uiState = uiState.copy(primaryVoltageStr = value, errorMessage = null)
        calculateTransformer()
    }
    fun onSecondaryVoltageChanged(value: String) {
        uiState = uiState.copy(secondaryVoltageStr = value, errorMessage = null)
        calculateTransformer()
    }
    fun onPhaseChanged(value: String) {
        uiState = uiState.copy(selectedPhase = value, errorMessage = null)
        calculateTransformer()
    }
     fun onPrimaryProtectionOnlyChanged(value: Boolean) {
        uiState = uiState.copy(primaryProtectionOnly = value, errorMessage = null)
        calculateTransformer()
    }

    fun clearInputs() {
        uiState = TransformerSizingUiState() // Reset to defaults
    }

    // --- Calculation Logic ---
    fun calculateTransformer() {
        viewModelScope.launch {
            var errorMsg: String? = null
            var calcKva: Double? = null
            var stdKva: Double? = null
            var primFla: Double? = null
            var secFla: Double? = null
            var primOcpdMax: Double? = null
            var primOcpdStd: Int? = null
            var secOcpdMax: Double? = null
            var secOcpdStd: Int? = null

            val primaryVoltage = uiState.primaryVoltageStr.toDoubleOrNull()
            val secondaryVoltage = uiState.secondaryVoltageStr.toDoubleOrNull()
            val loadKvaInput = uiState.loadKvaStr.toDoubleOrNull()
            val loadAmpsInput = uiState.loadAmpsStr.toDoubleOrNull()

            if (primaryVoltage == null || secondaryVoltage == null || primaryVoltage <= 0 || secondaryVoltage <= 0) {
                errorMsg = "Invalid Primary or Secondary Voltage."
            } else if (uiState.inputMode == "kVA" && (loadKvaInput == null || loadKvaInput <= 0)) {
                errorMsg = "Invalid Load kVA."
            } else if (uiState.inputMode == "Amps" && (loadAmpsInput == null || loadAmpsInput <= 0)) {
                errorMsg = "Invalid Load Amps."
            } else {
                try {
                    // 1. Determine kVA
                    val kva: Double = if (uiState.inputMode == "kVA") {
                        loadKvaInput!!
                    } else { // Input mode is Amps (calculate kVA from secondary side)
                        when (uiState.selectedPhase) {
                            "Single Phase" -> (secondaryVoltage * loadAmpsInput!!) / 1000.0
                            "Three Phase" -> (secondaryVoltage * loadAmpsInput!! * sqrt(3.0)) / 1000.0
                            else -> throw CalculationException("Invalid Phase.")
                        }
                    }
                    calcKva = kva

                    // 2. Find next standard kVA size
                    stdKva = standardKvaSizes.firstOrNull { it >= kva }
                    val effectiveKva = stdKva ?: kva // Use standard size if found for FLA calcs

                    // 3. Calculate Primary FLA
                    primFla = when (uiState.selectedPhase) {
                        "Single Phase" -> (effectiveKva * 1000.0) / primaryVoltage
                        "Three Phase" -> (effectiveKva * 1000.0) / (primaryVoltage * sqrt(3.0))
                        else -> throw CalculationException("Invalid Phase.")
                    }

                    // 4. Calculate Secondary FLA
                    secFla = when (uiState.selectedPhase) {
                        "Single Phase" -> (effectiveKva * 1000.0) / secondaryVoltage
                        "Three Phase" -> (effectiveKva * 1000.0) / (secondaryVoltage * sqrt(3.0))
                        else -> throw CalculationException("Invalid Phase.")
                    }

                    // 5. Calculate Primary OCPD (NEC 450.3(B))
                    val primaryMultiplier = if (uiState.primaryProtectionOnly) {
                        when {
                            primFla < 2.0 -> 3.00 // 300%
                            primFla < 9.0 -> 1.67 // 167%
                            else -> 1.25 // 125%
                        }
                    } else {
                        2.50 // 250% when secondary protection is provided
                    }
                    primOcpdMax = primFla * primaryMultiplier
                    primOcpdStd = findNextStandardSize(primOcpdMax, standardOcpdSizes, roundUp = true) // Generally round up for primary if needed

                    // 6. Calculate Secondary OCPD (NEC 450.3(B)) - Only if primary is > 125% (or primary only is false)
                    val secondaryProtectionRequired = !uiState.primaryProtectionOnly || primaryMultiplier > 1.25

                    if (secondaryProtectionRequired) {
                         val secondaryMultiplier = when {
                            secFla < 2.0 -> 3.00 // 300% (Though unlikely for secondary)
                            secFla < 9.0 -> 1.67 // 167%
                            else -> 1.25 // 125%
                        }
                        secOcpdMax = secFla * secondaryMultiplier
                        secOcpdStd = findNextStandardSize(secOcpdMax, standardOcpdSizes, roundUp = true) // Generally round up if needed
                    } else {
                        secOcpdMax = null
                        secOcpdStd = null
                    }

                    if (stdKva == null && errorMsg == null) {
                         errorMsg = "Warning: Calculated kVA exceeds standard sizes list. FLA/OCPD based on calculated kVA."
                    }


                } catch (e: CalculationException) {
                    errorMsg = e.message
                } catch (e: Exception) {
                    errorMsg = "Calculation error: ${e.message}"
                }
            }

            // Update UI State
            uiState = uiState.copy(
                calculatedKva = calcKva,
                standardKva = stdKva,
                primaryFla = primFla,
                secondaryFla = secFla,
                primaryOcpdMaxAmps = primOcpdMax,
                primaryOcpdStandardSize = primOcpdStd,
                secondaryOcpdMaxAmps = secOcpdMax,
                secondaryOcpdStandardSize = secOcpdStd,
                errorMessage = errorMsg
            )
        }
    }

    // Helper to find the next standard OCPD size UP (NEC 240.6(A))
    // Added roundUp flag based on 450.3(B) exceptions
    private fun findNextStandardSize(calculatedAmps: Double, standardSizes: List<Int>, roundUp: Boolean): Int? {
        val exactMatch = standardSizes.contains(calculatedAmps.toInt()) // Check if exact standard size exists
        return if (exactMatch && !roundUp) { // If exact match and rounding up isn't required/allowed by exception
             calculatedAmps.toInt()
        } else {
            standardSizes.firstOrNull { it >= calculatedAmps } // Find the next size up or equal
        }
         // Simplified - NEC 450.3(B) has specific conditions for rounding up vs down.
         // For FLA >= 9A, next size up is allowed ONLY if 125% is not a standard size.
         // For FLA < 9A, next size up is allowed from 167%.
         // For FLA < 2A, next size up is allowed from 300%.
         // This simplified logic just finds the next size >= calculated max.
    }

    // Custom exception
    class CalculationException(message: String): Exception(message)

    // Helper to format results nicely
    private fun Double.formatResult(decimals: Int = 1): String {
        return String.format("%.${decimals}f", this).trimEnd('0').trimEnd('.')
    }
}
