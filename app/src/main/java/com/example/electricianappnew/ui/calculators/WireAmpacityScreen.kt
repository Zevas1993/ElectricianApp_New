package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.electricianappnew.ui.calculators.viewmodel.WireAmpacityViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
// import java.util.Locale // Locale might not be needed if formatCalculationResult handles it
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.electricianappnew.ui.common.formatCalculationResult // Keep this import
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WireAmpacityScreen(
    modifier: Modifier = Modifier,
    viewModel: WireAmpacityViewModel = hiltViewModel(),
    navController: NavController
) {
    // Collect all StateFlows from ViewModel
    val materials by viewModel.materials.collectAsStateWithLifecycle()
    val wireSizes by viewModel.wireSizes.collectAsStateWithLifecycle()
    val insulationOptions by viewModel.insulationOptions.collectAsStateWithLifecycle()
    val selectedMaterial by viewModel.selectedMaterial.collectAsStateWithLifecycle()
    val selectedSize by viewModel.selectedSize.collectAsStateWithLifecycle()
    val selectedInsulation by viewModel.selectedInsulation.collectAsStateWithLifecycle()
    val ambientTempStr by viewModel.ambientTempStr.collectAsStateWithLifecycle()
    val numConductorsStr by viewModel.numConductorsStr.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle() // Collect the calculation result state

    val scrollState = rememberScrollState()

    // Local state for dropdown expanded status
    var expandedMaterial by remember { mutableStateOf(false) }
    var expandedSize by remember { mutableStateOf(false) }
    var expandedInsulation by remember { mutableStateOf(false) }


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
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Input Fields using Dropdowns
            Row(Modifier.fillMaxWidth()) {
                 // Material Dropdown
                 ExposedDropdownMenuBox(
                     expanded = expandedMaterial,
                     onExpandedChange = { expandedMaterial = it },
                     modifier = Modifier.weight(1f)
                 ) {
                     TextField(
                         readOnly = true,
                         value = selectedMaterial ?: "Select Material", // Use collected state
                         onValueChange = {},
                         label = { Text("Material") },
                         trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedMaterial) },
                         modifier = Modifier.menuAnchor()
                     )
                     ExposedDropdownMenu(
                         expanded = expandedMaterial,
                         onDismissRequest = { expandedMaterial = false }
                     ) {
                         materials.forEach { mat ->
                             DropdownMenuItem(
                                 text = { Text(mat) },
                                 onClick = {
                                     viewModel.updateSelectedMaterial(mat) // Update ViewModel StateFlow directly
                                     expandedMaterial = false
                                 }
                             )
                         }
                     }
                 }

                  Spacer(Modifier.width(8.dp))

                  // Size Dropdown
                  ExposedDropdownMenuBox(
                      expanded = expandedSize,
                      onExpandedChange = { expandedSize = it },
                      modifier = Modifier.weight(1f)
                  ) {
                      TextField(
                          readOnly = true,
                          value = selectedSize ?: "Select Size", // Use collected state
                          onValueChange = {},
                          label = { Text("Size") },
                          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedSize) },
                          modifier = Modifier.menuAnchor()
                      )
                      ExposedDropdownMenu(
                          expanded = expandedSize,
                          onDismissRequest = { expandedSize = false }
                     ) {
                         wireSizes.forEach { sz ->
                             DropdownMenuItem(
                                 text = { Text(sz) },
                                 onClick = {
                                     viewModel.updateSelectedSize(sz) // Update ViewModel StateFlow directly
                                     expandedSize = false
                                 }
                             )
                          }
                      }
                  }
            }

             Spacer(modifier = Modifier.height(8.dp))

             // Insulation Dropdown
             ExposedDropdownMenuBox(
                 expanded = expandedInsulation,
                 onExpandedChange = { expandedInsulation = it },
                 modifier = Modifier.fillMaxWidth()
             ) {
                 TextField(
                     readOnly = true,
                     value = selectedInsulation ?: "Select Insulation", // Use collected state
                     onValueChange = {},
                     label = { Text("Insulation (Temp Rating)") },
                     trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedInsulation) },
                     modifier = Modifier.menuAnchor()
                 )
                 ExposedDropdownMenu(
                     expanded = expandedInsulation,
                     onDismissRequest = { expandedInsulation = false }
                     ) {
                         insulationOptions.forEach { (name, temp) ->
                             DropdownMenuItem(
                                 text = { Text("$name ($temp°C)") },
                                 onClick = {
                                     viewModel.updateSelectedInsulation(name) // Update ViewModel StateFlow directly
                                     expandedInsulation = false
                                 }
                             )
                     }
                 }
             }


            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = ambientTempStr, // Use collected state
                onValueChange = viewModel::updateAmbientTempStr, // Update ViewModel StateFlow directly
                label = { Text("Ambient Temperature (°C)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = numConductorsStr, // Use collected state
                onValueChange = viewModel::updateNumConductorsStr, // Update ViewModel StateFlow directly
                label = { Text("Number of Current-Carrying Conductors") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Results Section (Use uiState for results and errors)
            Text("Results:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Base Ampacity: ${uiState.baseAmpacity?.formatCalculationResult() ?: "N/A"}")
            Text(text = "Temp Correction Factor: ${uiState.tempCorrectionFactor?.formatCalculationResult() ?: "N/A"}")
            Text(text = "Conductor Adjustment Factor: ${uiState.conductorAdjustmentFactor?.formatCalculationResult() ?: "N/A"}")
            Text(text = "Adjusted Ampacity: ${uiState.adjustedAmpacity?.formatCalculationResult() ?: "N/A"} A", style = MaterialTheme.typography.titleLarge)

            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }

             Spacer(modifier = Modifier.height(16.dp))
             Button(onClick = viewModel::clearInputs) { // Call ViewModel
                 Text("Clear / Reset")
             }
        }
    }
}

// Preview removed
