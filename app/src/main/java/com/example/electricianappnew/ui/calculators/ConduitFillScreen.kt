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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.electricianappnew.ui.calculators.viewmodel.ConduitFillViewModel
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import androidx.compose.foundation.text.KeyboardOptions
import com.example.electricianappnew.ui.common.WireInputRow // Import shared component
import com.example.electricianappnew.ui.common.formatCalculationResult // Import renamed shared helper
import java.util.Locale // Import Locale
import com.example.electricianappnew.data.model.WireEntry // Import WireEntry from central model location

// Data objects and classes should be top-level or appropriately scoped
object NecConduitWireData {
    data class ConduitInfo(val name: String, val sizes: Map<String, Double>)
    data class WireInfo(val name: String, val sizes: Map<String, Double>)

    val conduitTypes = listOf(
        ConduitInfo("EMT", mapOf("1/2\"" to 0.304, "3/4\"" to 0.533, "1\"" to 0.864)),
        ConduitInfo("RMC", mapOf("1/2\"" to 0.314, "3/4\"" to 0.551, "1\"" to 0.899)),
    )
    val wireTypes = listOf(
        WireInfo("THHN", mapOf("14 AWG" to 0.0097, "12 AWG" to 0.0133, "10 AWG" to 0.0211)),
        WireInfo("XHHW", mapOf("14 AWG" to 0.0135, "12 AWG" to 0.0181, "10 AWG" to 0.0278)),
    )
    fun getAllowableFillPercent(numWires: Int): Double {
        return when {
            numWires == 1 -> 53.0
            numWires == 2 -> 31.0
            numWires > 2 -> 40.0
            else -> 0.0
        }
    }
}

// Removed local WireEntry definition - now imported from ViewModel

// Main Screen Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConduitFillScreen(
    modifier: Modifier = Modifier,
    viewModel: ConduitFillViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState = viewModel.uiState // Correct state access

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
            Row(Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBoxInput(
                    label = "Conduit Type",
                    options = viewModel.conduitTypeNames,
                    selectedOption = uiState.selectedConduitTypeName,
                    onOptionSelected = viewModel::onConduitTypeChange,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                ExposedDropdownMenuBoxInput(
                    label = "Conduit Size",
                    options = viewModel.availableConduitSizes,
                    selectedOption = uiState.selectedConduitSize,
                    onOptionSelected = viewModel::onConduitSizeChange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Conductors:", style = MaterialTheme.typography.titleMedium)

            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(uiState.wireEntries, key = { _, item -> item.id }) { index, entry -> // Added key
                    WireInputRow( // Use shared composable
                        entry = entry,
                        wireTypeOptions = viewModel.wireTypeNames, // Pass options
                        wireSizeOptions = viewModel.availableWireSizes, // Pass options
                        onEntryChange = { updatedEntry -> viewModel.updateWireEntry(index, updatedEntry) },
                        onRemoveClick = { viewModel.removeWireEntry(index) }
                    )
                    HorizontalDivider()
                }
            }

            Button(onClick = viewModel::addWireEntry, modifier = Modifier.align(Alignment.End)) {
                Text("Add Conductor")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results
            Text("Results:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Total Conductor Area: ${uiState.totalWireArea?.formatCalculationResult(4) ?: "N/A"} in²") // Use renamed shared helper
            Text(text = "Allowable Fill Area: ${uiState.allowableFillArea?.formatCalculationResult(4) ?: "N/A"} in²") // Use renamed shared helper
            val percentageColor = if (uiState.isOverfill) MaterialTheme.colorScheme.error else LocalContentColor.current
            Text(text = "Fill Percentage: ${uiState.fillPercentage?.formatCalculationResult(1) ?: "N/A"} %", color = percentageColor) // Use renamed shared helper
            if (uiState.isOverfill) {
                Text(text = "OVERFILL!", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium) // Added text=
            }

            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error) // Added text=
            }
        } // <<< Closing brace for Column
    } // <<< Closing brace for Scaffold content lambda
}

// Removed local WireInputRow - using shared version from common package

// Removed local formatResult - using shared version from common package

// Preview function remains separate
@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
fun ConduitFillScreenPreview() {
    ElectricianAppNewTheme {
        // ConduitFillScreen() // Preview needs NavController
        // For preview, consider creating a dummy NavController or a simplified preview composable
        Text("Preview requires NavController") // Placeholder
    }
}
