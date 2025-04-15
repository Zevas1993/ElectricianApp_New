package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType // Ensure KeyboardType is imported
// Remove incorrect import: import androidx.compose.ui.text.input.KeyboardType.Companion.NumberDecimal
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // Import Hilt ViewModel
import com.example.electricianappnew.ui.calculators.viewmodel.PipeBendingViewModel // Import ViewModel
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput // Import shared component
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import kotlin.math.PI // Add import for PI

// TODO: Implement actual bending formulas and multipliers
object PipeBendingData {
    // Multipliers for common offset angles
    val offsetMultipliers = mapOf(
        10.0 to 6.0,
        22.5 to 2.6,
        30.0 to 2.0,
        45.0 to 1.4,
        60.0 to 1.2
    )
    // Constants for saddle bends (center mark multipliers)
    val saddleCenterMultipliers = mapOf(
        22.5 to 2.5, // Example
        30.0 to 2.0, // Example
        45.0 to 1.4  // Example
    )

    // Gain for 90 degree bends (depends on conduit size and bender type - needs real data)
    fun getGain(conduitSize: String): Double {
        // Using common approximate gain values for EMT benders
         return when (conduitSize) {
            "1/2\"" -> 2.25    // 2 1/4"
            "3/4\"" -> 3.0     // 3"
            "1\"" -> 4.0     // 4"
            "1 1/4\"" -> 5.0   // 5" (Approx)
            "1 1/2\"" -> 6.0   // 6" (Approx)
            "2\"" -> 7.25    // 7 1/4" (Approx)
            else -> 5.0 // Default guess if size not listed
        }
    }
     // Shrink per inch of offset/saddle (depends on angle)
    fun getShrinkPerInch(angle: Double): Double? {
         return when (angle) {
            10.0 -> 1.0/16.0 // 1/16"
            22.5 -> 3.0/16.0 // 3/16"
            30.0 -> 1.0/4.0  // 1/4"
            45.0 -> 3.0/8.0  // 3/8"
            60.0 -> 1.0/2.0  // 1/2"
            else -> null
        }
    }
}

enum class BendType { Offset, ThreePointSaddle, FourPointSaddle, NinetyDegreeStub }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PipeBendingScreen(
    modifier: Modifier = Modifier,
    viewModel: PipeBendingViewModel = hiltViewModel() // Inject ViewModel
) {
    val uiState = viewModel.uiState // Observe state

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pipe Bending Calculator", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // Inputs
        ExposedDropdownMenuBoxInput(
            label = "Bend Type",
            options = uiState.bendTypes.map { it.name }, // Display enum names
            selectedOption = uiState.selectedBendType.name,
            onOptionSelected = { viewModel.onBendTypeChange(BendType.valueOf(it)) }, // Convert back to enum
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.selectedBendType != BendType.NinetyDegreeStub) {
             ExposedDropdownMenuBoxInput(
                 label = "Angle (°)",
                 options = uiState.commonAngles.map { it.toString() }, // Convert Double to String for options
                 selectedOption = uiState.selectedAngleStr,
                 onOptionSelected = viewModel::onAngleChange,
                 modifier = Modifier.fillMaxWidth()
             )
             Spacer(modifier = Modifier.height(8.dp))
        }

         ExposedDropdownMenuBoxInput(
             label = "Conduit Size",
             options = uiState.conduitSizes,
             selectedOption = uiState.conduitSizeStr,
             onOptionSelected = viewModel::onConduitSizeChange,
             modifier = Modifier.fillMaxWidth()
         )
         Spacer(modifier = Modifier.height(8.dp))

        // Ensure OutlinedTextField calls have correct parameters and label structure
        when (uiState.selectedBendType) {
            BendType.Offset -> {
                OutlinedTextField(
                    value = uiState.offsetDepthStr,
                    onValueChange = viewModel::onOffsetDepthChange,
                    label = { Text("Offset Depth (in)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            BendType.ThreePointSaddle -> {
                 OutlinedTextField(
                    value = uiState.saddleDepthStr,
                    onValueChange = viewModel::onSaddleDepthChange,
                    label = { Text("Saddle Depth / Obstruction Height (in)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            BendType.FourPointSaddle -> {
                 OutlinedTextField(
                    value = uiState.saddleDepthStr,
                    onValueChange = viewModel::onSaddleDepthChange,
                    label = { Text("Saddle Depth / Obstruction Height (in)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                   value = uiState.obstacleWidthStr,
                   onValueChange = viewModel::onObstacleWidthChange,
                   label = { Text("Obstacle Width (in)") },
                   keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                   modifier = Modifier.fillMaxWidth()
                )
            }
            BendType.NinetyDegreeStub -> {
                 OutlinedTextField(
                    value = uiState.stubHeightStr,
                    onValueChange = viewModel::onStubHeightChange,
                    label = { Text("Stub-up Height (in)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        // Removed duplicated code block from here

        Spacer(modifier = Modifier.height(16.dp))

        // Results - Standard Text calls
        Text("Results:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        uiState.mark1?.let { Text(text = it) } // Use named parameter 'text'
        uiState.mark2?.let { Text(text = it) } // Use named parameter 'text'
        uiState.mark3?.let { Text(text = it) } // Use named parameter 'text'
        uiState.mark4?.let { Text(text = it) } // Use named parameter 'text'
        uiState.travel?.let { Text(text = "Travel: ${it.formatResult(3)}\"") } // Use named parameter 'text'
        uiState.shrink?.let { Text(text = "Shrink: ${it.formatResult(3)}\"") } // Use named parameter 'text'
        uiState.gain?.let { Text(text = "Gain (90°): ${it.formatResult(3)}\"") } // Use named parameter 'text'


        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error) // Use named parameter 'text'
        }

         Spacer(modifier = Modifier.height(16.dp))
         Button(onClick = viewModel::clearInputs) { // Corrected reference
             Text("Clear / Reset")
         }
    }
}

// Re-use or move this helper
private fun Double.formatResult(decimals: Int = 3): String { // Keep 3 decimals for bending
    return String.format("%.${decimals}f", this).trimEnd('0').trimEnd('.')
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PipeBendingScreenPreview() {
    ElectricianAppNewTheme {
        PipeBendingScreen() // Preview uses default ViewModel state
    }
}
