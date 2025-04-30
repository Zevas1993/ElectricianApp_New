package com.example.electricianappnew.ui.calculators.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.calculator.LuminaireCalculator // Import the new calculator class
import com.example.electricianappnew.calculator.LuminaireCalculationResult // Import the result data class
import com.example.electricianappnew.calculator.CalculationException // Import the custom exception
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch // Import launch for coroutines
import javax.inject.Inject

// --- Calculation Modes ---
enum class CalculationMode {
    CALCULATE_FIXTURES, // Calculate required fixtures based on desired FC
    CALCULATE_FOOT_CANDLES // Calculate resulting FC based on desired total fixtures
}

// --- Layout Types ---
enum class LuminaireLayoutType {
    GRID, // Automatic grid based on aspect ratio and S/MH ratio
    TWO_BY_TWO,
    TWO_BY_THREE,
    THREE_BY_THREE,
    TWO_BY_FOUR, // Added 2x4 layout
    THREE_BY_FOUR, // Added 3x4 layout
    FOUR_BY_FOUR, // Added 4x4 layout
    // Add more layout types as needed
}

// --- Lighting Types ---
enum class LightingType {
    GENERAL,
    TASK,
    ACCENT
}

// --- UI State ---
data class LuminaireLayoutUiState(
    // Inputs
    val calculationMode: CalculationMode = CalculationMode.CALCULATE_FIXTURES, // Added calculation mode

    val roomLengthFeetStr: String = "12", // Defaulting some values for quicker testing
    val roomLengthFeetError: Boolean = false,
    val roomLengthFeetErrorMessage: String? = null,

    val roomWidthFeetStr: String = "10",
    val roomWidthFeetError: Boolean = false,
    val roomWidthFeetErrorMessage: String? = null,

    val ceilingHeightFeetStr: String = "8",
    val ceilingHeightFeetError: Boolean = false,
    val ceilingHeightFeetErrorMessage: String? = null,

    val workPlaneHeightFeetStr: String = "2.5", // Added work plane height input
    val workPlaneHeightFeetError: Boolean = false,
    val workPlaneHeightFeetErrorMessage: String? = null,

    val selectedLayout: LuminaireLayoutType = LuminaireLayoutType.GRID, // Added selected layout
    val selectedLightingType: LightingType = LightingType.GENERAL, // Added lighting type

    val desiredFootCandlesStr: String = "30", // e.g., General Lighting (Relevant for CALCULATE_FIXTURES)
    val desiredFootCandlesError: Boolean = false,
    val desiredFootCandlesErrorMessage: String? = null,

    val desiredTotalFixturesStr: String = "", // New input for desired number of fixtures (Relevant for CALCULATE_FOOT_CANDLES)
    val desiredTotalFixturesError: Boolean = false,
    val desiredTotalFixturesErrorMessage: String? = null,

    val manualRowsStr: String = "", // Added manual rows input for GRID layout
    val manualRowsError: Boolean = false,
    val manualRowsErrorMessage: String? = null,

    val manualColsStr: String = "", // Added manual cols input for GRID layout
    val manualColsError: Boolean = false,
    val manualColsErrorMessage: String? = null,

    val fixtureLumensStr: String = "800",    // e.g., common 6" LED recessed
    val fixtureLumensError: Boolean = false,
    val fixtureLumensErrorMessage: String? = null,

    val fixtureWattageStr: String = "10",     // e.g., common 6" LED recessed
    val fixtureWattageError: Boolean = false,
    val fixtureWattageErrorMessage: String? = null,

    val ceilingReflectanceStr: String = "80", // Added reflectance inputs
    val ceilingReflectanceError: Boolean = false,
    val ceilingReflectanceErrorMessage: String? = null,

    val wallReflectanceStr: String = "50",
    val wallReflectanceError: Boolean = false,
    val wallReflectanceErrorMessage: String? = null,

    val floorReflectanceStr: String = "20",
    val floorReflectanceError: Boolean = false,
    val floorReflectanceErrorMessage: String? = null,

    val lightLossFactorStr: String = "0.85",         // LLF - User input for now
    val lightLossFactorError: Boolean = false,
    val lightLossFactorErrorMessage: String? = null,


    // Results
    val roomAreaSqFt: Double? = null,
    val totalLumensNeeded: Double? = null, // Total lumens needed based on desired FC (calculated in CALCULATE_FIXTURES mode)
    val numFixturesRequired: Int? = null, // Number of fixtures required (set in CALCULATE_FIXTURES mode)
    val recommendedLayoutRows: Int? = null, // Recommended layout rows (set in CALCULATE_FIXTURES mode, GRID layout)
    val recommendedLayoutCols: Int? = null, // Recommended layout columns (set in CALCULATE_FIXTURES mode, GRID layout)
    val spacingLengthFeet: Double? = null, // Spacing between columns (along length, set in CALCULATE_FIXTURES mode, GRID layout)
    val spacingWidthFeet: Double? = null,  // Spacing between rows (along width, set in CALCULATE_FIXTURES mode, GRID layout)
    val offsetLengthFeet: Double? = null,  // Offset from side wall (along length, set in CALCULATE_FIXTURES mode, GRID layout)
    val offsetWidthFeet: Double? = null,   // Offset from end wall (along width, set in CALCULATE_FIXTURES mode, GRID layout)
    val totalWattage: Double? = null, // Total wattage (calculated in both modes)
    val wattsPerSqFt: Double? = null, // Watts per square foot (calculated in both modes)
    val calculatedFootCandles: Double? = null, // Calculated FC based on actual layout (set in CALCULATE_FOOT_CANDLES mode, or for fixed layouts in CALCULATE_FIXTURES mode)
    val roomCavityRatio: Double? = null, // Room Cavity Ratio (calculated in both modes)
    val coefficientOfUtilization: Double? = null, // Coefficient of Utilization (calculated in both modes)
    val errorMessage: String? = null // General calculation error message
)

@HiltViewModel
class LuminaireLayoutViewModel @Inject constructor(
    private val luminaireCalculator: LuminaireCalculator // Inject the calculator
) : ViewModel() {

    var uiState by mutableStateOf(LuminaireLayoutUiState())
        private set

    // --- Input Handlers ---
    // Using a single handler for simplicity, could break down further
    fun onInputChange(
        length: String = uiState.roomLengthFeetStr,
        width: String = uiState.roomWidthFeetStr,
        height: String = uiState.ceilingHeightFeetStr,
        workPlaneHeight: String = uiState.workPlaneHeightFeetStr,
        footCandles: String = uiState.desiredFootCandlesStr,
        lumens: String = uiState.fixtureLumensStr,
        wattage: String = uiState.fixtureWattageStr,
        ceilingReflectance: String = uiState.ceilingReflectanceStr, // Add reflectance inputs
        wallReflectance: String = uiState.wallReflectanceStr,
        floorReflectance: String = uiState.floorReflectanceStr,
        llf: String = uiState.lightLossFactorStr,
        desiredFixtures: String = uiState.desiredTotalFixturesStr,
        manualRows: String = uiState.manualRowsStr, // Added manual rows input
        manualCols: String = uiState.manualColsStr // Added manual cols input
    ) {
        uiState = uiState.copy(
            roomLengthFeetStr = length,
            roomWidthFeetStr = width,
            ceilingHeightFeetStr = height,
            workPlaneHeightFeetStr = workPlaneHeight,
            desiredFootCandlesStr = footCandles,
            fixtureLumensStr = lumens,
            fixtureWattageStr = wattage,
            ceilingReflectanceStr = ceilingReflectance, // Update reflectance state
            wallReflectanceStr = wallReflectance,
            floorReflectanceStr = floorReflectance,
            lightLossFactorStr = llf,
            desiredTotalFixturesStr = desiredFixtures,
            manualRowsStr = manualRows, // Update manual rows state
            manualColsStr = manualCols, // Update manual cols state
            errorMessage = null // Clear general error on input change
        )
        calculateLayout()
    }

    fun onLayoutSelected(layoutType: LuminaireLayoutType) {
        uiState = uiState.copy(selectedLayout = layoutType)
        calculateLayout() // Recalculate when layout changes
    }

    fun onLightingTypeSelected(lightingType: LightingType) {
        uiState = uiState.copy(selectedLightingType = lightingType)
        // Future: Potentially update default desired FC based on lighting type
        calculateLayout() // Recalculate when lighting type changes
    }

    fun onCalculationModeSelected(mode: CalculationMode) {
        uiState = uiState.copy(
            calculationMode = mode,
            // Clear the input that is not relevant for the new mode
            desiredFootCandlesStr = if (mode == CalculationMode.CALCULATE_FOOT_CANDLES) "" else uiState.desiredFootCandlesStr,
            desiredTotalFixturesStr = if (mode == CalculationMode.CALCULATE_FIXTURES) "" else uiState.desiredTotalFixturesStr,
            // Clear any errors related to the inputs that were just cleared
            desiredFootCandlesError = false,
            desiredFootCandlesErrorMessage = null,
            desiredTotalFixturesError = false,
            desiredTotalFixturesErrorMessage = null,
            errorMessage = null // Clear general error
        )
        calculateLayout() // Recalculate when mode changes
    }


    fun clearInputs() {
        uiState = LuminaireLayoutUiState() // Reset to default state, which includes clearing errors
        // Trigger calculation with default values
        calculateLayout()
    }

    // --- Calculation Logic ---
    fun calculateLayout() {
        // Perform input validation before calling the calculator
        var hasError = false
        var newState = uiState.copy(
            // Clear previous specific input errors and results
            roomLengthFeetError = false, roomLengthFeetErrorMessage = null,
            roomWidthFeetError = false, roomWidthFeetErrorMessage = null,
            ceilingHeightFeetError = false, ceilingHeightFeetErrorMessage = null,
            workPlaneHeightFeetError = false, workPlaneHeightFeetErrorMessage = null, // Clear work plane height error
            desiredFootCandlesError = false, desiredFootCandlesErrorMessage = null,
            desiredTotalFixturesError = false, desiredTotalFixturesErrorMessage = null, // Clear desired fixtures error
            manualRowsError = false, manualRowsErrorMessage = null, // Clear manual rows error
            manualColsError = false, manualColsErrorMessage = null, // Clear manual cols error
            fixtureLumensError = false, fixtureLumensErrorMessage = null,
            fixtureWattageError = false, fixtureWattageErrorMessage = null,
            ceilingReflectanceError = false, ceilingReflectanceErrorMessage = null, // Clear reflectance errors
            wallReflectanceError = false, wallReflectanceErrorMessage = null,
            floorReflectanceError = false, floorReflectanceErrorMessage = null,
            lightLossFactorError = false, lightLossFactorErrorMessage = null,
            errorMessage = null, // Clear general error
            roomAreaSqFt = null,
            totalLumensNeeded = null,
            numFixturesRequired = null,
            recommendedLayoutRows = null,
            recommendedLayoutCols = null,
            spacingLengthFeet = null,
            spacingWidthFeet = null,
            offsetLengthFeet = null,
            offsetWidthFeet = null,
            totalWattage = null,
            wattsPerSqFt = null,
            calculatedFootCandles = null,
            roomCavityRatio = null, // Clear RCR
            coefficientOfUtilization = null // Clear calculated CU
        )

        val lengthFeet = newState.roomLengthFeetStr.toDoubleOrNull()
        val widthFeet = newState.roomWidthFeetStr.toDoubleOrNull()
        val heightFeet = newState.ceilingHeightFeetStr.toDoubleOrNull()
        val workPlaneHeight = newState.workPlaneHeightFeetStr.toDoubleOrNull() // Parse work plane height
        val fixtureLumens = newState.fixtureLumensStr.toDoubleOrNull()
        val fixtureWatts = newState.fixtureWattageStr.toDoubleOrNull()
        val ceilingReflectance = newState.ceilingReflectanceStr.toIntOrNull() // Parse reflectances
        val wallReflectance = newState.wallReflectanceStr.toIntOrNull()
        val floorReflectance = newState.floorReflectanceStr.toIntOrNull()
        val llf = newState.lightLossFactorStr.toDoubleOrNull()

        // Validate common inputs
        if (lengthFeet == null || lengthFeet <= 0) {
            newState = newState.copy(roomLengthFeetError = true, roomLengthFeetErrorMessage = "Invalid Length.")
            hasError = true
        }
        if (widthFeet == null || widthFeet <= 0) {
            newState = newState.copy(roomWidthFeetError = true, roomWidthFeetErrorMessage = "Invalid Width.")
            hasError = true
        }
        if (heightFeet == null || heightFeet <= 0) {
            newState = newState.copy(ceilingHeightFeetError = true, ceilingHeightFeetErrorMessage = "Invalid Height.")
            hasError = true
        }
        if (workPlaneHeight == null || workPlaneHeight < 0) { // Validate work plane height
            newState = newState.copy(workPlaneHeightFeetError = true, workPlaneHeightFeetErrorMessage = "Invalid Work Plane Height.")
            hasError = true
        } else if (heightFeet != null && workPlaneHeight >= heightFeet) { // Validate work plane height vs ceiling height
             newState = newState.copy(workPlaneHeightFeetError = true, workPlaneHeightFeetErrorMessage = "Work plane height must be less than ceiling height.")
             hasError = true
        }
        if (fixtureLumens == null || fixtureLumens <= 0) {
            newState = newState.copy(fixtureLumensError = true, fixtureLumensErrorMessage = "Invalid Lumens.")
            hasError = true
        }
        if (fixtureWatts == null || fixtureWatts < 0) {
            newState = newState.copy(fixtureWattageError = true, fixtureWattageErrorMessage = "Invalid Wattage.")
            hasError = true
        }
        if (ceilingReflectance == null || ceilingReflectance !in 0..100) { // Validate reflectances
            newState = newState.copy(ceilingReflectanceError = true, ceilingReflectanceErrorMessage = "Invalid Reflectance (0-100).")
            hasError = true
        }
         if (wallReflectance == null || wallReflectance !in 0..100) {
            newState = newState.copy(wallReflectanceError = true, wallReflectanceErrorMessage = "Invalid Reflectance (0-100).")
            hasError = true
        }
         if (floorReflectance == null || floorReflectance !in 0..100) {
            newState = newState.copy(floorReflectanceError = true, floorReflectanceErrorMessage = "Invalid Reflectance (0-100).")
            hasError = true
        }
        if (llf == null || llf <= 0 || llf > 1.0) {
            newState = newState.copy(lightLossFactorError = true, lightLossFactorErrorMessage = "Invalid LLF (0-1).")
            hasError = true
        }

        // Validate mode-specific inputs
        when (newState.calculationMode) {
            CalculationMode.CALCULATE_FIXTURES -> {
                val desiredFc = newState.desiredFootCandlesStr.toDoubleOrNull()
                if (desiredFc == null || desiredFc <= 0) {
                    newState = newState.copy(desiredFootCandlesError = true, desiredFootCandlesErrorMessage = "Invalid Foot Candles.")
                    hasError = true
                }
            }
            CalculationMode.CALCULATE_FOOT_CANDLES -> {
        val desiredTotalFixtures = newState.desiredTotalFixturesStr.toIntOrNull()
        if (desiredTotalFixtures == null || desiredTotalFixtures <= 0) {
            newState = newState.copy(desiredTotalFixturesError = true, desiredTotalFixturesErrorMessage = "Invalid number of fixtures.")
            hasError = true
        }
    }
        } // End of when block

        // Validate manual rows/cols if GRID layout is selected in CALCULATE_FIXTURES mode
        if (newState.calculationMode == CalculationMode.CALCULATE_FIXTURES && newState.selectedLayout == LuminaireLayoutType.GRID) {
            val manualRows = newState.manualRowsStr.toIntOrNull()
            val manualCols = newState.manualColsStr.toIntOrNull()

            // If either manual input is provided, both must be valid positive integers
            if (newState.manualRowsStr.isNotBlank() || newState.manualColsStr.isNotBlank()) {
                if (manualRows == null || manualRows <= 0) {
                    newState = newState.copy(manualRowsError = true, manualRowsErrorMessage = "Invalid Rows.")
                    hasError = true
                }
                if (manualCols == null || manualCols <= 0) {
                    newState = newState.copy(manualColsError = true, manualColsErrorMessage = "Invalid Cols.")
                    hasError = true
                }
            }
        }

        // Only proceed with calculation if there are no input errors
        if (hasError) {
            // Update UI state with specific errors and clear results
            uiState = newState // newState already contains the specific errors and cleared results
            return // Stop calculation if there are input errors
        }

        // If no input errors, proceed with calculation
        // Launch a coroutine because the calculator's calculate function is now suspend
        viewModelScope.launch {
            val result = luminaireCalculator.calculate(
                roomLengthFeetStr = newState.roomLengthFeetStr,
                roomWidthFeetStr = newState.roomWidthFeetStr,
                ceilingHeightFeetStr = newState.ceilingHeightFeetStr,
                workPlaneHeightFeetStr = newState.workPlaneHeightFeetStr,
                selectedLayout = newState.selectedLayout,
                desiredFootCandlesStr = newState.desiredFootCandlesStr,
                fixtureLumensStr = newState.fixtureLumensStr,
                fixtureWattageStr = newState.fixtureWattageStr,
                ceilingReflectanceStr = newState.ceilingReflectanceStr, // Pass reflectance strings
                wallReflectanceStr = newState.wallReflectanceStr,
                floorReflectanceStr = newState.floorReflectanceStr,
                lightLossFactorStr = newState.lightLossFactorStr,
                desiredTotalFixturesStr = newState.desiredTotalFixturesStr, // Pass the desired fixtures string
                manualRowsStr = newState.manualRowsStr, // Pass manual rows string
                manualColsStr = newState.manualColsStr, // Pass manual cols string
                calculationMode = newState.calculationMode // Pass the calculation mode
            )

            // Update UI state with calculation results and any general error from the calculator
            uiState = newState.copy( // Start from the state with cleared specific errors
                roomAreaSqFt = result.roomAreaSqFt,
                totalLumensNeeded = result.totalLumensNeeded,
                numFixturesRequired = result.numFixturesRequired,
                recommendedLayoutRows = result.recommendedLayoutRows,
                recommendedLayoutCols = result.recommendedLayoutCols,
                spacingLengthFeet = result.spacingLengthFeet,
                spacingWidthFeet = result.spacingWidthFeet,
                offsetLengthFeet = result.offsetLengthFeet,
                offsetWidthFeet = result.offsetWidthFeet,
                totalWattage = result.totalWattage,
                wattsPerSqFt = result.wattsPerSqFt,
                calculatedFootCandles = result.calculatedFootCandles,
                roomCavityRatio = result.roomCavityRatio,
                coefficientOfUtilization = result.coefficientOfUtilization,
                errorMessage = result.errorMessage // This will show calculation-specific errors
            )
        }
    }

}
