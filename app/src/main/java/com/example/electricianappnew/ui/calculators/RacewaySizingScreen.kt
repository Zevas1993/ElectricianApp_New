package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electricianappnew.ui.calculators.viewmodel.RacewaySizingViewModel
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput // Assuming WireInputRow uses this
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import androidx.compose.foundation.text.KeyboardOptions // Import KeyboardOptions

// Assuming WireEntry data class is accessible (defined in ConduitFillScreen or common location)
// Assuming WireInputRow composable is accessible (defined in ConduitFillScreen or common location)
// If not, they need to be defined here or moved to common.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacewaySizingScreen(
    modifier: Modifier = Modifier,
    viewModel: RacewaySizingViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState // Observe state directly

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Raceway Sizing Calculator",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )

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
            itemsIndexed(uiState.wireEntries) { index, entry ->
                // Use the WireInputRow composable (needs to be defined/accessible)
                WireInputRow( // Assuming WireInputRow exists and takes these params
                    entry = entry,
                    wireTypeOptions = viewModel.wireTypeNames, // Pass options
                    wireSizeOptions = viewModel.availableWireSizes, // Pass options
                    onEntryChange = { updatedEntry -> viewModel.updateWireEntry(index, updatedEntry) },
                    onRemoveClick = { viewModel.removeWireEntry(index) }
                )
                HorizontalDivider()
            }
            // Button moved outside the LazyColumn items
        }

        // Place Add Button after the list, before results
        Button(
            onClick = viewModel::addWireEntry,
            modifier = Modifier.padding(top = 8.dp).align(Alignment.End) // Align button within the Column
        ) {
            Text("Add Conductor")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Results ---
        Text("Results:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Total Conductor Area: ${uiState.totalWireArea?.formatResult(4) ?: "N/A"} in²")
        Text("Required Fill %: ${uiState.requiredFillPercent?.formatResult(0) ?: "N/A"} %")
        Text("Minimum Raceway Area: ${uiState.minimumRacewayArea?.formatResult(4) ?: "N/A"} in²")
        Text(
            text = "Calculated Raceway Size: ${uiState.calculatedRacewaySize ?: "N/A"}",
            style = MaterialTheme.typography.titleLarge
        )

        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }

         Spacer(modifier = Modifier.height(16.dp))
         Button(onClick = viewModel::clearInputs, modifier = Modifier.align(Alignment.CenterHorizontally)) {
             Text("Clear / Reset")
         }
    }
}

// IMPORTANT: Requires WireInputRow composable to be defined here or imported from common
// Duplicating/Adapting WireInputRow from ConduitFillScreen for now:
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WireInputRow(
    entry: WireEntry,
    wireTypeOptions: List<String>, // Pass options from ViewModel
    wireSizeOptions: List<String>, // Pass options from ViewModel
    onEntryChange: (WireEntry) -> Unit,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
         ExposedDropdownMenuBoxInput(
             label = "Type",
             options = wireTypeOptions, // Use passed options
             selectedOption = entry.type,
             onOptionSelected = { newType ->
                 // Logic to potentially reset size might be needed here or in VM
                 onEntryChange(entry.copy(type = newType))
             },
             modifier = Modifier.weight(2f)
         )
         Spacer(Modifier.width(8.dp))
          ExposedDropdownMenuBoxInput(
             label = "Size",
             options = wireSizeOptions, // Use passed options (might need filtering based on selected type)
             selectedOption = entry.size,
             onOptionSelected = { newSize -> onEntryChange(entry.copy(size = newSize)) },
             modifier = Modifier.weight(1.5f)
         )
         Spacer(Modifier.width(8.dp))
         OutlinedTextField(
             value = entry.quantity.toString(),
             onValueChange = { qtyStr ->
                 onEntryChange(entry.copy(quantity = qtyStr.toIntOrNull() ?: 1))
             },
             label = { Text("Qty") },
             keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
             modifier = Modifier.weight(1f)
         )

        IconButton(onClick = onRemoveClick) {
            Icon(Icons.Default.Delete, contentDescription = "Remove Conductor")
        }
    }
}


// Helper to format results nicely
private fun Double.formatResult(decimals: Int = 2): String {
    return String.format("%.${decimals}f", this).trimEnd('0').trimEnd('.')
}


@Preview(showBackground = true, widthDp = 360)
@Composable
fun RacewaySizingScreenPreview() {
    ElectricianAppNewTheme {
        RacewaySizingScreen()
    }
}
