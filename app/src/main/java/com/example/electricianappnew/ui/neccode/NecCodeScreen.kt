package com.example.electricianappnew.ui.neccode

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.electricianappnew.data.model.* // Import models including NecSearchResult implementations
import com.example.electricianappnew.navigation.Screen // Import Screen object for routes
import com.example.electricianappnew.ui.neccode.viewmodel.NecCodeViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NecCodeScreen(
    modifier: Modifier = Modifier,
    viewModel: NecCodeViewModel = hiltViewModel(),
    navController: NavController? = null // Add optional NavController
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(
            "NEC Code Reference Search", // Updated title
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp)) // Reduced space

        // --- Navigation Buttons ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { navController?.navigate(Screen.CircuitColorReference.route) }) {
                Text("Colors")
            }
            Button(onClick = { navController?.navigate(Screen.ElectricalFormulas.route) }) {
                Text("Formulas")
            }
            Button(onClick = { navController?.navigate(Screen.ElectricalSymbols.route) }) {
                Text("Symbols")
            }
        }
        Spacer(modifier = Modifier.height(16.dp)) // Space before search

        // --- Search Input ---
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged, // Trigger search on change
            label = { Text("Search NEC Tables...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Display Loading or Error
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "An error occurred",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            // Display Search Results
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (uiState.searchQuery.isNotBlank() && uiState.searchResults.isEmpty()) {
                    item {
                        Text(
                            text = "No results found for \"${uiState.searchQuery}\"",
                            modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                        )
                    }
                } else {
                    items(
                        items = uiState.searchResults,
                        // Optional: Add a key if items have unique stable IDs across types
                        // key = { result -> result.hashCode() } // Example key, adjust if needed
                    ) { result ->
                        SearchResultItem(result = result)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

// Composable to display a single search result item
@Composable
fun SearchResultItem(result: NecSearchResult) {
    // Use a Column for more flexible layout per result type
    Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp).fillMaxWidth()) {
        Text(result.type, style = MaterialTheme.typography.labelSmall) // Show type first
        Spacer(modifier = Modifier.height(2.dp))
        when (result) {
            is NecSearchResult.AmpacityResult -> {
                Text(result.title, style = MaterialTheme.typography.titleMedium)
                Text(result.details, style = MaterialTheme.typography.bodyMedium)
            }
            is NecSearchResult.ConduitResult -> {
                Text(result.title, style = MaterialTheme.typography.titleMedium)
                Text(result.details, style = MaterialTheme.typography.bodyMedium)
            }
            is NecSearchResult.BoxFillResult -> {
                Text(result.title, style = MaterialTheme.typography.titleMedium)
                Text(result.details, style = MaterialTheme.typography.bodyMedium)
            }
            is NecSearchResult.ConductorResult -> {
                Text(result.title, style = MaterialTheme.typography.titleMedium)
                Text(result.details, style = MaterialTheme.typography.bodyMedium)
            }
            is NecSearchResult.WireAreaResult -> {
                Text(result.title, style = MaterialTheme.typography.titleMedium)
                Text(result.details, style = MaterialTheme.typography.bodyMedium)
            }
            is NecSearchResult.FullCodeResult -> { // Added case for full code results
                Text(result.title, style = MaterialTheme.typography.titleMedium) // Title includes section & article
                Text(result.details, style = MaterialTheme.typography.bodyMedium) // Details is the section text
            }
            is NecSearchResult.ConductorImpedanceResult -> { // Added case for conductor impedance results
                Text(result.title, style = MaterialTheme.typography.titleMedium)
                Text(result.details, style = MaterialTheme.typography.bodyMedium)
            }
            // Add cases for other result types here later
        }
    }
    // Original ListItem approach (less flexible for varying details):
    // ListItem(
    //     headlineContent = { Text(result.title, fontWeight = FontWeight.Bold) },
    //     supportingContent = { Text(result.details) },
    //     overlineContent = { Text(result.type) } // Show the type (e.g., "Ampacity (310.16)")
    // )
    // Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
    //     Text(result.type, style = MaterialTheme.typography.labelSmall)
    //     Text(result.title, style = MaterialTheme.typography.titleMedium)
    //     Text(result.details, style = MaterialTheme.typography.bodyMedium)
    // }
}


// Removed old Header and Row composables for individual tables

@Preview(showBackground = true)
@Composable
fun NecCodeScreenPreview() {
    ElectricianAppNewTheme {
        // Preview will show initial empty state or require a mock ViewModel
        NecCodeScreen() // Preview doesn't need NavController
    }
}
