package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Add import for Back Arrow
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController // Add NavController import
import com.example.electricianappnew.ui.calculators.viewmodel.RacewaySizingViewModel
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput // Assuming WireInputRow uses this
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import androidx.compose.foundation.text.KeyboardOptions // Import KeyboardOptions
import androidx.compose.material3.Scaffold // Import Scaffold
import androidx.compose.material3.TopAppBar // Import TopAppBar
import java.util.Locale // Import Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Import lifecycle-aware collection
import com.example.electricianappnew.data.model.WireEntry // Import WireEntry from central model location
import com.example.electricianappnew.ui.common.WireInputRow // Import shared
import com.example.electricianappnew.ui.common.formatCalculationResult // Import shared
import android.util.Log // Import Log

// Assuming WireEntry data class is accessible (defined in ConduitFillScreen or common location)
// Assuming WireInputRow composable is accessible (defined in ConduitFillScreen or common location)
// If not, they need to be defined here or moved to common.

@OptIn(ExperimentalMaterial3Api::class) // Already present, needed for TopAppBar too
@Composable
fun RacewaySizingScreen(
    modifier: Modifier = Modifier,
    viewModel: RacewaySizingViewModel = hiltViewModel(),
    navController: NavController // Add NavController parameter
) {
    // Collect state flows using lifecycle-aware collection
    val uiState by viewModel.uiState.collectAsStateWithLifecycle() // Collect UI state
    // Note: racewayTypeNames and wireTypeNames are currently mutableStateOf, not StateFlow.
    // We'll keep observing them directly for now, but ideally they'd be StateFlows too.
    // val racewayTypeNames by viewModel.racewayTypeNames.collectAsStateWithLifecycle() // Collect raceway types
    // val wireTypeNames by viewModel.wireTypeNames.collectAsStateWithLifecycle() // Collect wire types
    val conductorSizes by viewModel.conductorSizes.collectAsStateWithLifecycle() // Collect conductor sizes

    // Log collected state changes (optional, for debugging)
    LaunchedEffect(uiState) { Log.d("RacewaySizingScreen", "UI State updated: $uiState") }
    LaunchedEffect(viewModel.racewayTypeNames) { Log.d("RacewaySizingScreen", "Raceway Types updated: ${viewModel.racewayTypeNames}") }
    LaunchedEffect(viewModel.wireTypeNames) { Log.d("RacewaySizingScreen", "Wire Types updated: ${viewModel.wireTypeNames}") }
    LaunchedEffect(conductorSizes) { Log.d("RacewaySizingScreen", "Conductor Sizes collected: $conductorSizes") }

    // --- LaunchedEffects for Initial/Default Selections (Optional but good practice) ---
    // Select first raceway type when list loads and none is selected
    LaunchedEffect(viewModel.racewayTypeNames) {
        if (uiState.selectedRacewayType.isBlank() && viewModel.racewayTypeNames.isNotEmpty()) {
            Log.d("RacewaySizingScreen", "Selecting initial raceway type: ${viewModel.racewayTypeNames.first()}")
            viewModel.onRacewayTypeChange(viewModel.racewayTypeNames.first())
        }
    }

    // Ensure initial wire entry uses default type/size once lists are loaded
    // This logic is mostly handled in the ViewModel's init now, but we could add
    // checks here if needed, especially if adding entries dynamically.

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Raceway Sizing Calculator") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(16.dp) // Add screen padding
                .fillMaxSize()
        ) {
            // Text( // Title is now in TopAppBar
            //     text = "Raceway Sizing Calculator",
            //     style = MaterialTheme.typography.headlineMedium,
            //     modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
            // )

            // Raceway Type Selection
            ExposedDropdownMenuBoxInput(
                label = "Raceway Type",
                options = viewModel.racewayTypeNames, // Use directly observed mutableStateOf list
                selectedOption = uiState.selectedRacewayType, // Use collected state
                onOptionSelected = viewModel::onRacewayTypeChange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Conductors:", style = MaterialTheme.typography.titleMedium)

            LazyColumn(modifier = Modifier.weight(1f)) { // Make the list scrollable
                itemsIndexed(uiState.wireEntries, key = { _, item -> item.id }) { index, entry ->
                    // Content for each wire entry item
                    WireInputRow(
                        entry = entry, // Use entry from collected uiState.wireEntries
                        wireTypeOptions = viewModel.wireTypeNames, // Use directly observed mutableStateOf list
                        wireSizeOptions = conductorSizes, // Pass the collected list of all conductor sizes
                        onEntryChange = { updatedEntry -> viewModel.updateWireEntry(index, updatedEntry) },
                        onRemoveClick = { viewModel.removeWireEntry(index) }
                        // wireTypeEnabled is implicitly true here unless we add loading state to this screen too
                    )
                    HorizontalDivider()
                }
                // Add Button as the last item in the LazyColumn
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = viewModel::addWireEntry) {
                            Text("Add Conductor")
                        }
                    }
                }
            }

            // Spacer(modifier = Modifier.height(24.dp)) // Spacer might not be needed if button is last item

            // --- Results ---
            Text("Results:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Total Conductor Area: ${uiState.totalWireArea?.formatCalculationResult(4) ?: "N/A"} in²") // Use renamed shared helper
            Text(text = "Required Fill %: ${uiState.requiredFillPercent?.formatCalculationResult(0) ?: "N/A"} %") // Use renamed shared helper
            Text(text = "Minimum Raceway Area: ${uiState.minimumRacewayArea?.formatCalculationResult(4) ?: "N/A"} in²") // Use renamed shared helper
            Text(
                text = "Calculated Raceway Size: ${uiState.calculatedRacewaySize ?: "N/A"}",
                style = MaterialTheme.typography.titleLarge
            )

            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error) // Added text=
            }

             Spacer(modifier = Modifier.height(16.dp))
             Button(onClick = viewModel::clearInputs, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                 Text("Clear / Reset")
             }
        } // Closes Column
    } // Closes Scaffold content lambda
} // Closes RacewaySizingScreen composable

// Removed local helpers - using shared versions from ui.common

// Preview removed
