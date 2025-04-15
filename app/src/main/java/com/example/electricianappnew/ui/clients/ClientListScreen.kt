package com.example.electricianappnew.ui.clients

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete // Import Delete icon
import androidx.compose.material.icons.filled.Search // Import Search icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // Import Hilt ViewModel
import com.example.electricianappnew.data.model.Client
import com.example.electricianappnew.ui.clients.viewmodel.ClientListViewModel // Import ViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme


@Composable
fun ClientListScreen(
    modifier: Modifier = Modifier,
    viewModel: ClientListViewModel = hiltViewModel(), // Inject ViewModel
    onClientClick: (String) -> Unit,
    onAddClientClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState() // Observe state
    var showDeleteDialog by remember { mutableStateOf<Client?>(null) } // State to control delete dialog

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClientClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Client")
            }
        }
    ) { paddingValues ->
        Column(modifier = modifier.padding(paddingValues).padding(16.dp)) {
            Text("Clients", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))

            // Search Input Field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label = { Text("Search Clients...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))


            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                 // Check displayedClients for empty state after filtering
                uiState.displayedClients.isEmpty() && !uiState.isLoading -> {
                     Text(
                         if (uiState.searchQuery.isBlank()) "No clients found. Add one!" else "No clients match your search.",
                         modifier = Modifier.align(Alignment.CenterHorizontally)
                     )
                }
                else -> {
                    LazyColumn {
                         // Iterate over displayedClients
                        items(uiState.displayedClients, key = { it.id }) { client ->
                            ClientListItem(
                                client = client,
                                onClick = { onClientClick(client.id) },
                                onDeleteClick = { showDeleteDialog = client } // Show dialog on delete click
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Deletion
    showDeleteDialog?.let { clientToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Client?") },
            text = { Text("Are you sure you want to delete client '${clientToDelete.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteClient(clientToDelete)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ClientListItem(
    client: Client,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit // Add delete callback
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp), // Adjust padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(client.name, style = MaterialTheme.typography.titleMedium)
            Text(client.primaryAddress, style = MaterialTheme.typography.bodySmall)
            if (client.phone.isNotEmpty()) {
                 Text("Ph: ${client.phone}", style = MaterialTheme.typography.bodySmall)
            }
        }
        // Delete Button
        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Client", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClientListScreenPreview() {
    ElectricianAppNewTheme {
        ClientListScreen(onClientClick = {}, onAddClientClick = {})
    }
}
