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
import com.example.electricianappnew.ui.calculators.viewmodel.LuminaireLayoutViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

@Composable
fun LuminaireLayoutScreen(
    modifier: Modifier = Modifier,
    viewModel: LuminaireLayoutViewModel = hiltViewModel()
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
            text = "Luminaire Layout Calculator (Lumen Method)",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )

        // --- Inputs ---
        Text("Room & Lighting Parameters", style = MaterialTheme.typography.titleMedium)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) // Fixed Divider

        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            OutlinedTextField(
                value = uiState.roomLengthStr,
                onValueChange = viewModel::onRoomLengthChanged,
                label = { Text("Length (ft)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = uiState.roomWidthStr,
                onValueChange = viewModel::onRoomWidthChanged,
                label = { Text("Width (ft)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }
         Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
             OutlinedTextField(
                 value = uiState.roomHeightStr,
                 onValueChange = viewModel::onRoomHeightChanged,
                 label = { Text("Ceiling Ht (ft)") },
                 keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                 modifier = Modifier.weight(1f)
             )
             Spacer(Modifier.width(8.dp))
             OutlinedTextField(
                 value = uiState.workPlaneHeightStr,
                 onValueChange = viewModel::onWorkPlaneHeightChanged,
                 label = { Text("Work Plane Ht (ft)") },
                 keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                 modifier = Modifier.weight(1f)
             )
         }

        OutlinedTextField(
            value = uiState.targetIlluminanceStr,
            onValueChange = viewModel::onTargetIlluminanceChanged,
            label = { Text("Target Illuminance (fc)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
         OutlinedTextField(
            value = uiState.lumensPerLuminaireStr,
            onValueChange = viewModel::onLumensPerLuminaireChanged,
            label = { Text("Lumens per Luminaire") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
         OutlinedTextField(
            value = uiState.coefficientOfUtilizationStr,
            onValueChange = viewModel::onCoefficientOfUtilizationChanged,
            label = { Text("Coefficient of Utilization (CU)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
         OutlinedTextField(
            value = uiState.lightLossFactorStr,
            onValueChange = viewModel::onLightLossFactorChanged,
            label = { Text("Light Loss Factor (LLF)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )


        Spacer(modifier = Modifier.height(24.dp))

        // --- Results ---
        Text("Results:", style = MaterialTheme.typography.titleMedium)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) // Fixed Divider
        Spacer(modifier = Modifier.height(8.dp))

        ResultRow("Room Area:", uiState.roomArea, "sq ft")
        ResultRow("Cavity Height:", uiState.cavityHeight, "ft")
        ResultRow("Room Cavity Ratio (RCR):", uiState.roomCavityRatio, "")
        ResultRow("Total Required Lumens:", uiState.requiredLumens, "lm")
        ResultRow("Calculated # Luminaires:", uiState.numLuminairesExact, "")
        Text( // Display rounded-up number separately and bold
            text = "Required # Luminaires (Rounded Up): ${uiState.numLuminairesActual ?: "N/A"}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
        // TODO: Add Spacing Calculation Display

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
            text = value?.formatResult(1) ?: "N/A",
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
fun LuminaireLayoutScreenPreview() {
    ElectricianAppNewTheme {
        LuminaireLayoutScreen()
    }
}
