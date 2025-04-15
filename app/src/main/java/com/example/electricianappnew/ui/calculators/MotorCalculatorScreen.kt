package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electricianappnew.ui.calculators.viewmodel.MotorCalculatorViewModel
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorCalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: MotorCalculatorViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState // Observe state directly
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState) // Make column scrollable
    ) {
        Text(
            text = "Motor Calculator (NEC Art. 430)",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )

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

        ResultRow("FLC (from NEC Tables):", uiState.flc, "A")
        ResultRow("Min. Conductor Ampacity (125% FLC):", uiState.minConductorAmpacity, "A")
        ResultRow("Max. Overload Size (FLA based):", uiState.maxOverloadAmps, "A")
        ResultRow("Max. Protection (% FLC):", uiState.maxProtectionPercent?.toDouble(), "%") // Format Int as Double
        ResultRow("Max. Protection (Amps):", uiState.maxProtectionAmps, "A")
        ResultRow("Std. Protection Size (Next Up):", uiState.standardBreakerFuseSize?.toDouble(), "A", isBold = true) // Format Int as Double


        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }

         Spacer(modifier = Modifier.height(16.dp))
         Button(onClick = viewModel::clearInputs, modifier = Modifier.align(Alignment.CenterHorizontally)) {
             Text("Clear / Reset")
         }
         Spacer(modifier = Modifier.height(16.dp)) // Add padding at the bottom
    }
}

// Reusing ResultRow from DwellingLoadScreen (ideally move to common)
@Composable
private fun ResultRow(label: String, value: Double?, unit: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (isBold) androidx.compose.ui.text.font.FontWeight.Bold else null)
        Text(
            text = value?.formatResult(1) ?: "N/A", // Use 1 decimal for Amps typically
            fontWeight = if (isBold) androidx.compose.ui.text.font.FontWeight.Bold else null
        )
        Text(unit, fontWeight = if (isBold) androidx.compose.ui.text.font.FontWeight.Bold else null)
    }
}

// Reusing formatResult (ideally move to common)
private fun Double.formatResult(decimals: Int = 1): String {
    return String.format("%.${decimals}f", this).trimEnd('0').trimEnd('.')
}


@Preview(showBackground = true, widthDp = 360)
@Composable
fun MotorCalculatorScreenPreview() {
    ElectricianAppNewTheme {
        MotorCalculatorScreen()
    }
}
