package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons // Add import for Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Add import for Back Arrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
// Removed kotlin.math import as logic is in ViewModel

// TODO: Replace with actual data loading/lookup
object NecConductorData {
    // Simplified placeholder data - Needs real NEC Chapter 9, Table 8 data
    // Resistance (ohms per kFT or per meter) and Circular Mils (CM)
    data class ConductorProperties(val resistancePer1000ft: Double, val cm: Double)

    fun getProperties(material: String, size: String): ConductorProperties? {
        // VERY simplified - needs actual table lookup
        return when {
            material == "Copper" && size == "14 AWG" -> ConductorProperties(resistancePer1000ft = 3.14, cm = 4110.0)
            material == "Copper" && size == "12 AWG" -> ConductorProperties(resistancePer1000ft = 1.98, cm = 6530.0)
            material == "Copper" && size == "10 AWG" -> ConductorProperties(resistancePer1000ft = 1.24, cm = 10380.0)
            material == "Aluminum" && size == "12 AWG" -> ConductorProperties(resistancePer1000ft = 3.18, cm = 6530.0) // Example Al
             // Add many more entries...
            else -> null
        }
    }

    // K factor (approximate resistivity) - can also be derived from Table 8
    // K = R * CM / 1000
    // For Copper: ~12.9 ohm-cmil/ft @ 75C
    // For Aluminum: ~21.2 ohm-cmil/ft @ 75C
    fun getKFactor(material: String): Double? {
        return when (material) {
            "Copper" -> 12.9
            "Aluminum" -> 21.2
            else -> null
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class) // Already present, needed for TopAppBar too
@Composable
fun VoltageDropScreen(
    modifier: Modifier = Modifier,
    viewModel: VoltageDropViewModel = hiltViewModel(), // Inject ViewModel
    navController: NavController // Add NavController parameter
) {
    val uiState = viewModel.uiState // Observe state

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
                .fillMaxWidth(),
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
             options = uiState.phases, // Use ViewModel state
             selectedOption = uiState.selectedPhase,
             onOptionSelected = viewModel::onPhaseChange, // Use ViewModel
             modifier = Modifier.fillMaxWidth()
         )
         Spacer(modifier = Modifier.height(8.dp))
         Row(Modifier.fillMaxWidth()){
             ExposedDropdownMenuBoxInput(
                 label = "Material",
                 options = uiState.materials, // Use ViewModel state
                 selectedOption = uiState.selectedMaterial,
                 onOptionSelected = viewModel::onMaterialChange, // Use ViewModel
                 modifier = Modifier.weight(1f)
             )
             Spacer(Modifier.width(8.dp))
             ExposedDropdownMenuBoxInput(
                 label = "Size",
                 options = viewModel.wireSizes, // Access directly from ViewModel
                 selectedOption = uiState.selectedSize,
                 onOptionSelected = viewModel::onSizeChange, // Use ViewModel
                 modifier = Modifier.weight(1f)
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
        Text("Voltage Drop: ${uiState.voltageDropVolts?.formatResult() ?: "N/A"} V") // Use ViewModel state
        Text("Voltage Drop %: ${uiState.voltageDropPercent?.formatResult() ?: "N/A"} %") // Use ViewModel state
        Text("End Voltage: ${uiState.endVoltage?.formatResult() ?: "N/A"} V") // Use ViewModel state


        uiState.errorMessage?.let { error -> // Use ViewModel state
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }

         Spacer(modifier = Modifier.height(16.dp))
         Button(onClick = viewModel::clearInputs) { // Use ViewModel
             Text("Clear / Reset")
         }
    }
}

// Helper to format results nicely (can be shared or moved)
private fun Double.formatResult(decimals: Int = 2): String { // Added decimals parameter
    return String.format("%.${decimals}f", this).trimEnd('0').trimEnd('.')
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun VoltageDropScreenPreview() {
    ElectricianAppNewTheme {
        VoltageDropScreen() // Preview uses default ViewModel state
    }
}
