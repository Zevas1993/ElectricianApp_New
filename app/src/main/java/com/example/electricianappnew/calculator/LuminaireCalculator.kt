package com.example.electricianappnew.calculator

import com.example.electricianappnew.data.repository.NecDataRepository
import com.example.electricianappnew.data.jsonmodels.NecCuTable
import com.example.electricianappnew.data.jsonmodels.NecCuValue
import com.example.electricianappnew.ui.calculators.viewmodel.CalculationMode
import com.example.electricianappnew.ui.calculators.viewmodel.LuminaireLayoutType
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlin.math.*

// --- Constants ---
const val DEFAULT_S_MH_RATIO = 1.0 // Typical S/MH ratio for general lighting

// --- Custom Exception ---
class CalculationException(message: String) : Exception(message)

// --- Calculation Result Data Class ---
data class LuminaireCalculationResult(
    val roomAreaSqFt: Double? = null,
    val totalLumensNeeded: Double? = null,
    val numFixturesRequired: Int? = null, // Number of fixtures required based on desired FC (Mode: CALCULATE_FIXTURES)
    val recommendedLayoutRows: Int? = null,
    val recommendedLayoutCols: Int? = null,
    val spacingLengthFeet: Double? = null,
    val spacingWidthFeet: Double? = null,
    val offsetLengthFeet: Double? = null,
    val offsetWidthFeet: Double? = null,
    val totalWattage: Double? = null,
    val wattsPerSqFt: Double? = null,
    val calculatedFootCandles: Double? = null, // Calculated FC based on actual layout (Mode: CALCULATE_FOOT_CANDLES or fixed layouts in CALCULATE_FIXTURES)
    val roomCavityRatio: Double? = null,
    val coefficientOfUtilization: Double? = null, // This is the CU used in calculations (either input or looked up)
    val errorMessage: String? = null
)

// Main calculation logic class
class LuminaireCalculator @Inject constructor(
    private val necDataRepository: NecDataRepository // Inject NecDataRepository
) {

    /**
     * Calculates the Room Cavity Ratio (RCR).
     * RCR = 2.5 * Room Cavity Height * (Room Length + Room Width) / (Room Length * Room Width)
     * Room Cavity Height = Ceiling Height - Workplane Height
     */
    private fun calculateRoomCavityRatio(
        roomLength: Double,
        roomWidth: Double,
        ceilingHeight: Double,
        workPlaneHeight: Double
    ): Double {
        val roomCavityHeight = ceilingHeight - workPlaneHeight
        if (roomCavityHeight <= 0 || roomLength <= 0 || roomWidth <= 0) {
            // This case should ideally be caught by ViewModel validation, but handle defensively.
            return 0.0
        }
        return 2.5 * roomCavityHeight * (roomLength + roomWidth) / (roomLength * roomWidth)
    }

    /**
     * Finds the grid layout (rows x cols) for a given number of fixtures
     * that best matches the room's aspect ratio.
     * This is a helper for the CALCULATE_FOOT_CANDLES mode with GRID layout.
     */
    private fun findBestGridLayout(numFixtures: Int, roomLength: Double, roomWidth: Double): Pair<Int, Int> {
        if (numFixtures <= 0) return Pair(0, 0)
        if (roomLength <= 0 || roomWidth <= 0) return Pair(1, numFixtures) // Default if room dimensions invalid

        val roomAspectRatio = roomLength / roomWidth
        var minAspectRatioDiff = Double.MAX_VALUE // Initialize minAspectRatioDiff
        var bestRows = 0 // Initialize bestRows
        var bestCols = 0 // Initialize bestCols

        for (i in 1..numFixtures) {
            if (numFixtures % i == 0) {
                val rows = i
                val cols = numFixtures / i
                val aspectRatioDiff = abs((cols.toDouble() / rows.toDouble()) - roomAspectRatio)
                if (aspectRatioDiff < minAspectRatioDiff) {
                    minAspectRatioDiff = aspectRatioDiff
                    bestRows = rows
                    bestCols = cols
                }
            }
        }
        return Pair(bestRows, bestCols)
    }

    /**
     * Finds an optimal grid layout (rows x cols) for a given minimum number of fixtures
     * that meets or exceeds the required number and satisfies the S/MH ratio limit.
     * This is a helper for the CALCULATE_FIXTURES mode with GRID layout.
     * It searches for a grid layout (rows x cols) that meets or exceeds the minimum required fixtures
     * and satisfies the S/MH ratio limit, prioritizing the layout with the fewest total fixtures.
     */
    private fun findOptimalGridLayout(minNumFixtures: Int, roomLength: Double, roomWidth: Double, mountingHeight: Double): Pair<Int, Int> {
        if (minNumFixtures <= 0 || roomLength <= 0 || roomWidth <= 0 || mountingHeight <= 0) {
            return Pair(0, 0) // Invalid input
        }

        var bestRows = 0
        var bestCols = 0
        var minTotalFixturesFound = Int.MAX_VALUE

        // Calculate maximum possible rows and columns based on S/MH ratio limit
        // Add a small epsilon to avoid floating point issues with floor
        val maxPossibleRows = floor(roomLength / (mountingHeight * DEFAULT_S_MH_RATIO) + 1e-9).toInt() + 1
        val maxPossibleCols = floor(roomWidth / (mountingHeight * DEFAULT_S_MH_RATIO) + 1e-9).toInt() + 1

        // Iterate through possible rows and columns up to the calculated maximums
        for (r in 1..maxPossibleRows) {
            for (c in 1..maxPossibleCols) {
                val totalFixtures = r * c

                // Only consider layouts that meet or exceed the minimum required fixtures
                if (totalFixtures < minNumFixtures) {
                    continue
                }

                // Calculate spacing in both directions
                // Note: Spacing is typically calculated as Room Dimension / (Number of Fixtures - 1) for layouts with fixtures at the ends.
                // However, the current calculation uses Room Dimension / Number of Fixtures, which implies fixtures are centered within bays.
                // The offset calculation (spacing / 2) supports the centered-within-bay model.
                // Let's stick to the current spacing calculation logic (Dim / Num Fixtures) for consistency with the offset logic,
                // but acknowledge this is a simplification. A more accurate model might use (Dim / (Num Fixtures - 1)) for spacing
                // and a different offset calculation, or consider different layout patterns (e.g., fixtures at walls vs. centered).
                // For now, I will keep the current spacing calculation logic but update the loop bounds.
                val spacingLength = roomLength / r
                val spacingWidth = roomWidth / c

                // Calculate S/MH ratios
                val smhLength = spacingLength / mountingHeight
                val smhWidth = spacingWidth / mountingHeight

                // Check if both S/MH ratios are within the acceptable limit
                if (smhLength <= DEFAULT_S_MH_RATIO && smhWidth <= DEFAULT_S_MH_RATIO) {
                    // Found a valid layout. If it uses fewer fixtures than the best found so far, update.
                    if (totalFixtures < minTotalFixturesFound) {
                        minTotalFixturesFound = totalFixtures
                        bestRows = r
                        bestCols = c
                    }
                }
            }
        }

        // If no valid layout was found within the search limits, bestRows will still be 0.
        // This could happen if the required number of fixtures is very high or the S/MH ratio limit is strict.
        // The calling function should handle the case where bestRows or bestCols is 0.
        if (bestRows == 0 || bestCols == 0) {
             // Consider adding a more specific error message here if needed,
             // but for now, returning 0,0 is handled by the main calculate function.
             return Pair(0, 0)
        }

        return Pair(bestRows, bestCols)
    }

    /**
     * Looks up the Coefficient of Utilization (CU) from the NEC data based on RCR and reflectances.
     * It performs linear interpolation if the exact RCR is not found in the table.
     *
     * @param rcr Room Cavity Ratio.
     * @param ceilingReflectance Ceiling reflectance percentage (0-100).
     * @param wallReflectance Wall reflectance percentage (0-100).
     * @param floorReflectance Floor reflectance percentage (0-100).
     * @return The Coefficient of Utilization or null if no matching table is found or RCR is invalid.
     */
    private suspend fun lookupCoefficientOfUtilization(
        rcr: Double,
        ceilingReflectance: Int,
        wallReflectance: Int,
        floorReflectance: Int
    ): Double? {
        // Get all CU tables from the repository
        val cuTables = necDataRepository.getAllCuTables().firstOrNull() ?: return null

        // Find the CU table that matches the provided reflectances
        val matchingTable = cuTables.find { table ->
            table.ceilingReflectance == ceilingReflectance &&
            table.wallReflectance == wallReflectance &&
            table.floorReflectance == floorReflectance
        } ?: return null // Return null if no matching table is found

        val cuValues = matchingTable.cuValues.sortedBy { it.rcr }

        if (cuValues.isEmpty()) {
            return null // Should not happen if table exists, but defensive check
        }

        // Handle RCR values outside the table range
        if (rcr <= cuValues.first().rcr) {
            return cuValues.first().cu
        }
        if (rcr >= cuValues.last().rcr) {
            return cuValues.last().cu
        }

        // Find the two CU values that bracket the calculated RCR
        val lower = cuValues.last { it.rcr <= rcr }
        val upper = cuValues.first { it.rcr >= rcr }

        // Perform linear interpolation
        return if (lower.rcr == upper.rcr) {
            lower.cu // Should only happen if rcr exactly matches a table value
        } else {
            val fraction = (rcr - lower.rcr) / (upper.rcr - lower.rcr)
            lower.cu + fraction * (upper.cu - lower.cu)
        }
    }


    /**
     * Performs the luminaire layout calculations based on the provided inputs.
     * Supports two modes:
     * 1. Calculate required fixtures and suggest layout based on desired foot candles.
     * 2. Calculate resulting foot candles and suggest layout based on a desired number of fixtures.
     *
     * @param roomLengthFeetStr Room length in feet (string).
     * @param roomWidthFeetStr Room width in feet (string).
     * @param ceilingHeightFeetStr Ceiling height in feet (string).
     * @param workPlaneHeightFeetStr Work plane height in feet (string).
     * @param selectedLayout The selected layout type (GRID, TWO_BY_TWO, etc.).
     * @param desiredFootCandlesStr Desired foot candles (string).
     * @param fixtureLumensStr Fixture lumens (string).
     * @param fixtureWattageStr Fixture wattage (string).
     * @param ceilingReflectanceStr Ceiling reflectance percentage (string).
     * @param wallReflectanceStr Wall reflectance percentage (string).
     * @param floorReflectanceStr Floor reflectance percentage (string).
     * @param lightLossFactorStr Light Loss Factor (string).
     * @param desiredTotalFixturesStr Desired total number of fixtures (string, optional, used in CALCULATE_FOOT_CANDLES mode).
     * @param calculationMode The calculation mode (CALCULATE_FIXTURES or CALCULATE_FOOT_CANDLES).
     * @return LuminaireCalculationResult containing the calculation results or an error message.
     */
    suspend fun calculate( // Make calculate suspend function
        roomLengthFeetStr: String,
        roomWidthFeetStr: String,
        ceilingHeightFeetStr: String,
        workPlaneHeightFeetStr: String,
        selectedLayout: LuminaireLayoutType,
        desiredFootCandlesStr: String,
        fixtureLumensStr: String,
        fixtureWattageStr: String,
        ceilingReflectanceStr: String, // Add reflectance inputs
        wallReflectanceStr: String,
        floorReflectanceStr: String,
        lightLossFactorStr: String,
        desiredTotalFixturesStr: String,
        manualRowsStr: String, // Add manual rows input
        manualColsStr: String, // Add manual cols input
        calculationMode: CalculationMode
    ): LuminaireCalculationResult {

        // Parse inputs (ViewModel should handle basic validation, but parse here)
        val lengthFeet = roomLengthFeetStr.toDoubleOrNull() ?: return LuminaireCalculationResult(errorMessage = "Invalid input for Room Length. Please enter a valid number.")
        val widthFeet = roomWidthFeetStr.toDoubleOrNull() ?: return LuminaireCalculationResult(errorMessage = "Invalid input for Room Width. Please enter a valid number.")
        val heightFeet = ceilingHeightFeetStr.toDoubleOrNull() ?: return LuminaireCalculationResult(errorMessage = "Invalid input for Ceiling Height. Please enter a valid number.")
        val workPlaneHeight = workPlaneHeightFeetStr.toDoubleOrNull() ?: return LuminaireCalculationResult(errorMessage = "Invalid input for Work Plane Height. Please enter a valid number.")
        val fixtureLumens = fixtureLumensStr.toDoubleOrNull() ?: return LuminaireCalculationResult(errorMessage = "Invalid input for Fixture Lumens. Please enter a valid number.")
        val fixtureWatts = fixtureWattageStr.toDoubleOrNull() ?: return LuminaireCalculationResult(errorMessage = "Invalid input for Fixture Wattage. Please enter a valid number.")
        val ceilingReflectance = ceilingReflectanceStr.toIntOrNull() ?: return LuminaireCalculationResult(errorMessage = "Invalid input for Ceiling Reflectance. Please enter a valid integer.") // Parse reflectances
        val wallReflectance = wallReflectanceStr.toIntOrNull() ?: return LuminaireCalculationResult(errorMessage = "Invalid input for Wall Reflectance. Please enter a valid integer.")
        val floorReflectance = floorReflectanceStr.toIntOrNull() ?: return LuminaireCalculationResult(errorMessage = "Invalid input for Floor Reflectance. Please enter a valid integer.")
        val llf = lightLossFactorStr.toDoubleOrNull() ?: return LuminaireCalculationResult(errorMessage = "Invalid input for Light Loss Factor (LLF). Please enter a valid number.")

        // Calculation mode specific inputs
        val desiredFc = if (calculationMode == CalculationMode.CALCULATE_FIXTURES) {
            desiredFootCandlesStr.toDoubleOrNull() ?: return LuminaireCalculationResult(errorMessage = "Invalid input for Desired Foot Candles. Please enter a valid number.")
        } else null

        val desiredTotalFixtures = if (calculationMode == CalculationMode.CALCULATE_FOOT_CANDLES) {
            desiredTotalFixturesStr.toIntOrNull() ?: return LuminaireCalculationResult(errorMessage = "Invalid input for Desired Total Fixtures. Please enter a valid integer.")
        } else null


        // Additional validation
        if (lengthFeet <= 0 || widthFeet <= 0 || heightFeet <= 0 || workPlaneHeight < 0) {
             return LuminaireCalculationResult(errorMessage = "Room dimensions (Length, Width, Height) must be positive, and Work Plane Height cannot be negative.")
        }
        if (workPlaneHeight >= heightFeet) {
             return LuminaireCalculationResult(errorMessage = "Work plane height must be less than ceiling height.")
        }
        if (fixtureLumens <= 0) {
             return LuminaireCalculationResult(errorMessage = "Fixture lumens must be positive.")
        }
        if (fixtureWatts < 0) {
             return LuminaireCalculationResult(errorMessage = "Fixture wattage cannot be negative.")
        }
        if (ceilingReflectance !in 0..100 || wallReflectance !in 0..100 || floorReflectance !in 0..100) { // Validate reflectances
             return LuminaireCalculationResult(errorMessage = "Reflectance values must be between 0 and 100.")
        }
        if (llf <= 0 || llf > 1.0) {
             return LuminaireCalculationResult(errorMessage = "Light Loss Factor (LLF) must be between 0 and 1.")
        }
        if (calculationMode == CalculationMode.CALCULATE_FIXTURES && (desiredFc == null || desiredFc <= 0)) {
             return LuminaireCalculationResult(errorMessage = "Desired Foot Candles must be positive in this mode.")
        }
         if (calculationMode == CalculationMode.CALCULATE_FOOT_CANDLES && (desiredTotalFixtures == null || desiredTotalFixtures <= 0)) {
             return LuminaireCalculationResult(errorMessage = "Desired Total Fixtures must be positive in this mode.")
        }


        try {
            // 1. Calculate Room Area
            val areaSqFt = lengthFeet * widthFeet

            // 2. Calculate Mounting Height
            val mountingHeight = heightFeet - workPlaneHeight
            if (mountingHeight <= 0) {
                 throw CalculationException("Mounting height must be greater than 0.")
            }

            // Calculate Room Cavity Ratio (RCR)
            val rcr = calculateRoomCavityRatio(lengthFeet, widthFeet, heightFeet, workPlaneHeight)

            // Lookup Coefficient of Utilization (CU) using the new function and reflectances
            val calculatedCu = lookupCoefficientOfUtilization(rcr, ceilingReflectance, wallReflectance, floorReflectance)

            if (calculatedCu == null) {
                 return LuminaireCalculationResult(errorMessage = "Coefficient of Utilization data not found for reflectances: Ceiling $ceilingReflectance%, Wall $wallReflectance%, Floor $floorReflectance%. Please ensure this data is available in nec_data.json.")
            }

            val effectiveLumensPerFixture = fixtureLumens * calculatedCu * llf
            if (effectiveLumensPerFixture <= 0) {
                 // This check is still relevant even if CU is found, as LLF or lumens could be zero/negative
                 throw CalculationException("Effective lumens per fixture is zero or less. Check fixture lumens, CU, and LLF.")
            }

            var finalRows: Int = 0
            var finalCols: Int = 0
            var actualFixturesInLayout: Int = 0
            var calculatedFootCandlesResult: Double? = null
            var numFixturesRequiredResult: Int? = null // Result for required fixtures based on desired FC

            when (calculationMode) {
                CalculationMode.CALCULATE_FIXTURES -> {
                    // --- Mode 1: Calculate required fixtures and suggest layout based on desired FC ---

                    // Calculate minimum required fixtures based on desired foot candles (Lumen Method)
                    val minNumFixturesRequired = ceil((desiredFc ?: 0.0) * areaSqFt / effectiveLumensPerFixture).toInt()

                    if (minNumFixturesRequired <= 0) {
                         throw CalculationException("Calculated minimum number of fixtures is zero or less. Consider increasing desired foot candles or fixture lumens.")
                    }
                    numFixturesRequiredResult = minNumFixturesRequired

                    // Determine layout based on selected type
                    when (selectedLayout) {
                        LuminaireLayoutType.GRID -> {
                            // Use manual inputs if provided and valid, otherwise find optimal grid
                            val manualRows = manualRowsStr.toIntOrNull() // Access manual inputs directly
                            val manualCols = manualColsStr.toIntOrNull()

                            if (manualRows != null && manualRows > 0 && manualCols != null && manualCols > 0) {
                                finalRows = manualRows
                                finalCols = manualCols
                                actualFixturesInLayout = finalRows * finalCols
                                // Check if the manually entered layout meets the minimum required fixtures
                                if (actualFixturesInLayout < minNumFixturesRequired) {
                                     throw CalculationException("Manually entered layout ($finalRows x $finalCols = $actualFixturesInLayout fixtures) does not meet the minimum required fixtures ($minNumFixturesRequired). Please increase the number of rows or columns.")
                                }
                                // Calculate spacing and check S/MH ratio for manually entered layout
                                val manualSpacingLength = if (finalRows > 1) lengthFeet / (finalRows - 1.0) else lengthFeet
                                val manualSpacingWidth = if (finalCols > 1) widthFeet / (finalCols - 1.0) else widthFeet
                                val manualSmhLength = manualSpacingLength / mountingHeight
                                val manualSmhWidth = manualSpacingWidth / mountingHeight
                                if (manualSmhLength > DEFAULT_S_MH_RATIO || manualSmhWidth > DEFAULT_S_MH_RATIO) {
                                     throw CalculationException("Manually entered layout ($finalRows x $finalCols) exceeds the recommended S/MH ratio of ${DEFAULT_S_MH_RATIO}. Consider adjusting the layout or mounting height.")
                                }

                            } else {
                // Find an optimal grid layout that meets or exceeds the required fixtures
                val (bestRows, bestCols) = findOptimalGridLayout(minNumFixturesRequired, lengthFeet, widthFeet, mountingHeight)

                if (bestRows == 0 || bestCols == 0) {
                     throw CalculationException("Could not find a suitable grid layout that meets the requirements and spacing limits. Consider adjusting room dimensions, mounting height, or desired foot candles.")
                }

                finalRows = bestRows
                finalCols = bestCols
                actualFixturesInLayout = finalRows * finalCols // Actual fixtures in the chosen grid layout
            }
            calculatedFootCandlesResult = (actualFixturesInLayout * effectiveLumensPerFixture) / areaSqFt // Calculate actual FC for this layout

        }
        LuminaireLayoutType.TWO_BY_TWO -> {
            finalRows = 2
                            finalCols = 2
                            actualFixturesInLayout = finalRows * finalCols
                            calculatedFootCandlesResult = (actualFixturesInLayout * effectiveLumensPerFixture) / areaSqFt
                            // Note: The UI should indicate if this fixed layout meets the desired FC target.
                        }
                        LuminaireLayoutType.TWO_BY_THREE -> {
                            finalRows = 2
                            finalCols = 3
                            actualFixturesInLayout = finalRows * finalCols
                            calculatedFootCandlesResult = (actualFixturesInLayout * effectiveLumensPerFixture) / areaSqFt
                        }
                        LuminaireLayoutType.THREE_BY_THREE -> {
                            finalRows = 3
                            finalCols = 3
                            actualFixturesInLayout = finalRows * finalCols
                            calculatedFootCandlesResult = (actualFixturesInLayout * effectiveLumensPerFixture) / areaSqFt
                        }
                        LuminaireLayoutType.TWO_BY_FOUR -> {
                            finalRows = 2
                            finalCols = 4
                            actualFixturesInLayout = finalRows * finalCols
                            calculatedFootCandlesResult = (actualFixturesInLayout * effectiveLumensPerFixture) / areaSqFt
                        }
                        LuminaireLayoutType.THREE_BY_FOUR -> {
                            finalRows = 3
                            finalCols = 4
                            actualFixturesInLayout = finalRows * finalCols
                            calculatedFootCandlesResult = (actualFixturesInLayout * effectiveLumensPerFixture) / areaSqFt
                        }
                        LuminaireLayoutType.FOUR_BY_FOUR -> {
                            finalRows = 4
                            finalCols = 4
                            actualFixturesInLayout = finalRows * finalCols
                            calculatedFootCandlesResult = (actualFixturesInLayout * effectiveLumensPerFixture) / areaSqFt
                        }
                    }
                }
                CalculationMode.CALCULATE_FOOT_CANDLES -> {
                    // --- Mode 2: Calculate resulting FC and suggest layout based on a desired number of fixtures ---

                    numFixturesRequiredResult = null // Not calculating required fixtures in this mode

                    when (selectedLayout) {
                        LuminaireLayoutType.GRID -> {
                            actualFixturesInLayout = desiredTotalFixtures ?: 0 // Use desired total fixtures for calculation
                            if (actualFixturesInLayout <= 0) {
                                throw CalculationException("Desired number of fixtures must be positive.")
                            }
                            // Find the best grid layout for the desired number of fixtures
                            val (bestRows, bestCols) = findBestGridLayout(actualFixturesInLayout, lengthFeet, widthFeet)
                            finalRows = bestRows
                            finalCols = bestCols
                        }
                        LuminaireLayoutType.TWO_BY_TWO -> {
                            finalRows = 2
                            finalCols = 2
                            actualFixturesInLayout = finalRows * finalCols
                        }
                        LuminaireLayoutType.TWO_BY_THREE -> {
                            finalRows = 2
                            finalCols = 3
                            actualFixturesInLayout = finalRows * finalCols
                        }
                        LuminaireLayoutType.THREE_BY_THREE -> {
                            finalRows = 3
                            finalCols = 3
                            actualFixturesInLayout = finalRows * finalCols
                        }
                        LuminaireLayoutType.TWO_BY_FOUR -> {
                            finalRows = 2
                            finalCols = 4
                            actualFixturesInLayout = finalRows * finalCols
                        }
                        LuminaireLayoutType.THREE_BY_FOUR -> {
                            finalRows = 3
                            finalCols = 4
                            actualFixturesInLayout = finalRows * finalCols
                        }
                        LuminaireLayoutType.FOUR_BY_FOUR -> {
                            finalRows = 4
                            finalCols = 4
                            actualFixturesInLayout = finalRows * finalCols
                        }
                    }

                    // Calculate resulting foot candles based on the actual number of fixtures in the determined layout
                    if (actualFixturesInLayout <= 0) {
                         throw CalculationException("Calculated number of fixtures in layout is zero or less.")
                    }
                    calculatedFootCandlesResult = (actualFixturesInLayout * effectiveLumensPerFixture) / areaSqFt
                }
            }


            // Calculate spacing and offset based on the final determined layout (finalRows, finalCols)
            // These are the dimensions for the *recommended* layout diagram.
            // For GRID layout, use (N-1) for spacing calculation for fixtures at ends.
            // For fixed layouts, use N for spacing calculation for fixtures centered in bays.
            val finalSpacingLengthFeet = when (selectedLayout) {
                LuminaireLayoutType.GRID -> if (finalRows > 1) lengthFeet / (finalRows - 1.0) else lengthFeet
                else -> if (finalRows > 0) lengthFeet / finalRows else lengthFeet // Use N for fixed layouts
            }
            val finalSpacingWidthFeet = when (selectedLayout) {
                LuminaireLayoutType.GRID -> if (finalCols > 1) widthFeet / (finalCols - 1.0) else widthFeet
                else -> if (finalCols > 0) widthFeet / finalCols else widthFeet // Use N for fixed layouts
            }

            val finalOffsetLengthFeet = when (selectedLayout) {
                LuminaireLayoutType.GRID -> if (finalRows > 0) finalSpacingLengthFeet / 2.0 else 0.0 // Offset is half of the spacing
                else -> if (finalRows > 0) finalSpacingLengthFeet / 2.0 else 0.0 // Offset is half of the spacing for fixed layouts too
            }
            val finalOffsetWidthFeet = when (selectedLayout) {
                LuminaireLayoutType.GRID -> if (finalCols > 0) finalSpacingWidthFeet / 2.0 else 0.0 // Offset is half of the spacing
                else -> if (finalCols > 0) finalSpacingWidthFeet / 2.0 else 0.0 // Offset is half of the spacing for fixed layouts too
            }


            // Ensure actualFixturesInLayout is positive before proceeding with wattage calculations
            if (actualFixturesInLayout <= 0) {
                 // This should ideally not happen if input validation and layout finding are correct,
                 // but as a safeguard.
                 throw CalculationException("Calculated number of fixtures in layout is zero or less.")
            }

            val totalWatts = actualFixturesInLayout * fixtureWatts

            // Calculate Watts per Square Foot based on the actual number of fixtures in the layout
            val wattsSqFt = totalWatts / areaSqFt

            // Return Results
            return LuminaireCalculationResult(
                roomAreaSqFt = areaSqFt,
                totalLumensNeeded = (desiredFc ?: 0.0) * areaSqFt, // Calculate total lumens needed based on desired FC (0 if not in that mode)
                numFixturesRequired = numFixturesRequiredResult, // Only set if calculating required fixtures
                recommendedLayoutRows = finalRows,
                recommendedLayoutCols = finalCols,
                spacingLengthFeet = finalSpacingLengthFeet,
                spacingWidthFeet = finalSpacingWidthFeet,
                offsetLengthFeet = finalOffsetLengthFeet,
                offsetWidthFeet = finalOffsetWidthFeet,
                totalWattage = totalWatts,
                wattsPerSqFt = wattsSqFt,
                calculatedFootCandles = calculatedFootCandlesResult, // Set if calculating resulting FC or for fixed layouts
                roomCavityRatio = rcr,
                coefficientOfUtilization = calculatedCu, // The CU value used in the calculation
                errorMessage = null // Clear error on success
            )

        } catch (e: CalculationException) {
             return LuminaireCalculationResult(errorMessage = "Calculation Error: ${e.message}")
        } catch (e: Exception) {
            // Catch any other unexpected errors
            return LuminaireCalculationResult(errorMessage = "An unexpected error occurred: ${e.message}")
        }
    }
}
