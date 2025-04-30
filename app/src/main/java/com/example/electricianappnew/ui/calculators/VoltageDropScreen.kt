package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons // Add import for Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Add import for Back Arrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
// LaunchedEffect import removed
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType // Ensure KeyboardType is imported
// Remove incorrect import: import androidx.compose.ui.text.input.KeyboardType.Companion.NumberDecimal
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // Import Hilt ViewModel
import androidx.navigation.NavController // Add NavController import
import com.example.electricianappnew.ui.calculators.viewmodel.VoltageDropViewModel // Import ViewModel
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
// Removed kotlin.math import as logic is in ViewModel
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import java.util.Locale
import com.example.electricianappnew.ui.common.formatCalculationResult
import android.util.Log
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Import collectAsStateWithLifecycle


@OptIn(ExperimentalMaterial3Api::class) // Already present, needed for TopAppBar too
@Composable
fun VoltageDropScreen(
    modifier: Modifier = Modifier,
    viewModel: VoltageDropViewModel = hiltViewModel(), // Inject ViewModel
    navController: NavController // Add NavController parameter
) {
    val uiState = viewModel.uiState // Observe UI state
    val wireSizes by viewModel.wireSizes.collectAsStateWithLifecycle() // Collect wireSizes flow
    val phases by viewModel.phases.collectAsStateWithLifecycle() // Collect phases flow
    val materials by viewModel.materials.collectAsStateWithLifecycle() // Collect materials flow
    val scrollState = rememberScrollState()
    val TAG = "VoltageDropScreen"

    // LaunchedEffect removed

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voltage Drop Calculator") },
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
                 .fillMaxWidth()
                 .verticalScroll(scrollState), // Make column scrollable
             horizontalAlignment = Alignment.CenterHorizontally
         ) {
             // Text("Voltage Drop Calculator", style = MaterialTheme.typography.headlineSmall) // Title is now in TopAppBar
            // Spacer(modifier = Modifier.height(16.dp)) // Adjust spacing if needed

            // Inputs
             OutlinedTextField(
                value = uiState.systemVoltageStr,
                onValueChange = viewModel::onSystemVoltageChange, // Use ViewModel
                label = { Text("System Voltage (V)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
             Spacer(modifier = Modifier.height(8.dp))
             ExposedDropdownMenuBoxInput(
                 label = "Phase",
                 options = phases, // Use collected phases state
                 selectedOption = uiState.selectedPhase,
                 onOptionSelected = viewModel::onPhaseChange, // Use ViewModel
                 modifier = Modifier.fillMaxWidth()
                 // Removed explicit zIndex
             )
             Spacer(modifier = Modifier.height(8.dp))
             Row(Modifier.fillMaxWidth()){
                 ExposedDropdownMenuBoxInput(
                     label = "Material",
                     options = materials, // Use collected materials state
                 selectedOption = uiState.selectedMaterial,
                 onOptionSelected = viewModel::onMaterialChange, // Use ViewModel
                 modifier = Modifier.weight(1f)
                 // Removed explicit zIndex
                 )
                 Spacer(Modifier.width(8.dp))
                 Log.d(TAG, "Wire Sizes before dropdown: $wireSizes") // Add logging here
                 ExposedDropdownMenuBoxInput(
                     label = "Size",
                     options = wireSizes, // Use collected wireSizes state
                     selectedOption = uiState.selectedSize,
                     onOptionSelected = viewModel::onSizeChange,
                     modifier = Modifier.weight(1f)
                     // Removed explicit zIndex
                 )
             }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.loadCurrentStr,
                onValueChange = viewModel::onLoadCurrentChange, // Use ViewModel
                label = { Text("Load Current (A)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), // Use Decimal
                modifier = Modifier.fillMaxWidth()
            )
             Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.distanceStr,
                onValueChange = viewModel::onDistanceChange, // Use ViewModel
                label = { Text("One-Way Distance (ft)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), // Use Decimal
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Results
            Text("Results:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Voltage Drop: ${uiState.voltageDropVolts?.formatCalculationResult() ?: "N/A"} V") // Use renamed shared helper
            Text(text = "Voltage Drop %: ${uiState.voltageDropPercent?.formatCalculationResult() ?: "N/A"} %") // Use renamed shared helper
            Text(text = "End Voltage: ${uiState.endVoltage?.formatCalculationResult() ?: "N/A"} V") // Use renamed shared helper


            uiState.errorMessage?.let { error -> // Use ViewModel state
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error) // Added text=
            }

             Spacer(modifier = Modifier.height(16.dp))
             Button(onClick = viewModel::clearInputs) { // Use ViewModel
                 Text("Clear / Reset")
             }
        } // Closes Column
    } // Closes Scaffold content lambda
} // Closes VoltageDropScreen composable

// Removed local formatVoltageDropResult - using shared formatCalculationResult

// Preview removed
