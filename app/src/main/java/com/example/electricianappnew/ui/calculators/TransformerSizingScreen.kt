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
import com.example.electricianappnew.ui.calculators.viewmodel.TransformerSizingViewModel
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import kotlin.math.sqrt // Keep sqrt
import androidx.compose.material3.Scaffold // Import Scaffold
import androidx.compose.material3.TopAppBar // Import TopAppBar
import androidx.compose.ui.text.font.FontWeight // Import FontWeight
import java.util.Locale // Import Locale
import com.example.electricianappnew.ui.common.CalculationResultRow // Import shared
import com.example.electricianappnew.ui.common.formatCalculationResult // Import shared

@OptIn(ExperimentalMaterial3Api::class) // Already present, needed for TopAppBar too
@Composable
fun TransformerSizingScreen(
    modifier: Modifier = Modifier,
    viewModel: TransformerSizingViewModel = hiltViewModel(),
    navController: NavController // Add NavController parameter
) {
    val uiState = viewModel.uiState // Observe state directly
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transformer Sizing") },
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
            //     text = "Transformer Sizing Calculator (NEC Art. 450)",
            //     style = MaterialTheme.typography.headlineMedium,
            //     modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
            // )

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

            CalculationResultRow("Calculated Load:", uiState.calculatedKva, "kVA") // Use renamed shared
            CalculationResultRow("Standard kVA Size:", uiState.standardKva, "kVA", isBold = true) // Use renamed shared
            val effectiveKvaStr = uiState.standardKva?.formatCalculationResult(1) ?: uiState.calculatedKva?.formatCalculationResult(1) ?: "?" // Use renamed shared helper
            CalculationResultRow("Primary FLA (@$effectiveKvaStr kVA):", uiState.primaryFla, "A") // Use renamed shared
            CalculationResultRow("Secondary FLA (@$effectiveKvaStr kVA):", uiState.secondaryFla, "A") // Use renamed shared
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) // Fixed Divider
            CalculationResultRow("Max Primary OCPD Amps:", uiState.primaryOcpdMaxAmps, "A") // Use renamed shared
            CalculationResultRow("Std Primary OCPD Size:", uiState.primaryOcpdStandardSize?.toDouble(), "A", isBold = true) // Use renamed shared, Format Int as Double
            if (!uiState.primaryProtectionOnly || (uiState.primaryOcpdMaxAmps != null && uiState.primaryFla != null && uiState.primaryOcpdMaxAmps > uiState.primaryFla!! * 1.25)) {
                 CalculationResultRow("Max Secondary OCPD Amps:", uiState.secondaryOcpdMaxAmps, "A") // Use renamed shared
                 CalculationResultRow("Std Secondary OCPD Size:", uiState.secondaryOcpdStandardSize?.toDouble(), "A", isBold = true) // Use renamed shared, Format Int as Double
            } else {
                 Text(text = "(Secondary OCPD likely not required per 450.3(B))", style = MaterialTheme.typography.bodySmall) // Added text=
            }


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
} // Closes TransformerSizingScreen composable

// All local helpers confirmed removed
