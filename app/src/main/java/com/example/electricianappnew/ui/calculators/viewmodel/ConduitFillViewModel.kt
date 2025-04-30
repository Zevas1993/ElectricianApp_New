package com.example.electricianappnew.ui.calculators.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.* // Import all runtime functions
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.NecConduitEntry
import com.example.electricianappnew.data.model.NecWireAreaEntry
import com.example.electricianappnew.data.repository.NecDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // Import Flow operators
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import android.util.Log
import com.example.electricianappnew.data.model.*
import com.example.electricianappnew.data.model.WireEntry

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
    // Removed loading states - handled by initialValue in stateIn
)

@HiltViewModel
class ConduitFillViewModel @Inject constructor(
    private val necDataRepository: NecDataRepository // Inject repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConduitFillUiState())
    val uiState: StateFlow<ConduitFillUiState> = _uiState.asStateFlow()

    // --- StateFlows for Dropdown Options ---

    // Conduit Types
    val conduitTypeNames: StateFlow<List<String>> = necDataRepository.getDistinctConduitTypes()
        // .onStart { Log.d("ConduitFillVM", "Starting conduit types flow...") } // Removed Log
        // .onEach { Log.d("ConduitFillVM", "Received conduit types: $it") } // Removed Log
        .catch { e ->
            Log.e("ConduitFillVM", "Error fetching conduit types", e) // Keep error log
            _uiState.update { it.copy(errorMessage = "Error loading conduit types: ${e.message}") } // Removed loading state update
            emit(emptyList()) // Emit empty list on error
        }
        // Removed onCompletion loading state update
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Removed conductorMaterials flow

    // Conductor Sizes (Distinct sizes from nec_wire_areas table)
    val conductorSizes: StateFlow<List<String>> = necDataRepository.getAllNecWireAreaEntries()
        .map { entries ->
            // Extract distinct sizes and sort them (implement custom sort if needed)
            entries.map { it.size }.distinct().sorted() // Simple alphabetical sort for now
        }
        .catch { e ->
            Log.e("ConduitFillVM", "Error fetching distinct conductor sizes from wire areas", e)
            _uiState.update { it.copy(errorMessage = "Error loading conductor sizes: ${e.message}") }
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    // Wire Insulation Types (from nec_wire_areas) - This is correct
    val wireTypeNames: StateFlow<List<String>> = necDataRepository.getDistinctWireTypes()
        // .onStart { Log.d("ConduitFillVM", "Starting wire insulation types flow...") } // Removed Log
        // .onEach { Log.d("ConduitFillVM", "Received wire insulation types: $it") } // Removed Log
        .catch { e ->
            Log.e("ConduitFillVM", "Error fetching wire types", e) // Keep error log
            _uiState.update { it.copy(errorMessage = "Error loading wire types: ${e.message}") } // Removed loading state update
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Available Conduit Sizes (depends on selectedConduitTypeName)
    // Use flatMapLatest to react to changes in selectedConduitTypeName
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val availableConduitSizes: StateFlow<List<String>> = _uiState
        .map { it.selectedConduitTypeName } // Observe changes in selected type name
        .distinctUntilChanged() // Only react when the name actually changes
        .onEach { typeName -> Log.d("ConduitFillVM", "flatMapLatest: Input typeName changed to: '$typeName'") } // ADDED Log
        .flatMapLatest { typeName -> // Switch to the new flow when typeName changes
            Log.d("ConduitFillVM", "flatMapLatest: Executing lambda for typeName: '$typeName'") // ADDED Log
            if (typeName.isBlank()) {
                Log.d("ConduitFillVM", "flatMapLatest: typeName is blank, returning empty list flow.") // ADDED Log
                flowOf(emptyList()) // Return empty list if no type selected
            } else {
                Log.d("ConduitFillVM", "flatMapLatest: typeName is '$typeName', calling repository...") // ADDED Log
                // Removed loading state update
                necDataRepository.getDistinctConduitSizesForType(typeName)
                    .onStart { Log.d("ConduitFillVM", "flatMapLatest: Starting conduit sizes flow for type: '$typeName'") } // ADDED Log
                    .onEach { sizes -> Log.d("ConduitFillVM", "flatMapLatest: Received conduit sizes for '$typeName': $sizes") } // ADDED Log
                    .catch { e ->
                        Log.e("ConduitFillVM", "flatMapLatest: Error fetching conduit sizes for $typeName", e) // Keep error log
                        _uiState.update { it.copy(errorMessage = "Error loading sizes for $typeName: ${e.message}") } // Removed loading state update
                        emit(emptyList())
                    }
                    // Removed onCompletion loading state update
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Removed init block, observeInitialSelections, and fetchWireSizesForType
    // Initial state will be handled by StateFlow initial values and UI collection

    fun onConduitTypeChange(newTypeName: String) {
        // Log.d("ConduitFillVM", "onConduitTypeChange: $newTypeName") // Removed Log
        // Update the selected type. The availableConduitSizes flow will react.
        // Reset the selected size. The UI should observe availableConduitSizes and select the first one when it updates.
        _uiState.update { it.copy(selectedConduitTypeName = newTypeName, selectedConduitSize = "", errorMessage = null) }
        // No need to manually wait for sizes here, UI will handle it reactively.
        // Calculation will be triggered by onConduitSizeChange when UI updates the size.
    }

    fun onConduitSizeChange(newSize: String) {
        // Log.d("ConduitFillVM", "onConduitSizeChange: $newSize") // Removed Log
        if (newSize.isNotBlank()) { // Only calculate if a valid size is selected
             _uiState.update { it.copy(selectedConduitSize = newSize, errorMessage = null) }
             calculateFill()
        } else {
             _uiState.update { it.copy(selectedConduitSize = "", errorMessage = "Please select a conduit size.") } // Keep size blank, maybe show error
        }
    }

     fun addWireEntry() {
         viewModelScope.launch { // Need scope for repository access
             // Get default values from the current state of the flows
             val defaultWireType = wireTypeNames.value.firstOrNull() ?: ""
             // Fetch sizes specifically for the default wire type
             val sizesForDefaultType = if (defaultWireType.isNotEmpty()) {
                 try {
                     necDataRepository.getDistinctWireSizesForType(defaultWireType).firstOrNull() ?: emptyList()
                 } catch (e: Exception) {
                     Log.e("ConduitFillVM", "Error fetching sizes for default type $defaultWireType", e)
                     emptyList() // Fallback to empty list on error
                 }
             } else {
                 emptyList()
              }
              val defaultConductorSize = sizesForDefaultType.firstOrNull() ?: "" // Get first size for the specific type

              // Log.d("ConduitFillVM", "Adding wire entry with defaults: Type=$defaultWireType, Size=$defaultConductorSize") // Removed Log

              val newEntry = WireEntry(type = defaultWireType, size = defaultConductorSize) // Use correct default size
              _uiState.update { currentState ->
                  currentState.copy(wireEntries = currentState.wireEntries + newEntry)
              }
              // Log.d("ConduitFillVM", "Wire entry added.") // Removed Log
              calculateFill() // Recalculate after adding (already outside launch, but fine here)
          }
          // Log.d("ConduitFillVM", "Wire entry added.") // Removed Log
          calculateFill() // Recalculate after adding
     }

     fun removeWireEntry(index: Int) {
        if (_uiState.value.wireEntries.size > 1) {
            _uiState.update { currentState ->
                val updatedList = currentState.wireEntries.filterIndexed { i, _ -> i != index }
                currentState.copy(wireEntries = updatedList)
            }
            calculateFill()
        }
    }

    fun updateWireEntry(index: Int, updatedEntry: WireEntry) {
        viewModelScope.launch {
            val currentList = _uiState.value.wireEntries.toMutableList()
            if (index >= 0 && index < currentList.size) {
                val originalEntry = currentList[index]
                var entryToUpdate = updatedEntry

                // Ensure quantity is positive
                if (entryToUpdate.quantity < 1) {
                    entryToUpdate = entryToUpdate.copy(quantity = 1)
                }

                // Removed logic for fetching/updating availableSizes.
                // The UI will be responsible for fetching/displaying relevant sizes based on the selected type.
                // We just update the entry in the list.
                currentList[index] = entryToUpdate

                _uiState.update { it.copy(wireEntries = currentList.toList(), errorMessage = null) }
                calculateFill() // Calculate fill after state update
            }
        }
    }


    fun calculateFill() {
        viewModelScope.launch {
             val currentState = _uiState.value // Get snapshot of current state

             if (currentState.selectedConduitTypeName.isBlank() || currentState.selectedConduitSize.isBlank()) {
                 // Log.d("ConduitFillVM", "Skipping calculation: Conduit type or size is blank.") // Removed Log
                 // Don't clear results here, let the UI show existing ones until valid inputs are back
                 return@launch
             }

             // Log.d("ConduitFillVM", "Calculating fill for: ${currentState.selectedConduitTypeName} ${currentState.selectedConduitSize}") // Removed Log

             // Fetch conduit area from repository
            val conduitEntry = try {
                 necDataRepository.getConduitEntry(currentState.selectedConduitTypeName, currentState.selectedConduitSize)
            } catch (e: Exception) {
                Log.e("ConduitFillVM", "Error fetching conduit entry", e)
                _uiState.update { it.copy(errorMessage = "Error fetching conduit data: ${e.message}") }
                return@launch
            }

            val conduitArea = conduitEntry?.internalAreaSqIn

            if (conduitArea == null || conduitArea <= 0) {
                Log.w("ConduitFillVM", "Conduit properties not found or invalid area for ${currentState.selectedConduitTypeName} ${currentState.selectedConduitSize}")
                _uiState.update { it.copy(errorMessage = "Conduit properties not found or invalid area.", totalWireArea = null, allowableFillArea = null, fillPercentage = null, isOverfill = false) }
                return@launch
            }

            var currentTotalWireArea = 0.0
            var totalWireCount = 0
            var calculationPossible = true
             var errorMsg: String? = null

             if (currentState.wireEntries.isEmpty()) {
                 // Log.d("ConduitFillVM", "No wire entries, setting fill to 0.") // Removed Log
                  _uiState.update { it.copy(totalWireArea = 0.0, allowableFillArea = conduitArea * 0.4, fillPercentage = 0.0, isOverfill = false, errorMessage = null) } // Default to 40% allowable if empty
                  return@launch
             }

            // Fetch wire areas asynchronously (can be optimized further if needed)
            for (entry in currentState.wireEntries) {
                 if (entry.type.isBlank() || entry.size.isBlank()) {
                     errorMsg = "Select wire type and size for all entries."
                     calculationPossible = false
                     break
                 }
                 val wireAreaEntry = try {
                     necDataRepository.getWireAreaEntry(entry.type, entry.size)
                 } catch (e: Exception) {
                     Log.e("ConduitFillVM", "Error fetching wire area for ${entry.type} ${entry.size}", e)
                     errorMsg = "Error fetching wire data for ${entry.type} ${entry.size}"
                     calculationPossible = false
                     break
                 }

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
                // Log.w("ConduitFillVM", "Calculation not possible: $errorMsg") // Keep warning log for now, maybe remove later
                _uiState.update { it.copy(errorMessage = errorMsg, totalWireArea = null, allowableFillArea = null, fillPercentage = null, isOverfill = false) }
                return@launch
            }

            // Determine allowable fill percentage (NEC Chapter 9, Table 1)
            val allowablePercentDecimal = when {
                totalWireCount == 1 && conduitEntry.fillArea1WireSqIn > 0 -> conduitEntry.fillArea1WireSqIn / conduitArea
                totalWireCount == 2 && conduitEntry.fillArea2WiresSqIn > 0 -> conduitEntry.fillArea2WiresSqIn / conduitArea
                totalWireCount > 2 && conduitEntry.fillAreaOver2WiresSqIn > 0 -> conduitEntry.fillAreaOver2WiresSqIn / conduitArea
                // Fallback percentages
                totalWireCount == 1 -> 0.53
                totalWireCount == 2 -> 0.31
                totalWireCount > 2 -> 0.40
                else -> 0.40 // Default to 40% if count is 0 (though handled above)
            }

            val allowableArea = conduitArea * allowablePercentDecimal
            val percentage = if (allowableArea > 0) (currentTotalWireArea / allowableArea) * 100 else 0.0 // Calculate % of allowable area
             val overfill = currentTotalWireArea > allowableArea

             // Log.d("ConduitFillVM", "Calculation Result: TotalArea=$currentTotalWireArea, AllowableArea=$allowableArea, Percentage=$percentage, Overfill=$overfill") // Removed Log

             _uiState.update {
                 it.copy(
                    totalWireArea = currentTotalWireArea,
                    allowableFillArea = allowableArea,
                    fillPercentage = percentage,
                    isOverfill = overfill,
                    errorMessage = null // Clear error if calculation succeeds
                )
            }
        }
    }

    // Removed local formatResult helper - use shared one from common package
}
