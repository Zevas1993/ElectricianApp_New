package com.example.electricianappnew.ui.inventory.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.InventoryItem
import com.example.electricianappnew.data.model.Material
import com.example.electricianappnew.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first // Add specific import for first()
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// State for Add/Edit Material Screen
data class AddEditMaterialUiState(
    val materialId: String? = null, // Null if adding new
    val name: String = "",
    val category: String = "",
    val partNumber: String = "",
    // Initial Inventory Item details (optional on material creation)
    val initialQuantityStr: String = "",
    val unitOfMeasure: String = "",
    val location: String = "",
    val lowStockThresholdStr: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false // For loading existing material
)

@HiltViewModel
class AddEditMaterialViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val materialId: String? = savedStateHandle["materialId"] // Get ID if editing

    private val _uiState = MutableStateFlow(AddEditMaterialUiState())
    val uiState: StateFlow<AddEditMaterialUiState> = _uiState

    init {
        if (materialId != null) {
            loadMaterial(materialId)
        }
    }

    private fun loadMaterial(id: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                 val material = inventoryRepository.getMaterialById(id).first() // Use first() within launch
                if (material != null) {
                    _uiState.value = _uiState.value.copy(
                        materialId = material.id,
                        name = material.name,
                        category = material.category,
                        partNumber = material.partNumber ?: "",
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Material not found.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Failed to load material: ${e.message}")
            }
        }
    }

    // --- Input Handlers ---
    fun onNameChange(value: String) { _uiState.value = uiState.value.copy(name = value) }
    fun onCategoryChange(value: String) { _uiState.value = uiState.value.copy(category = value) }
    fun onPartNumberChange(value: String) { _uiState.value = uiState.value.copy(partNumber = value) }
    fun onInitialQuantityChange(value: String) { _uiState.value = uiState.value.copy(initialQuantityStr = value) }
    fun onUnitOfMeasureChange(value: String) { _uiState.value = uiState.value.copy(unitOfMeasure = value) }
    fun onLocationChange(value: String) { _uiState.value = uiState.value.copy(location = value) }
    fun onLowStockThresholdChange(value: String) { _uiState.value = uiState.value.copy(lowStockThresholdStr = value) }


    fun saveMaterial() {
        val currentState = uiState.value
        if (currentState.name.isBlank() || currentState.category.isBlank() || currentState.unitOfMeasure.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Name, Category, and Unit of Measure are required.")
            return
        }

        val initialQuantity = currentState.initialQuantityStr.toDoubleOrNull() ?: 0.0
        val lowStockThreshold = currentState.lowStockThresholdStr.toDoubleOrNull()

        val materialToSave = Material(
            id = currentState.materialId ?: UUID.randomUUID().toString(), // Generate new ID if adding
            name = currentState.name.trim(),
            category = currentState.category.trim(),
            partNumber = currentState.partNumber.trim()
        )

        // Create initial InventoryItem only if adding a new material AND quantity is provided
        val initialInventoryItem: InventoryItem? = if (currentState.materialId == null && currentState.initialQuantityStr.isNotBlank()) {
            InventoryItem(
                id = UUID.randomUUID().toString(),
                materialId = materialToSave.id,
                quantityOnHand = initialQuantity,
                unitOfMeasure = currentState.unitOfMeasure.trim(),
                location = currentState.location.trim(),
                lowStockThreshold = lowStockThreshold
            )
        } else {
            null
        }


        _uiState.value = currentState.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                // Use insert or update based on whether materialId exists
                if (currentState.materialId == null) {
                    inventoryRepository.insertMaterial(materialToSave)
                } else {
                    inventoryRepository.updateMaterial(materialToSave)
                }
                initialInventoryItem?.let {
                    inventoryRepository.insertInventoryItem(it) // Insert initial item if applicable
                    // Optionally add an initial transaction? Depends on workflow.
                }
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = "Failed to save: ${e.message}")
            }
        }
    }
}
