package com.example.electricianappnew.ui.calculators.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.repository.NecDataRepository // Import repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.ceil // Add import for ceil

// Detailed UI State for Dwelling Load Calculation (Standard Method)
data class DwellingLoadUiState(
    // Inputs
    val squareFeetStr: String = "",
    val numSmallApplianceCircuitsStr: String = "2", // NEC minimum
    val numLaundryCircuitsStr: String = "1", // NEC minimum
    val fixedAppliancesVaStr: String = "0", // Total VA of fixed appliances (excluding dryer, range, AC, heat)
    val numFixedAppliancesStr: String = "0",
    val dryerVaStr: String = "5000", // NEC minimum or nameplate
    val rangeVaStr: String = "0", // Nameplate rating
    val acVaStr: String = "0", // Larger of AC or Heat
    val heatVaStr: String = "0", // Larger of AC or Heat

    // Results
    val generalLightingLoadVa: Double? = null,
    val applianceLaundryLoadVa: Double? = null,
    val totalLightingApplianceLaundryVa: Double? = null,
    val demandAppliedLightingApplianceLaundryVa: Double? = null,
    val dryerDemandVa: Double? = null,
    val rangeDemandVa: Double? = null,
    val fixedApplianceDemandVa: Double? = null,
    val largestMotorVa: Double? = null, // TODO: Add motor input later if needed
    val acHeatDemandVa: Double? = null,
    val calculatedServiceLoadVa: Double? = null,
    val calculatedServiceLoadAmps: Double? = null, // Assuming 240V
    val errorMessage: String? = null
)

@HiltViewModel
class DwellingLoadViewModel @Inject constructor(
    // Inject repository even if not directly used for tables yet, for consistency
    private val necDataRepository: NecDataRepository
) : ViewModel() {

    var uiState by mutableStateOf(DwellingLoadUiState())
        private set

    // --- Input Change Handlers ---
    fun onSquareFeetChanged(value: String) {
        uiState = uiState.copy(squareFeetStr = value, errorMessage = null)
        calculateLoad()
    }
    fun onNumSmallAppliancesChanged(value: String) {
        uiState = uiState.copy(numSmallApplianceCircuitsStr = value, errorMessage = null)
        calculateLoad()
    }
    fun onNumLaundryCircuitsChanged(value: String) {
        uiState = uiState.copy(numLaundryCircuitsStr = value, errorMessage = null)
        calculateLoad()
    }
    fun onFixedAppliancesVaChanged(value: String) {
        uiState = uiState.copy(fixedAppliancesVaStr = value, errorMessage = null)
        calculateLoad()
    }
     fun onNumFixedAppliancesChanged(value: String) {
        uiState = uiState.copy(numFixedAppliancesStr = value, errorMessage = null)
        calculateLoad()
    }
    fun onDryerVaChanged(value: String) {
        uiState = uiState.copy(dryerVaStr = value, errorMessage = null)
        calculateLoad()
    }
    fun onRangeVaChanged(value: String) {
        uiState = uiState.copy(rangeVaStr = value, errorMessage = null)
        calculateLoad()
    }
    fun onAcVaChanged(value: String) {
        uiState = uiState.copy(acVaStr = value, errorMessage = null)
        calculateLoad()
    }
    fun onHeatVaChanged(value: String) {
        uiState = uiState.copy(heatVaStr = value, errorMessage = null)
        calculateLoad()
    }

    fun clearInputs() {
        uiState = DwellingLoadUiState() // Reset to defaults
    }

    // --- Calculation Logic (Standard Method - NEC Article 220) ---
    fun calculateLoad() {
        viewModelScope.launch {
            val sqFt = uiState.squareFeetStr.toDoubleOrNull()
            val numSmallApp = uiState.numSmallApplianceCircuitsStr.toIntOrNull()
            val numLaundry = uiState.numLaundryCircuitsStr.toIntOrNull()
            val fixedVa = uiState.fixedAppliancesVaStr.toDoubleOrNull() ?: 0.0
            val numFixed = uiState.numFixedAppliancesStr.toIntOrNull() ?: 0
            val dryerVaInput = uiState.dryerVaStr.toDoubleOrNull() ?: 0.0
            val rangeVa = uiState.rangeVaStr.toDoubleOrNull() ?: 0.0
            val acVa = uiState.acVaStr.toDoubleOrNull() ?: 0.0
            val heatVa = uiState.heatVaStr.toDoubleOrNull() ?: 0.0

            // Basic input validation
            if (sqFt == null || numSmallApp == null || numLaundry == null || sqFt <= 0 || numSmallApp < 0 || numLaundry < 0) {
                uiState = uiState.copy(errorMessage = "Invalid area or circuit counts.", calculatedServiceLoadVa = null, calculatedServiceLoadAmps = null)
                return@launch
            }

            var error: String? = null
            var finalLoadVa: Double? = null

            try {
                // 1. General Lighting Load (220.14(J)) - 3 VA/sq ft
                val generalLightingVa = sqFt * 3.0

                // 2. Small Appliance Circuits (220.52(A)) - 1500 VA each
                val smallApplianceVa = max(numSmallApp, 2) * 1500.0 // NEC minimum 2

                // 3. Laundry Circuit (220.52(B)) - 1500 VA each
                val laundryVa = max(numLaundry, 1) * 1500.0 // NEC minimum 1

                // 4. Total Lighting, Small Appliance, Laundry
                val totalLightingApplianceLaundry = generalLightingVa + smallApplianceVa + laundryVa

                // 5. Apply Demand Factor (Table 220.42)
                val demandAppliedLightingApplianceLaundry = when {
                    totalLightingApplianceLaundry <= 3000 -> totalLightingApplianceLaundry * 1.00 // First 3kVA @ 100%
                    else -> 3000.0 + (totalLightingApplianceLaundry - 3000.0) * 0.35 // Remainder @ 35% (simplified - ignores > 120kVA rule for dwellings)
                }

                // 6. Fixed Appliance Load (220.53) - 75% demand if 4 or more
                val fixedApplianceDemand = if (numFixed >= 4) fixedVa * 0.75 else fixedVa

                // 7. Dryer Load (220.54) - Min 5000 VA or nameplate
                val dryerLoad = max(dryerVaInput, 5000.0)
                // Demand factor for dryers is 100% unless specific exceptions apply (not handled here)
                val dryerDemand = dryerLoad

                // 8. Range Load (Table 220.55) - Complex, using simplified approach for now
                // TODO: Implement full Table 220.55 logic (Column A, B, C, Notes)
                val rangeDemand = calculateRangeDemand(rangeVa) // Placeholder for complex logic

                // 9. AC / Heat Load (220.60) - Largest of the two
                val acHeatDemand = max(acVa, heatVa)

                // 10. Sum all demand loads
                // TODO: Add largest motor calculation (220.50) if motor inputs are added
                val totalDemandVa = demandAppliedLightingApplianceLaundry +
                                    fixedApplianceDemand +
                                    dryerDemand +
                                    rangeDemand +
                                    acHeatDemand

                finalLoadVa = totalDemandVa

                // Update state with intermediate and final results
                uiState = uiState.copy(
                    generalLightingLoadVa = generalLightingVa,
                    applianceLaundryLoadVa = smallApplianceVa + laundryVa,
                    totalLightingApplianceLaundryVa = totalLightingApplianceLaundry,
                    demandAppliedLightingApplianceLaundryVa = demandAppliedLightingApplianceLaundry,
                    dryerDemandVa = dryerDemand,
                    rangeDemandVa = rangeDemand,
                    fixedApplianceDemandVa = fixedApplianceDemand,
                    acHeatDemandVa = acHeatDemand,
                    calculatedServiceLoadVa = finalLoadVa,
                    calculatedServiceLoadAmps = finalLoadVa / 240.0, // Assuming 240V service
                    errorMessage = null
                )

            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = "Calculation Error: ${e.message}", calculatedServiceLoadVa = null, calculatedServiceLoadAmps = null)
            }
        }
    }

    // Refined Range Demand calculation (NEC Table 220.55 - Simplified for single appliance)
    private fun calculateRangeDemand(nameplateVa: Double): Double {
        if (nameplateVa <= 0) return 0.0

        val nameplateKw = nameplateVa / 1000.0

        // Use Column C for <= 12 kW (assumes 1 appliance)
        if (nameplateKw <= 12.0) {
            // Note 3: For ranges < 3.5 kW, use nameplate. (Simplified: using 8kW base still)
            // Note 4: Branch circuit load is different, this is for service/feeder.
            return 8000.0 // 8 kW demand from Column C
        }
        // Use Note 1 for > 12 kW
        else {
            // Increase 8 kW demand by 5% for each full kW over 12 kW
            val excessKw = ceil(nameplateKw - 12.0) // Round up to next full kW
            val percentageIncrease = excessKw * 0.05
            return 8000.0 * (1.0 + percentageIncrease)
        }
        // TODO: Implement Columns A & B for multiple appliances later.
        // TODO: Implement Note 3 more accurately if needed.
    }


    // Helper to format results nicely
    private fun Double.formatResult(decimals: Int = 0): String {
        return String.format("%.${decimals}f", this)
    }
}
