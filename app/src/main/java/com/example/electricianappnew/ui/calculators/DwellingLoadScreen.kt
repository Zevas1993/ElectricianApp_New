package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.electricianappnew.ui.calculators.viewmodel.DwellingLoadViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

@OptIn(ExperimentalMaterial3Api::class) // Needed for TopAppBar
@Composable
fun DwellingLoadScreen(
    modifier: Modifier = Modifier,
    viewModel: DwellingLoadViewModel = hiltViewModel(),
    navController: NavController // Add NavController parameter
) {
    val uiState = viewModel.uiState // Observe state directly

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Dwelling Load Calculator (Standard Method)",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )

        LazyColumn(modifier = Modifier.weight(1f)) { // Make content scrollable

            // --- Inputs ---
            item { InputSectionHeader("General Loads") }
            item {
                LoadInputRow(
                    label = "Floor Area (sq ft)",
                    value = uiState.squareFeetStr,
                    onValueChange = viewModel::onSquareFeetChanged,
                    keyboardType = KeyboardType.Number
                )
            }
            item {
                LoadInputRow(
                    label = "Small Appliance Circuits (min 2)",
                    value = uiState.numSmallApplianceCircuitsStr,
                    onValueChange = viewModel::onNumSmallAppliancesChanged,
                    keyboardType = KeyboardType.Number
                )
            }
            item {
                LoadInputRow(
                    label = "Laundry Circuits (min 1)",
                    value = uiState.numLaundryCircuitsStr,
                    onValueChange = viewModel::onNumLaundryCircuitsChanged,
                    keyboardType = KeyboardType.Number
                )
            }

            item { InputSectionHeader("Fixed Appliances (Excl. Dryer, Range, AC, Heat)") }
            item {
                LoadInputRow(
                    label = "Number of Fixed Appliances",
                    value = uiState.numFixedAppliancesStr,
                    onValueChange = viewModel::onNumFixedAppliancesChanged,
                    keyboardType = KeyboardType.Number
                )
            }
             item {
                LoadInputRow(
                    label = "Total Fixed Appliance VA",
                    value = uiState.fixedAppliancesVaStr,
                    onValueChange = viewModel::onFixedAppliancesVaChanged,
                    keyboardType = KeyboardType.Decimal // Allow decimal for VA
                )
            }

            item { InputSectionHeader("Major Loads") }
             item {
                LoadInputRow(
                    label = "Dryer VA (min 5000)",
                    value = uiState.dryerVaStr,
                    onValueChange = viewModel::onDryerVaChanged,
                    keyboardType = KeyboardType.Decimal
                )
            }
             item {
                LoadInputRow(
                    label = "Range VA (Nameplate)",
                    value = uiState.rangeVaStr,
                    onValueChange = viewModel::onRangeVaChanged,
                    keyboardType = KeyboardType.Decimal
                )
            }
             item {
                LoadInputRow(
                    label = "Air Conditioning VA",
                    value = uiState.acVaStr,
                    onValueChange = viewModel::onAcVaChanged,
                    keyboardType = KeyboardType.Decimal
                )
            }
             item {
                LoadInputRow(
                    label = "Heating VA",
                    value = uiState.heatVaStr,
                    onValueChange = viewModel::onHeatVaChanged,
                    keyboardType = KeyboardType.Decimal
                )
            }
            // TODO: Add input for Largest Motor if needed for 220.50

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // --- Results ---
            item {
                Text("Calculated Load Breakdown:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                ResultRow("General Lighting Load:", uiState.generalLightingLoadVa, "VA")
                ResultRow("Small Appliance & Laundry Load:", uiState.applianceLaundryLoadVa, "VA")
                ResultRow("Subtotal (Lighting/App/Laundry):", uiState.totalLightingApplianceLaundryVa, "VA")
                ResultRow("Demand Applied (Table 220.42):", uiState.demandAppliedLightingApplianceLaundryVa, "VA")
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) // Replaced deprecated Divider
                ResultRow("Fixed Appliance Demand (220.53):", uiState.fixedApplianceDemandVa, "VA")
                ResultRow("Dryer Demand (220.54):", uiState.dryerDemandVa, "VA")
                ResultRow("Range Demand (Table 220.55):", uiState.rangeDemandVa, "VA")
                ResultRow("AC / Heat Demand (Largest, 220.60):", uiState.acHeatDemandVa, "VA")
                // ResultRow("Largest Motor (25%):", uiState.largestMotorVa, "VA") // Add later
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) // Replaced deprecated Divider
                ResultRow("Total Calculated Load:", uiState.calculatedServiceLoadVa, "VA", isBold = true)
                ResultRow("Load (Amps @ 240V):", uiState.calculatedServiceLoadAmps, "A", isBold = true)


                uiState.errorMessage?.let { error ->
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
private fun InputSectionHeader(title: String) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        HorizontalDivider() // Replaced deprecated Divider
        Spacer(modifier = Modifier.height(8.dp))
    }
}


@Composable
private fun LoadInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        singleLine = true
    )
}

@Composable
private fun ResultRow(label: String, value: Double?, unit: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (isBold) androidx.compose.ui.text.font.FontWeight.Bold else null)
        Text(
            text = value?.formatResult(0) ?: "N/A",
            fontWeight = if (isBold) androidx.compose.ui.text.font.FontWeight.Bold else null
        )
        Text(unit, fontWeight = if (isBold) androidx.compose.ui.text.font.FontWeight.Bold else null)
    }
}


// Helper to format results nicely
private fun Double.formatResult(decimals: Int = 0): String {
    return String.format("%.${decimals}f", this)
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun DwellingLoadScreenPreview() {
    ElectricianAppNewTheme {
        DwellingLoadScreen()
    }
}
