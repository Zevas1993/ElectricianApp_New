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
// import kotlin.coroutines.currentCoroutineContext // Import currentCoroutineContext - REMOVED
import java.util.Locale
import javax.inject.Inject
import android.util.Log
import com.example.electricianappnew.data.model.*
import com.example.electricianappnew.data.model.WireEntry
import kotlinx.coroutines.channels.BufferOverflow // ADDED
import kotlinx.coroutines.flow.MutableSharedFlow // ADDED
import kotlinx.coroutines.flow.asSharedFlow // ADDED

// Define UI events for one-shot actions like showing errors
sealed class UiEvent {
    data class ShowError(val message: String) : UiEvent()
    // Add other UI events here as needed
}

// Data class for UI State (excluding dropdown lists, which are separate state in VM)
data class ConduitFillUiState(
    val selectedConduitTypeName: String = "",
    val selectedConduitSize: String = "",
    val wireEntries: List<WireEntry> = emptyList(), // Start with an empty list
    val totalWireArea: Double? = null,
    val allowableFillArea: Double? = null,
    val fillPercentage: Double? = null,
    val isOverfill: Boolean = false
    // Removed errorMessage - now handled by UiEvent
    // Removed loading states - handled by initialValue in stateIn
)

@HiltViewModel
class ConduitFillViewModel @Inject constructor(
    private val necDataRepository: NecDataRepository // Inject repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConduitFillUiState())
    val uiState: StateFlow<ConduitFillUiState> = _uiState.asStateFlow()

    // SharedFlow for one-shot UI events
    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val uiEvent = _uiEvent.asSharedFlow()

    // --- StateFlows for Dropdown Options ---

    // Conduit Types
    val conduitTypeNames: StateFlow<List<String>> = necDataRepository.getDistinctConduitTypes()
        .catch { e ->
            Log.e("ConduitFillVM", "Error fetching conduit types", e)
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowError("Error loading conduit types: ${e.message}")) }
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // Wire Insulation Types (from nec_wire_areas)
    val wireTypeNames: StateFlow<List<String>> = necDataRepository.getDistinctWireTypes()
        .catch { e ->
            Log.e("ConduitFillVM", "Error fetching wire types", e)
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowError("Error loading wire types: ${e.message}")) }
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // Available Conduit Sizes (depends on selectedConduitTypeName)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val availableConduitSizes: StateFlow<List<String>> = _uiState
        .map { it.selectedConduitTypeName } // Observe changes in selected type name
        .distinctUntilChanged() // Only react when the name actually changes
        .onEach { typeName -> Log.d("ConduitFillVM", "flatMapLatest: Input typeName changed to: '$typeName'") }
        .flatMapLatest { typeName -> // Switch to the new flow when typeName changes
            Log.d("ConduitFillVM", "flatMapLatest: Executing lambda for typeName: '$typeName'")
            if (typeName.isBlank()) {
                Log.d("ConduitFillVM", "flatMapLatest: typeName is blank, returning empty list flow.")
                flowOf(emptyList()) // Return empty list if no type selected
            } else {
                Log.d("ConduitFillVM", "flatMapLatest: typeName is '$typeName', calling repository...")
                necDataRepository.getDistinctConduitSizesForType(typeName)
                    .onStart { Log.d("ConduitFillVM", "flatMapLatest: Starting conduit sizes flow for type: '$typeName'") }
                    .onEach { sizes -> Log.d("ConduitFillVM", "flatMapLatest: Received conduit sizes for '$typeName': $sizes") }
                    .catch { e ->
                        Log.e("ConduitFillVM", "flatMapLatest: Error fetching conduit sizes for $typeName", e)
                        viewModelScope.launch { _uiEvent.emit(UiEvent.ShowError("Error loading sizes for $typeName: ${e.message}")) }
                        emit(emptyList())
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // Available Conductor Sizes (depends on the wire type of each WireEntry)
    // This will be a map from WireEntry index to a StateFlow of available sizes for that entry's type
    // We will manage this in the ViewModel and expose a way for the UI to get the flow for each entry.
    private val _availableConductorSizesForEntry = MutableStateFlow<Map<Int, StateFlow<List<String>>>>(emptyMap())
    val availableConductorSizesForEntry: StateFlow<Map<Int, StateFlow<List<String>>>> = _availableConductorSizesForEntry.asStateFlow()

    // Function to get the StateFlow of sizes for a specific wire entry index
    fun getAvailableConductorSizesFlow(index: Int): StateFlow<List<String>> {
        // If the flow for this index doesn't exist, create it
        return _availableConductorSizesForEntry.value[index] ?: createConductorSizesFlowForEntry(index)
    }

    // Private helper to create the conductor sizes flow for a given entry index
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun createConductorSizesFlowForEntry(index: Int): StateFlow<List<String>> {
        val flow = _uiState
            .map { it.wireEntries.getOrNull(index)?.type ?: "" } // Observe the type of the wire entry at this index
            .distinctUntilChanged() // Only react when the type actually changes
            .flatMapLatest { wireType -> // Switch to the new flow when wireType changes
                if (wireType.isBlank()) {
                    flowOf(emptyList()) // Return empty list if no type selected
                } else {
                    necDataRepository.getDistinctWireSizesForType(wireType) // Use the new repository method
                        .catch { e ->
                            Log.e("ConduitFillVM", "Error fetching conductor sizes for type $wireType at index $index", e)
                            // Note: We don't emit a UI event here to avoid overwhelming the user with errors for individual wire entries.
                            // Errors related to specific wire entries will be handled during the calculation phase.
                            emit(emptyList())
                        }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly, // Change to Eagerly
                initialValue = emptyList()
            )

        // Add the created flow to the map
        _availableConductorSizesForEntry.update { it + (index to flow) }
        return flow
    }

init {
    Log.d("ConduitFillVM", "init: ViewModel init block started.")
    viewModelScope.launch {
        Log.d("ConduitFillVM", "init: Waiting for isDataLoaded...")
        // Wait for the main data loaded flag (from DatabaseCallback)
        // Although the repository reads from JSON, this flag might still be
        // a useful signal that initial setup is complete.
        necDataRepository.isDataLoaded.filter { it }.first()
        Log.d("ConduitFillVM", "init: isDataLoaded is true. Proceeding.")

        Log.d("ConduitFillVM", "init: Collecting first non-empty wire types. Current value: ${wireTypeNames.value}")
        // Collect the first non-empty list of wire types
        val wireTypes = wireTypeNames.filter { it.isNotEmpty() }.first()
        Log.d("ConduitFillVM", "init: Collected wire types: $wireTypes")
        val defaultWireType = wireTypes.firstOrNull() ?: ""

        if (defaultWireType.isNotBlank()) {
            Log.d("ConduitFillVM", "init: Default wire type found: $defaultWireType. Creating conductor sizes flow.")
            // Create the flow for conductor sizes for the initial entry (index 0)
            val sizesForDefaultTypeFlow = createConductorSizesFlowForEntry(0)
            Log.d("ConduitFillVM", "init: Collecting first non-empty conductor sizes for type: $defaultWireType. Current value: ${sizesForDefaultTypeFlow.value}")
            // Collect the first non-empty list of sizes for the default wire type
            val conductorSizes = sizesForDefaultTypeFlow.filter { it.isNotEmpty() }.first()
            Log.d("ConduitFillVM", "init: Collected conductor sizes: $conductorSizes")
            val defaultConductorSize = conductorSizes.firstOrNull() ?: ""

            if (defaultConductorSize.isNotBlank()) {
                Log.d("ConduitFillVM", "init: Default conductor size found: $defaultConductorSize. Creating initial wire entry.")
                val initialEntry = WireEntry(type = defaultWireType, size = defaultConductorSize)
                _uiState.update { currentState ->
                    currentState.copy(wireEntries = listOf(initialEntry))
                }
                Log.d("ConduitFillVM", "init: Initial wire entry added: $initialEntry")
            } else {
                Log.w("ConduitFillVM", "init: Could not determine default conductor size for type: $defaultWireType")
            }
        } else {
            Log.w("ConduitFillVM", "init: Could not determine default wire type.")
        }
    }
}

    fun onConduitTypeChange(newTypeName: String) {
        // Update the selected type. The availableConduitSizes flow will react.
        _uiState.update { it.copy(selectedConduitTypeName = newTypeName, selectedConduitSize = "") } // Removed errorMessage = null

        // Launch a coroutine to observe availableConduitSizes and select the first non-empty list
        viewModelScope.launch {
            availableConduitSizes
                .filter { it.isNotEmpty() } // Wait for the first non-empty list
                .firstOrNull() // Get the first non-empty list and cancel collection
                ?.let { sizes ->
                    // Once sizes are available, automatically select the first one
                    _uiState.update { it.copy(selectedConduitSize = sizes.first()) }
                    Log.d("ConduitFillVM", "Automatically selected first conduit size: ${sizes.first()}") // ADDED Log
                }
        }
    }

    fun onConduitSizeChange(newSize: String) {
        // Log.d("ConduitFillVM", "onConduitSizeChange: $newSize") // Removed Log
        if (newSize.isNotBlank()) { // Only calculate if a valid size is selected
             _uiState.update { it.copy(selectedConduitSize = newSize) } // Removed errorMessage = null
             calculateFill()
        } else {
             viewModelScope.launch { _uiEvent.emit(UiEvent.ShowError("Please select a conduit size.")) } // Emit error event
             _uiState.update { it.copy(selectedConduitSize = "") } // Keep size blank
        }
    }

    fun addWireEntry() {
        // Access the value directly since SharingStarted is Eagerly
        val defaultWireType = wireTypeNames.value.firstOrNull() ?: ""

        if (defaultWireType.isNotBlank()) {
            // Get the StateFlow for the default wire type and access its value directly
            val sizesForDefaultTypeFlow = createConductorSizesFlowForEntry(_uiState.value.wireEntries.size) // Create flow for the new entry's index
            val defaultConductorSize = sizesForDefaultTypeFlow.value.firstOrNull() ?: "" // Get the first size from the first non-empty list

            val newEntry = WireEntry(type = defaultWireType, size = defaultConductorSize) // Use correct default size
            _uiState.update { currentState ->
                currentState.copy(wireEntries = currentState.wireEntries + newEntry)
            }
            // No need to call calculateFill here, updateWireEntry will handle it
        } else {
             // Optionally handle the case where wire types are still not loaded
             Log.w("ConduitFillVM", "Cannot add wire entry: Wire types not loaded.")
             viewModelScope.launch { _uiEvent.emit(UiEvent.ShowError("Cannot add wire entry: Wire types not loaded.")) } // Emit error event
        }
    }

    fun removeWireEntry(index: Int) {
        if (_uiState.value.wireEntries.size > 0) { // Allow removing even the last one now
            _uiState.update { currentState ->
                val updatedList = currentState.wireEntries.filterIndexed { i, _ -> i != index }
                currentState.copy(wireEntries = updatedList)
            }
            // Remove the corresponding size flow from the map
            _availableConductorSizesForEntry.update { currentMap ->
                currentMap.toMutableMap().apply {
                    remove(index)
                    // Adjust keys for entries after the removed one
                    val keysToAdjust = keys.filter { it > index }.sorted()
                    keysToAdjust.forEach { oldIndex ->
                        val flow = remove(oldIndex)
                        if (flow != null) {
                            put(oldIndex - 1, flow)
                        }
                    }
                }.toMap()
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

                _uiState.update { it.copy(wireEntries = currentList.toList()) } // Removed errorMessage = null
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
                viewModelScope.launch { _uiEvent.emit(UiEvent.ShowError("Error fetching conduit data: ${e.message}")) } // Emit error event
                return@launch
            }

            val conduitArea = conduitEntry?.internalAreaSqIn

            if (conduitArea == null || conduitArea <= 0) {
                Log.w("ConduitFillVM", "Conduit properties not found or invalid area for ${currentState.selectedConduitTypeName} ${currentState.selectedConduitSize}")
                viewModelScope.launch { _uiEvent.emit(UiEvent.ShowError("Conduit properties not found or invalid area.")) } // Emit error event
                _uiState.update { it.copy(totalWireArea = null, allowableFillArea = null, fillPercentage = null, isOverfill = false) }
                return@launch
            }

            var currentTotalWireArea = 0.0
            var totalWireCount = 0
            var calculationPossible = true
             var errorMsg: String? = null

             if (currentState.wireEntries.isEmpty()) {
                 // Log.d("ConduitFillVM", "No wire entries, setting fill to 0.") // Removed Log
                  _uiState.update { it.copy(totalWireArea = 0.0, allowableFillArea = conduitArea * 0.4, fillPercentage = 0.0, isOverfill = false) } // Default to 40% allowable if empty, Removed errorMessage = null
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
                viewModelScope.launch { if (errorMsg != null) _uiEvent.emit(UiEvent.ShowError(errorMsg)) } // Emit error event
                _uiState.update { it.copy(totalWireArea = null, allowableFillArea = null, fillPercentage = null, isOverfill = false) } // Removed errorMessage = null
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
                    isOverfill = overfill
                    // Removed errorMessage = null // Clear error if calculation succeeds
                )
            }
        }
    }

    // Removed local formatResult helper - use shared one from common package
}
