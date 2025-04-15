package com.example.electricianappnew.ui.calculators.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.NecBoxFillEntry
import com.example.electricianappnew.data.repository.NecDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// More detailed state for Box Fill
data class BoxFillUiState(
    // Inputs - Using maps to store counts per size for conductors/grounds
    val conductorCounts: Map<String, String> = mapOf(), // Map<Size, CountString>
    val groundCounts: Map<String, String> = mapOf(),    // Map<Size, CountString>
    val numClampsStr: String = "0",
    val numSupportFittingsStr: String = "0",
    val numDevicesStr: String = "0", // Yokes/Straps
    val boxVolumeStr: String = "", // User can enter volume directly

    // Results
    val calculatedFillVolume: Double? = null,
    val boxVolume: Double? = null,
    val isOverfill: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class BoxFillViewModel @Inject constructor(
    private val necDataRepository: NecDataRepository
) : ViewModel() {

    var uiState by mutableStateOf(BoxFillUiState())
        private set

    // State for dropdown options
    var conductorSizes by mutableStateOf<List<String>>(emptyList())
        private set

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            conductorSizes = necDataRepository.getDistinctBoxFillConductorSizes()
            // Initialize maps with available sizes, count set to "0"
            val initialConductorCounts = conductorSizes.associateWith { "0" }
            val initialGroundCounts = conductorSizes.associateWith { "0" }
            uiState = uiState.copy(
                conductorCounts = initialConductorCounts,
                groundCounts = initialGroundCounts
            )
        }
    }

    // --- Input Change Handlers ---

    fun onConductorCountChange(size: String, countStr: String) {
        val currentCounts = uiState.conductorCounts.toMutableMap()
        currentCounts[size] = countStr
        uiState = uiState.copy(conductorCounts = currentCounts, errorMessage = null)
        calculateBoxFill()
    }

    fun onGroundCountChange(size: String, countStr: String) {
        val currentCounts = uiState.groundCounts.toMutableMap()
        currentCounts[size] = countStr
        uiState = uiState.copy(groundCounts = currentCounts, errorMessage = null)
        calculateBoxFill()
    }

    fun onClampCountChange(countStr: String) {
        uiState = uiState.copy(numClampsStr = countStr, errorMessage = null)
        calculateBoxFill()
    }

    fun onSupportFittingCountChange(countStr: String) {
        uiState = uiState.copy(numSupportFittingsStr = countStr, errorMessage = null)
        calculateBoxFill()
    }

    fun onDeviceCountChange(countStr: String) {
        uiState = uiState.copy(numDevicesStr = countStr, errorMessage = null)
        calculateBoxFill()
    }

     fun onBoxVolumeChange(volumeStr: String) {
        uiState = uiState.copy(boxVolumeStr = volumeStr, errorMessage = null)
        calculateBoxFill() // Recalculate when box volume changes
    }

    fun clearInputs() {
        // Reset counts and volume, keep fetched sizes
        val initialConductorCounts = conductorSizes.associateWith { "0" }
        val initialGroundCounts = conductorSizes.associateWith { "0" }
        uiState = BoxFillUiState(
            conductorCounts = initialConductorCounts,
            groundCounts = initialGroundCounts
        )
    }


    // --- Calculation Logic ---

    fun calculateBoxFill() {
        viewModelScope.launch {
            var calculatedVolume = 0.0
            var error: String? = null
            var largestConductorSize: String? = null // Track largest size for clamps, grounds, etc.
            var largestGroundSize: String? = null // Track largest ground specifically
            var largestDeviceConductorSize: String? = null // Track largest connected to a device

            try {
                // 1. Conductor Fill (314.16(B)(1))
                uiState.conductorCounts.forEach { (size, countStr) ->
                    val count = countStr.toIntOrNull() ?: 0
                    if (count > 0) {
                        val entry = necDataRepository.getBoxFillEntry("Conductor", size)
                        if (entry == null) throw CalculationException("Volume allowance not found for $size conductor.")
                        calculatedVolume += count * entry.volumeAllowanceCuIn
                        largestConductorSize = getLargerSize(largestConductorSize, size)
                        // Track largest connected to device if devices exist
                        if ((uiState.numDevicesStr.toIntOrNull() ?: 0) > 0) {
                             largestDeviceConductorSize = getLargerSize(largestDeviceConductorSize, size)
                        }
                    }
                }

                // 2. Clamp Fill (314.16(B)(2)) - One allowance based on largest conductor
                val numClamps = uiState.numClampsStr.toIntOrNull() ?: 0
                if (numClamps > 0) {
                    if (largestConductorSize == null) throw CalculationException("Cannot calculate clamp fill without conductors.")
                    val entry = necDataRepository.getBoxFillEntry("Clamp", largestConductorSize)
                        ?: necDataRepository.getBoxFillEntry("Clamp", "N/A") // Fallback if specific size not found for clamps
                    if (entry == null) throw CalculationException("Volume allowance not found for clamps (based on $largestConductorSize conductor).")
                    calculatedVolume += entry.volumeAllowanceCuIn // Only add ONCE regardless of numClamps > 0
                }

                // 3. Support Fittings Fill (314.16(B)(3)) - One allowance each based on largest conductor
                val numFittings = uiState.numSupportFittingsStr.toIntOrNull() ?: 0
                if (numFittings > 0) {
                     if (largestConductorSize == null) throw CalculationException("Cannot calculate fitting fill without conductors.")
                     val entry = necDataRepository.getBoxFillEntry("Fitting", largestConductorSize)
                         ?: necDataRepository.getBoxFillEntry("Fitting", "N/A") // Fallback
                     if (entry == null) throw CalculationException("Volume allowance not found for support fittings (based on $largestConductorSize conductor).")
                     calculatedVolume += numFittings * entry.volumeAllowanceCuIn
                }

                // 4. Device Fill (314.16(B)(4)) - Double allowance per yoke based on largest conductor connected TO DEVICE
                val numDevices = uiState.numDevicesStr.toIntOrNull() ?: 0
                if (numDevices > 0) {
                     if (largestDeviceConductorSize == null) throw CalculationException("Cannot calculate device fill without conductors connected to them.")
                     val entry = necDataRepository.getBoxFillEntry("Device", largestDeviceConductorSize)
                         ?: necDataRepository.getBoxFillEntry("Device", "N/A") // Fallback
                     if (entry == null) throw CalculationException("Volume allowance not found for devices (based on $largestDeviceConductorSize conductor).")
                     calculatedVolume += numDevices * entry.volumeAllowanceCuIn * entry.countMultiplier // Multiplier is usually 2
                }

                // 5. Equipment Grounding Conductor Fill (314.16(B)(5)) - One allowance based on largest EGC
                uiState.groundCounts.forEach { (size, countStr) ->
                     val count = countStr.toIntOrNull() ?: 0
                     if (count > 0) {
                         largestGroundSize = getLargerSize(largestGroundSize, size)
                     }
                 }
                if (largestGroundSize != null) {
                    val entry = necDataRepository.getBoxFillEntry("Ground", largestGroundSize)
                         ?: necDataRepository.getBoxFillEntry("Ground", "N/A") // Fallback
                    if (entry == null) throw CalculationException("Volume allowance not found for grounding conductors (based on $largestGroundSize).")
                    calculatedVolume += entry.volumeAllowanceCuIn // Only add ONCE for all grounds
                }

                // --- Comparison ---
                val boxVol = uiState.boxVolumeStr.toDoubleOrNull()
                val overfill = if (boxVol != null && boxVol > 0) calculatedVolume > boxVol else false

                uiState = uiState.copy(
                    calculatedFillVolume = calculatedVolume,
                    boxVolume = boxVol,
                    isOverfill = overfill,
                    errorMessage = null
                )

            } catch (e: CalculationException) {
                uiState = uiState.copy(errorMessage = e.message, calculatedFillVolume = null, boxVolume = uiState.boxVolumeStr.toDoubleOrNull(), isOverfill = false)
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = "Calculation error: ${e.message}", calculatedFillVolume = null, boxVolume = uiState.boxVolumeStr.toDoubleOrNull(), isOverfill = false)
            }
        }
    }

    // Helper to compare wire sizes (simplified, assumes AWG format primarily)
    // Returns the larger of the two sizes, or newSize if currentLargest is null
    private fun getLargerSize(currentLargest: String?, newSize: String): String {
        if (newSize == "N/A") return currentLargest ?: newSize // Should not happen for conductors/grounds typically
        if (currentLargest == null || currentLargest == "N/A") return newSize

        // Basic comparison logic (needs refinement for kcmil vs AWG)
        val currentIsKcmil = currentLargest.contains("kcmil")
        val newIsKcmil = newSize.contains("kcmil")

        if (currentIsKcmil && !newIsKcmil) return currentLargest // kcmil > AWG
        if (!currentIsKcmil && newIsKcmil) return newSize      // kcmil > AWG

        if (currentIsKcmil) { // Both kcmil
            val currentVal = currentLargest.substringBefore(" ").toDoubleOrNull() ?: 0.0
            val newVal = newSize.substringBefore(" ").toDoubleOrNull() ?: 0.0
            return if (newVal > currentVal) newSize else currentLargest
        } else { // Both AWG (smaller number is larger wire)
            val currentVal = currentLargest.substringBefore(" ").toIntOrNull() ?: 99
            val newVal = newSize.substringBefore(" ").toIntOrNull() ?: 99
            return if (newVal < currentVal) newSize else currentLargest
        }
    }

     // Custom exception for clarity
    class CalculationException(message: String): Exception(message)
}
