package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons // Add import for Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Add import for Back Arrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
// Removed unused import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController // Add NavController import
import com.example.electricianappnew.ui.calculators.viewmodel.FaultCurrentViewModel
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput
// Removed unused import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import androidx.compose.material3.Scaffold // Import Scaffold
import androidx.compose.material3.TopAppBar // Import TopAppBar
import androidx.compose.ui.text.font.FontWeight // Import FontWeight
import java.util.Locale // Import Locale
import com.example.electricianappnew.ui.common.CalculationResultRow // Import shared
import com.example.electricianappnew.ui.common.formatCalculationResult // Import shared

@OptIn(ExperimentalMaterial3Api::class) // Already present, needed for TopAppBar too
@Composable
fun FaultCurrentScreen(
    modifier: Modifier = Modifier,
    viewModel: FaultCurrentViewModel = hiltViewModel(),
    navController: NavController // Add NavController parameter
) {
    val uiState = viewModel.uiState // Observe state directly
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fault Current Calculator") },
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
                .verticalScroll(scrollState) // Make column scrollable
        ) {
            // Text( // Title is now in TopAppBar
            //     text = "Fault Current Calculator (Transformer Impedance Method)",
            //     style = MaterialTheme.typography.headlineMedium,
            //     modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
            // )

            // --- Inputs ---
            Text("Transformer Information", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) // Fixed Divider

            OutlinedTextField(
                value = uiState.transformerKvaStr,
                onValueChange = viewModel::onKvaChanged,
                label = { Text("Transformer kVA") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                 ExposedDropdownMenuBoxInput(
                     label = "Phase",
                     options = uiState.phases,
                     selectedOption = uiState.selectedPhase,
                     onOptionSelected = viewModel::onPhaseChanged,
                     modifier = Modifier.weight(1f)
                 )
                 Spacer(Modifier.width(8.dp))
                 OutlinedTextField(
                    value = uiState.transformerVoltageStr, // Correct state variable name
                    onValueChange = viewModel::onVoltageChanged,
                    label = { Text("Secondary Voltage (V)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = uiState.transformerImpedancePercentStr,
                onValueChange = viewModel::onImpedanceChanged,
                label = { Text("Transformer Impedance (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            // TODO: Add inputs for Point-to-Point method later (Conductor R/X, Length)

            Spacer(modifier = Modifier.height(24.dp))

            // --- Results ---
            Text("Results:", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) // Fixed Divider
            Spacer(modifier = Modifier.height(8.dp))

            CalculationResultRow("Secondary FLA:", uiState.secondaryFla, "A") // Use renamed shared
            CalculationResultRow("Available Fault Current (AFC):", uiState.faultCurrentAmps, "A", isBold = true) // Use renamed shared


            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error) // Added text=
            }

             Spacer(modifier = Modifier.height(16.dp))
             Button(onClick = viewModel::clearInputs, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                 Text("Clear / Reset")
             }
             Spacer(modifier = Modifier.height(16.dp)) // Add padding at the bottom
        } // Closes Column
    } // Closes Scaffold content lambda
} // Closes FaultCurrentScreen composable

// Removed local FaultCurrentResultRow - using shared CalculationResultRow
// Removed local formatFaultCurrentResult - using shared formatCalculationResult

// Preview removed
