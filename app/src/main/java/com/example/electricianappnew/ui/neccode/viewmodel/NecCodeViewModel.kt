package com.example.electricianappnew.ui.neccode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.* // Correct wildcard import
import com.example.electricianappnew.data.repository.NecDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // Correct wildcard import
import javax.inject.Inject

// Ensure UiState uses the exact model names from NecDataModels.kt
data class NecCodeUiState(
    val necAmpacityData: List<NecAmpacityEntry> = emptyList(),
    val necConductorData: List<NecConductorEntry> = emptyList(),
    val necConduitData: List<NecConduitEntry> = emptyList(),
    val necWireAreaData: List<NecWireAreaEntry> = emptyList(),
    val necBoxFillData: List<NecBoxFillEntry> = emptyList(),
    val necTempCorrectionData: List<NecTempCorrectionEntry> = emptyList(),
    val necConductorAdjustmentData: List<NecConductorAdjustmentEntry> = emptyList(),
    val necMotorFLCData: List<NecMotorFLCEntry> = emptyList(),
    // Add other tables if needed and confirmed to exist in DAO/Repo
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class NecCodeViewModel @Inject constructor(
    private val necDataRepository: NecDataRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Ensure these method calls exactly match the NecDataRepository interface
    private val _ampacityFlow = necDataRepository.getAllNecAmpacityEntries().shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    private val _conductorFlow = necDataRepository.getAllNecConductorEntries().shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    private val _conduitFlow = necDataRepository.getAllNecConduitEntries().shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    private val _wireAreaFlow = necDataRepository.getAllNecWireAreaEntries().shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    private val _boxFillFlow = necDataRepository.getAllNecBoxFillEntries().shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    private val _tempCorrectionFlow = necDataRepository.getAllNecTempCorrectionEntries().shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    private val _conductorAdjustmentFlow = necDataRepository.getAllNecConductorAdjustmentEntries().shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    private val _motorFLCFlow = necDataRepository.getAllNecMotorFLCEntries().shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    // Removed flows for non-existent Raceway/Wire

    // Combine the existing flows
    val uiState: StateFlow<NecCodeUiState> = combine(
        _ampacityFlow, _conductorFlow, _conduitFlow, _wireAreaFlow, _boxFillFlow,
        _tempCorrectionFlow, _conductorAdjustmentFlow, _motorFLCFlow,
        _searchQuery
        // Ensure the number of flows matches the indices accessed below
    ) { flows ->
        // Explicitly cast using correct model names
        val ampacities = flows[0] as? List<NecAmpacityEntry> ?: emptyList()
        val conductors = flows[1] as? List<NecConductorEntry> ?: emptyList()
        val conduits = flows[2] as? List<NecConduitEntry> ?: emptyList()
        val wireAreas = flows[3] as? List<NecWireAreaEntry> ?: emptyList()
        val boxFills = flows[4] as? List<NecBoxFillEntry> ?: emptyList()
        val tempCorrections = flows[5] as? List<NecTempCorrectionEntry> ?: emptyList()
        val conductorAdjustments = flows[6] as? List<NecConductorAdjustmentEntry> ?: emptyList()
        val motorFLCs = flows[7] as? List<NecMotorFLCEntry> ?: emptyList()
        val query = flows[8] as String // Index matches the number of flows + query

        // Apply filtering using correct model properties
        NecCodeUiState(
            necAmpacityData = filterAmpacities(ampacities, query),
            necConductorData = filterConductors(conductors, query),
            necConduitData = filterConduits(conduits, query),
            necWireAreaData = filterWireAreas(wireAreas, query),
            necBoxFillData = filterBoxFills(boxFills, query),
            necTempCorrectionData = filterTempCorrections(tempCorrections, query),
            necConductorAdjustmentData = filterConductorAdjustments(conductorAdjustments, query),
            necMotorFLCData = filterMotorFLCs(motorFLCs, query),
            searchQuery = query,
            isLoading = false,
            errorMessage = null
        )
    }.catch { e ->
        emit(NecCodeUiState(isLoading = false, errorMessage = "Error combining NEC data: ${e.message}"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = NecCodeUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // --- Filtering Functions (Using correct models/properties) ---

    private fun filterAmpacities(list: List<NecAmpacityEntry>, query: String): List<NecAmpacityEntry> {
        if (query.isBlank()) return list
        return list.filter {
            it.size.contains(query, ignoreCase = true) ||
            it.material.contains(query, ignoreCase = true) ||
            it.tempRating.toString().contains(query) ||
            it.ampacity.toString().contains(query)
        }
    }

    private fun filterConductors(list: List<NecConductorEntry>, query: String): List<NecConductorEntry> {
         if (query.isBlank()) return list
         return list.filter {
             it.size.contains(query, ignoreCase = true) ||
             it.material.contains(query, ignoreCase = true)
         }
    }

     private fun filterConduits(list: List<NecConduitEntry>, query: String): List<NecConduitEntry> {
         if (query.isBlank()) return list
         return list.filter {
             it.type.contains(query, ignoreCase = true) ||
             it.size.contains(query, ignoreCase = true)
         }
    }

     private fun filterWireAreas(list: List<NecWireAreaEntry>, query: String): List<NecWireAreaEntry> {
         if (query.isBlank()) return list
         return list.filter {
             it.size.contains(query, ignoreCase = true) ||
             it.insulationType.contains(query, ignoreCase = true)
         }
    }

     private fun filterBoxFills(list: List<NecBoxFillEntry>, query: String): List<NecBoxFillEntry> {
         if (query.isBlank()) return list
         return list.filter {
             it.itemType.contains(query, ignoreCase = true) ||
             it.conductorSize.contains(query, ignoreCase = true)
         }
    }

     private fun filterTempCorrections(list: List<NecTempCorrectionEntry>, query: String): List<NecTempCorrectionEntry> {
         if (query.isBlank()) return list
         return list.filter {
             it.ambientTempCelsius.toString().contains(query) ||
             it.tempRating.toString().contains(query)
         }
    }

     private fun filterConductorAdjustments(list: List<NecConductorAdjustmentEntry>, query: String): List<NecConductorAdjustmentEntry> {
         if (query.isBlank()) return list
         return list.filter {
             it.minConductors.toString().contains(query) ||
             it.maxConductors.toString().contains(query)
         }
    }

     private fun filterMotorFLCs(list: List<NecMotorFLCEntry>, query: String): List<NecMotorFLCEntry> {
         if (query.isBlank()) return list
         return list.filter {
             it.hp.toString().contains(query) ||
             it.voltage.toString().contains(query) ||
             it.phase.toString().contains(query) ||
             it.flc.toString().contains(query)
         }
    }
    // Removed filter functions for Raceway and Wire
}
