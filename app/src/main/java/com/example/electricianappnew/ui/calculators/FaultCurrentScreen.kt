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
import com.example.electricianappnew.ui.calculators.viewmodel.FaultCurrentViewModel
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaultCurrentScreen(
    modifier: Modifier = Modifier,
    viewModel: FaultCurrentViewModel = hiltViewModel()
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
            text = "Fault Current Calculator (Transformer Impedance Method)",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )

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

        ResultRow("Secondary FLA:", uiState.secondaryFla, "A")
        ResultRow("Available Fault Current (AFC):", uiState.faultCurrentAmps, "A", isBold = true)


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

// Reusing ResultRow (ideally move to common)
@Composable
private fun ResultRow(label: String, value: Double?, unit: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (isBold) androidx.compose.ui.text.font.FontWeight.Bold else null)
        Text(
            text = value?.formatResult(0) ?: "N/A", // Show whole amps for AFC/FLA
            fontWeight = if (isBold) androidx.compose.ui.text.font.FontWeight.Bold else null
        )
        Text(unit, fontWeight = if (isBold) androidx.compose.ui.text.font.FontWeight.Bold else null)
    }
}

// Reusing formatResult (ideally move to common)
private fun Double.formatResult(decimals: Int = 0): String {
    return String.format("%.${decimals}f", this)
}


@Preview(showBackground = true, widthDp = 360)
@Composable
fun FaultCurrentScreenPreview() {
    ElectricianAppNewTheme {
        FaultCurrentScreen()
    }
}
