package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.example.electricianappnew.ui.calculators.viewmodel.UiEvent // Import UiEvent

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
    // Removed conductorSizes as it's now dynamic per wire entry

    // Collect the map of available conductor sizes flows
    val availableConductorSizesMap by viewModel.availableConductorSizesForEntry.collectAsStateWithLifecycle()

    // Log the collected states to see what the UI is observing
    Log.d("ConduitFillScreen", "UI State: $uiState")
    Log.d("ConduitFillScreen", "Conduit Types: $conduitTypeNames")
    Log.d("ConduitFillScreen", "Available Conduit Sizes: $availableConduitSizes")
    Log.d("ConduitFillScreen", "Wire Types: $wireTypeNames")
    Log.d("ConduitFillScreen", "Wire Entries: ${uiState.wireEntries}")


    // Snackbar host state for displaying messages
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe UI events from ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                // Handle other UiEvents here
            }
        }
    }

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) } // Attach SnackbarHost to Scaffold
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

            // Use a Box to manage the layout of the LazyColumn and potentially other elements
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) { // Ensure LazyColumn fills the available space
                    itemsIndexed(uiState.wireEntries, key = { _, item -> item.id }) { index, entry ->
                        // Get the StateFlow for the available sizes for this specific wire entry
                        val sizesFlow = viewModel.getAvailableConductorSizesFlow(index)
                        // Collect the list of sizes from this flow
                        val conductorSizesForEntry by sizesFlow.collectAsStateWithLifecycle()

                        WireInputRow(
                            entry = entry,
                            wireTypeOptions = wireTypeNames, // Use collected insulation types
                            wireSizeOptions = conductorSizesForEntry, // Use collected sizes for this specific entry
                            onEntryChange = { updatedEntry -> viewModel.updateWireEntry(index, updatedEntry) },
                            onRemoveClick = { viewModel.removeWireEntry(index) }
                        )
                        HorizontalDivider()
                    }
                }
            }


            Button(
                onClick = viewModel::addWireEntry,
                modifier = Modifier.align(Alignment.End),
                // Enable adding only when necessary dropdown data is loaded (wire types)
                enabled = wireTypeNames.isNotEmpty() // This is the condition we are checking
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

            // Removed old Error Message Display
            // uiState.errorMessage?.let { error ->
            //     Spacer(modifier = Modifier.height(8.dp))
            //     Text(text = error, color = MaterialTheme.colorScheme.error)
            // }
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
