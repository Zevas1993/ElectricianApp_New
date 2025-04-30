package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.graphics.Color // Not explicitly used
// import androidx.compose.ui.text.input.KeyboardType // Not explicitly used
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Import for lifecycle-aware collection
import androidx.navigation.NavController
import android.util.Log
import com.example.electricianappnew.ui.calculators.viewmodel.ConduitFillViewModel
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import androidx.compose.foundation.text.KeyboardOptions
import com.example.electricianappnew.ui.common.WireInputRow
import com.example.electricianappnew.ui.common.formatCalculationResult
// import java.util.Locale // Not explicitly used
import com.example.electricianappnew.data.model.WireEntry

// Removed NecConduitWireData object as data comes from ViewModel/Repository

// Main Screen Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConduitFillScreen(
    modifier: Modifier = Modifier,
    viewModel: ConduitFillViewModel = hiltViewModel(),
    navController: NavController
) {
    // Collect state flows using lifecycle-aware collection
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val conduitTypeNames by viewModel.conduitTypeNames.collectAsStateWithLifecycle()
    val availableConduitSizes by viewModel.availableConduitSizes.collectAsStateWithLifecycle()
    val wireTypeNames by viewModel.wireTypeNames.collectAsStateWithLifecycle() // Insulation types
    val conductorSizes by viewModel.conductorSizes.collectAsStateWithLifecycle() // Wire gauge sizes

    // Removed LaunchedEffects for logging state changes.
    // Removed LaunchedEffects for initial selections. UI will react to ViewModel state.

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conduit Fill Calculator") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Conduit Selection
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically // Align items vertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBoxInput(
                        label = "Conduit Type",
                        options = conduitTypeNames,
                        selectedOption = uiState.selectedConduitTypeName,
                        onOptionSelected = viewModel::onConduitTypeChange,
                        enabled = conduitTypeNames.isNotEmpty(), // Enable when options are loaded
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Removed loading indicator
                }
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBoxInput(
                        label = "Conduit Size",
                        options = availableConduitSizes,
                        selectedOption = uiState.selectedConduitSize,
                        onOptionSelected = viewModel::onConduitSizeChange,
                        // Enable only if a type is selected AND sizes are available for it
                        enabled = uiState.selectedConduitTypeName.isNotEmpty() && availableConduitSizes.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Removed loading indicator
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Conductors:", style = MaterialTheme.typography.titleMedium)

            // Removed wire types loading indicator

            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(uiState.wireEntries, key = { _, item -> item.id }) { index, entry ->
                    WireInputRow(
                        entry = entry,
                        wireTypeOptions = wireTypeNames, // Use collected insulation types
                        wireSizeOptions = conductorSizes, // Use collected conductor sizes (gauge)
                        onEntryChange = { updatedEntry -> viewModel.updateWireEntry(index, updatedEntry) },
                        onRemoveClick = { viewModel.removeWireEntry(index) }
                        // Removed wireTypeEnabled - dropdown enabled state handled internally by WireInputRow based on options
                    )
                    HorizontalDivider()
                }
            }

            Button(
                onClick = viewModel::addWireEntry,
                modifier = Modifier.align(Alignment.End),
                // Enable adding only when necessary dropdown data is loaded
                enabled = wireTypeNames.isNotEmpty() && conductorSizes.isNotEmpty()
            ) {
                Text("Add Conductor")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results Section (remains largely the same)
            Text("Results:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Total Conductor Area: ${uiState.totalWireArea?.formatCalculationResult(4) ?: "N/A"} in²")
            Text(text = "Allowable Fill Area: ${uiState.allowableFillArea?.formatCalculationResult(4) ?: "N/A"} in²")
            val percentageColor = if (uiState.isOverfill) MaterialTheme.colorScheme.error else LocalContentColor.current
            Text(text = "Fill Percentage: ${uiState.fillPercentage?.formatCalculationResult(1) ?: "N/A"} %", color = percentageColor)
            if (uiState.isOverfill) {
                Text(text = "OVERFILL!", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
            }

            // Error Message Display
            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}


// Preview function remains separate
@Preview(showBackground = true, widthDp = 360, heightDp = 700) // Increased height
@Composable
fun ConduitFillScreenPreview() {
    ElectricianAppNewTheme {
        // This preview is difficult because it relies heavily on Hilt ViewModel and Flows.
        // A more robust preview might involve:
        // 1. Creating a fake ViewModel with static data.
        // 2. Passing dummy data directly to a simplified version of the screen.
        Surface {
            Column(Modifier.padding(16.dp)) {
                Text("Conduit Fill Calculator (Preview)")
                Spacer(Modifier.height(8.dp))
                Text("Dropdowns and dynamic data require running the app.")
                // Add static result examples if needed
                 Spacer(Modifier.height(16.dp))
                 Text("Results:", style = MaterialTheme.typography.titleMedium)
                 Spacer(modifier = Modifier.height(8.dp))
                 Text(text = "Total Conductor Area: 0.0456 in²")
                 Text(text = "Allowable Fill Area: 0.1216 in²")
                 Text(text = "Fill Percentage: 37.5 %")
            }
        }
    }
}
