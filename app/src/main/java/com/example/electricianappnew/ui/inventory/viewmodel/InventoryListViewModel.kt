package com.example.electricianappnew.ui.inventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.InventoryItemWithMaterial
import com.example.electricianappnew.data.model.Material // Import Material explicitly
import com.example.electricianappnew.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // Correct wildcard import
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryListUiState(
    val allInventoryItems: List<InventoryItemWithMaterial> = emptyList(),
    val displayedInventoryItems: List<InventoryItemWithMaterial> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class InventoryListViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    // Holds the raw data list and loading/error state from the repository
    private val _internalState = MutableStateFlow(InventoryListUiState(isLoading = true))

    // Publicly exposed state combines internal state and search query for filtering
    val uiState: StateFlow<InventoryListUiState> = combine(
        _internalState,
        _searchQuery
    ) { inventoryState, query ->
        val filteredItems = if (query.isBlank()) {
            inventoryState.allInventoryItems
        } else {
            inventoryState.allInventoryItems.filter { itemWithMaterial ->
                val material = itemWithMaterial.material
                val item = itemWithMaterial.inventoryItem
                (material?.name?.contains(query, ignoreCase = true) == true) ||
                (material?.partNumber?.contains(query, ignoreCase = true) == true) ||
                (material?.category?.contains(query, ignoreCase = true) == true) ||
                item.location.contains(query, ignoreCase = true)
            }
        }
        // Return a new state object with the filtered list and current search query
        inventoryState.copy(
            searchQuery = query,
            displayedInventoryItems = filteredItems
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = InventoryListUiState() // Start with default empty state
    )

    init {
        loadInventoryItems() // Initial load
    }

    private fun loadInventoryItems() {
        viewModelScope.launch {
            _internalState.update { it.copy(isLoading = true, errorMessage = null) } // Set loading true
            inventoryRepository.getAllInventoryItemsWithMaterial()
                .catch { exception ->
                    _internalState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load inventory: ${exception.message}"
                        )
                    }
                }
                .collect { items ->
                     _internalState.update {
                         it.copy(
                            allInventoryItems = items, // Update the full list
                            isLoading = false,
                            errorMessage = null // Clear error on success
                        )
                    }
                }
        }
    }

     fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteMaterial(itemWithMaterial: InventoryItemWithMaterial) {
        // Ensure material is not null before attempting delete
        val materialToDelete: Material = itemWithMaterial.material ?: return
        viewModelScope.launch {
            try {
                inventoryRepository.deleteMaterial(materialToDelete)
                // The Flow collection in loadInventoryItems will automatically update the list.
                // We might want to clear any existing error message shown in the UI.
                 _internalState.update { it.copy(errorMessage = null) }
            } catch (e: Exception) {
                 // Update the error message in the internal state flow
                 _internalState.update { it.copy(errorMessage = "Failed to delete material: ${e.message}") }
            }
        }
    }
}
