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
import kotlinx.coroutines.flow.* // Import Flow operators
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.electricianappnew.ui.common.formatCalculationResult // Import shared helper
import android.util.Log // Import Log
// import java.util.Locale // Not explicitly used here

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

    // Change uiState to use MutableStateFlow and expose as StateFlow
    private val _uiState = MutableStateFlow(RacewaySizingUiState())
    val uiState: StateFlow<RacewaySizingUiState> = _uiState.asStateFlow()

    // State for dropdown options (keep as mutableStateOf for now, could be converted later)
    var racewayTypeNames by mutableStateOf<List<String>>(emptyList())
        private set
    var wireTypeNames by mutableStateOf<List<String>>(emptyList()) // Keep as mutableStateOf for now
        private set

    // StateFlow for all distinct conductor sizes (similar to ConduitFillViewModel)
    val conductorSizes: StateFlow<List<String>> = necDataRepository.getAllNecWireAreaEntries()
        .map { entries -> entries.map { it.size }.distinct().sorted() } // Simple alphabetical sort
        .catch { e ->
            Log.e("RacewaySizingVM", "Error fetching distinct conductor sizes", e)
            _uiState.update { it.copy(errorMessage = "Error loading conductor sizes: ${e.message}") } // Update _uiState
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    init {
        loadInitialData() // Keep init block for raceway/wire types
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Collect flows to get the lists
            val collectedRacewayTypes = necDataRepository.getDistinctConduitTypes().firstOrNull() ?: emptyList()
            racewayTypeNames = collectedRacewayTypes

            val collectedWireTypes = necDataRepository.getDistinctWireTypes().firstOrNull() ?: emptyList()
            wireTypeNames = collectedWireTypes

            val initialRacewayType = collectedRacewayTypes.firstOrNull() ?: ""
            val initialWireType = collectedWireTypes.firstOrNull() ?: ""

            // Collect wire sizes based on the initial wire type
            val collectedWireSizes = if (initialWireType.isNotEmpty()) {
                necDataRepository.getDistinctWireSizesForType(initialWireType).firstOrNull() ?: emptyList()
            } else {
                emptyList()
            }
            // Removed availableWireSizes update
            val initialWireSize = collectedWireSizes.firstOrNull() ?: ""

            // Update UI state after all data is collected
            _uiState.update { // Update _uiState
                it.copy(
                    selectedRacewayType = initialRacewayType,
                    // Ensure the initial WireEntry no longer uses availableSizes
                    wireEntries = listOf(WireEntry(type = initialWireType, size = initialWireSize)) // Removed availableSizes
                )
            }

            // Initial calculation might run if defaults are valid
            if (initialRacewayType.isNotEmpty()) {
                calculateRacewaySize()
            }
        }
    }

    // --- Input Change Handlers ---

    fun onRacewayTypeChange(newTypeName: String) {
        _uiState.update { // Update _uiState
            it.copy(selectedRacewayType = newTypeName, errorMessage = null, calculatedRacewaySize = null) // Clear result on type change
        }
        calculateRacewaySize()
    }

    // Removed updateAvailableWireSizes function


    fun addWireEntry() {
        viewModelScope.launch { // Ensure DB access is in scope if needed for defaults
            val defaultType = wireTypeNames.firstOrNull() ?: ""
            // Fetch sizes for the default type (still needed for default size selection)
            val defaultSizes = if (defaultType.isNotEmpty()) {
                necDataRepository.getDistinctWireSizesForType(defaultType).firstOrNull() ?: emptyList()
            } else {
                emptyList()
            }
            val defaultSize = defaultSizes.firstOrNull() ?: ""
            // Add new entry without availableSizes
            val newEntry = WireEntry(type = defaultType, size = defaultSize) // Removed availableSizes
            _uiState.update { currentState -> // Update _uiState
                 currentState.copy(wireEntries = currentState.wireEntries + newEntry)
            }
            calculateRacewaySize()
        }
    }

     fun removeWireEntry(index: Int) {
        if (_uiState.value.wireEntries.size > 0) { // Allow removing the last one, check _uiState.value
            _uiState.update { currentState -> // Update _uiState
                val updatedList = currentState.wireEntries.filterIndexed { i, _ -> i != index }
                currentState.copy(wireEntries = updatedList)
            }
            calculateRacewaySize()
        }
    }

    fun updateWireEntry(index: Int, updatedEntry: WireEntry) {
        // Use viewModelScope for potential DB access if type changes
        viewModelScope.launch {
            val currentList = _uiState.value.wireEntries.toMutableList() // Get list from _uiState.value
            if (index >= 0 && index < currentList.size) {
                val oldEntry = currentList[index]
            // Ensure quantity is at least 1
            val finalEntry = if (updatedEntry.quantity < 1) updatedEntry.copy(quantity = 1) else updatedEntry

            // Check if wire type changed. If so, reset the size to the first available for the new type.
            if (oldEntry.type != finalEntry.type) {
                viewModelScope.launch { // Need scope for DB access
                        val newSizes = necDataRepository.getDistinctWireSizesForType(finalEntry.type).firstOrNull() ?: emptyList()
                        val newDefaultSize = newSizes.firstOrNull() ?: ""
                        currentList[index] = finalEntry.copy(size = newDefaultSize) // Update entry with new type and reset size
                        _uiState.update { it.copy(wireEntries = currentList.toList(), errorMessage = null) } // Update _uiState
                        calculateRacewaySize() // Recalculate
                    }
                } else {
                    // Type didn't change, just update the entry directly
                    currentList[index] = finalEntry // Update with the final entry (which already has quantity check)
                    _uiState.update { it.copy(wireEntries = currentList.toList(), errorMessage = null) } // Update _uiState
                    calculateRacewaySize() // Recalculate
                }
            }
        } // End viewModelScope.launch
    }

    // --- Calculation Logic ---
    // --- Calculation Logic ---
    private fun calculateRacewaySize() { // Made private as it's called internally
        viewModelScope.launch {
            val currentState = _uiState.value // Get snapshot of current state

            if (currentState.selectedRacewayType.isBlank()) {
                _uiState.update { // Update _uiState
                    it.copy(errorMessage = "Please select a raceway type.", totalWireArea = null, requiredFillPercent = null, minimumRacewayArea = null, calculatedRacewaySize = null)
                }
                return@launch
            }

            var currentTotalWireArea = 0.0
            var totalWireCount = 0
            var calculationPossible = true
            var errorMsg: String? = null

            // 1. Calculate Total Wire Area
            if (currentState.wireEntries.isEmpty()) {
                 _uiState.update { // Update _uiState
                     it.copy(errorMessage = "Add at least one conductor.", totalWireArea = 0.0, requiredFillPercent = null, minimumRacewayArea = null, calculatedRacewaySize = null)
                 }
                 return@launch
            }

            for (entry in currentState.wireEntries) { // Iterate over currentState
                if (entry.type.isBlank() || entry.size.isBlank()) {
                    // Don't break, just mark as impossible and clear results later
                    errorMsg = "Select wire type and size for all entries."
                    calculationPossible = false
                    // continue // Allow checking other entries for errors if needed
                }
                // Ensure quantity is valid before proceeding with area calculation
                if (entry.quantity <= 0) {
                    errorMsg = "Wire quantity must be positive."
                    calculationPossible = false
                    // continue
                }

                if (calculationPossible) { // Only fetch area if inputs seem valid so far
                    val wireAreaEntry = necDataRepository.getWireAreaEntry(entry.type, entry.size)
                    if (wireAreaEntry == null) {
                        errorMsg = "Wire area not found for: ${entry.type} ${entry.size}"
                        calculationPossible = false
                    } else {
                        currentTotalWireArea += wireAreaEntry.areaSqIn * entry.quantity
                    }
                }
                 // Always count wires if quantity is positive, even if area lookup fails,
                 // as the count determines fill percentage.
                 if(entry.quantity > 0) {
                    totalWireCount += entry.quantity
                 }
            }

            // If calculation became impossible at any point, update state and exit
            if (!calculationPossible) {
                _uiState.update { // Update _uiState
                    it.copy(
                        errorMessage = errorMsg ?: "Invalid input.", // Provide a default error
                        totalWireArea = null, // Clear results if input is invalid
                        requiredFillPercent = null,
                        minimumRacewayArea = null,
                        calculatedRacewaySize = null
                    )
                }
                return@launch
            }

            // Check again for zero wires after validation loop (e.g., if all entries had qty 0)
             if (totalWireCount == 0) {
                 _uiState.update { // Update _uiState
                     it.copy(errorMessage = "Total conductor quantity is zero.", totalWireArea = 0.0, requiredFillPercent = null, minimumRacewayArea = null, calculatedRacewaySize = null)
                 }
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
                 _uiState.update { // Update _uiState
                     it.copy(errorMessage = "Invalid fill percentage.", totalWireArea = currentTotalWireArea, requiredFillPercent = fillPercent, minimumRacewayArea = null, calculatedRacewaySize = null)
                 }
                 return@launch
            }
            val minAreaRequired = currentTotalWireArea / allowablePercentDecimal

            // 4. Find Smallest Suitable Raceway Size
            // Fetch the list of raceways for the selected type
            val allRacewaysForType: List<NecConduitEntry> = try {
                 necDataRepository.getAllConduitEntriesForType(currentState.selectedRacewayType) // Use currentState
            } catch (e: Exception) {
                 _uiState.update { it.copy(errorMessage = "Error fetching raceway data: ${e.message}", calculatedRacewaySize = null) } // Update _uiState
                 return@launch
            }

            // Now apply list operations on the fetched list
            val suitableRaceway: NecConduitEntry? = allRacewaysForType
                .filter { it.internalAreaSqIn > 0 } // Ensure area is positive
                .sortedBy { it.internalAreaSqIn } // Sort by area ascending
                .firstOrNull { it.internalAreaSqIn >= minAreaRequired } // Find first that fits

            if (suitableRaceway == null && minAreaRequired > 0) { // Only show error if area is required
                errorMsg = "No suitable size found for ${currentState.selectedRacewayType} with required area ${minAreaRequired.formatCalculationResult(4)} in²." // Use currentState
            } else {
                errorMsg = null // Clear previous errors if calculation is now successful
            }


            _uiState.update { // Update _uiState
                it.copy(
                    totalWireArea = currentTotalWireArea,
                    requiredFillPercent = fillPercent,
                    minimumRacewayArea = minAreaRequired,
                    calculatedRacewaySize = suitableRaceway?.size, // Store the trade size name
                    errorMessage = errorMsg
                )
            }
        } // End viewModelScope.launch
    }

    fun clearInputs() {
        viewModelScope.launch {
            // Reset state, keeping fetched dropdown options but re-fetching initial sizes
            val initialRacewayType = racewayTypeNames.firstOrNull() ?: ""
            val initialWireType = wireTypeNames.firstOrNull() ?: ""

            val initialWireSizes = if (initialWireType.isNotEmpty()) {
                 necDataRepository.getDistinctWireSizesForType(initialWireType).firstOrNull() ?: emptyList()
            } else {
                emptyList()
            }
            // Removed availableWireSizes update
            val initialWireSize = initialWireSizes.firstOrNull() ?: ""

            _uiState.value = RacewaySizingUiState( // Correct: Assign to _uiState.value
                selectedRacewayType = initialRacewayType,
                wireEntries = listOf(WireEntry(type = initialWireType, size = initialWireSize)) // Removed availableSizes
            )
            calculateRacewaySize() // Recalculate
        }
    }

    // Removed local formatResult helper - use shared one from common package
}
