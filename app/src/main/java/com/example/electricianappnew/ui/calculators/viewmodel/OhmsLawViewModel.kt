package com.example.electricianappnew.ui.calculators.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel // Add HiltViewModel import back
import javax.inject.Inject // Keep Inject
import kotlin.math.pow
import kotlin.math.pow
import kotlin.math.sqrt

// Enum updated to include Power
enum class CalculateVariable { Voltage, Current, Resistance, Power }

// Data class to hold the UI state, added powerEnabled
data class OhmsLawUiState(
    val voltageStr: String = "",
    val currentStr: String = "",
    val resistanceStr: String = "",
    val powerStr: String = "", // Now potentially an input
    val calculateTarget: CalculateVariable = CalculateVariable.Resistance, // Default target
    val voltageEnabled: Boolean = true,
    val currentEnabled: Boolean = true,
    val resistanceEnabled: Boolean = false, // Default target is Resistance, so disable it
    val powerEnabled: Boolean = true, // Power is enabled by default
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
        uiState = uiState.copy(resistanceStr = newValue, errorMessage = null) // Don't clear power on resistance change if power is input
        // Consider clearing calculated value if target changes? Handled in onTargetChange
    }

    // Added handler for power input change
    fun onPowerChange(newValue: String) {
        uiState = uiState.copy(powerStr = newValue, errorMessage = null)
    }


    fun onTargetChange(newTarget: CalculateVariable) {
        // Clear the field that is now the target, enable/disable others
        uiState = when (newTarget) {
            CalculateVariable.Voltage -> uiState.copy(
                voltageStr = "", voltageEnabled = false,
                currentEnabled = true, resistanceEnabled = true, powerEnabled = true,
                errorMessage = null
            )
            CalculateVariable.Current -> uiState.copy(
                currentStr = "", currentEnabled = false,
                voltageEnabled = true, resistanceEnabled = true, powerEnabled = true,
                errorMessage = null
            )
            CalculateVariable.Resistance -> uiState.copy(
                resistanceStr = "", resistanceEnabled = false,
                voltageEnabled = true, currentEnabled = true, powerEnabled = true,
                errorMessage = null
            )
            CalculateVariable.Power -> uiState.copy( // Handle Power target
                powerStr = "", powerEnabled = false,
                voltageEnabled = true, currentEnabled = true, resistanceEnabled = true,
                errorMessage = null
            )
        }.copy(calculateTarget = newTarget) // Update the target last
         // Clear the calculated value of the *new* target
        clearCalculatedValue(newTarget)
    }

     // Helper to clear the specific field being calculated
    private fun clearCalculatedValue(target: CalculateVariable) {
        uiState = when (target) {
            CalculateVariable.Voltage -> uiState.copy(voltageStr = "")
            CalculateVariable.Current -> uiState.copy(currentStr = "")
            CalculateVariable.Resistance -> uiState.copy(resistanceStr = "")
            CalculateVariable.Power -> uiState.copy(powerStr = "")
        }
    }


    fun clearInputs() {
        uiState = OhmsLawUiState() // Reset to initial state
    }

    fun calculate() {
        val vInput = uiState.voltageStr.toDoubleOrNull()
        val iInput = uiState.currentStr.toDoubleOrNull()
        val rInput = uiState.resistanceStr.toDoubleOrNull()
        val pInput = uiState.powerStr.toDoubleOrNull() // Read power input

        // Use input value if field is enabled, otherwise null (it's the target)
        var v: Double? = if (uiState.voltageEnabled) vInput else null
        var i: Double? = if (uiState.currentEnabled) iInput else null
        var r: Double? = if (uiState.resistanceEnabled) rInput else null
        var p: Double? = if (uiState.powerEnabled) pInput else null // Use power input if enabled

        var error: String? = null
        // Start with current input values, update the calculated one
        var calculatedVoltageStr = if (uiState.voltageEnabled) uiState.voltageStr else ""
        var calculatedCurrentStr = if (uiState.currentEnabled) uiState.currentStr else ""
        var calculatedResistanceStr = if (uiState.resistanceEnabled) uiState.resistanceStr else ""
        var calculatedPowerStr = if (uiState.powerEnabled) uiState.powerStr else "" // Start with power input or empty

        try {
            when (uiState.calculateTarget) {
                CalculateVariable.Voltage -> {
                    // Calculate V from I and R
                    if (i != null && r != null) {
                        v = i * r
                        p = i * i * r // P = I^2 * R
                        calculatedVoltageStr = v.formatResult()
                        calculatedPowerStr = p.formatResult()
                    }
                    // Calculate V from P and I
                    else if (p != null && i != null) {
                         if (i == 0.0) { error = "Current cannot be zero for P/I calculation"; throw ArithmeticException(error) }
                         v = p / i
                         r = p / i.pow(2) // R = P / I^2
                         calculatedVoltageStr = v.formatResult()
                         calculatedResistanceStr = r.formatResult()
                    }
                     // Calculate V from P and R
                    else if (p != null && r != null) {
                         if (r < 0) { error = "Resistance cannot be negative for sqrt(P*R)"; throw IllegalArgumentException(error) }
                         v = sqrt(p * r)
                         i = sqrt(p / r) // I = sqrt(P/R) - handle r=0 case if needed, though unlikely for power calc
                         calculatedVoltageStr = v.formatResult()
                         calculatedCurrentStr = i.formatResult()
                    }
                    else { error = "Need two values (I&R, P&I, or P&R) to calculate Voltage"; throw IllegalArgumentException(error) }
                }
                CalculateVariable.Current -> {
                     // Calculate I from V and R
                    if (v != null && r != null) {
                        if (r == 0.0) { error = "Resistance cannot be zero for V/R calculation"; throw ArithmeticException(error) }
                        i = v / r
                        p = v * v / r // P = V^2 / R
                        calculatedCurrentStr = i.formatResult()
                        calculatedPowerStr = p.formatResult()
                    }
                     // Calculate I from P and V
                    else if (p != null && v != null) {
                         if (v == 0.0) { error = "Voltage cannot be zero for P/V calculation"; throw ArithmeticException(error) }
                         i = p / v
                         r = v.pow(2) / p // R = V^2 / P
                         calculatedCurrentStr = i.formatResult()
                         calculatedResistanceStr = r.formatResult()
                    }
                     // Calculate I from P and R
                    else if (p != null && r != null) {
                         if (r <= 0) { error = "Resistance must be positive for sqrt(P/R)"; throw IllegalArgumentException(error) }
                         i = sqrt(p / r)
                         v = sqrt(p * r) // V = sqrt(P*R)
                         calculatedCurrentStr = i.formatResult()
                         calculatedVoltageStr = v.formatResult()
                    }
                    else { error = "Need two values (V&R, P&V, or P&R) to calculate Current"; throw IllegalArgumentException(error) }
                }
                CalculateVariable.Resistance -> {
                     // Calculate R from V and I
                    if (v != null && i != null) {
                        if (i == 0.0) { error = "Current cannot be zero for V/I calculation"; throw ArithmeticException(error) }
                        r = v / i
                        p = v * i // P = V * I
                        calculatedResistanceStr = r.formatResult()
                        calculatedPowerStr = p.formatResult()
                    }
                    // Calculate R from V and P
                    else if (v != null && p != null) {
                         if (p == 0.0) { error = "Power cannot be zero for V^2/P calculation"; throw ArithmeticException(error) }
                         r = v.pow(2) / p
                         i = p / v // I = P / V
                         calculatedResistanceStr = r.formatResult()
                         calculatedCurrentStr = i.formatResult()
                    }
                    // Calculate R from P and I
                    else if (p != null && i != null) {
                         if (i == 0.0) { error = "Current cannot be zero for P/I^2 calculation"; throw ArithmeticException(error) }
                         r = p / i.pow(2)
                         v = p / i // V = P / I
                         calculatedResistanceStr = r.formatResult()
                         calculatedVoltageStr = v.formatResult()
                    }
                    else { error = "Need two values (V&I, V&P, or P&I) to calculate Resistance"; throw IllegalArgumentException(error) }
                 }
                 CalculateVariable.Power -> { // Calculate Power
                     // Calculate P from V and I
                    if (v != null && i != null) {
                        p = v * i
                        r = if (i != 0.0) v / i else null // Calculate R if possible
                        calculatedPowerStr = p.formatResult()
                        if (r != null) calculatedResistanceStr = r.formatResult()
                    }
                     // Calculate P from V and R
                    else if (v != null && r != null) {
                         if (r == 0.0) { error = "Resistance cannot be zero for V^2/R calculation"; throw ArithmeticException(error) }
                         p = v.pow(2) / r
                         i = v / r // Calculate I
                         calculatedPowerStr = p.formatResult()
                         calculatedCurrentStr = i.formatResult()
                    }
                     // Calculate P from I and R
                    else if (i != null && r != null) {
                         p = i.pow(2) * r
                         v = i * r // Calculate V
                         calculatedPowerStr = p.formatResult()
                         calculatedVoltageStr = v.formatResult()
                    }
                    else { error = "Need two values (V&I, V&R, or I&R) to calculate Power"; throw IllegalArgumentException(error) }
                 }
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
        // Update the state with results or error, ensuring calculated fields are updated
        uiState = uiState.copy(
            voltageStr = if (uiState.calculateTarget == CalculateVariable.Voltage) calculatedVoltageStr else uiState.voltageStr,
            currentStr = if (uiState.calculateTarget == CalculateVariable.Current) calculatedCurrentStr else uiState.currentStr,
            resistanceStr = if (uiState.calculateTarget == CalculateVariable.Resistance) calculatedResistanceStr else uiState.resistanceStr,
            powerStr = if (uiState.calculateTarget == CalculateVariable.Power) calculatedPowerStr else calculatedPowerStr, // Update power always if calculated
            errorMessage = error
        )
    }

    // Helper to format results nicely (can be shared or moved)
    private fun Double.formatResult(decimals: Int = 3): String {
        return String.format("%.${decimals}f", this).trimEnd('0').trimEnd('.')
    }
}
