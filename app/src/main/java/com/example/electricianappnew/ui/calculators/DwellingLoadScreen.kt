package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.electricianappnew.ui.calculators.viewmodel.DwellingLoadViewModel // Added explicit import
import java.util.Locale // Added Locale import
import com.example.electricianappnew.ui.common.CalculationResultRow // Import shared
import com.example.electricianappnew.ui.common.InputSectionHeader // Import shared
import com.example.electricianappnew.ui.common.LoadInputRow // Import shared
import com.example.electricianappnew.ui.common.formatCalculationResult // Import shared

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DwellingLoadScreen(
    modifier: Modifier = Modifier,
    viewModel: DwellingLoadViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dwelling Load Calculator") },
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
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    InputSectionHeader("General Loads")
                    LoadInputRow(
                        label = "Floor Area (sq ft)",
                        value = uiState.squareFeetStr,
                        onValueChange = viewModel::onSquareFeetChanged,
                        keyboardType = KeyboardType.Number
                    )
                    LoadInputRow(
                        label = "Small Appliance Circuits (min 2)",
                        value = uiState.numSmallApplianceCircuitsStr,
                        onValueChange = viewModel::onNumSmallAppliancesChanged,
                        keyboardType = KeyboardType.Number
                    )
                    LoadInputRow(
                        label = "Laundry Circuits (min 1)",
                        value = uiState.numLaundryCircuitsStr,
                        onValueChange = viewModel::onNumLaundryCircuitsChanged,
                        keyboardType = KeyboardType.Number
                    )

                    InputSectionHeader("Fixed Appliances (Excl. Dryer, Range, AC, Heat)")
                    LoadInputRow(
                        label = "Number of Fixed Appliances",
                        value = uiState.numFixedAppliancesStr,
                        onValueChange = viewModel::onNumFixedAppliancesChanged,
                        keyboardType = KeyboardType.Number
                    )
                    LoadInputRow(
                        label = "Total Fixed Appliance VA",
                        value = uiState.fixedAppliancesVaStr,
                        onValueChange = viewModel::onFixedAppliancesVaChanged,
                        keyboardType = KeyboardType.Decimal
                    )

                    InputSectionHeader("Major Loads")
                    LoadInputRow(
                        label = "Dryer VA (min 5000)",
                        value = uiState.dryerVaStr,
                        onValueChange = viewModel::onDryerVaChanged,
                        keyboardType = KeyboardType.Decimal
                    )
                    LoadInputRow(
                        label = "Range VA (Nameplate)",
                        value = uiState.rangeVaStr,
                        onValueChange = viewModel::onRangeVaChanged,
                        keyboardType = KeyboardType.Decimal
                    )
                    LoadInputRow(
                        label = "Air Conditioning VA",
                        value = uiState.acVaStr,
                        onValueChange = viewModel::onAcVaChanged,
                        keyboardType = KeyboardType.Decimal
                    )
                    LoadInputRow(
                        label = "Heating VA",
                        value = uiState.heatVaStr,
                        onValueChange = viewModel::onHeatVaChanged,
                        keyboardType = KeyboardType.Decimal
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- Results ---
                    Text("Calculated Load Breakdown:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column {
                        CalculationResultRow("General Lighting Load:", uiState.generalLightingLoadVa, "VA") // Use renamed shared
                        CalculationResultRow("Small Appliance & Laundry Load:", uiState.applianceLaundryLoadVa, "VA") // Use renamed shared
                        CalculationResultRow("Subtotal (Lighting/App/Laundry):", uiState.totalLightingApplianceLaundryVa, "VA") // Use renamed shared
                        CalculationResultRow("Demand Applied (Table 220.42):", uiState.demandAppliedLightingApplianceLaundryVa, "VA") // Use renamed shared
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        CalculationResultRow("Fixed Appliance Demand (220.53):", uiState.fixedApplianceDemandVa, "VA") // Use renamed shared
                        CalculationResultRow("Dryer Demand (220.54):", uiState.dryerDemandVa, "VA") // Use renamed shared
                        CalculationResultRow("Range Demand (Table 220.55):", uiState.rangeDemandVa, "VA") // Use renamed shared
                        CalculationResultRow("AC / Heat Demand (Largest, 220.60):", uiState.acHeatDemandVa, "VA") // Use renamed shared
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        CalculationResultRow("Total Calculated Load:", uiState.calculatedServiceLoadVa, "VA", isBold = true) // Use renamed shared
                        CalculationResultRow("Load (Amps @ 240V):", uiState.calculatedServiceLoadAmps, "A", isBold = true) // Use renamed shared

                        uiState.errorMessage?.let { error ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = error, color = MaterialTheme.colorScheme.error) // Added text=
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = viewModel::clearInputs,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Clear / Reset")
                    }
                }
            }
        } // Closes Column
    } // Closes Scaffold content lambda
} // Closes DwellingLoadScreen composable

// All local helpers confirmed removed
