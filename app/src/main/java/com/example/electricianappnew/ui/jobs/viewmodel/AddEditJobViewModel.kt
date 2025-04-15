package com.example.electricianappnew.ui.jobs.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.Client
import com.example.electricianappnew.data.model.Job
import com.example.electricianappnew.data.repository.ClientRepository
import com.example.electricianappnew.data.repository.JobTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // Correct wildcard import
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

// State for Add/Edit Job Screen
data class AddEditJobUiState(
    val jobId: String? = null, // Null if adding new
    val jobName: String = "",
    val address: String = "",
    val description: String = "",
    val status: String = "Not Started", // Default status
    val selectedClientId: String = "", // Use clientId
    val originalDateCreated: Date? = null, // Added to preserve original creation date

    val clients: List<Client> = emptyList(), // List of clients for dropdown/selection
    val availableStatuses: List<String> = listOf("Not Started", "In Progress", "On Hold", "Completed", "Cancelled"), // Example statuses

    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false // For loading existing job
)

@HiltViewModel
class AddEditJobViewModel @Inject constructor(
    private val jobTaskRepository: JobTaskRepository,
    private val clientRepository: ClientRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val jobId: String? = savedStateHandle["jobId"]

    private val _uiState = MutableStateFlow(AddEditJobUiState())
    val uiState: StateFlow<AddEditJobUiState> = _uiState

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Fetch available clients
                val clientList = clientRepository.getAllClients().first() // Get clients for selection

                // If editing, fetch existing job data
                if (jobId != null) {
                    val job = jobTaskRepository.getJobById(jobId).first()
                    if (job != null) {
                        _uiState.value = _uiState.value.copy(
                            jobId = job.id,
                            jobName = job.jobName,
                            address = job.address,
                            description = job.description,
                            status = job.status,
                            selectedClientId = job.clientId, // Load clientId
                            originalDateCreated = job.dateCreated, // Store original date
                            clients = clientList,
                            isLoading = false
                        )
                    } else {
                         _uiState.value = _uiState.value.copy(isLoading = false, clients = clientList, errorMessage = "Job not found.")
                    }
                } else {
                    // If adding new, just load clients and set default client selection if possible
                     _uiState.value = _uiState.value.copy(
                         clients = clientList,
                         selectedClientId = clientList.firstOrNull()?.id ?: "", // Select first client ID by default
                         isLoading = false
                     )
                }
            } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Failed to load data: ${e.message}")
            }
        }
    }

    // --- Input Handlers ---
    fun onJobNameChange(value: String) { _uiState.value = uiState.value.copy(jobName = value) }
    fun onAddressChange(value: String) { _uiState.value = uiState.value.copy(address = value) }
    fun onStatusChange(value: String) { _uiState.value = uiState.value.copy(status = value) }
    fun onDescriptionChange(value: String) { _uiState.value = uiState.value.copy(description = value) }
    fun onClientChange(clientId: String) { _uiState.value = uiState.value.copy(selectedClientId = clientId) }


    fun saveJob() {
        val currentState = uiState.value
         if (currentState.jobName.isBlank() || currentState.selectedClientId.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Job Name and Client are required.")
            return
        }

        // Find the client name based on the selected ID for saving into the Job's clientName field (for display convenience)
        val selectedClientName = currentState.clients.find { it.id == currentState.selectedClientId }?.name ?: ""
        // It's okay if clientName is empty if the client was somehow deleted, but clientId must be valid.

        val jobToSave = Job(
            id = currentState.jobId ?: UUID.randomUUID().toString(),
            jobName = currentState.jobName.trim(),
            clientId = currentState.selectedClientId, // Save the selected client's ID
            clientName = selectedClientName, // Save name for convenience
            address = currentState.address.trim(),
            description = currentState.description.trim(),
            status = currentState.status,
            dateCreated = currentState.originalDateCreated ?: Date(), // Use state field
            dateUpdated = Date()
        )

        _uiState.value = currentState.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                if (currentState.jobId == null) {
                    jobTaskRepository.insertJob(jobToSave)
                } else {
                    jobTaskRepository.updateJob(jobToSave)
                }
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = "Failed to save job: ${e.message}")
            }
        }
    }
}
