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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController // Add NavController import
import com.example.electricianappnew.ui.calculators.viewmodel.MotorCalculatorViewModel
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import androidx.compose.material3.Scaffold // Import Scaffold
import androidx.compose.material3.TopAppBar // Import TopAppBar
import androidx.compose.ui.text.font.FontWeight // Import FontWeight
import java.util.Locale // Import Locale
import com.example.electricianappnew.ui.common.CalculationResultRow // Import shared
import com.example.electricianappnew.ui.common.formatCalculationResult // Import shared

@OptIn(ExperimentalMaterial3Api::class) // Already present, needed for TopAppBar too
@Composable
fun MotorCalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: MotorCalculatorViewModel = hiltViewModel(),
    navController: NavController // Add NavController parameter
) {
    val uiState = viewModel.uiState // Observe state directly
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Motor Calculator") },
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
            //     text = "Motor Calculator (NEC Art. 430)",
            //     style = MaterialTheme.typography.headlineMedium,
            //     modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
            // )

            // --- Inputs ---
            Text("Motor Information", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) // Fixed Divider

            OutlinedTextField(
                value = uiState.horsepowerStr,
                onValueChange = viewModel::onHorsepowerChanged,
                label = { Text("Motor HP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                ExposedDropdownMenuBoxInput(
                    label = "Phase",
                    options = uiState.phases.map { it.toString() }, // Convert Int list to String list
                    selectedOption = uiState.phase.toString(),
                    onOptionSelected = { viewModel.onPhaseChanged(it.toIntOrNull() ?: uiState.phase) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                ExposedDropdownMenuBoxInput(
                    label = "Voltage",
                    // Dynamically get voltages based on selected phase
                    options = (uiState.standardVoltages[uiState.phase] ?: emptyList()).map { it.toString() },
                    selectedOption = uiState.voltageStr,
                    onOptionSelected = viewModel::onVoltageChanged,
                    modifier = Modifier.weight(1f)
                )
            }

            ExposedDropdownMenuBoxInput(
                label = "Motor Type",
                options = uiState.motorTypes,
                selectedOption = uiState.motorType,
                onOptionSelected = viewModel::onMotorTypeChanged,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Overload Protection Info", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) // Fixed Divider

            OutlinedTextField(
                value = uiState.flaStr,
                onValueChange = viewModel::onFlaChanged,
                label = { Text("Nameplate FLA (Amps)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                 OutlinedTextField(
                    value = uiState.serviceFactorStr,
                    onValueChange = viewModel::onServiceFactorChanged,
                    label = { Text("Service Factor (e.g., 1.15)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                     enabled = uiState.tempRiseStr.isBlank() // Disable if Temp Rise is entered
                )
                 Spacer(Modifier.width(8.dp))
                 OutlinedTextField(
                    value = uiState.tempRiseStr,
                    onValueChange = viewModel::onTempRiseChanged,
                    label = { Text("Temp Rise (°C)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                     enabled = uiState.serviceFactorStr.isBlank() // Disable if SF is entered
                )
            }

             Spacer(modifier = Modifier.height(16.dp))
             Text("Branch-Circuit Protection Info", style = MaterialTheme.typography.titleMedium)
             HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) // Fixed Divider

             ExposedDropdownMenuBoxInput(
                label = "Protection Device Type",
                options = uiState.protectionDeviceTypes,
                selectedOption = uiState.protectionDeviceType,
                onOptionSelected = viewModel::onProtectionDeviceTypeChanged,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )


            Spacer(modifier = Modifier.height(24.dp))

            // --- Results ---
            Text("Calculated Values:", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) // Fixed Divider
            Spacer(modifier = Modifier.height(8.dp))

            CalculationResultRow("FLC (from NEC Tables):", uiState.flc, "A") // Use renamed shared
            CalculationResultRow("Min. Conductor Ampacity (125% FLC):", uiState.minConductorAmpacity, "A") // Use renamed shared
            CalculationResultRow("Max. Overload Size (FLA based):", uiState.maxOverloadAmps, "A") // Use renamed shared
            CalculationResultRow("Max. Protection (% FLC):", uiState.maxProtectionPercent?.toDouble(), "%") // Use renamed shared, Format Int as Double
            CalculationResultRow("Max. Protection (Amps):", uiState.maxProtectionAmps, "A") // Use renamed shared
            CalculationResultRow("Std. Protection Size (Next Up):", uiState.standardBreakerFuseSize?.toDouble(), "A", isBold = true) // Use renamed shared, Format Int as Double


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
} // Closes MotorCalculatorScreen composable

// All local helpers confirmed removed
