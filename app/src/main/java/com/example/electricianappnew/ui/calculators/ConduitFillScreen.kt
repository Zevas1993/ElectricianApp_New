package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons // Add import for Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Add import for Back Arrow
// import androidx.compose.material3.Divider // Keep commented or remove
import androidx.compose.material3.HorizontalDivider // Add import
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.material3.LocalContentColor // Import for LocalContentColor
import androidx.hilt.navigation.compose.hiltViewModel // Import for hiltViewModel
import androidx.compose.ui.tooling.preview.Preview // Import for Preview
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme // Import for custom theme
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController // Add NavController import
import com.example.electricianappnew.ui.calculators.viewmodel.ConduitFillViewModel
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.Color
// Removed incorrect import: import androidx.compose.ui.platform.LocalContentColor
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

// TODO: Replace with actual data loading/lookup from NEC tables
object NecConduitWireData {
    // Simplified placeholder data - Needs real NEC Chapter 9, Table 4 & 5 data
    data class ConduitInfo(val name: String, val sizes: Map<String, Double>) // Size -> Area (in^2)
    data class WireInfo(val name: String, val sizes: Map<String, Double>) // Size -> Area (in^2)

    val conduitTypes = listOf(
        ConduitInfo("EMT", mapOf("1/2\"" to 0.304, "3/4\"" to 0.533, "1\"" to 0.864 /*...*/)),
        ConduitInfo("RMC", mapOf("1/2\"" to 0.314, "3/4\"" to 0.551, "1\"" to 0.899 /*...*/)),
        // Add more conduit types
    )

    val wireTypes = listOf(
        WireInfo("THHN", mapOf("14 AWG" to 0.0097, "12 AWG" to 0.0133, "10 AWG" to 0.0211 /*...*/)),
        WireInfo("XHHW", mapOf("14 AWG" to 0.0135, "12 AWG" to 0.0181, "10 AWG" to 0.0278 /*...*/)),
        // Add more wire types
    )

    // NEC Chapter 9, Table 1 - Allowable Fill Percentages
    fun getAllowableFillPercent(numWires: Int): Double {
        return when {
            numWires == 1 -> 53.0
            numWires == 2 -> 31.0
            numWires > 2 -> 40.0
            else -> 0.0 // Should not happen
        }
    }
}

data class WireEntry(
    val id: Int = System.identityHashCode(Any()), // Simple unique ID
    var type: String = NecConduitWireData.wireTypes.first().name,
    var size: String = NecConduitWireData.wireTypes.first().sizes.keys.first(),
    var quantity: Int = 1
)

@OptIn(ExperimentalMaterial3Api::class) // Already present, needed for TopAppBar too
@Composable
fun ConduitFillScreen(
    modifier: Modifier = Modifier,
    viewModel: ConduitFillViewModel = hiltViewModel(), // Inject ViewModel
    navController: NavController // Add NavController parameter
) {
    val uiState = viewModel.uiState // Observe state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conduit Fill Calculator") },
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
                .fillMaxWidth()
        ) {
            // Text("Conduit Fill Calculator", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.align(Alignment.CenterHorizontally)) // Title is now in TopAppBar
            // Spacer(modifier = Modifier.height(16.dp)) // Adjust spacing if needed

        // Conduit Selection
        Row(Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBoxInput(
                label = "Conduit Type",
                options = viewModel.conduitTypeNames, // Access directly from viewModel
                selectedOption = uiState.selectedConduitTypeName, // This one is correctly in uiState
                onOptionSelected = viewModel::onConduitTypeChange, // Use ViewModel
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
             ExposedDropdownMenuBoxInput(
                label = "Conduit Size",
                options = viewModel.availableConduitSizes, // Access directly from viewModel
                selectedOption = uiState.selectedConduitSize, // This one is correctly in uiState
                onOptionSelected = viewModel::onConduitSizeChange, // Use ViewModel
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Conductors:", style = MaterialTheme.typography.titleMedium)

        LazyColumn(modifier = Modifier.weight(1f)) { // Make the list scrollable
            itemsIndexed(uiState.wireEntries) { index, entry -> // Use state list
                WireInputRow(
                    entry = entry,
                    onEntryChange = { updatedEntry -> viewModel.updateWireEntry(index, updatedEntry) }, // Use ViewModel
                    onRemoveClick = { viewModel.removeWireEntry(index) } // Use ViewModel
                )
                HorizontalDivider() // Replaced deprecated Divider
            }
        }

        Button(onClick = viewModel::addWireEntry, modifier = Modifier.align(Alignment.End)) { // Use ViewModel
            Text("Add Conductor") // Changed text slightly
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results
        Text("Results:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Total Conductor Area: ${uiState.totalWireArea?.formatResult(4) ?: "N/A"} in²") // Use state
        Text("Allowable Fill Area: ${uiState.allowableFillArea?.formatResult(4) ?: "N/A"} in²") // Use state
        val percentageColor = if (uiState.isOverfill) MaterialTheme.colorScheme.error else LocalContentColor.current // Use state
        Text("Fill Percentage: ${uiState.fillPercentage?.formatResult(1) ?: "N/A"} %", color = percentageColor) // Use state
        if (uiState.isOverfill) { // Use state
            Text("OVERFILL!", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
        }

        uiState.errorMessage?.let { error -> // Use state
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun WireInputRow(
    entry: WireEntry,
    onEntryChange: (WireEntry) -> Unit,
    onRemoveClick: () -> Unit
) {
     // Data for dropdowns should ideally come from ViewModel/State, but using object for now
     val currentWireInfo = NecConduitWireData.wireTypes.find { it.name == entry.type }
     val availableSizes = currentWireInfo?.sizes?.keys?.toList() ?: listOf(entry.size)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
         ExposedDropdownMenuBoxInput(
             label = "Type",
             options = NecConduitWireData.wireTypes.map { it.name }, // Use data from object
             selectedOption = entry.type,
             onOptionSelected = { newType ->
                 val newWireInfo = NecConduitWireData.wireTypes.first { it.name == newType }
                 onEntryChange(entry.copy(type = newType, size = newWireInfo.sizes.keys.first()))
             },
             modifier = Modifier.weight(2f)
         )
         Spacer(Modifier.width(8.dp))
          ExposedDropdownMenuBoxInput(
             label = "Size",
             options = availableSizes,
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


// Helper to format results nicely (can be shared or moved)
private fun Double.formatResult(decimals: Int = 2): String {
    return String.format("%.${decimals}f", this).trimEnd('0').trimEnd('.')
}

@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable // Add missing @Composable annotation
fun ConduitFillScreenPreview() {
    ElectricianAppNewTheme {
        ConduitFillScreen() // Preview uses default ViewModel state
    }
}
