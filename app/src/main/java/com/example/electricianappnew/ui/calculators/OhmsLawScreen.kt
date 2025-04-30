package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType // Ensure KeyboardType is imported
// Remove incorrect import: import androidx.compose.ui.text.input.KeyboardType.Companion.NumberDecimal
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // Import Hilt ViewModel
import androidx.navigation.NavController
import com.example.electricianappnew.ui.calculators.viewmodel.CalculateVariable
import com.example.electricianappnew.ui.calculators.viewmodel.OhmsLawViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import androidx.compose.material3.Scaffold // Import Scaffold
import androidx.compose.material3.TopAppBar // Import TopAppBar
// Removed kotlin.math imports as they are now in ViewModel

@OptIn(ExperimentalMaterial3Api::class) // Needed for TopAppBar
@Composable
fun OhmsLawScreen(
    modifier: Modifier = Modifier,
    viewModel: OhmsLawViewModel = hiltViewModel(), // Inject ViewModel
    navController: NavController // Add NavController parameter
) {
    val uiState = viewModel.uiState // Observe state from ViewModel

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ohm's Law Calculator") },
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
                .fillMaxWidth(), // REMOVED incorrect comma
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Ohm's Law Calculator", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            // Radio buttons to select calculation target
            Text("Calculate:")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CalculateVariable.values().forEach { target ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = uiState.calculateTarget == target,
                            onClick = { viewModel.onTargetChange(target) } // Call ViewModel function
                        )
                        Text(target.name)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Input Fields
            OutlinedTextField(
                value = uiState.voltageStr,
                onValueChange = viewModel::onVoltageChange, // Call ViewModel function
                label = { Text("Voltage (V)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), // Use KeyboardType.Decimal
                enabled = uiState.voltageEnabled, // Use state from ViewModel
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.currentStr,
                onValueChange = viewModel::onCurrentChange, // Call ViewModel function
                label = { Text("Current (A)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), // Use KeyboardType.Decimal
                enabled = uiState.currentEnabled, // Use state from ViewModel
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.resistanceStr,
                onValueChange = viewModel::onResistanceChange, // Call ViewModel function
                label = { Text("Resistance (Ω)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), // Use KeyboardType.Decimal
                enabled = uiState.resistanceEnabled, // Use state from ViewModel
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp)) // Consistent spacing

            OutlinedTextField(
                value = uiState.powerStr,
                onValueChange = viewModel::onPowerChange, // Call ViewModel function
                label = { Text("Power (W)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), // Use KeyboardType.Decimal
                enabled = uiState.powerEnabled, // Use state from ViewModel
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp)) // Space before error/buttons

            uiState.errorMessage?.let { error -> // Show error message from state
                Text(error, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = viewModel::calculate) { // Call ViewModel function
                    Text("Calculate")
                }
                Button(onClick = viewModel::clearInputs) { // Call ViewModel function
                    Text("Clear")
                }
            }
        } // Closes Column
    } // Closes Scaffold content lambda
} // Closes OhmsLawScreen composable

// Removed formatResult helper - it's now private in ViewModel

// @Preview(showBackground = true)
// @Composable
// fun OhmsLawScreenPreview() {
//     ElectricianAppNewTheme {
//         // Preview needs a way to provide a dummy ViewModel or use default state
//         // For simplicity, we can just call the composable, but interactions won't work fully
//         // OhmsLawScreen() // Preview needs NavController
//     }
// }
