package com.example.electricianappnew.ui.clients.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.Client
import com.example.electricianappnew.data.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// State for Add/Edit Client Screen
data class AddEditClientUiState(
    val clientId: String? = null, // Null if adding new
    val name: String = "",
    val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false // For loading existing client
)

@HiltViewModel
class AddEditClientViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clientId: String? = savedStateHandle["clientId"] // Get ID if editing

    private val _uiState = MutableStateFlow(AddEditClientUiState())
    val uiState: StateFlow<AddEditClientUiState> = _uiState

    init {
        if (clientId != null) {
            loadClient(clientId)
        }
    }

    private fun loadClient(id: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val client = clientRepository.getClientById(id).first() // Use first() to get single value
                if (client != null) {
                    _uiState.value = _uiState.value.copy(
                        clientId = client.id,
                        name = client.name,
                        contactPerson = client.contactPerson ?: "",
                        phone = client.phone ?: "",
                        email = client.email ?: "",
                        address = client.primaryAddress ?: "", // Use primaryAddress field
                        notes = client.notes ?: "",
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Client not found.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Failed to load client: ${e.message}")
            }
        }
    }

    // --- Input Handlers ---
    fun onNameChange(value: String) { _uiState.value = uiState.value.copy(name = value) }
    fun onContactPersonChange(value: String) { _uiState.value = uiState.value.copy(contactPerson = value) }
    fun onPhoneChange(value: String) { _uiState.value = uiState.value.copy(phone = value) }
    fun onEmailChange(value: String) { _uiState.value = uiState.value.copy(email = value) }
    fun onAddressChange(value: String) { _uiState.value = uiState.value.copy(address = value) }
    fun onNotesChange(value: String) { _uiState.value = uiState.value.copy(notes = value) }


    fun saveClient() {
        val currentState = uiState.value
        if (currentState.name.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Client Name is required.")
            return
        }

        val clientToSave = Client(
            id = currentState.clientId ?: UUID.randomUUID().toString(),
            name = currentState.name.trim(),
            contactPerson = currentState.contactPerson.trim(), // Pass trimmed string directly
            phone = currentState.phone.trim(),                 // Pass trimmed string directly
            email = currentState.email.trim(),                 // Pass trimmed string directly
            primaryAddress = currentState.address.trim(),      // Pass trimmed string directly
            // billingAddress is missing from UI state, pass empty string
            billingAddress = "",
            notes = currentState.notes.trim()                  // Pass trimmed string directly
        )

        _uiState.value = currentState.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                if (currentState.clientId == null) {
                    clientRepository.insertClient(clientToSave)
                } else {
                    clientRepository.updateClient(clientToSave)
                }
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = "Failed to save client: ${e.message}")
            }
        }
    }
}
