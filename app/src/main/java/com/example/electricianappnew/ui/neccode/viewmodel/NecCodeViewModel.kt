package com.example.electricianappnew.ui.neccode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.* // Correct wildcard import
import com.example.electricianappnew.data.repository.NecDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch // Ensure launch is imported
import javax.inject.Inject

// Updated UiState for Search Functionality
data class NecCodeUiState(
    val searchQuery: String = "",
    val searchResults: List<NecSearchResult> = emptyList(), // Use the sealed interface
    val isLoading: Boolean = false,
    val errorMessage: String? = null
    // Removed individual table lists, results will be combined in searchResults
)

@HiltViewModel
class NecCodeViewModel @Inject constructor(
    private val necDataRepository: NecDataRepository
) : ViewModel() {

    // Use MutableStateFlow for UI state
    private val _uiState = MutableStateFlow(NecCodeUiState())
    val uiState: StateFlow<NecCodeUiState> = _uiState.asStateFlow()

    // Keep track of the current search job to cancel if a new query comes in quickly
    private var searchJob: kotlinx.coroutines.Job? = null

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // Optional: Add debounce here if desired
        performSearch(query)
    }

    fun performSearch(query: String = _uiState.value.searchQuery) {
        searchJob?.cancel() // Cancel previous search if running
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isLoading = false, errorMessage = null) }
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            necDataRepository.searchNecData(query)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Search failed: ${e.message}", searchResults = emptyList()) }
                }
                .collect { results ->
                    _uiState.update { it.copy(isLoading = false, searchResults = results) }
                }
        }
    }

    // --- Filtering Functions (No longer needed here, handled by DAO/Repo) ---

    /* Removed filterAmpacities
    private fun filterAmpacities(list: List<NecAmpacityEntry>, query: String): List<NecAmpacityEntry> {
        if (query.isBlank()) return list
        return list.filter {
    }
    */

    /* Removed filterConductors
    private fun filterConductors(list: List<NecConductorEntry>, query: String): List<NecConductorEntry> {
         }
    }
    */

    /* Removed filterConduits
     private fun filterConduits(list: List<NecConduitEntry>, query: String): List<NecConduitEntry> {
         }
    }
    */

    /* Removed filterWireAreas
     private fun filterWireAreas(list: List<NecWireAreaEntry>, query: String): List<NecWireAreaEntry> {
         }
    }
    */

    /* Removed filterBoxFills
     private fun filterBoxFills(list: List<NecBoxFillEntry>, query: String): List<NecBoxFillEntry> {
         }
    }
    */

    /* Removed filterTempCorrections
     private fun filterTempCorrections(list: List<NecTempCorrectionEntry>, query: String): List<NecTempCorrectionEntry> {
         }
    }
    */

    /* Removed filterConductorAdjustments
     private fun filterConductorAdjustments(list: List<NecConductorAdjustmentEntry>, query: String): List<NecConductorAdjustmentEntry> {
         }
    }
    */

    /* Removed filterMotorFLCs
     private fun filterMotorFLCs(list: List<NecMotorFLCEntry>, query: String): List<NecMotorFLCEntry> {
         }
    }
    */
}
