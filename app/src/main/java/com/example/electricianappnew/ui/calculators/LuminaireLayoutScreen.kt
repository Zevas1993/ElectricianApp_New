package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.electricianappnew.ui.calculators.viewmodel.LuminaireLayoutUiState
import com.example.electricianappnew.ui.calculators.viewmodel.LuminaireLayoutViewModel
import com.example.electricianappnew.ui.calculators.viewmodel.LuminaireLayoutType
import com.example.electricianappnew.ui.calculators.viewmodel.LightingType
import com.example.electricianappnew.ui.calculators.viewmodel.CalculationMode // Import CalculationMode
import com.example.electricianappnew.ui.common.formatCalculationResult
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp // Import Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuminaireLayoutScreen(
    modifier: Modifier = Modifier,
    viewModel: LuminaireLayoutViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState = viewModel.uiState // Collect state from Flow
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Luminaire Layout Calculator") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- Calculation Mode Selection ---
            Text("Select Calculation Mode:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalculationMode.entries.forEach { mode ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.calculationMode == mode,
                            onClick = { viewModel.onCalculationModeSelected(mode) }
                        )
                        Text(
                            text = mode.name.replace("_", " "),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Layout Selection ---
            Text("Select a layout:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Column { // Use Column for radio buttons
                LuminaireLayoutType.entries.forEach { layoutType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.selectedLayout == layoutType,
                            onClick = { viewModel.onLayoutSelected(layoutType) }
                        )
                        Text(
                            text = layoutType.name.replace("_", " "),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Lighting Type Selection ---
            Text("Select Lighting Type:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Column { // Use Column for radio buttons
                for (lightingType in LightingType.entries) {
                     Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.selectedLightingType == lightingType,
                            onClick = { viewModel.onLightingTypeSelected(lightingType) }
                        )
                        Text(
                            text = lightingType.name.replace("_", " "),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Inputs ---
            Text("Room & Fixture Details", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Room Dimensions (Feet)
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                InputTextField(
                    value = uiState.roomLengthFeetStr,
                    onValueChange = { viewModel.onInputChange(length = it) },
                    label = "Length (ft)",
                    modifier = Modifier.weight(1f),
                    isError = uiState.roomLengthFeetError,
                    errorMessage = uiState.roomLengthFeetErrorMessage
                )
                Spacer(Modifier.width(8.dp))
                InputTextField(
                    value = uiState.roomWidthFeetStr,
                    onValueChange = { viewModel.onInputChange(width = it) },
                    label = "Width (ft)",
                    modifier = Modifier.weight(1f),
                    isError = uiState.roomWidthFeetError,
                    errorMessage = uiState.roomWidthFeetErrorMessage
                )
                Spacer(Modifier.width(8.dp))
                 InputTextField(
                    value = uiState.ceilingHeightFeetStr,
                    onValueChange = { viewModel.onInputChange(height = it) },
                    label = "Height (ft)",
                    modifier = Modifier.weight(1f),
                    isError = uiState.ceilingHeightFeetError,
                    errorMessage = uiState.ceilingHeightFeetErrorMessage
                )
            }

            // Work Plane Height
            InputTextField(
                value = uiState.workPlaneHeightFeetStr,
                onValueChange = { viewModel.onInputChange(workPlaneHeight = it) },
                label = "Work Plane Height (ft)",
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                isError = uiState.workPlaneHeightFeetError,
                errorMessage = uiState.workPlaneHeightFeetErrorMessage
            )

            // Conditional Input based on Calculation Mode
            when (uiState.calculationMode) {
                CalculationMode.CALCULATE_FIXTURES -> {
                    InputTextField(
                        value = uiState.desiredFootCandlesStr,
                        onValueChange = { viewModel.onInputChange(footCandles = it) },
                        label = "Desired Foot Candles (fc)",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        isError = uiState.desiredFootCandlesError,
                        errorMessage = uiState.desiredFootCandlesErrorMessage
                    )
                    // Manual Rows/Cols input only visible for GRID layout in CALCULATE_FIXTURES mode
                    if (uiState.selectedLayout == LuminaireLayoutType.GRID) {
                         Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            InputTextField(
                                value = uiState.manualRowsStr,
                                onValueChange = { viewModel.onInputChange(manualRows = it) },
                                label = "Manual Rows (Optional)",
                                modifier = Modifier.weight(1f),
                                keyboardType = KeyboardType.Number,
                                isError = uiState.manualRowsError,
                                errorMessage = uiState.manualRowsErrorMessage
                            )
                            Spacer(Modifier.width(8.dp))
                            InputTextField(
                                value = uiState.manualColsStr,
                                onValueChange = { viewModel.onInputChange(manualCols = it) },
                                label = "Manual Cols (Optional)",
                                modifier = Modifier.weight(1f),
                                keyboardType = KeyboardType.Number,
                                isError = uiState.manualColsError,
                                errorMessage = uiState.manualColsErrorMessage
                            )
                         }
                    }
                }
                CalculationMode.CALCULATE_FOOT_CANDLES -> {
                    InputTextField(
                        value = uiState.desiredTotalFixturesStr,
                        onValueChange = { viewModel.onInputChange(desiredFixtures = it) },
                        label = "Desired Total Fixtures",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        keyboardType = KeyboardType.Number,
                        isError = uiState.desiredTotalFixturesError,
                        errorMessage = uiState.desiredTotalFixturesErrorMessage
                    )
                }
            }


            // Fixture Details
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                InputTextField(
                    value = uiState.fixtureLumensStr,
                    onValueChange = { viewModel.onInputChange(lumens = it) },
                    label = "Lumens/Fixture",
                    modifier = Modifier.weight(1f),
                    isError = uiState.fixtureLumensError,
                    errorMessage = uiState.fixtureLumensErrorMessage,
                )
                Spacer(Modifier.width(8.dp))
                InputTextField(
                    value = uiState.fixtureWattageStr,
                    onValueChange = { viewModel.onInputChange(wattage = it) },
                    label = "Watts/Fixture",
                    modifier = Modifier.weight(1f),
                    isError = uiState.fixtureWattageError,
                    errorMessage = uiState.fixtureWattageErrorMessage
                )
            }

            // Reflectance Inputs
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                InputTextField(
                    value = uiState.ceilingReflectanceStr,
                    onValueChange = { viewModel.onInputChange(ceilingReflectance = it) },
                    label = "Ceiling Reflectance (%)",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number,
                    isError = uiState.ceilingReflectanceError,
                    errorMessage = uiState.ceilingReflectanceErrorMessage
                )
                Spacer(Modifier.width(8.dp))
                InputTextField(
                    value = uiState.wallReflectanceStr,
                    onValueChange = { viewModel.onInputChange(wallReflectance = it) },
                    label = "Wall Reflectance (%)",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number,
                    isError = uiState.wallReflectanceError,
                    errorMessage = uiState.wallReflectanceErrorMessage
                )
                Spacer(Modifier.width(8.dp))
                InputTextField(
                    value = uiState.floorReflectanceStr,
                    onValueChange = { viewModel.onInputChange(floorReflectance = it) },
                    label = "Floor Reflectance (%)",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number,
                    isError = uiState.floorReflectanceError,
                    errorMessage = uiState.floorReflectanceErrorMessage
                )
            }

            // Factors
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                 InputTextField(
                    value = uiState.lightLossFactorStr,
                    onValueChange = { viewModel.onInputChange(llf = it) },
                    label = "LLF (0.0-1.0)",
                    modifier = Modifier.weight(1f),
                    isError = uiState.lightLossFactorError,
                    errorMessage = uiState.lightLossFactorErrorMessage
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Results ---
            Text("Calculation Results", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Spacer(modifier = Modifier.height(8.dp))

            // Display calculated results if valid
            if (uiState.errorMessage == null && (uiState.numFixturesRequired != null || uiState.calculatedFootCandles != null)) {
                ResultTextRow(label = "Room Area:", value = uiState.roomAreaSqFt?.formatCalculationResult(1) + " sq ft")
                ResultTextRow(label = "Total Lumens Needed:", value = uiState.totalLumensNeeded?.formatCalculationResult(0))

                // Display Fixtures Required or Calculated Foot Candles based on calculation mode
                when (uiState.calculationMode) {
                    CalculationMode.CALCULATE_FIXTURES -> {
                        ResultTextRow(label = "Fixtures Required:", value = uiState.numFixturesRequired?.toString() ?: "N/A")
                    }
                    CalculationMode.CALCULATE_FOOT_CANDLES -> {
                         ResultTextRow(label = "Calculated Foot Candles:", value = uiState.calculatedFootCandles?.formatCalculationResult(1) + " fc")
                    }
                }

                // Display Calculated RCR and CU
                ResultTextRow(label = "Room Cavity Ratio (RCR):", value = uiState.roomCavityRatio?.formatCalculationResult(2))
                ResultTextRow(label = "Coefficient of Utilization (CU):", value = uiState.coefficientOfUtilization?.formatCalculationResult(2))


                // Display layout details only if a layout was determined (i.e., in CALCULATE_FIXTURES mode with GRID layout, or for fixed layouts)
                if (uiState.recommendedLayoutRows != null && uiState.recommendedLayoutCols != null) {
                    ResultTextRow(
                        label = "Recommended Layout:",
                        value = "${uiState.recommendedLayoutRows} Rows x ${uiState.recommendedLayoutCols} Cols"
                    )
                    ResultTextRow(
                        label = "Offset (Length / Width):",
                        value = "${uiState.offsetLengthFeet?.formatCalculationResult(2) ?: "N/A"} ft / ${uiState.offsetWidthFeet?.formatCalculationResult(2) ?: "N/A"} ft"
                    )
                    ResultTextRow(
                        label = "Spacing (Length / Width):",
                        value = "${uiState.spacingLengthFeet?.formatCalculationResult(2)?.let { "$it ft" } ?: "N/A"} / ${uiState.spacingWidthFeet?.formatCalculationResult(2)?.let { "$it ft" } ?: "N/A"}"
                    )
                }


                ResultTextRow(label = "Total Wattage:", value = uiState.totalWattage?.formatCalculationResult(1) + " W")
                ResultTextRow(label = "Watts per Sq Ft:", value = uiState.wattsPerSqFt?.formatCalculationResult(2) + " W/sq ft")


                Spacer(modifier = Modifier.height(16.dp))
                Text("Layout Diagram:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // Updated Canvas Call
                LuminaireLayoutDiagram(
                    roomWidth = uiState.roomWidthFeetStr.toFloatOrNull() ?: 0f, // Access value, convert to Float
                    roomHeight = uiState.roomLengthFeetStr.toFloatOrNull() ?: 0f, // Access value, convert to Float
                    luminaireCols = uiState.recommendedLayoutCols ?: 0, // Access value, provide default
                    luminaireRows = uiState.recommendedLayoutRows ?: 0, // Access value, provide default
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(
                            // Use calculated area aspect ratio if available, otherwise fallback to input strings
                            // Safely access nullable values and provide defaults
                            if (uiState.roomAreaSqFt != null && uiState.roomLengthFeetStr.toFloatOrNull() != null && uiState.roomWidthFeetStr.toFloatOrNull() != null) {
                                max(0.1f, (uiState.roomWidthFeetStr.toFloatOrNull() ?: 1f) / max(0.1f, (uiState.roomLengthFeetStr.toFloatOrNull() ?: 1f)))
                            } else {
                                max(0.1f, (uiState.roomWidthFeetStr.toFloatOrNull() ?: 1f) / max(0.1f, (uiState.roomLengthFeetStr.toFloatOrNull() ?: 1f)))
                            }
                        )
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                )

            } else if (uiState.errorMessage == null) {
                 Text("Enter room and fixture details to calculate layout.")
            }

            // Display error message if any
            uiState.errorMessage?.let { error: String ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = viewModel::clearInputs, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Clear / Reset")
            }
            Spacer(modifier = Modifier.height(16.dp)) // Add space at the bottom
        }
    }
}



// Reusable Input Text Field
@Composable
fun InputTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Decimal, // Default to Decimal
    isError: Boolean = false, // Add isError parameter
    errorMessage: String? = null // Add errorMessage parameter
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(), // Ensure TextField fills the width of its parent Column
            singleLine = true,
            isError = isError // Pass error state to OutlinedTextField
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp) // Add some padding for alignment
            )
        }
    }
}


// Reusable Row for displaying results
@Composable
fun ResultTextRow(label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value ?: "N/A", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

/**
 * Draws a top-down room outline with luminaires arranged in a grid.
 *
 * @param roomWidth      Width of the room (e.g. in meters or feet).
 * @param roomHeight     Height of the room.
 * @param luminaireCols  Number of luminaires along the width.
 * @param luminaireRows  Number of luminaires along the height.
 * @param modifier       Modifier for sizing/layout.
 * @param roomBorderDp   Stroke width for the room outline.
 * @param luminaireDp    Diameter of each luminaire marker.
 */
@Composable
fun LuminaireLayoutDiagram(
    roomWidth: Float,
    roomHeight: Float,
    luminaireCols: Int,
    luminaireRows: Int,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(200.dp),
    roomBorderDp: Dp = 2.dp,
    luminaireDp: Dp = 8.dp
) {
    Canvas(modifier = modifier.padding(16.dp)) {
        // Compute scale to fit the given room dimensions into the canvas size
        val scaleX = size.width  / roomWidth
        val scaleY = size.height / roomHeight
        val scale  = minOf(scaleX, scaleY)

        // Room rectangle coordinates
        val roomRectWidth  = roomWidth  * scale
        val roomRectHeight = roomHeight * scale
        val left   = (size.width  - roomRectWidth ) / 2f
        val top    = (size.height - roomRectHeight) / 2f
        val right  = left + roomRectWidth
        val bottom = top  + roomRectHeight

        // Draw room outline
        drawRect(
            color   = Color.LightGray,
            topLeft = Offset(left, top),
            size    = androidx.compose.ui.geometry.Size(roomRectWidth, roomRectHeight),
            style   = Stroke(width = roomBorderDp.toPx())
        )

        // Spacing between luminaires
        val dx = if (luminaireCols > 1) roomRectWidth  / (luminaireCols - 1) else 0f
        val dy = if (luminaireRows > 1) roomRectHeight / (luminaireRows - 1) else 0f

        // Draw each luminaire as a filled circle
        for (i in 0 until luminaireCols) {
            for (j in 0 until luminaireRows) {
                val cx = left + i * dx
                val cy = top  + j * dy
                drawCircle(
                    color  = Color.Yellow,
                    radius = luminaireDp.toPx() / 2f,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}
