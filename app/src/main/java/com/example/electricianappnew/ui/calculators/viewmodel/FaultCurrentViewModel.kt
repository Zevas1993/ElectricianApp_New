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
import kotlin.math.sqrt

// UI State for Fault Current (Transformer Impedance Method)
data class FaultCurrentUiState(
    // Inputs
    val transformerKvaStr: String = "",
    val transformerVoltageStr: String = "", // Secondary Voltage
    val transformerImpedancePercentStr: String = "", // %Z
    val selectedPhase: String = "Three Phase",

    // Results
    val secondaryFla: Double? = null,
    val faultCurrentAmps: Double? = null,
    val errorMessage: String? = null,

    // Dropdown Options
    val phases: List<String> = listOf("Single Phase", "Three Phase")
    // TODO: Add standard voltages if needed for dropdown
)

@HiltViewModel
class FaultCurrentViewModel @Inject constructor(
    private val necDataRepository: NecDataRepository // Inject repository
) : ViewModel() {

    var uiState by mutableStateOf(FaultCurrentUiState())
        private set

    // --- Input Change Handlers ---
    fun onKvaChanged(value: String) {
        uiState = uiState.copy(transformerKvaStr = value, errorMessage = null)
        calculateFaultCurrent()
    }
    fun onVoltageChanged(value: String) {
        uiState = uiState.copy(transformerVoltageStr = value, errorMessage = null)
        calculateFaultCurrent()
    }
     fun onImpedanceChanged(value: String) {
        uiState = uiState.copy(transformerImpedancePercentStr = value, errorMessage = null)
        calculateFaultCurrent()
    }
    fun onPhaseChanged(value: String) {
        uiState = uiState.copy(selectedPhase = value, errorMessage = null)
        calculateFaultCurrent()
    }

     fun clearInputs() {
        uiState = FaultCurrentUiState() // Reset to defaults
    }

    // --- Calculation Logic (Transformer Impedance Method) ---
    fun calculateFaultCurrent() {
        viewModelScope.launch {
            val kva = uiState.transformerKvaStr.toDoubleOrNull()
            val voltage = uiState.transformerVoltageStr.toDoubleOrNull() // Secondary Voltage
            val impedancePercent = uiState.transformerImpedancePercentStr.toDoubleOrNull()

            // Input Validation
            if (kva == null || voltage == null || impedancePercent == null || kva <= 0 || voltage <= 0 || impedancePercent <= 0) {
                clearResults("Enter valid positive numbers for kVA, Voltage, and % Impedance.")
                return@launch
            }

            var error: String? = null
            var fla: Double? = null
            var afc: Double? = null

            try {
                // 1. Calculate Secondary Full Load Amps (FLA)
                fla = when (uiState.selectedPhase) {
                    "Single Phase" -> (kva * 1000.0) / voltage
                    "Three Phase" -> (kva * 1000.0) / (voltage * sqrt(3.0))
                    else -> throw CalculationException("Invalid Phase.")
                }

                // 2. Calculate Available Fault Current (AFC) at transformer secondary
                // AFC = (FLA * 100) / %Z
                val impedanceDecimal = impedancePercent / 100.0
                if (impedanceDecimal == 0.0) throw CalculationException("Impedance cannot be zero.")
                afc = fla / impedanceDecimal

            } catch (e: CalculationException) {
                error = e.message
            } catch (e: Exception) {
                error = "Calculation error: ${e.message}"
            }

            // Update UI State
            uiState = uiState.copy(
                secondaryFla = fla,
                faultCurrentAmps = afc,
                errorMessage = error
            )
        }
    }

     private fun clearResults(errorMessage: String?) {
         uiState = uiState.copy(
            secondaryFla = null,
            faultCurrentAmps = null,
            errorMessage = errorMessage
        )
    }

    // Custom exception
    class CalculationException(message: String): Exception(message)

    // Helper to format results nicely
    private fun Double.formatResult(decimals: Int = 0): String { // Usually show whole amps for AFC
        return String.format("%.${decimals}f", this)
    }
}
