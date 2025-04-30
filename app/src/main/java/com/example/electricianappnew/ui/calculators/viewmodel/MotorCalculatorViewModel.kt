package com.example.electricianappnew.ui.calculators.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.repository.NecDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

// Detailed UI State for Motor Calculations
data class MotorCalculatorUiState(
    // Inputs
    val horsepowerStr: String = "",
    val voltageStr: String = "", // Allow selecting standard voltages
    val phase: Int = 3, // Default to 3 phase
    val motorType: String = "Three-Phase AC Squirrel Cage", // Default type
    val flaStr: String = "", // Nameplate FLA for Overloads
    val serviceFactorStr: String = "1.15", // Common default SF
    val tempRiseStr: String = "", // Alternative to SF for overloads (e.g., 40)
    val protectionDeviceType: String = "Inverse Time Breaker", // Default protection

    // Results
    val flc: Double? = null, // Full Load Current from NEC Tables
    val minConductorAmpacity: Double? = null,
    val maxOverloadAmps: Double? = null,
    val maxProtectionPercent: Int? = null,
    val maxProtectionAmps: Double? = null,
    val standardBreakerFuseSize: Int? = null, // Next standard size up if needed
    val errorMessage: String? = null,

    // Dropdown Options (Could be fetched or hardcoded)
    val phases: List<Int> = listOf(1, 3),
    val standardVoltages: Map<Int, List<Int>> = mapOf( // Phase -> Voltages
        1 to listOf(115, 120, 208, 230, 240),
        3 to listOf(200, 208, 230, 240, 460, 480, 575, 600)
    ),
     // TODO: Define standard motor types based on NEC tables
    val motorTypes: List<String> = listOf("Single-Phase AC", "Three-Phase AC Squirrel Cage"),
    val protectionDeviceTypes: List<String> = listOf(
        "Non-Time Delay Fuse",
        "Dual Element Fuse",
        "Inverse Time Breaker"
        // "Instantaneous Trip Breaker" // Requires different calc logic
    ),
     val standardFuseBreakerSizes: List<Int> = listOf(
        15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100, 110, 125, 150,
        175, 200, 225, 250, 300, 350, 400, 450, 500, 600, 700, 800, 1000,
        1200, 1600, 2000, 2500, 3000, 4000, 5000, 6000 // NEC 240.6(A)
    )
)

@HiltViewModel
class MotorCalculatorViewModel @Inject constructor(
    private val necDataRepository: NecDataRepository
) : ViewModel() {

    var uiState by mutableStateOf(MotorCalculatorUiState())
        private set

    // --- Input Change Handlers ---
    fun onHorsepowerChanged(value: String) {
        uiState = uiState.copy(horsepowerStr = value, errorMessage = null)
        calculateMotorValues()
    }
    fun onVoltageChanged(value: String) {
        uiState = uiState.copy(voltageStr = value, errorMessage = null)
        calculateMotorValues()
    }
    fun onPhaseChanged(value: Int) {
        // Reset voltage if it's not valid for the new phase
        val validVoltages = uiState.standardVoltages[value] ?: emptyList()
        val currentVoltage = uiState.voltageStr.toIntOrNull()
        val newVoltageStr = if (currentVoltage != null && validVoltages.contains(currentVoltage)) uiState.voltageStr else ""
        uiState = uiState.copy(phase = value, voltageStr = newVoltageStr, errorMessage = null)
        calculateMotorValues()
    }
     fun onMotorTypeChanged(value: String) {
        uiState = uiState.copy(motorType = value, errorMessage = null)
        calculateMotorValues()
    }
    fun onFlaChanged(value: String) {
        uiState = uiState.copy(flaStr = value, errorMessage = null)
        calculateMotorValues()
    }
    fun onServiceFactorChanged(value: String) {
        uiState = uiState.copy(serviceFactorStr = value, tempRiseStr = "", errorMessage = null) // Clear temp rise if SF is entered
        calculateMotorValues()
    }
    fun onTempRiseChanged(value: String) {
        uiState = uiState.copy(tempRiseStr = value, serviceFactorStr = "", errorMessage = null) // Clear SF if temp rise is entered
        calculateMotorValues()
    }
     fun onProtectionDeviceTypeChanged(value: String) {
        uiState = uiState.copy(protectionDeviceType = value, errorMessage = null)
        calculateMotorValues()
    }

    fun clearInputs() {
        uiState = MotorCalculatorUiState() // Reset to defaults
    }

    // --- Calculation Logic ---
    fun calculateMotorValues() {
        viewModelScope.launch {
            val hp = uiState.horsepowerStr.toDoubleOrNull()
            val voltage = uiState.voltageStr.toIntOrNull() // Voltage from NEC tables is usually Int
            val phase = uiState.phase
            val fla = uiState.flaStr.toDoubleOrNull()
            val sf = uiState.serviceFactorStr.toDoubleOrNull()
            val tempRise = uiState.tempRiseStr.toIntOrNull()

            // Input Validation
            if (hp == null || voltage == null || hp <= 0 || voltage <= 0) {
                clearResults("Invalid HP or Voltage.")
                return@launch
            }
             if (fla == null || fla <= 0) {
                 clearResults("Valid Nameplate FLA required for Overload calculation.")
                 // Allow calculation of FLC, conductor, protection even without FLA? Maybe.
                 // For now, require FLA.
                 return@launch
             }
             if (sf == null && tempRise == null) {
                 clearResults("Enter Service Factor OR Temperature Rise for Overload calculation.")
                 return@launch
             }
             if (sf != null && sf < 1.0) {
                 clearResults("Service Factor must be 1.0 or greater.")
                 return@launch
             }
             if (tempRise != null && tempRise <= 0) {
                 clearResults("Temperature Rise must be positive.")
                 return@launch
             }


            val phaseString = when (phase) {
                1 -> "Single"
                3 -> "Three"
                else -> {
                    clearResults("Invalid phase selected.")
                    return@launch
                }
            }

            var error: String? = null
            var flcValue: Double? = null
            var minConductorAmps: Double? = null
            var maxOverload: Double? = null
            var maxProtectionPerc: Int? = null
            var maxProtection: Double? = null
            var standardProtectionSize: Int? = null

            try {
                // 1. Get FLC from NEC Tables (430.248 / 430.250)
                val flcEntry = necDataRepository.getMotorFLCEntry(hp, voltage, phaseString)
                flcValue = flcEntry?.flc
                if (flcValue == null) throw CalculationException("FLC not found for $hp HP, $voltage V, $phaseString-Phase motor.")

                // 2. Calculate Minimum Conductor Ampacity (430.22) - 125% of FLC
                minConductorAmps = flcValue * 1.25

                // 3. Calculate Max Overload Size (430.32)
                val overloadMultiplier = when {
                    sf != null && sf >= 1.15 -> 1.25
                    tempRise != null && tempRise <= 40 -> 1.25
                    else -> 1.15 // Default case (SF < 1.15 or Temp Rise > 40C or not specified)
                }
                maxOverload = fla * overloadMultiplier
                // TODO: Add logic for finding next standard size DOWN if needed per 430.32(C)

                // 4. Get Max Short-Circuit/Ground-Fault Protection (Table 430.52)
                // Fetch the percentage based on the device type
                val percentageEntry = necDataRepository.getMotorProtectionPercentageEntry(uiState.protectionDeviceType)
                maxProtectionPerc = percentageEntry?.maxPercentFLC

                if (maxProtectionPerc == null) {
                     error = "Max protection percentage not found for ${uiState.protectionDeviceType}."
                } else {
                    maxProtection = flcValue * (maxProtectionPerc / 100.0)

                    when (uiState.protectionDeviceType) {
                        "Non-Time Delay Fuse" -> {
                            val fuseEntry = necDataRepository.getMotorProtectionNonTimeDelayFuseSizeEntry(hp, voltage)
                            standardProtectionSize = fuseEntry?.maxFuse // Use maxFuse property
                            if (standardProtectionSize == null) {
                                 error = "Non-Time Delay Fuse size not found for $hp HP, $voltage V."
                            }
                        }
                        "Dual Element Fuse" -> {
                            val fuseEntry = necDataRepository.getMotorProtectionTimeDelayFuseSizeEntry(hp, voltage) // Dual Element uses Time Delay table
                            standardProtectionSize = fuseEntry?.maxFuse // Use maxFuse property
                             if (standardProtectionSize == null) {
                                 error = "Dual Element Fuse size not found for $hp HP, $voltage V."
                            }
                        }
                        "Inverse Time Breaker" -> {
                            // For breakers, find the next standard size up from the calculated max amps
                            standardProtectionSize = findNextStandardSize(maxProtection, uiState.standardFuseBreakerSizes)
                             if (standardProtectionSize == null) {
                                // Handle cases where calculated value exceeds largest standard size
                                error = "Calculated max protection (${maxProtection.formatResult(1)} A) exceeds largest standard size."
                            }
                        }
                        // TODO: Add "Instantaneous Trip Breaker" logic if needed
                        else -> {
                            error = "Unsupported protection device type: ${uiState.protectionDeviceType}"
                            maxProtectionPerc = null
                            maxProtection = null
                            standardProtectionSize = null
                        }
                    }
                }


            } catch (e: CalculationException) {
                error = e.message
                // Clear results on specific calculation errors
                flcValue = null; minConductorAmps = null; maxOverload = null; maxProtectionPerc = null; maxProtection = null; standardProtectionSize = null;
            }
            catch (e: Exception) {
                error = "Unexpected error: ${e.message}"
                flcValue = null; minConductorAmps = null; maxOverload = null; maxProtectionPerc = null; maxProtection = null; standardProtectionSize = null;
            }

            uiState = uiState.copy(
                flc = flcValue,
                minConductorAmpacity = minConductorAmps,
                maxOverloadAmps = maxOverload,
                maxProtectionPercent = maxProtectionPerc,
                maxProtectionAmps = maxProtection,
                standardBreakerFuseSize = standardProtectionSize,
                errorMessage = error
            )
        }
    }

     // Helper to find the next standard OCPD size UP
    private fun findNextStandardSize(calculatedAmps: Double?, standardSizes: List<Int>): Int? {
        if (calculatedAmps == null) return null
        return standardSizes.firstOrNull { it >= calculatedAmps }
    }

    private fun clearResults(errorMessage: String?) {
         uiState = uiState.copy(
            flc = null,
            minConductorAmpacity = null,
            maxOverloadAmps = null,
            maxProtectionPercent = null,
            maxProtectionAmps = null,
            standardBreakerFuseSize = null,
            errorMessage = errorMessage
        )
    }

    // Custom exception for clarity
    class CalculationException(message: String): Exception(message)

    // Helper to format results nicely
    private fun Double.formatResult(decimals: Int = 1): String {
        return String.format("%.${decimals}f", this)
    }
}
