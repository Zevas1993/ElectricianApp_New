package com.example.electricianappnew.ui.calculators.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.electricianappnew.ui.calculators.BendType // Assuming BendType enum is in the Screen file or common location
import com.example.electricianappnew.ui.calculators.PipeBendingData // Assuming data object is accessible
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.* // Import math functions including PI

// Define the UI State data class based on UI needs
data class PipeBendingUiState(
    val bendTypes: List<BendType> = BendType.values().toList(),
    val selectedBendType: BendType = BendType.Offset,
    val commonAngles: List<Double> = listOf(10.0, 22.5, 30.0, 45.0, 60.0), // Example angles
    val selectedAngleStr: String = "30.0", // Default angle as String
    val conduitSizes: List<String> = listOf("1/2\"", "3/4\"", "1\"", "1 1/4\"", "1 1/2\"", "2\""), // Example sizes
    val conduitSizeStr: String = "1/2\"", // Default size
    val offsetDepthStr: String = "",
    val saddleDepthStr: String = "", // Used for 3-point and 4-point obstacle height
    val obstacleWidthStr: String = "", // Added for 4-point saddle
    val stubHeightStr: String = "",
    val mark1: String? = null,
    val mark2: String? = null,
    val mark3: String? = null,
    val mark4: String? = null,
    val travel: Double? = null,
    val shrink: Double? = null,
    val gain: Double? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class PipeBendingViewModel @Inject constructor() : ViewModel() {

    var uiState by mutableStateOf(PipeBendingUiState())
        private set

    // --- Input Change Handlers ---

    fun onBendTypeChange(newType: BendType) {
        uiState = uiState.copy(selectedBendType = newType, errorMessage = null)
        calculateBendingMarks() // Recalculate when type changes
    }

    fun onAngleChange(newAngleStr: String) {
        uiState = uiState.copy(selectedAngleStr = newAngleStr, errorMessage = null)
        calculateBendingMarks() // Recalculate when angle changes
    }

    fun onConduitSizeChange(newSizeStr: String) {
        uiState = uiState.copy(conduitSizeStr = newSizeStr, errorMessage = null)
        calculateBendingMarks() // Recalculate when size changes (for gain/stub)
    }

    fun onOffsetDepthChange(newDepthStr: String) {
        uiState = uiState.copy(offsetDepthStr = newDepthStr, errorMessage = null)
        if (uiState.selectedBendType == BendType.Offset) calculateBendingMarks()
    }

    fun onSaddleDepthChange(newDepthStr: String) {
        uiState = uiState.copy(saddleDepthStr = newDepthStr, errorMessage = null)
        if (uiState.selectedBendType == BendType.ThreePointSaddle || uiState.selectedBendType == BendType.FourPointSaddle) calculateBendingMarks()
    }

    fun onStubHeightChange(newHeightStr: String) {
        uiState = uiState.copy(stubHeightStr = newHeightStr, errorMessage = null)
        if (uiState.selectedBendType == BendType.NinetyDegreeStub) calculateBendingMarks()
    }

     fun onObstacleWidthChange(newWidthStr: String) {
        uiState = uiState.copy(obstacleWidthStr = newWidthStr, errorMessage = null)
        if (uiState.selectedBendType == BendType.FourPointSaddle) calculateBendingMarks()
    }


    fun clearInputs() {
        uiState = PipeBendingUiState() // Reset to default state
    }

    // --- Calculation Logic ---

    private fun calculateBendingMarks() {
        val angle = uiState.selectedAngleStr.toDoubleOrNull()
        val conduitSize = uiState.conduitSizeStr

        // Clear previous results and errors
        uiState = uiState.copy(
            mark1 = null, mark2 = null, mark3 = null, mark4 = null,
            travel = null, shrink = null, gain = null, errorMessage = null
        )

        try {
            when (uiState.selectedBendType) {
                BendType.Offset -> calculateOffset(angle, conduitSize)
                BendType.ThreePointSaddle -> calculateThreePointSaddle(angle, conduitSize)
                BendType.FourPointSaddle -> calculateFourPointSaddle(angle, conduitSize) // Placeholder
                BendType.NinetyDegreeStub -> calculateNinetyDegreeStub(conduitSize)
            }
        } catch (e: Exception) {
            uiState = uiState.copy(errorMessage = "Calculation error: ${e.message}")
        }
    }

    private fun calculateOffset(angle: Double?, conduitSize: String) {
        val depth = uiState.offsetDepthStr.toDoubleOrNull()
        if (angle == null || depth == null || depth <= 0) {
            if (uiState.offsetDepthStr.isNotEmpty()) uiState = uiState.copy(errorMessage = "Invalid angle or depth for offset.")
            return
        }
        val multiplier = PipeBendingData.offsetMultipliers[angle]
        val shrinkPerInch = PipeBendingData.getShrinkPerInch(angle)

        if (multiplier == null || shrinkPerInch == null) {
            uiState = uiState.copy(errorMessage = "Multiplier/Shrink not defined for ${angle}° angle.")
            return
        }

        val distanceBetweenMarks = depth * multiplier
        val calculatedShrink = depth * shrinkPerInch

        uiState = uiState.copy(
            mark1 = "Mark 1: Center of Bend",
            mark2 = "Mark 2: ${distanceBetweenMarks.formatResult(3)}\" from Mark 1",
            shrink = calculatedShrink
        )
    }

     private fun calculateThreePointSaddle(angle: Double?, conduitSize: String) {
        val depth = uiState.saddleDepthStr.toDoubleOrNull()
         if (angle == null || depth == null || depth <= 0) {
             if (uiState.saddleDepthStr.isNotEmpty()) uiState = uiState.copy(errorMessage = "Invalid angle or depth for saddle.")
             return
         }
         val centerMultiplier = PipeBendingData.saddleCenterMultipliers[angle]
         val shrinkPerInch = PipeBendingData.getShrinkPerInch(angle)

         if (centerMultiplier == null || shrinkPerInch == null) {
             uiState = uiState.copy(errorMessage = "Multiplier/Shrink not defined for ${angle}° angle.")
             return
         }

         val mark2Distance = depth * centerMultiplier // Distance from center mark to outer marks
         val calculatedShrink = depth * shrinkPerInch

         uiState = uiState.copy(
             mark1 = "Mark 1: Center of Obstruction",
             mark2 = "Mark 2 & 3: ${mark2Distance.formatResult(3)}\" from Mark 1 (both sides)",
             shrink = calculatedShrink
         )
     }

     private fun calculateFourPointSaddle(angle: Double?, conduitSize: String) {
         val depth = uiState.saddleDepthStr.toDoubleOrNull() // Re-use saddle depth input for obstacle height
         if (angle == null || depth == null || depth <= 0) {
             if (uiState.saddleDepthStr.isNotEmpty()) uiState = uiState.copy(errorMessage = "Invalid angle or depth for 4-point saddle.")
             return
         }
         val multiplier = PipeBendingData.offsetMultipliers[angle] // Same multiplier as offset
         val shrinkPerInch = PipeBendingData.getShrinkPerInch(angle)

         if (multiplier == null || shrinkPerInch == null) {
             uiState = uiState.copy(errorMessage = "Multiplier/Shrink not defined for ${angle}° angle.")
             return
         }

         // 4-point saddle marks are typically based on offset calculations
         // Mark 1: Start of first offset bend
         // Mark 2: End of first offset bend (Distance = depth * multiplier)
         // Mark 3: Start of second offset bend
         // Mark 4: End of second offset bend
         val obstacleWidth = uiState.obstacleWidthStr.toDoubleOrNull()
         if (obstacleWidth == null || obstacleWidth <= 0) {
              if (uiState.obstacleWidthStr.isNotEmpty()) uiState = uiState.copy(errorMessage = "Invalid obstacle width for 4-point saddle.")
             // Clear marks 3 and 4 if width is invalid, keep marks 1 and 2
             uiState = uiState.copy(mark3 = null, mark4 = null, shrink = null)
             return
         }

         val distanceBetweenMarks = depth * multiplier
         val calculatedShrink = depth * shrinkPerInch * 2 // Shrink occurs for both offsets
         val mark3Position = distanceBetweenMarks + obstacleWidth // Distance from Mark 1

         uiState = uiState.copy(
             mark1 = "Mark 1: Start Bend 1",
             mark2 = "Mark 2: ${distanceBetweenMarks.formatResult(3)}\" from Mark 1",
             mark3 = "Mark 3: ${mark3Position.formatResult(3)}\" from Mark 1",
             mark4 = "Mark 4: ${distanceBetweenMarks.formatResult(3)}\" from Mark 3",
             shrink = calculatedShrink,
             errorMessage = null // Clear previous note if calculation succeeds
         )
     }


    private fun calculateNinetyDegreeStub(conduitSize: String) {
        val height = uiState.stubHeightStr.toDoubleOrNull()
        if (height == null || height <= 0) {
            if (uiState.stubHeightStr.isNotEmpty()) uiState = uiState.copy(errorMessage = "Invalid height for stub-up.")
            return
        }
        val gain = PipeBendingData.getGain(conduitSize) // Using placeholder gain

        val markLocation = height - gain

        uiState = uiState.copy(
            mark1 = "Mark at: ${markLocation.formatResult(3)}\" from end",
            gain = gain
        )
    }

    // Helper to format results nicely
    private fun Double.formatResult(decimals: Int = 3): String {
        return String.format("%.${decimals}f", this).trimEnd('0').trimEnd('.')
    }
}
