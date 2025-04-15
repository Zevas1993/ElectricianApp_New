package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons // Add import for Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Add import for Back Arrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController // Add NavController import
import com.example.electricianappnew.ui.calculators.viewmodel.BoxFillViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

@OptIn(ExperimentalMaterial3Api::class) // Needed for TopAppBar
@Composable
fun BoxFillScreen(
    modifier: Modifier = Modifier,
    viewModel: BoxFillViewModel = hiltViewModel(),
    navController: NavController // Add NavController parameter
) {
    val uiState = viewModel.uiState // Observe state directly - Remove 'by' delegate for direct access
    val conductorSizes = viewModel.conductorSizes // Get sizes for inputs

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Box Fill Calculator") },
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
            //     text = "Box Fill Calculator (NEC 314.16)",
            //     style = MaterialTheme.typography.headlineMedium,
            //     modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
            // )

        LazyColumn(modifier = Modifier.weight(1f)) { // Make inputs scrollable
            item {
                Text("Conductors (Originating/Spliced/Terminated):", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
            }
            // Use viewModel.conductorSizes for iteration
            items(viewModel.conductorSizes) { size ->
                CountInputRow(
                    label = size,
                    count = uiState.conductorCounts[size] ?: "0", // Access map within uiState
                    onCountChange = { countStr -> viewModel.onConductorCountChange(size, countStr) }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Text("Equipment Grounds:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
            }
            // Use viewModel.conductorSizes for iteration
            items(viewModel.conductorSizes) { size ->
                CountInputRow(
                    label = size,
                    count = uiState.groundCounts[size] ?: "0", // Access map within uiState
                    onCountChange = { countStr -> viewModel.onGroundCountChange(size, countStr) }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Text("Other Allowances:", style = MaterialTheme.typography.titleMedium)
                CountInputRow(label = "Cable Clamps", count = uiState.numClampsStr, onCountChange = viewModel::onClampCountChange) // Access property of uiState
                CountInputRow(label = "Support Fittings (Studs/Hickeys)", count = uiState.numSupportFittingsStr, onCountChange = viewModel::onSupportFittingCountChange) // Access property of uiState
                CountInputRow(label = "Devices (Yokes/Straps)", count = uiState.numDevicesStr, onCountChange = viewModel::onDeviceCountChange) // Access property of uiState
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Text("Box Volume:", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = uiState.boxVolumeStr, // Access property of uiState
                    onValueChange = viewModel::onBoxVolumeChange,
                    label = { Text("Total Box Volume (in³)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // --- Results ---
            item {
                Text("Results:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Calculated Fill Volume: ${uiState.calculatedFillVolume?.formatResult(2) ?: "N/A"} in³") // Access property of uiState
                Text("Box Volume: ${uiState.boxVolume?.formatResult(2) ?: "N/A"} in³") // Access property of uiState

                val resultColor = if (uiState.isOverfill) MaterialTheme.colorScheme.error else LocalContentColor.current // Access property of uiState
                Text(
                    text = if (uiState.isOverfill) "OVERFILL!" else "OK", // Access property of uiState
                    style = MaterialTheme.typography.titleLarge,
                    color = resultColor
                )

                uiState.errorMessage?.let { error -> // Access property of uiState
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
             item { Spacer(modifier = Modifier.height(16.dp)) }
             item {
                 Button(onClick = viewModel::clearInputs, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                     Text("Clear / Reset")
                 }
             }
        }
    }
}

@Composable
private fun CountInputRow(
    label: String,
    count: String,
    onCountChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = count,
            onValueChange = { newValue ->
                // Allow empty string or positive integers
                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                    onCountChange(newValue)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(100.dp), // Adjust width as needed
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.End)
        )
    }
}

// Helper to format results nicely (can be shared or moved)
private fun Double.formatResult(decimals: Int = 2): String {
    return String.format("%.${decimals}f", this).trimEnd('0').trimEnd('.')
}


@Preview(showBackground = true, widthDp = 360)
@Composable
fun BoxFillScreenPreview() {
    // Provide a preview-specific ViewModel or mock data if needed
    // For now, it will use the default state which might be empty lists
    ElectricianAppNewTheme {
        BoxFillScreen()
    }
}
