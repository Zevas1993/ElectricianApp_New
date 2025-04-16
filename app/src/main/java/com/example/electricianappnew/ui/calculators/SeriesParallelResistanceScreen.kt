package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.electricianappnew.ui.calculators.viewmodel.ResistanceEntry
import com.example.electricianappnew.ui.calculators.viewmodel.SeriesParallelResistanceViewModel
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import androidx.compose.material3.Scaffold // Import Scaffold
import androidx.compose.material3.TopAppBar // Import TopAppBar
import androidx.compose.ui.text.font.FontWeight // Import FontWeight
import java.util.Locale // Import Locale
import com.example.electricianappnew.ui.common.formatCalculationResult // Import shared

@OptIn(ExperimentalMaterial3Api::class) // Already present, needed for TopAppBar too
@Composable
fun SeriesParallelResistanceScreen(
    modifier: Modifier = Modifier,
    viewModel: SeriesParallelResistanceViewModel = hiltViewModel(),
    navController: NavController // Add NavController parameter
) {
    val uiState = viewModel.uiState // Observe state directly

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Series/Parallel Resistance") },
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
            //     text = "Series/Parallel Resistance",
            //     style = MaterialTheme.typography.headlineMedium,
            //     modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
            // )

            // Connection Type Selector
            ExposedDropdownMenuBoxInput(
                label = "Connection Type",
                options = uiState.connectionTypes,
                selectedOption = uiState.connectionType.name,
                onOptionSelected = viewModel::onConnectionTypeChanged,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Resistors (Ohms):", style = MaterialTheme.typography.titleMedium)

            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(uiState.resistanceEntries, key = { _, item -> item.id }) { index, entry ->
                    ResistanceInputRow(
                        entry = entry,
                        index = index,
                        onValueChange = { newValue -> viewModel.updateResistanceEntry(index, newValue) },
                        onRemoveClick = { viewModel.removeResistanceEntry(index) },
                        canRemove = uiState.resistanceEntries.size > 2 // Can only remove if more than 2 exist
                    )
                }
                item {
                    Button(
                        onClick = viewModel::addResistanceEntry,
                        modifier = Modifier.padding(top = 8.dp).align(Alignment.End) // This align might not work as expected in LazyColumn item
                    ) {
                        Text("Add Resistor")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Results ---
            Text("Result:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Total Resistance: ${uiState.totalResistance?.formatCalculationResult(2) ?: "N/A"} Ω", // Use renamed shared helper
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
} // Closes SeriesParallelResistanceScreen composable

// Helper functions moved outside SeriesParallelResistanceScreen composable
@Composable
fun ResistanceInputRow( // REMOVED private modifier
    entry: ResistanceEntry,
    index: Int,
    onValueChange: (String) -> Unit,
    onRemoveClick: () -> Unit,
    canRemove: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = entry.valueStr,
            onValueChange = onValueChange,
            label = { Text("R${index + 1} (Ω)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onRemoveClick, enabled = canRemove) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Remove Resistor R${index + 1}",
                tint = if (canRemove) LocalContentColor.current else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}

// Removed local formatResistanceResult - using shared formatCalculationResult

// Preview removed
