package com.example.electricianappnew.ui.calculators.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.NecConduitEntry
import com.example.electricianappnew.data.model.NecWireAreaEntry
import com.example.electricianappnew.data.repository.NecDataRepository
import com.example.electricianappnew.data.model.WireEntry // Import centrally defined WireEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.electricianappnew.ui.common.formatCalculationResult // Import shared helper
import java.util.Locale // Import Locale

// Detailed UI State for Raceway Sizing
data class RacewaySizingUiState(
    val selectedRacewayType: String = "",
    val wireEntries: List<WireEntry> = listOf(WireEntry()), // Use list like Conduit Fill
    val totalWireArea: Double? = null,
    val requiredFillPercent: Double? = null,
    val minimumRacewayArea: Double? = null,
    val calculatedRacewaySize: String? = null, // The resulting trade size
    val errorMessage: String? = null
)

@HiltViewModel
class RacewaySizingViewModel @Inject constructor(
    private val necDataRepository: NecDataRepository
) : ViewModel() {

    var uiState by mutableStateOf(RacewaySizingUiState())
        private set

    // State for dropdown options
    var racewayTypeNames by mutableStateOf<List<String>>(emptyList())
        private set
    var wireTypeNames by mutableStateOf<List<String>>(emptyList())
        private set
    var availableWireSizes by mutableStateOf<List<String>>(emptyList()) // Sizes for the currently selected wire type
        private set

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            racewayTypeNames = necDataRepository.getDistinctConduitTypes() // Reuse conduit types
            wireTypeNames = necDataRepository.getDistinctWireTypes()

            val initialRacewayType = racewayTypeNames.firstOrNull() ?: ""
            val initialWireType = wireTypeNames.firstOrNull() ?: ""

            availableWireSizes = if (initialWireType.isNotEmpty()) {
                necDataRepository.getDistinctWireSizesForType(initialWireType)
            } else {
                emptyList()
            }
            val initialWireSize = availableWireSizes.firstOrNull() ?: ""

            uiState = uiState.copy(
                selectedRacewayType = initialRacewayType,
                wireEntries = listOf(WireEntry(type = initialWireType, size = initialWireSize))
            )
            // Initial calculation might run if defaults are valid
            if (initialRacewayType.isNotEmpty()) {
                calculateRacewaySize()
            }
        }
    }

    // --- Input Change Handlers ---

    fun onRacewayTypeChange(newTypeName: String) {
        uiState = uiState.copy(selectedRacewayType = newTypeName, errorMessage = null, calculatedRacewaySize = null) // Clear result on type change
        calculateRacewaySize()
    }

    // Reuse logic similar to ConduitFillViewModel for managing wire entries
    private fun updateAvailableWireSizes(wireType: String) {
         viewModelScope.launch {
             availableWireSizes = necDataRepository.getDistinctWireSizesForType(wireType)
             calculateRacewaySize() // Recalculate if wire sizes change might affect area lookups implicitly
         }
    }

    fun addWireEntry() {
         val defaultType = wireTypeNames.firstOrNull() ?: ""
         val defaultSize = availableWireSizes.firstOrNull() ?: ""
        val updatedList = uiState.wireEntries + WireEntry(type = defaultType, size = defaultSize)
        uiState = uiState.copy(wireEntries = updatedList)
        calculateRacewaySize()
    }

     fun removeWireEntry(index: Int) {
        if (uiState.wireEntries.size > 0) { // Allow removing the last one
            val updatedList = uiState.wireEntries.filterIndexed { i, _ -> i != index }
            uiState = uiState.copy(wireEntries = updatedList)
            calculateRacewaySize()
        }
    }

     fun updateWireEntry(index: Int, updatedEntry: WireEntry) {
        val currentList = uiState.wireEntries.toMutableList()
        if (index >= 0 && index < currentList.size) {
            val finalEntry = if(updatedEntry.quantity < 1) updatedEntry.copy(quantity = 1) else updatedEntry
             if (currentList[index].type != finalEntry.type) {
                 updateAvailableWireSizes(finalEntry.type)
                 currentList[index] = finalEntry // Keep size for now
             } else {
                currentList[index] = finalEntry
             }
            uiState = uiState.copy(wireEntries = currentList.toList(), errorMessage = null)
            calculateRacewaySize()
        }
    }

    // --- Calculation Logic ---
    fun calculateRacewaySize() {
        viewModelScope.launch {
            if (uiState.selectedRacewayType.isBlank()) {
                uiState = uiState.copy(errorMessage = "Please select a raceway type.", calculatedRacewaySize = null)
                return@launch
            }

            var currentTotalWireArea = 0.0
            var totalWireCount = 0
            var calculationPossible = true
            var errorMsg: String? = null

            // 1. Calculate Total Wire Area
            for (entry in uiState.wireEntries) {
                 if (entry.type.isBlank() || entry.size.isBlank()) {
                     errorMsg = "Select wire type and size for all entries."
                     calculationPossible = false
                     break
                 }
                val wireAreaEntry = necDataRepository.getWireAreaEntry(entry.type, entry.size)
                if (wireAreaEntry == null) {
                    errorMsg = "Wire area not found for: ${entry.type} ${entry.size}"
                    calculationPossible = false
                    break
                }
                 if (entry.quantity <= 0) {
                     errorMsg = "Wire quantity must be positive."
                     calculationPossible = false
                     break
                 }
                currentTotalWireArea += wireAreaEntry.areaSqIn * entry.quantity
                totalWireCount += entry.quantity
            }

            if (!calculationPossible) {
                uiState = uiState.copy(errorMessage = errorMsg, totalWireArea = null, requiredFillPercent = null, minimumRacewayArea = null, calculatedRacewaySize = null)
                return@launch
            }

             if (totalWireCount == 0) {
                 uiState = uiState.copy(errorMessage = "Add at least one conductor.", totalWireArea = 0.0, requiredFillPercent = null, minimumRacewayArea = null, calculatedRacewaySize = null)
                 return@launch
             }


            // 2. Determine Allowable Fill Percentage (NEC Chapter 9, Table 1)
            val allowablePercentDecimal = when {
                totalWireCount == 1 -> 0.53 // 53%
                totalWireCount == 2 -> 0.31 // 31%
                totalWireCount > 2 -> 0.40  // 40%
                else -> 0.0 // Should not happen due to check above
            }
            val fillPercent = allowablePercentDecimal * 100

            // 3. Calculate Minimum Required Raceway Area
            if (allowablePercentDecimal <= 0) {
                 uiState = uiState.copy(errorMessage = "Invalid fill percentage.", totalWireArea = currentTotalWireArea, requiredFillPercent = fillPercent, minimumRacewayArea = null, calculatedRacewaySize = null)
                 return@launch
            }
            val minAreaRequired = currentTotalWireArea / allowablePercentDecimal

            // 4. Find Smallest Suitable Raceway Size
            val availableRaceways = necDataRepository.getAllConduitEntriesForType(uiState.selectedRacewayType)
            val suitableRaceway = availableRaceways.firstOrNull { it.internalAreaSqIn >= minAreaRequired }

            if (suitableRaceway == null) {
                 // Use the imported shared helper function here
                errorMsg = "No suitable size found for ${uiState.selectedRacewayType} with required area ${minAreaRequired.formatCalculationResult(4)} in²." // Ensured correct function name is used
            }

            uiState = uiState.copy(
                totalWireArea = currentTotalWireArea,
                requiredFillPercent = fillPercent,
                minimumRacewayArea = minAreaRequired,
                calculatedRacewaySize = suitableRaceway?.size, // Store the trade size name
                errorMessage = errorMsg
            )
        }
    }

     fun clearInputs() {
        // Reset state, keeping fetched dropdown options
        val initialRacewayType = racewayTypeNames.firstOrNull() ?: ""
        val initialWireType = wireTypeNames.firstOrNull() ?: ""
        val initialWireSize = availableWireSizes.firstOrNull() ?: "" // Use current available sizes
        uiState = RacewaySizingUiState(
            selectedRacewayType = initialRacewayType,
            wireEntries = listOf(WireEntry(type = initialWireType, size = initialWireSize))
        )
        calculateRacewaySize() // Recalculate
    }

    // Removed local formatResult helper - use shared one from common package
}
