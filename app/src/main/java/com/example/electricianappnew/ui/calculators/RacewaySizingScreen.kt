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
import com.example.electricianappnew.data.model.WireEntry // Import WireEntry from central model location
import com.example.electricianappnew.ui.common.WireInputRow // Import shared
import com.example.electricianappnew.ui.common.formatCalculationResult // Import shared

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
    val uiState = viewModel.uiState // Observe state directly

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
                options = viewModel.racewayTypeNames,
                selectedOption = uiState.selectedRacewayType,
                onOptionSelected = viewModel::onRacewayTypeChange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Conductors:", style = MaterialTheme.typography.titleMedium)

            LazyColumn(modifier = Modifier.weight(1f)) { // Make the list scrollable
                itemsIndexed(uiState.wireEntries, key = { _, item -> item.id }) { index, entry ->
                    // Content for each wire entry item
                    WireInputRow(
                        entry = entry,
                        wireTypeOptions = viewModel.wireTypeNames, // Pass options
                        wireSizeOptions = viewModel.availableWireSizes, // Pass options
                        onEntryChange = { updatedEntry -> viewModel.updateWireEntry(index, updatedEntry) },
                        onRemoveClick = { viewModel.removeWireEntry(index) }
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
