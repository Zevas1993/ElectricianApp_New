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
import com.example.electricianappnew.ui.calculators.viewmodel.TransformerSizingViewModel
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import kotlin.math.sqrt // Keep sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransformerSizingScreen(
    modifier: Modifier = Modifier,
    viewModel: TransformerSizingViewModel = hiltViewModel()
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
            text = "Transformer Sizing Calculator (NEC Art. 450)",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )

        // --- Inputs ---
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
             ExposedDropdownMenuBoxInput(
                 label = "Phase",
                 options = uiState.phases,
                 selectedOption = uiState.selectedPhase,
                 onOptionSelected = viewModel::onPhaseChanged,
                 modifier = Modifier.weight(1f)
             )
             Spacer(Modifier.width(8.dp))
             ExposedDropdownMenuBoxInput(
                 label = "Input Load As",
                 options = uiState.inputModes,
                 selectedOption = uiState.inputMode,
                 onOptionSelected = viewModel::onInputModeChanged,
                 modifier = Modifier.weight(1f)
             )
        }

        if (uiState.inputMode == "kVA") {
            OutlinedTextField(
                value = uiState.loadKvaStr,
                onValueChange = viewModel::onLoadKvaChanged,
                label = { Text("Load (kVA)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        } else {
            OutlinedTextField(
                value = uiState.loadAmpsStr,
                onValueChange = viewModel::onLoadAmpsChanged,
                label = { Text("Load (Amps @ Secondary)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }

         OutlinedTextField(
            value = uiState.primaryVoltageStr,
            onValueChange = viewModel::onPrimaryVoltageChanged,
            label = { Text("Primary Voltage (V)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        OutlinedTextField(
            value = uiState.secondaryVoltageStr,
            onValueChange = viewModel::onSecondaryVoltageChanged,
            label = { Text("Secondary Voltage (V)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Primary Protection Only?", modifier = Modifier.weight(1f))
            Switch(
                checked = uiState.primaryProtectionOnly,
                onCheckedChange = viewModel::onPrimaryProtectionOnlyChanged
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        // --- Results ---
        Text("Results:", style = MaterialTheme.typography.titleMedium)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) // Fixed Divider
        Spacer(modifier = Modifier.height(8.dp))

        ResultRow("Calculated Load:", uiState.calculatedKva, "kVA")
        ResultRow("Standard kVA Size:", uiState.standardKva, "kVA", isBold = true)
        val effectiveKvaStr = uiState.standardKva?.formatResult(1) ?: uiState.calculatedKva?.formatResult(1) ?: "?"
        ResultRow("Primary FLA (@$effectiveKvaStr kVA):", uiState.primaryFla, "A")
        ResultRow("Secondary FLA (@$effectiveKvaStr kVA):", uiState.secondaryFla, "A")
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) // Fixed Divider
        ResultRow("Max Primary OCPD Amps:", uiState.primaryOcpdMaxAmps, "A")
        ResultRow("Std Primary OCPD Size:", uiState.primaryOcpdStandardSize?.toDouble(), "A", isBold = true) // Format Int as Double
        if (!uiState.primaryProtectionOnly || (uiState.primaryOcpdMaxAmps != null && uiState.primaryFla != null && uiState.primaryOcpdMaxAmps > uiState.primaryFla!! * 1.25)) {
             ResultRow("Max Secondary OCPD Amps:", uiState.secondaryOcpdMaxAmps, "A")
             ResultRow("Std Secondary OCPD Size:", uiState.secondaryOcpdStandardSize?.toDouble(), "A", isBold = true) // Format Int as Double
        } else {
             Text("(Secondary OCPD likely not required per 450.3(B))", style = MaterialTheme.typography.bodySmall)
        }


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
            text = value?.formatResult(1) ?: "N/A", // Use 1 decimal for Amps/kVA typically
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
fun TransformerSizingScreenPreview() {
    ElectricianAppNewTheme {
        TransformerSizingScreen()
    }
}
