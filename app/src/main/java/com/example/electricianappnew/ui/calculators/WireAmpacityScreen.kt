package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons // Add import for Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Add import for Back Arrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType // Ensure KeyboardType is imported
// Remove incorrect import: import androidx.compose.ui.text.input.KeyboardType.Companion.NumberDecimal
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // Import Hilt ViewModel
import androidx.navigation.NavController // Add NavController import
import com.example.electricianappnew.ui.calculators.viewmodel.WireAmpacityViewModel // Import ViewModel
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import androidx.compose.foundation.text.KeyboardOptions // Import KeyboardOptions
import androidx.compose.material3.Scaffold // Import Scaffold
import androidx.compose.material3.TopAppBar // Import TopAppBar
import java.util.Locale // Import Locale
import androidx.compose.foundation.verticalScroll // Import verticalScroll
import androidx.compose.foundation.rememberScrollState // Import rememberScrollState
import com.example.electricianappnew.ui.common.formatCalculationResult // Import shared

// TODO: Replace with actual data loading/lookup from NEC tables (e.g., Room DB, Assets)
object NecAmpacityData {
    // Expanded placeholder data - Based loosely on NEC 310.16 (verify against actual code)
    private val copperAmpacity = mapOf(
        "14 AWG" to mapOf(60 to 15.0, 75 to 20.0, 90 to 25.0),
        "12 AWG" to mapOf(60 to 20.0, 75 to 25.0, 90 to 30.0),
        "10 AWG" to mapOf(60 to 30.0, 75 to 35.0, 90 to 40.0),
        "8 AWG" to mapOf(60 to 40.0, 75 to 50.0, 90 to 55.0),
        "6 AWG" to mapOf(60 to 55.0, 75 to 65.0, 90 to 75.0),
        "4 AWG" to mapOf(60 to 70.0, 75 to 85.0, 90 to 95.0),
        "2 AWG" to mapOf(60 to 95.0, 75 to 115.0, 90 to 130.0),
        "1 AWG" to mapOf(60 to 110.0, 75 to 130.0, 90 to 150.0),
        "1/0" to mapOf(60 to 125.0, 75 to 150.0, 90 to 170.0),
        "2/0" to mapOf(60 to 145.0, 75 to 175.0, 90 to 195.0),
        "3/0" to mapOf(60 to 165.0, 75 to 200.0, 90 to 225.0),
        "4/0" to mapOf(60 to 195.0, 75 to 230.0, 90 to 260.0),
        "250 kcmil" to mapOf(60 to 215.0, 75 to 255.0, 90 to 290.0),
        // Add more kcmil sizes...
    )
    private val aluminumAmpacity = mapOf(
         "12 AWG" to mapOf(60 to 15.0, 75 to 20.0, 90 to 25.0), // Example starts at #12 for Al
         "10 AWG" to mapOf(60 to 25.0, 75 to 30.0, 90 to 35.0),
         "8 AWG" to mapOf(60 to 30.0, 75 to 40.0, 90 to 45.0),
         "6 AWG" to mapOf(60 to 40.0, 75 to 50.0, 90 to 60.0),
         "4 AWG" to mapOf(60 to 55.0, 75 to 65.0, 90 to 75.0),
         "2 AWG" to mapOf(60 to 75.0, 75 to 90.0, 90 to 100.0),
         "1 AWG" to mapOf(60 to 85.0, 75 to 100.0, 90 to 115.0),
         "1/0" to mapOf(60 to 100.0, 75 to 120.0, 90 to 135.0),
         "2/0" to mapOf(60 to 115.0, 75 to 135.0, 90 to 155.0),
         "3/0" to mapOf(60 to 130.0, 75 to 155.0, 90 to 175.0),
         "4/0" to mapOf(60 to 150.0, 75 to 180.0, 90 to 205.0),
         "250 kcmil" to mapOf(60 to 170.0, 75 to 205.0, 90 to 230.0),
         // Add more kcmil sizes...
    )

    fun getBaseAmpacity(material: String, size: String, tempRating: Int): Double? {
        val table = if (material == "Copper") copperAmpacity else aluminumAmpacity
        return table[size]?.get(tempRating)
    }

    // Expanded placeholder - Based loosely on NEC Table 310.15(B)(1) (verify!)
    fun getTemperatureCorrectionFactor(ambientTempC: Double, tempRating: Int): Double? {
         // Based on 30°C ambient base
         return when (tempRating) {
             60 -> when {
                 ambientTempC <= 25 -> 1.08; ambientTempC <= 30 -> 1.00; ambientTempC <= 35 -> 0.91;
                 ambientTempC <= 40 -> 0.82; ambientTempC <= 45 -> 0.71; else -> 0.58 // Approx for 50C
             }
             75 -> when {
                 ambientTempC <= 25 -> 1.05; ambientTempC <= 30 -> 1.00; ambientTempC <= 35 -> 0.94;
                 ambientTempC <= 40 -> 0.88; ambientTempC <= 45 -> 0.82; else -> 0.75 // Approx for 50C
             }
             90 -> when {
                 ambientTempC <= 25 -> 1.04; ambientTempC <= 30 -> 1.00; ambientTempC <= 35 -> 0.96;
                 ambientTempC <= 40 -> 0.91; ambientTempC <= 45 -> 0.87; else -> 0.82 // Approx for 50C
             }
             else -> null
         }
    }

     // Expanded placeholder - Based on NEC Table 310.15(C)(1)
    fun getConductorAdjustmentFactor(numConductors: Int): Double? {
        return when (numConductors) {
            in 1..3 -> 1.0 // Technically not an adjustment, but represents 100%
            in 4..6 -> 0.8
            in 7..9 -> 0.7
            in 10..20 -> 0.5
            in 21..30 -> 0.45
            in 31..40 -> 0.40
            else -> 0.35 // 41 and above
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class) // Already present, needed for TopAppBar too
@Composable
fun WireAmpacityScreen(
    modifier: Modifier = Modifier,
    viewModel: WireAmpacityViewModel = hiltViewModel(), // Inject ViewModel
    navController: NavController // Add NavController parameter
) {
    val uiState = viewModel.uiState // Observe state
    val scrollState = rememberScrollState() // Add scroll state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wire Ampacity Calculator") },
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
            // Text("Wire Ampacity Calculator", style = MaterialTheme.typography.headlineSmall) // Title is now in TopAppBar
            // Spacer(modifier = Modifier.height(16.dp)) // Adjust spacing if needed

            // Input Fields using Dropdowns
            Row(Modifier.fillMaxWidth()) {
                 ExposedDropdownMenuBoxInput(
                     label = "Material",
                     options = viewModel.materials, // Access directly from ViewModel
                     selectedOption = uiState.selectedMaterial,
                     onOptionSelected = viewModel::onMaterialChange, // Call ViewModel
                     modifier = Modifier.weight(1f)
                 )
                 Spacer(Modifier.width(8.dp))
                 ExposedDropdownMenuBoxInput(
                     label = "Size",
                     options = viewModel.wireSizes, // Access directly from ViewModel
                     selectedOption = uiState.selectedSize,
                     onOptionSelected = viewModel::onSizeChange, // Call ViewModel
                     modifier = Modifier.weight(1f)
                 )
            }
             Spacer(modifier = Modifier.height(8.dp))
             ExposedDropdownMenuBoxInput(
                 label = "Insulation (Temp Rating)",
                 options = viewModel.insulationOptions.map { it.first }, // Map Pair list to String list for options
                 selectedOption = uiState.selectedInsulation,
                 onOptionSelected = viewModel::onInsulationChange, // Call ViewModel
                 modifier = Modifier.fillMaxWidth(),
                 // Find the pair to display the temp rating
                 optionDisplayTransform = { name ->
                     val rating = viewModel.insulationOptions.find { it.first == name }?.second
                     if (rating != null) "$name (${rating}°C)" else name
                 }
             )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.ambientTempStr,
                onValueChange = viewModel::onAmbientTempChange, // Call ViewModel
                label = { Text("Ambient Temperature (°C)") },
                 keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), // Use Decimal and correct KeyboardOptions import
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.numConductorsStr,
                onValueChange = viewModel::onNumConductorsChange, // Call ViewModel
                label = { Text("Number of Current-Carrying Conductors") },
                 keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Correct KeyboardOptions import
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Results Section
            Text("Results:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Base Ampacity: ${uiState.baseAmpacity?.formatCalculationResult() ?: "N/A"}") // Use renamed shared helper
            Text(text = "Temp Correction Factor: ${uiState.tempCorrectionFactor?.formatCalculationResult() ?: "N/A"}") // Use renamed shared helper
            Text(text = "Conductor Adjustment Factor: ${uiState.conductorAdjustmentFactor?.formatCalculationResult() ?: "N/A"}") // Use renamed shared helper
            Text(text = "Adjusted Ampacity: ${uiState.adjustedAmpacity?.formatCalculationResult() ?: "N/A"} A", style = MaterialTheme.typography.titleLarge) // Use renamed shared helper


            uiState.errorMessage?.let { error -> // Show error from state
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error) // Added text=
            }

             Spacer(modifier = Modifier.height(16.dp))
             Button(onClick = viewModel::clearInputs) { // Call ViewModel
                 Text("Clear / Reset")
             }
        } // Closes Column
    } // Closes Scaffold content lambda
} // Closes WireAmpacityScreen composable

// Removed local formatAmpacityResult - using shared formatCalculationResult

// Preview removed
