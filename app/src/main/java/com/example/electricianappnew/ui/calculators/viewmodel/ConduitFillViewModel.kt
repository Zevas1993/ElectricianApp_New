package com.example.electricianappnew.ui.calculators.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.NecConduitEntry
import com.example.electricianappnew.data.model.NecWireAreaEntry
import com.example.electricianappnew.data.repository.NecDataRepository
import com.example.electricianappnew.ui.calculators.WireEntry // Assuming WireEntry is defined elsewhere (e.g., Screen)
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull // Keep for individual lookups
import kotlinx.coroutines.launch
import javax.inject.Inject
// Remove potentially conflicting imports if covered by wildcard or unused
// import com.example.electricianappnew.data.model.NecConduitEntry
// import com.example.electricianappnew.data.model.NecWireAreaEntry
import com.example.electricianappnew.data.model.* // Assuming wildcard import exists or add specific needed ones

// Data class for UI State (excluding dropdown lists, which are separate state in VM)
data class ConduitFillUiState(
    val selectedConduitTypeName: String = "",
    val selectedConduitSize: String = "",
    val wireEntries: List<WireEntry> = listOf(WireEntry()), // Start with one wire
    val totalWireArea: Double? = null,
    val allowableFillArea: Double? = null,
    val fillPercentage: Double? = null,
    val isOverfill: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ConduitFillViewModel @Inject constructor(
     private val necDataRepository: NecDataRepository // Inject repository
) : ViewModel() {

    var uiState by mutableStateOf(ConduitFillUiState())
        private set

    // State for dropdown options
    var conduitTypeNames by mutableStateOf<List<String>>(emptyList())
        private set
    var availableConduitSizes by mutableStateOf<List<String>>(emptyList())
        private set
    var wireTypeNames by mutableStateOf<List<String>>(emptyList())
        private set
    var availableWireSizes by mutableStateOf<List<String>>(emptyList()) // Sizes for the currently selected wire type in WireEntry

    // Removed hardcoded lists

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Fetch initial dropdown lists from repository
            conduitTypeNames = necDataRepository.getDistinctConduitTypes()
            wireTypeNames = necDataRepository.getDistinctWireTypes()

            val initialConduitType = conduitTypeNames.firstOrNull() ?: ""
            val initialWireType = wireTypeNames.firstOrNull() ?: ""

            // Fetch initial sizes based on the first type
            availableConduitSizes = if (initialConduitType.isNotEmpty()) {
                necDataRepository.getDistinctConduitSizesForType(initialConduitType)
            } else {
                emptyList()
            }
            availableWireSizes = if (initialWireType.isNotEmpty()) {
                necDataRepository.getDistinctWireSizesForType(initialWireType)
            } else {
                emptyList()
            }

            val initialConduitSize = availableConduitSizes.firstOrNull() ?: ""
            val initialWireSize = availableWireSizes.firstOrNull() ?: ""

            uiState = uiState.copy(
                selectedConduitTypeName = initialConduitType,
                selectedConduitSize = initialConduitSize,
                wireEntries = listOf(WireEntry(type = initialWireType, size = initialWireSize)) // Start with defaults
            )
            // Initial calculation might be needed if defaults are valid
            if (initialConduitType.isNotEmpty() && initialConduitSize.isNotEmpty()) {
                 calculateFill()
            }
        }
    }

    // Function to update available conduit sizes when type changes
    private fun updateAvailableConduitSizes(conduitType: String) {
        viewModelScope.launch {
            availableConduitSizes = necDataRepository.getDistinctConduitSizesForType(conduitType)
            // Reset selected size if the current one isn't valid for the new type
            val currentSize = uiState.selectedConduitSize
            val newSize = if (availableConduitSizes.contains(currentSize)) currentSize else availableConduitSizes.firstOrNull() ?: ""
            uiState = uiState.copy(selectedConduitSize = newSize)
            calculateFill() // Recalculate after size might have changed
        }
    }

     // Function to update available wire sizes for a specific entry (or globally if needed)
    private fun updateAvailableWireSizes(wireType: String) {
         viewModelScope.launch {
             availableWireSizes = necDataRepository.getDistinctWireSizesForType(wireType)
             // Note: This updates the *global* list for the ViewModel.
             // The UI dropdown for each row might need to handle filtering/validation
             // if the selected size becomes invalid for the new type.
             // For simplicity now, we let the user re-select the size if needed.
             calculateFill() // Recalculate as wire areas might change implicitly
         }
    }


    fun onConduitTypeChange(newTypeName: String) {
        uiState = uiState.copy(selectedConduitTypeName = newTypeName, errorMessage = null)
        updateAvailableConduitSizes(newTypeName) // Fetch and update sizes for the new type
        // calculateFill() is called within updateAvailableConduitSizes
    }

    fun onConduitSizeChange(newSize: String) {
        uiState = uiState.copy(selectedConduitSize = newSize, errorMessage = null)
        calculateFill()
    }

     fun addWireEntry() {
        // Add a new entry with default/first available type and size
         val defaultType = wireTypeNames.firstOrNull() ?: ""
         // Fetch sizes for the default type if needed (though availableWireSizes might be stale if types differ)
         // For simplicity, use the current availableWireSizes list, assuming it's relevant or user will adjust
         val defaultSize = availableWireSizes.firstOrNull() ?: ""

        val updatedList = uiState.wireEntries + WireEntry(type = defaultType, size = defaultSize)
        uiState = uiState.copy(wireEntries = updatedList)
        calculateFill() // Recalculate after adding
    }

     fun removeWireEntry(index: Int) {
        if (uiState.wireEntries.size > 1) {
            val updatedList = uiState.wireEntries.filterIndexed { i, _ -> i != index }
            uiState = uiState.copy(wireEntries = updatedList)
            calculateFill()
        }
    }

     fun updateWireEntry(index: Int, updatedEntry: WireEntry) {
        val currentList = uiState.wireEntries.toMutableList()
        if (index >= 0 && index < currentList.size) {
             // Ensure quantity is at least 1
            val finalEntry = if(updatedEntry.quantity < 1) updatedEntry.copy(quantity = 1) else updatedEntry

             // If type changed, fetch new available sizes for that type
             if (currentList[index].type != finalEntry.type) {
                 updateAvailableWireSizes(finalEntry.type) // Update the general list
                 // Decide how to handle the size: keep it, reset it, or validate it?
                 // Option: Keep it, let UI handle showing valid options.
                 currentList[index] = finalEntry
             } else {
                currentList[index] = finalEntry
             }

            uiState = uiState.copy(wireEntries = currentList.toList(), errorMessage = null)
            calculateFill()
        }
    }


    // Make public for LaunchedEffect in Screen
    fun calculateFill() {
         viewModelScope.launch {
            if (uiState.selectedConduitTypeName.isBlank() || uiState.selectedConduitSize.isBlank()) {
                 // Don't calculate if conduit type/size not selected yet (during init)
                 return@launch
            }
            // Fetch conduit area from repository
            val conduitEntry = necDataRepository.getConduitEntry(uiState.selectedConduitTypeName, uiState.selectedConduitSize)
            // Use internalAreaSqIn directly as it represents the full area
            val conduitArea = conduitEntry?.internalAreaSqIn

            if (conduitArea == null || conduitArea <= 0) {
                uiState = uiState.copy(errorMessage = "Conduit properties not found or invalid area.", totalWireArea = null, allowableFillArea = null, fillPercentage = null, isOverfill = false)
                return@launch
            }

            var currentTotalWireArea = 0.0
            var totalWireCount = 0
            var calculationPossible = true
            var errorMsg: String? = null

            if (uiState.wireEntries.isEmpty()){
                 uiState = uiState.copy(totalWireArea = 0.0, allowableFillArea = conduitArea * 0.4, fillPercentage = 0.0, isOverfill = false, errorMessage = null) // Default to 40% allowable if empty
                 return@launch
            }

            // Fetch wire areas asynchronously
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
                    break // Exit loop early
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
                uiState = uiState.copy(errorMessage = errorMsg, totalWireArea = null, allowableFillArea = null, fillPercentage = null, isOverfill = false)
                return@launch
            }

            // Determine allowable fill percentage (NEC Chapter 9, Table 1)
            // Use specific fill areas from conduit entry if available and valid
            val allowablePercentDecimal = when {
                totalWireCount == 1 && conduitEntry.fillArea1WireSqIn > 0 -> conduitEntry.fillArea1WireSqIn / conduitArea
                totalWireCount == 2 && conduitEntry.fillArea2WiresSqIn > 0 -> conduitEntry.fillArea2WiresSqIn / conduitArea
                totalWireCount > 2 && conduitEntry.fillAreaOver2WiresSqIn > 0 -> conduitEntry.fillAreaOver2WiresSqIn / conduitArea
                // Fallback percentages if specific areas aren't available/valid
                totalWireCount == 1 -> 0.53
                totalWireCount == 2 -> 0.31
                totalWireCount > 2 -> 0.40
                else -> 0.0 // Should not happen if wireEntries is checked
            }

            val allowableArea = conduitArea * allowablePercentDecimal
            val percentage = if (conduitArea > 0) (currentTotalWireArea / conduitArea) * 100 else 0.0
            val overfill = currentTotalWireArea > allowableArea

            uiState = uiState.copy(
                totalWireArea = currentTotalWireArea,
                allowableFillArea = allowableArea,
                fillPercentage = percentage,
                isOverfill = overfill,
                errorMessage = null // Clear error if calculation succeeds
            )
         }
    }

     // Helper to format results nicely (can be shared or moved)
    private fun Double.formatResult(decimals: Int = 2): String {
        return String.format("%.${decimals}f", this).trimEnd('0').trimEnd('.')
    }
}
