package com.example.electricianappnew.ui.jobs.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.InventoryItemWithMaterial
import com.example.electricianappnew.data.model.InventoryTransaction
import com.example.electricianappnew.data.model.Task
import com.example.electricianappnew.data.repository.InventoryRepository
import com.example.electricianappnew.data.repository.JobTaskRepository
import com.example.electricianappnew.navigation.NavArg // Import NavArg
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // Correct wildcard import
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

// State for Add/Edit Task Screen
data class AddEditTaskUiState(
    val taskId: String? = null, // Use String?
    val jobId: String, // Use String (non-nullable as it's required)
    val description: String = "",
    val status: String = "Pending",
    val assignedTo: String = "",
    val originalDateCreated: Date? = null,
    val availableInventory: List<InventoryItemWithMaterial> = emptyList(),
    val usedMaterials: Map<String, Double> = emptyMap(), // Map<InventoryItemId, QuantityUsed>
    val inventoryLoadingError: String? = null,
    val availableStatuses: List<String> = listOf("Pending", "In Progress", "Completed", "Blocked"),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val jobTaskRepository: JobTaskRepository,
    private val inventoryRepository: InventoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve jobId (required) and taskId (optional) as Strings
    private val jobId: String = checkNotNull(savedStateHandle.get<String>(NavArg.JOB_ID)) { "Job ID is required" }
    private val taskId: String? = savedStateHandle.get<String?>(NavArg.TASK_ID) // Null if adding new task

    private val _uiState = MutableStateFlow(AddEditTaskUiState(jobId = jobId)) // Initialize with String jobId
    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow() // Expose as immutable StateFlow

    init {
        // Job ID is already checked by checkNotNull above
        if (taskId != null) { // Check if editing an existing task (taskId is not null)
            loadTask(taskId)
        }
        loadAvailableInventory() // Always load inventory
    }

    private fun loadTask(id: String) { // Parameter is String
        _uiState.update { it.copy(isLoading = true) } // Use update for thread safety
        viewModelScope.launch {
            try {
                val task = jobTaskRepository.getTaskById(id).firstOrNull() // Use String id
                if (task != null) {
                    // TODO: Load previously saved material usage for this task if editing
                    _uiState.update {
                        it.copy(
                            taskId = task.id, // Update UI state taskId with String ID
                            jobId = task.jobId, // Already String
                            description = task.description,
                            status = task.status,
                            assignedTo = task.assignedTo,
                            originalDateCreated = task.dateCreated,
                            isLoading = false,
                            errorMessage = null // Clear error on success
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Task not found.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load task: ${e.message}") }
            }
        }
    }

    private fun loadAvailableInventory() {
        viewModelScope.launch {
             _uiState.value = _uiState.value.copy(inventoryLoadingError = null) // Use .value assignment
            inventoryRepository.getAllInventoryItemsWithMaterial()
                .catch { e ->
                    // Update the state directly on error
                    _uiState.value = _uiState.value.copy(inventoryLoadingError = "Failed to load inventory: ${e.message}") // Use .value assignment
                }
                .collect { inventoryList ->
                     // Update the state directly on successful collection
                    _uiState.value = _uiState.value.copy(availableInventory = inventoryList, inventoryLoadingError = null) // Use .value assignment and clear error
                }
        }
    }

    // --- Input Handlers ---
    fun onDescriptionChange(value: String) { _uiState.update { it.copy(description = value) } }
    fun onStatusChange(value: String) { _uiState.update { it.copy(status = value) } }
    fun onAssignedToChange(value: String) { _uiState.update { it.copy(assignedTo = value) } }

    // --- Material Usage Handlers ---
    fun addOrUpdateMaterialUsage(inventoryItemId: String, quantity: Double) {
        if (quantity <= 0) {
            removeMaterialUsage(inventoryItemId)
            return
        }
        val currentUsage = _uiState.value.usedMaterials.toMutableMap()
        currentUsage[inventoryItemId] = quantity
        _uiState.update { it.copy(usedMaterials = currentUsage) }
    }

    fun removeMaterialUsage(inventoryItemId: String) {
        val currentUsage = _uiState.value.usedMaterials.toMutableMap()
        currentUsage.remove(inventoryItemId)
         _uiState.update { it.copy(usedMaterials = currentUsage) }
    }


    fun saveTask() {
        val currentState = _uiState.value // Capture current state
        if (currentState.description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Task Description is required.") }
            return
        }

        // --- Pre-check for sufficient stock ---
        var insufficientStockError: String? = null
        for ((inventoryItemId, quantityUsed) in currentState.usedMaterials) {
            val inventoryItem = currentState.availableInventory.find { it.inventoryItem.id == inventoryItemId }?.inventoryItem
            if (inventoryItem != null) {
                if (inventoryItem.quantityOnHand < quantityUsed) {
                    val materialName = currentState.availableInventory.find { it.inventoryItem.id == inventoryItemId }?.material?.name ?: "Item ID: $inventoryItemId"
                    insufficientStockError = "Insufficient stock for '$materialName'. Available: ${inventoryItem.quantityOnHand}, Needed: $quantityUsed."
                    break // Stop checking on first error
                }
            } else {
                 insufficientStockError = "Could not verify stock for item ID: $inventoryItemId."
                 break
            }
        }

        if (insufficientStockError != null) {
            _uiState.update { it.copy(isSaving = false, errorMessage = insufficientStockError) }
            return // Abort save
        }
        // --- End Pre-check ---

        // Generate new ID if taskId is null (creating), otherwise use existing taskId (editing)
        val currentTaskId = taskId ?: UUID.randomUUID().toString()

        val taskToSave = Task(
            id = currentTaskId, // Use String ID (new or existing)
            jobId = jobId, // Use the String jobId from SavedStateHandle
            description = currentState.description.trim(),
            status = currentState.status,
            assignedTo = currentState.assignedTo.trim(),
            dateCreated = currentState.originalDateCreated ?: Date(),
            dateCompleted = if (currentState.status == "Completed") Date() else null
        )

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                // Save Task (Insert or Update based on whether taskId was initially null)
                if (taskId == null) {
                    jobTaskRepository.insertTask(taskToSave)
                } else {
                    jobTaskRepository.updateTask(taskToSave)
                }

                // Process Inventory Transactions
                // TODO: Handle edits vs initial save for transactions (e.g., calculate difference)
                currentState.usedMaterials.forEach { (inventoryItemId, quantityUsed) ->
                    val inventoryItem = currentState.availableInventory.find { it.inventoryItem.id == inventoryItemId }?.inventoryItem
                    if (inventoryItem != null) {
                        val newQuantityOnHand = inventoryItem.quantityOnHand - quantityUsed

                        val transaction = InventoryTransaction(
                            inventoryItemId = inventoryItemId,
                            transactionType = "TASK_USAGE",
                            quantityChange = -quantityUsed,
                            timestamp = Date(),
                            notes = "Used for task: ${taskToSave.description}", // Use description from taskToSave
                            relatedJobId = taskToSave.jobId, // Use String jobId
                            relatedTaskId = taskToSave.id // Use the String taskId (currentTaskId)
                        )

                        inventoryRepository.insertTransaction(transaction)
                        inventoryRepository.updateInventoryItemQuantity(inventoryItemId, newQuantityOnHand)
                    } else {
                         println("Warning: Could not find inventory item details for ID: $inventoryItemId during transaction creation.")
                    }
                }

                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to save task or update inventory: ${e.message}") }
            }
        }
    }
}
