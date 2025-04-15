package com.example.electricianappnew.ui.inventory.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.InventoryItem
import com.example.electricianappnew.data.model.InventoryTransaction
import com.example.electricianappnew.data.model.Material
import com.example.electricianappnew.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // Correct wildcard import
import kotlinx.coroutines.launch
import java.util.Date // Import Date
import javax.inject.Inject

data class InventoryDetailUiState(
    val inventoryItem: InventoryItem? = null,
    val material: Material? = null,
    val transactions: List<InventoryTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class InventoryDetailViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val inventoryItemId: String = checkNotNull(savedStateHandle["inventoryItemId"])

    private val _uiState = MutableStateFlow(InventoryDetailUiState(isLoading = true))
    val uiState: StateFlow<InventoryDetailUiState> = _uiState

    init {
        loadInventoryDetails()
    }

    fun loadInventoryDetails() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null) // Ensure loading state is set
            try {
                // Collect the first emission from the Flows
                val item = inventoryRepository.getInventoryItemById(inventoryItemId).first() // Collect first item
                val mat = item?.let { inventoryRepository.getMaterialById(it.materialId).first() } // Collect first material
                val trans = inventoryRepository.getTransactionsForItem(inventoryItemId).first() // Collect first list of transactions

                if (item == null || mat == null) {
                     _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Inventory item or material details not found.")
                } else {
                    _uiState.value = _uiState.value.copy(
                        inventoryItem = item,
                        material = mat,
                        transactions = trans,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load details: ${e.message}"
                )
            }
        }
    }

    fun adjustStock(quantityChange: Double, reason: String = "Manual Adjustment") {
        val currentItem = _uiState.value.inventoryItem ?: return // Exit if item not loaded

        val newQuantity = currentItem.quantityOnHand + quantityChange

        // Prevent negative stock if removing
        if (newQuantity < 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Cannot remove more stock than available.")
            return
        }

        val transaction = InventoryTransaction(
            inventoryItemId = currentItem.id,
            transactionType = if (quantityChange > 0) "MANUAL_ADD" else "MANUAL_REMOVE", // Use transactionType
            quantityChange = quantityChange,
            timestamp = Date(), // Current time
            notes = reason // Use notes field for the reason
            // relatedJobId and relatedTaskId are null for manual adjustments
        )

        viewModelScope.launch {
            try {
                // 1. Insert the transaction record
                inventoryRepository.insertTransaction(transaction)

                // 2. Update the inventory item's quantity
                // Ideally, this would be atomic with the transaction insert in the repo/DAO
                inventoryRepository.updateInventoryItemQuantity(currentItem.id, newQuantity)

                // 3. Refresh the UI state (or rely on Flow collection)
                // For immediate feedback, update the state directly
                 _uiState.update {
                    it.copy(
                        inventoryItem = currentItem.copy(quantityOnHand = newQuantity),
                        transactions = listOf(transaction) + it.transactions, // Add to top of list
                        errorMessage = null // Clear any previous error
                    )
                }
                 // Optionally, trigger a full reload if relying on Flow collection
                 // loadInventoryDetails()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to adjust stock: ${e.message}"
                )
            }
        }
    }
}
