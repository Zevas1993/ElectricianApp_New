package com.example.electricianappnew.ui.calculators.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.util.Log // Import Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.repository.NecDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // Import Flow operators
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sqrt
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.acos

// Data class for UI State
data class VoltageDropUiState(
    val systemVoltageStr: String = "120",
    val selectedPhase: String = "Single Phase",
    val selectedMaterial: String = "Copper",
    val selectedSize: String = "12 AWG",
    val selectedRacewayType: String = "EMT", // Added raceway type
    val loadCurrentStr: String = "",
    val distanceStr: String = "", // Assuming feet
    val powerFactorStr: String = "1.0", // Added power factor
    val voltageDropVolts: Double? = null,
    val voltageDropPercent: Double? = null,
    val endVoltage: Double? = null,
    val errorMessage: String? = null
    // Dropdown lists (phases, materials, wireSizes, racewayTypes) are now provided by separate StateFlows below
)

@HiltViewModel
class VoltageDropViewModel @Inject constructor(
    private val necDataRepository: NecDataRepository // Inject repository
) : ViewModel() {

    companion object {
        private const val TAG = "VoltageDropViewModel" // Add TAG
    }

    var uiState by mutableStateOf(VoltageDropUiState())
        private set

    // StateFlow for wire sizes, collected from the repository
    val wireSizes: StateFlow<List<String>> = necDataRepository.getDistinctAmpacityWireSizes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L), // Keep flow active 5s after last subscriber
            initialValue = emptyList() // Start with empty list
        )

    // StateFlow for phases (currently static, but follows the pattern)
    val phases: StateFlow<List<String>> = flowOf(listOf("Single Phase", "Three Phase"))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = listOf("Single Phase", "Three Phase") // Provide initial value immediately
        )

    // StateFlow for materials (currently static, but follows the pattern)
    val materials: StateFlow<List<String>> = flowOf(listOf("Copper", "Aluminum"))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = listOf("Copper", "Aluminum") // Provide initial value immediately
        )

    // StateFlow for raceway types, collected from the repository
    val racewayTypes: StateFlow<List<String>> = necDataRepository.getDistinctConduitTypes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList() // Start with empty list
        )


    init {
        // Observe the wireSizes flow to set the initial selectedSize once data is loaded
        viewModelScope.launch {
            wireSizes.filter { it.isNotEmpty() }.first().let { loadedSizes ->
                val initialSize = loadedSizes.firstOrNull { it == "12 AWG" } ?: loadedSizes.first()
                uiState = uiState.copy(selectedSize = initialSize)
                Log.d(TAG, "Initial wire sizes loaded. Selected: $initialSize")
            }
        }
         // Observe the racewayTypes flow to set the initial selectedRacewayType once data is loaded
        viewModelScope.launch {
            racewayTypes.filter { it.isNotEmpty() }.first().let { loadedTypes ->
                val initialType = loadedTypes.firstOrNull { it == "EMT" } ?: loadedTypes.first()
                uiState = uiState.copy(selectedRacewayType = initialType)
                Log.d(TAG, "Initial raceway types loaded. Selected: $initialType")
            }
        }
    }

    // Event handlers for input changes
    fun onSystemVoltageChange(newValue: String) {
        uiState = uiState.copy(systemVoltageStr = newValue, errorMessage = null)
        calculateVoltageDrop()
    }
    fun onPhaseChange(newValue: String) {
        uiState = uiState.copy(selectedPhase = newValue, errorMessage = null)
        calculateVoltageDrop()
    }
    fun onMaterialChange(newValue: String) {
        uiState = uiState.copy(selectedMaterial = newValue, errorMessage = null)
        calculateVoltageDrop()
    }
    fun onSizeChange(newValue: String) {
        uiState = uiState.copy(selectedSize = newValue, errorMessage = null)
        calculateVoltageDrop()
    }
    fun onRacewayTypeChange(newValue: String) { // Added raceway type handler
        uiState = uiState.copy(selectedRacewayType = newValue, errorMessage = null)
        calculateVoltageDrop()
    }
    fun onLoadCurrentChange(newValue: String) {
        uiState = uiState.copy(loadCurrentStr = newValue, errorMessage = null)
        calculateVoltageDrop()
    }
    fun onDistanceChange(newValue: String) {
        uiState = uiState.copy(distanceStr = newValue, errorMessage = null)
        calculateVoltageDrop()
    }
    fun onPowerFactorChange(newValue: String) { // Added power factor handler
        uiState = uiState.copy(powerFactorStr = newValue, errorMessage = null)
        calculateVoltageDrop()
    }


    fun clearInputs() {
        // Reset to defaults, keeping fetched wireSizes and racewayTypes from the StateFlow
        val currentWireSizes = wireSizes.value // Get current value from StateFlow
        val initialSize = currentWireSizes.firstOrNull { it == "12 AWG" } ?: currentWireSizes.firstOrNull() ?: ""
        val currentRacewayTypes = racewayTypes.value // Get current value from StateFlow
        val initialRacewayType = currentRacewayTypes.firstOrNull { it == "EMT" } ?: currentRacewayTypes.firstOrNull() ?: ""

        // Reset uiState, selectedSize and selectedRacewayType are based on the current StateFlows
        uiState = VoltageDropUiState(
            selectedSize = initialSize,
            selectedRacewayType = initialRacewayType
            // Other fields reset to defaults
        )
        Log.d(TAG, "Inputs cleared. Selected size reset to: $initialSize, Selected raceway type reset to: $initialRacewayType")
        // Calculation will clear results as other fields are blank
    }

    // Make public for LaunchedEffect in Screen
    fun calculateVoltageDrop() {
         viewModelScope.launch {
            val systemVoltage = uiState.systemVoltageStr.toDoubleOrNull()
            val loadCurrent = uiState.loadCurrentStr.toDoubleOrNull()
            val distance = uiState.distanceStr.toDoubleOrNull()
            val powerFactor = uiState.powerFactorStr.toDoubleOrNull()

            // Clear results if inputs are invalid or incomplete early
            if (systemVoltage == null || loadCurrent == null || distance == null || powerFactor == null ||
                loadCurrent <= 0 || distance <= 0 || systemVoltage <= 0 || powerFactor < 0 || powerFactor > 1) {
                 uiState = uiState.copy(
                     voltageDropVolts = null, voltageDropPercent = null, endVoltage = null,
                     errorMessage = if (systemVoltage != null && loadCurrent != null && distance != null && powerFactor != null) {
                         if (powerFactor < 0 || powerFactor > 1) "Power Factor must be between 0 and 1." else "Invalid input values."
                     } else {
                         "Invalid input values."
                     }
                 )
                return@launch
            }

            // Fetch conductor impedance properties from repository (using AC impedance)
            val conductorImpedance = necDataRepository.getConductorImpedanceEntry(
                uiState.selectedMaterial,
                uiState.selectedSize,
                uiState.selectedRacewayType
            )
            val resistanceAcPer1000ft = conductorImpedance?.resistanceAcOhmsPer1000ft
            val reactancePer1000ft = conductorImpedance?.reactanceOhmsPer1000ft

            if (resistanceAcPer1000ft == null || reactancePer1000ft == null) {
                 uiState = uiState.copy(
                     voltageDropVolts = null, voltageDropPercent = null, endVoltage = null,
                     errorMessage = "Conductor impedance properties not found for ${uiState.selectedMaterial} ${uiState.selectedSize} in ${uiState.selectedRacewayType}."
                 )
                return@launch
            }

            var vd: Double? = null
            var vdPercent: Double? = null
            var endV: Double? = null
            var error: String? = null

            try {
                // AC Voltage Drop Formulas using Ohms/1000ft and Power Factor:
                // VD = (K * L * I * (R_ac * PF + X * sin(arccos(PF)))) / 1000
                // K = 2 for single phase, sqrt(3) for three phase
                val kFactor = if (uiState.selectedPhase == "Single Phase") 2.0 else sqrt(3.0)
                val angle = acos(powerFactor) // Angle whose cosine is PF
                val sinAngle = sin(angle)

                vd = (kFactor * distance * loadCurrent * (resistanceAcPer1000ft * powerFactor + reactancePer1000ft * sinAngle)) / 1000.0

                if (vd > systemVoltage) {
                    error = "Calculated voltage drop exceeds system voltage."
                    vdPercent = null
                    endV = null
                } else {
                    vdPercent = (vd / systemVoltage) * 100
                    endV = systemVoltage - vd
                }

            } catch (e: Exception) {
                error = "Calculation error: ${e.message}"
                vd = null; vdPercent = null; endV = null; // Clear results on error
            }

             uiState = uiState.copy(
                 voltageDropVolts = vd,
                 voltageDropPercent = vdPercent,
                 endVoltage = endV,
                 errorMessage = error // Set error message if any occurred
             )
         }
    }
}
