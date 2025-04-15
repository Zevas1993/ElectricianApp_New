package com.example.electricianappnew.ui.calculators.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel // Add HiltViewModel import back
import javax.inject.Inject // Keep Inject
import kotlin.math.pow
import kotlin.math.sqrt

// Enum moved here or to a common location if used elsewhere
enum class CalculateVariable { Voltage, Current, Resistance }

// Data class to hold the UI state
data class OhmsLawUiState(
    val voltageStr: String = "",
    val currentStr: String = "",
    val resistanceStr: String = "",
    val powerStr: String = "", // Output only
    val calculateTarget: CalculateVariable = CalculateVariable.Resistance,
    val voltageEnabled: Boolean = true,
    val currentEnabled: Boolean = true,
    val resistanceEnabled: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel // Add Hilt annotation
class OhmsLawViewModel @Inject constructor() : ViewModel() { // Keep Inject constructor

    var uiState by mutableStateOf(OhmsLawUiState())
        private set // Make the setter private so only ViewModel can change state

    fun onVoltageChange(newValue: String) {
        uiState = uiState.copy(voltageStr = newValue, powerStr = "", errorMessage = null) // Clear results on input change
    }

    fun onCurrentChange(newValue: String) {
        uiState = uiState.copy(currentStr = newValue, powerStr = "", errorMessage = null)
    }

    fun onResistanceChange(newValue: String) {
        uiState = uiState.copy(resistanceStr = newValue, powerStr = "", errorMessage = null)
    }

    fun onTargetChange(newTarget: CalculateVariable) {
        // Clear the field that is now the target, enable/disable others
        val newState = when (newTarget) {
            CalculateVariable.Voltage -> uiState.copy(voltageStr = "", voltageEnabled = false, currentEnabled = true, resistanceEnabled = true)
            CalculateVariable.Current -> uiState.copy(currentStr = "", voltageEnabled = true, currentEnabled = false, resistanceEnabled = true)
            CalculateVariable.Resistance -> uiState.copy(resistanceStr = "", voltageEnabled = true, currentEnabled = true, resistanceEnabled = false)
        }
        uiState = newState.copy(calculateTarget = newTarget, powerStr = "", errorMessage = null)
    }

    fun clearInputs() {
        uiState = OhmsLawUiState() // Reset to initial state
    }

    fun calculate() {
        val vInput = uiState.voltageStr.toDoubleOrNull()
        val iInput = uiState.currentStr.toDoubleOrNull()
        val rInput = uiState.resistanceStr.toDoubleOrNull()

        var v: Double? = if (uiState.voltageEnabled) vInput else null
        var i: Double? = if (uiState.currentEnabled) iInput else null
        var r: Double? = if (uiState.resistanceEnabled) rInput else null
        var p: Double? = null
        var error: String? = null
        var calculatedVoltageStr = uiState.voltageStr
        var calculatedCurrentStr = uiState.currentStr
        var calculatedResistanceStr = uiState.resistanceStr

        try {
            when (uiState.calculateTarget) {
                CalculateVariable.Voltage -> {
                    if (i == null || r == null) { error = "Enter Current and Resistance"; throw IllegalArgumentException(error) }
                    v = i * r
                    p = i * i * r
                    calculatedVoltageStr = v.formatResult()
                }
                CalculateVariable.Current -> {
                    if (v == null || r == null) { error = "Enter Voltage and Resistance"; throw IllegalArgumentException(error) }
                    if (r == 0.0) { error = "Resistance cannot be zero"; throw ArithmeticException(error) }
                    i = v / r
                    p = v * v / r
                    calculatedCurrentStr = i.formatResult()
                }
                CalculateVariable.Resistance -> {
                    if (v == null || i == null) { error = "Enter Voltage and Current"; throw IllegalArgumentException(error) }
                    if (i == 0.0) { error = "Current cannot be zero"; throw ArithmeticException(error) }
                    r = v / i
                    p = v * i
                    calculatedResistanceStr = r.formatResult()
                }
            }
             // Calculate power if possible and not already done
             p = p ?: when {
                 v != null && i != null -> v * i
                 v != null && r != null && r != 0.0 -> v.pow(2) / r
                 i != null && r != null -> i.pow(2) * r
                 else -> null
             }

        } catch (e: IllegalArgumentException) {
            error = e.message // Use specific error message
        }
        catch (e: ArithmeticException) {
             error = e.message ?: "Calculation error (e.g., division by zero)."
        }
         catch (e: Exception) {
            error = "Error: ${e.message}"
        }

        // Update the state with results or error
        uiState = uiState.copy(
            voltageStr = calculatedVoltageStr,
            currentStr = calculatedCurrentStr,
            resistanceStr = calculatedResistanceStr,
            powerStr = p?.formatResult() ?: "",
            errorMessage = error
        )
    }

    // Helper to format results nicely (can be shared or moved)
    private fun Double.formatResult(decimals: Int = 3): String {
        return String.format("%.${decimals}f", this).trimEnd('0').trimEnd('.')
    }
}
