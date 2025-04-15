package com.example.electricianappnew.ui.neccode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
// Import correct model names
import com.example.electricianappnew.data.model.NecAmpacityEntry
import com.example.electricianappnew.data.model.NecConductorEntry
import com.example.electricianappnew.data.model.NecConduitEntry
import com.example.electricianappnew.data.model.NecWireAreaEntry
import com.example.electricianappnew.data.model.NecBoxFillEntry
import com.example.electricianappnew.data.model.NecTempCorrectionEntry
import com.example.electricianappnew.data.model.NecConductorAdjustmentEntry
import com.example.electricianappnew.data.model.NecMotorFLCEntry
// Import ViewModel
import com.example.electricianappnew.ui.neccode.viewmodel.NecCodeViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NecCodeScreen(
    modifier: Modifier = Modifier,
    viewModel: NecCodeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(
            "NEC Code Reference",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            label = { Text("Search Tables...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "Error loading NEC data",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {

                // --- Ampacity ---
                item {
                    Text("Wire Ampacity (Table 310.16)", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    AmpacityHeader()
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface)
                }
                if (uiState.necAmpacityData.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                     item { Text("No matching ampacity data found.", modifier = Modifier.padding(8.dp)) }
                } else {
                    items(
                        items = uiState.necAmpacityData, // Use correct state property
                        key = { entry: NecAmpacityEntry -> "${entry.material}-${entry.size}-${entry.tempRating}" }
                    ) { entry: NecAmpacityEntry ->
                        AmpacityRow(entry = entry)
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }

                // --- Conductor Properties ---
                item {
                    Text("Conductor Properties (Ch 9, Tbl 8)", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    ConductorHeader()
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface)
                }
                 if (uiState.necConductorData.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                     item { Text("No matching conductor data found.", modifier = Modifier.padding(8.dp)) }
                 } else {
                    items(
                        items = uiState.necConductorData, // Use correct state property
                        key = { entry: NecConductorEntry -> "${entry.material}-${entry.size}" }
                    ) { entry: NecConductorEntry ->
                        ConductorRow(entry = entry)
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                 }
                 item { Spacer(modifier = Modifier.height(16.dp)) }

                 // --- Conduit Area ---
                 item {
                     Text("Conduit Area (Ch 9, Tbl 4)", style = MaterialTheme.typography.titleLarge)
                     Spacer(modifier = Modifier.height(8.dp))
                     ConduitHeader()
                     HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface)
                 }
                 if (uiState.necConduitData.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                     item { Text("No matching conduit data found.", modifier = Modifier.padding(8.dp)) }
                 } else {
                     items(
                         items = uiState.necConduitData, // Use correct state property
                         key = { entry: NecConduitEntry -> "${entry.type}-${entry.size}" }
                     ) { entry: NecConduitEntry ->
                         ConduitRow(entry = entry)
                         HorizontalDivider(thickness = 0.5.dp)
                     }
                 }
                 item { Spacer(modifier = Modifier.height(16.dp)) }

                 // --- Wire Area ---
                 item {
                     Text("Wire Area (Ch 9, Tbl 5)", style = MaterialTheme.typography.titleLarge)
                     Spacer(modifier = Modifier.height(8.dp))
                     WireAreaHeader()
                     HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface)
                 }
                  if (uiState.necWireAreaData.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                     item { Text("No matching wire area data found.", modifier = Modifier.padding(8.dp)) }
                 } else {
                     items(
                         items = uiState.necWireAreaData, // Use correct state property
                         key = { entry: NecWireAreaEntry -> "${entry.insulationType}-${entry.size}" }
                     ) { entry: NecWireAreaEntry ->
                         WireAreaRow(entry = entry)
                         HorizontalDivider(thickness = 0.5.dp)
                     }
                 }
                 item { Spacer(modifier = Modifier.height(16.dp)) }

                 // --- Box Fill ---
                 item {
                     Text("Box Fill (Table 314.16(B))", style = MaterialTheme.typography.titleLarge)
                     Spacer(modifier = Modifier.height(8.dp))
                     BoxFillHeader()
                     HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface)
                 }
                  if (uiState.necBoxFillData.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                     item { Text("No matching box fill data found.", modifier = Modifier.padding(8.dp)) }
                 } else {
                     items(
                         items = uiState.necBoxFillData, // Use correct state property
                         key = { entry: NecBoxFillEntry -> "${entry.itemType}-${entry.conductorSize}" }
                     ) { entry: NecBoxFillEntry ->
                         BoxFillRow(entry = entry)
                         HorizontalDivider(thickness = 0.5.dp)
                     }
                 }
                 item { Spacer(modifier = Modifier.height(16.dp)) }

                 // --- Temp Correction ---
                 item {
                     Text("Temp. Correction (Table 310.15(B))", style = MaterialTheme.typography.titleLarge)
                     Spacer(modifier = Modifier.height(8.dp))
                     TempCorrectionHeader()
                     HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface)
                 }
                  if (uiState.necTempCorrectionData.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                     item { Text("No matching temp correction data found.", modifier = Modifier.padding(8.dp)) }
                 } else {
                     items(
                         items = uiState.necTempCorrectionData, // Use correct state property
                         key = { entry: NecTempCorrectionEntry -> "${entry.tempRating}-${entry.ambientTempCelsius}" }
                     ) { entry: NecTempCorrectionEntry ->
                         TempCorrectionRow(entry = entry)
                         HorizontalDivider(thickness = 0.5.dp)
                     }
                 }
                 item { Spacer(modifier = Modifier.height(16.dp)) }

                 // --- Conductor Adjustment ---
                 item {
                     Text("Conductor Adjustment (Table 310.15(C))", style = MaterialTheme.typography.titleLarge)
                     Spacer(modifier = Modifier.height(8.dp))
                     ConductorAdjustmentHeader()
                     HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface)
                 }
                  if (uiState.necConductorAdjustmentData.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                     item { Text("No matching conductor adjustment data found.", modifier = Modifier.padding(8.dp)) }
                 } else {
                     items(
                         items = uiState.necConductorAdjustmentData, // Use correct state property
                         key = { entry: NecConductorAdjustmentEntry -> "${entry.minConductors}-${entry.maxConductors}" }
                     ) { entry: NecConductorAdjustmentEntry ->
                         ConductorAdjustmentRow(entry = entry)
                         HorizontalDivider(thickness = 0.5.dp)
                     }
                 }
                 item { Spacer(modifier = Modifier.height(16.dp)) }

                 // --- Motor FLC ---
                 item {
                     Text("Motor FLC (Tables 430.248 & 430.250)", style = MaterialTheme.typography.titleLarge)
                     Spacer(modifier = Modifier.height(8.dp))
                     MotorFLCHeader()
                     HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface)
                 }
                  if (uiState.necMotorFLCData.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                     item { Text("No matching motor FLC data found.", modifier = Modifier.padding(8.dp)) }
                 } else {
                     items(
                         items = uiState.necMotorFLCData, // Use correct state property
                         key = { entry: NecMotorFLCEntry -> "${entry.hp}-${entry.voltage}-${entry.phase}" }
                     ) { entry: NecMotorFLCEntry ->
                         MotorFLCRow(entry = entry)
                         HorizontalDivider(thickness = 0.5.dp)
                     }
                 }
                 // Removed Raceway and Wire sections
            }
        }
    }
}

// --- Header and Row Composables for each table (Using correct model names) ---

// Ampacity
@Composable
fun AmpacityHeader() {
     Row(
         modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(vertical = 6.dp)
     ) {
        Text("Size", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("Material", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("Temp (°C)", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("Amps", modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun AmpacityRow(entry: NecAmpacityEntry) { // Correct type
     Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(entry.size, modifier = Modifier.weight(1f))
        Text(entry.material, modifier = Modifier.weight(1f))
        Text("${entry.tempRating}°C", modifier = Modifier.weight(1f)) // Correct property
        Text("${entry.ampacity}", modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

// Conductor Properties
@Composable
fun ConductorHeader() {
     Row(
         modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(vertical = 6.dp)
     ) {
        Text("Size", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("Material", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("Ω/kft (DC)", modifier = Modifier.weight(1.5f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
        Text("Area (in²)", modifier = Modifier.weight(1.5f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun ConductorRow(entry: NecConductorEntry) { // Correct type
     Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(entry.size, modifier = Modifier.weight(1f))
        Text(entry.material, modifier = Modifier.weight(1f))
        Text(String.format("%.3f", entry.resistanceDcOhmsPer1000ft), modifier = Modifier.weight(1.5f), textAlign = TextAlign.End) // Correct property
        Text(entry.areaSqIn?.let { String.format("%.4f", it) } ?: "-", modifier = Modifier.weight(1.5f), textAlign = TextAlign.End) // Correct property
    }
}

// Conduit Area
@Composable
fun ConduitHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(vertical = 6.dp)
    ) {
        Text("Type", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
        Text("Size", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text(">2 Wires (in²)", modifier = Modifier.weight(1.5f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold) // 40% fill
    }
}
@Composable
fun ConduitRow(entry: NecConduitEntry) { // Correct type
    Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(entry.type, modifier = Modifier.weight(1.5f))
        Text(entry.size, modifier = Modifier.weight(1f))
        Text(String.format("%.4f", entry.fillAreaOver2WiresSqIn), modifier = Modifier.weight(1.5f), textAlign = TextAlign.End) // Correct property
    }
}

// Wire Area
@Composable
fun WireAreaHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(vertical = 6.dp)
    ) {
        Text("Size", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("Insulation", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
        Text("Area (in²)", modifier = Modifier.weight(1.5f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun WireAreaRow(entry: NecWireAreaEntry) { // Correct type
    Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(entry.size, modifier = Modifier.weight(1f))
        Text(entry.insulationType, modifier = Modifier.weight(1.5f)) // Correct property
        Text(String.format("%.4f", entry.areaSqIn), modifier = Modifier.weight(1.5f), textAlign = TextAlign.End) // Correct property
    }
}

// Box Fill
@Composable
fun BoxFillHeader() {
     Row(
         modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(vertical = 6.dp)
     ) {
        Text("Item Type", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
        Text("Size", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("Volume (in³)", modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun BoxFillRow(entry: NecBoxFillEntry) { // Correct type
     Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(entry.itemType, modifier = Modifier.weight(1.5f)) // Correct property
        Text(entry.conductorSize, modifier = Modifier.weight(1f)) // Correct property
        Text(String.format("%.2f", entry.volumeAllowanceCuIn), modifier = Modifier.weight(1f), textAlign = TextAlign.End) // Correct property
    }
}

// Temp Correction
@Composable
fun TempCorrectionHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(vertical = 6.dp)
    ) {
        Text("Ambient °C", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("Rating °C", modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold) // Added Rating column
        Text("Factor", modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun TempCorrectionRow(entry: NecTempCorrectionEntry) { // Correct type
     Row(modifier = Modifier.padding(vertical = 6.dp)) {
         Text(entry.ambientTempCelsius.toString(), modifier = Modifier.weight(1f)) // Correct property
         Text(entry.tempRating.toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.End) // Added Rating
         Text(String.format("%.2f", entry.correctionFactor), modifier = Modifier.weight(1f), textAlign = TextAlign.End) // Correct property
     }
}

// Conductor Adjustment
@Composable
fun ConductorAdjustmentHeader() {
     Row(
         modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(vertical = 6.dp)
     ) {
        Text("Wires", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("Factor (%)", modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun ConductorAdjustmentRow(entry: NecConductorAdjustmentEntry) { // Correct type
     Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Text("${entry.minConductors}-${entry.maxConductors}", modifier = Modifier.weight(1f)) // Correct properties
        Text((entry.adjustmentFactor * 100).toInt().toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.End) // Correct property
    }
}

// Motor FLC
@Composable
fun MotorFLCHeader() {
     Row(
         modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(vertical = 6.dp)
     ) {
        Text("HP", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("Phase", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("Voltage", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("FLC (A)", modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun MotorFLCRow(entry: NecMotorFLCEntry) { // Correct type
     Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(entry.hp.toString(), modifier = Modifier.weight(1f))
        Text(entry.phase.toString(), modifier = Modifier.weight(1f)) // Correct property
        Text(entry.voltage.toString(), modifier = Modifier.weight(1f))
        Text(String.format("%.1f", entry.flc), modifier = Modifier.weight(1f), textAlign = TextAlign.End) // Correct property
    }
}


@Preview(showBackground = true)
@Composable
fun NecCodeScreenPreview() {
    ElectricianAppNewTheme {
        NecCodeScreen() // Preview won't show real data
    }
}
