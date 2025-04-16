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
import com.example.electricianappnew.ui.calculators.viewmodel.LuminaireLayoutViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import androidx.compose.material3.Scaffold // Import Scaffold
import androidx.compose.material3.TopAppBar // Import TopAppBar
import androidx.compose.ui.text.font.FontWeight // Import FontWeight
import java.util.Locale // Import Locale
import com.example.electricianappnew.ui.common.CalculationResultRow // Import shared
import com.example.electricianappnew.ui.common.formatCalculationResult // Import shared

@OptIn(ExperimentalMaterial3Api::class) // Needed for TopAppBar
@Composable
fun LuminaireLayoutScreen(
    modifier: Modifier = Modifier,
    viewModel: LuminaireLayoutViewModel = hiltViewModel(),
    navController: NavController // Add NavController parameter
) {
    val uiState = viewModel.uiState // Observe state directly
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Luminaire Layout") },
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
            //     text = "Luminaire Layout Calculator (Lumen Method)",
            //     style = MaterialTheme.typography.headlineMedium,
            //     modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
            // )

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

            CalculationResultRow("Room Area:", uiState.roomArea, "sq ft") // Use renamed shared
            CalculationResultRow("Cavity Height:", uiState.cavityHeight, "ft") // Use renamed shared
            CalculationResultRow("Room Cavity Ratio (RCR):", uiState.roomCavityRatio, "") // Use renamed shared
            CalculationResultRow("Total Required Lumens:", uiState.requiredLumens, "lm") // Use renamed shared
            CalculationResultRow("Calculated # Luminaires:", uiState.numLuminairesExact, "") // Use renamed shared
            Text( // Display rounded-up number separately and bold
                text = "Required # Luminaires (Rounded Up): ${uiState.numLuminairesActual ?: "N/A"}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
            // TODO: Add Spacing Calculation Display

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
} // Closes LuminaireLayoutScreen composable

// All local helpers confirmed removed
