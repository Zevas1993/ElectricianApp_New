package com.example.electricianappnew.ui.clients.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Removed duplicate import
import com.example.electricianappnew.data.model.Client
import com.example.electricianappnew.data.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // Import combine, stateIn, SharingStarted, update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientListUiState(
    val allClients: List<Client> = emptyList(), // Store the full list
    val displayedClients: List<Client> = emptyList(), // Filtered list for UI
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ClientListViewModel @Inject constructor(
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _allClientsFlow = MutableStateFlow(ClientListUiState(isLoading = true)) // Holds raw data + loading state

    // Combine the full client list and the search query to produce the final UI state
    val uiState: StateFlow<ClientListUiState> = combine(
        _allClientsFlow,
        _searchQuery
    ) { clientsState, query ->
        val filteredClients = if (query.isBlank()) {
            clientsState.allClients
        } else {
            clientsState.allClients.filter { client ->
                client.name.contains(query, ignoreCase = true) ||
                // Removed companyName check as it doesn't exist in the model
                client.primaryAddress.contains(query, ignoreCase = true) || // Use primaryAddress
                (client.phone?.contains(query, ignoreCase = true) == true) ||
                (client.email?.contains(query, ignoreCase = true) == true)
            }
        }
        clientsState.copy(
            searchQuery = query,
            displayedClients = filteredClients // Update the displayed list
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClientListUiState()
    )

    init {
        loadClients() // Initial load
    }

    private fun loadClients() {
        viewModelScope.launch {
            _allClientsFlow.update { it.copy(isLoading = true, errorMessage = null) }
            clientRepository.getAllClients()
                .catch { exception ->
                    _allClientsFlow.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load clients: ${exception.message}"
                        )
                    }
                }
                .collect { clientList ->
                    _allClientsFlow.update {
                        it.copy(
                            allClients = clientList, // Store the full list
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            try {
                clientRepository.deleteClient(client)
                // Flow collection in loadClients should handle UI update.
                // Clear any previous error shown in the main state flow
                 _allClientsFlow.update { it.copy(errorMessage = null) }
            } catch (e: Exception) {
                 // Update the error message in the main state flow
                 _allClientsFlow.update { it.copy(errorMessage = "Failed to delete client: ${e.message}") }
            }
        }
    }
}
