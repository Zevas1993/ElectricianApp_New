package com.example.electricianappnew.ui.calculators.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Placeholder import removed
import com.example.electricianappnew.data.repository.NecDataRepository // Import repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sqrt

// Data class for UI State
data class VoltageDropUiState(
    val systemVoltageStr: String = "120",
    val selectedPhase: String = "Single Phase",
    val selectedMaterial: String = "Copper",
    val selectedSize: String = "12 AWG",
    val loadCurrentStr: String = "",
    val distanceStr: String = "", // Assuming feet
    val voltageDropVolts: Double? = null,
    val voltageDropPercent: Double? = null,
    val endVoltage: Double? = null,
    val errorMessage: String? = null,

    // Data for dropdowns - TODO: Load these from repository eventually
    val phases: List<String> = listOf("Single Phase", "Three Phase"),
    val materials: List<String> = listOf("Copper", "Aluminum")
    // wireSizes removed from UiState
)

@HiltViewModel
class VoltageDropViewModel @Inject constructor(
    private val necDataRepository: NecDataRepository // Inject repository
) : ViewModel() {

    var uiState by mutableStateOf(VoltageDropUiState())
        private set

    // State for dropdown options
    var wireSizes by mutableStateOf<List<String>>(emptyList())
        private set

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            wireSizes = necDataRepository.getDistinctAmpacityWireSizes() // Reuse ampacity sizes
            val initialSize = wireSizes.firstOrNull { it == "12 AWG" } ?: wireSizes.firstOrNull() ?: ""
            uiState = uiState.copy(selectedSize = initialSize)
            // No initial calculation needed as other fields are blank
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
    fun onLoadCurrentChange(newValue: String) {
        uiState = uiState.copy(loadCurrentStr = newValue, errorMessage = null)
        calculateVoltageDrop()
    }
    fun onDistanceChange(newValue: String) {
        uiState = uiState.copy(distanceStr = newValue, errorMessage = null)
        calculateVoltageDrop()
    }

    fun clearInputs() {
        // Reset to defaults, keeping fetched wireSizes
        val initialSize = wireSizes.firstOrNull { it == "12 AWG" } ?: wireSizes.firstOrNull() ?: ""
        uiState = VoltageDropUiState(selectedSize = initialSize)
        // Calculation will clear results as other fields are blank
    }

    // Make public for LaunchedEffect in Screen
    fun calculateVoltageDrop() {
         viewModelScope.launch {
            val systemVoltage = uiState.systemVoltageStr.toDoubleOrNull()
            val loadCurrent = uiState.loadCurrentStr.toDoubleOrNull()
            val distance = uiState.distanceStr.toDoubleOrNull()

            // Clear results if inputs are invalid or incomplete early
            if (systemVoltage == null || loadCurrent == null || distance == null || loadCurrent <= 0 || distance <= 0 || systemVoltage <= 0) {
                 uiState = uiState.copy(
                     voltageDropVolts = null, voltageDropPercent = null, endVoltage = null,
                     errorMessage = if (systemVoltage != null && loadCurrent != null && distance != null) null else "Invalid input values." // Show error only if calculation wasn't attempted
                 )
                return@launch
            }

            // Fetch conductor properties from repository
            val conductorProps = necDataRepository.getConductorProperties(uiState.selectedMaterial, uiState.selectedSize)
            val resistancePer1000ft = conductorProps?.resistanceDcOhmsPer1000ft // Re-applying fix for certainty

            if (resistancePer1000ft == null || resistancePer1000ft <= 0) {
                 uiState = uiState.copy(
                     voltageDropVolts = null, voltageDropPercent = null, endVoltage = null,
                     errorMessage = "Conductor resistance properties not found for ${uiState.selectedMaterial} ${uiState.selectedSize}."
                 )
                return@launch
            }


            var vd: Double? = null
            var vdPercent: Double? = null
            var endV: Double? = null
            var error: String? = null

            try {
                // Voltage Drop Formulas using Ohms/1000ft:
                // Single Phase: VD = (2 * R * L * I) / 1000
                // Three Phase:  VD = (sqrt(3) * R * L * I) / 1000
                vd = when (uiState.selectedPhase) {
                    "Single Phase" -> (2 * resistancePer1000ft * distance * loadCurrent) / 1000.0
                    "Three Phase" -> (sqrt(3.0) * resistancePer1000ft * distance * loadCurrent) / 1000.0
                    else -> { error = "Invalid phase selected."; null }
                }

                if (vd != null) {
                    if (vd > systemVoltage) {
                        error = "Calculated voltage drop exceeds system voltage."
                        vdPercent = null
                        endV = null
                    } else {
                        vdPercent = (vd / systemVoltage) * 100
                        endV = systemVoltage - vd
                    }
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

    // Helper to format results nicely (can be shared or moved)
    private fun Double.formatResult(decimals: Int = 2): String {
        return String.format("%.${decimals}f", this).trimEnd('0').trimEnd('.')
    }
}
