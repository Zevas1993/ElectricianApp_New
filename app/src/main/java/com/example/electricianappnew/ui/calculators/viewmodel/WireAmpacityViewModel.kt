package com.example.electricianappnew.ui.calculators.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.repository.NecDataRepository // Import repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

// Data class for UI State
data class WireAmpacityUiState(
    val selectedMaterial: String = "Copper",
    val selectedSize: String = "12 AWG",
    val selectedInsulation: String = "THHN/THWN-2", // Default to 90C
    val ambientTempStr: String = "30",
    val numConductorsStr: String = "3",
    val baseAmpacity: Double? = null,
    val tempCorrectionFactor: Double? = null,
    val conductorAdjustmentFactor: Double? = null,
    val adjustedAmpacity: Double? = null,
    val errorMessage: String? = null
    // Dropdown lists are now separate state in ViewModel
)

// Placeholder object for factors not yet in DB/Repo - REMOVED

@HiltViewModel
class WireAmpacityViewModel @Inject constructor(
    private val necDataRepository: NecDataRepository // Inject repository
) : ViewModel() {

    var uiState by mutableStateOf(WireAmpacityUiState())
        private set

    // State for dropdown options
    var materials by mutableStateOf(listOf("Copper", "Aluminum")) // Keep simple for now
        private set
    var wireSizes by mutableStateOf<List<String>>(emptyList())
        private set
    // Store insulation as Pair(Name, TempRating) for easier use
    var insulationOptions by mutableStateOf<List<Pair<String, Int>>>(emptyList())
        private set

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Fetch wire sizes (assuming they are mostly independent of material/temp for the list)
            wireSizes = necDataRepository.getDistinctAmpacityWireSizes()
            // Fetch distinct temp ratings to build insulation options map/list
            val ratings = necDataRepository.getDistinctAmpacityTempRatings()
            // Create a more structured insulation list (assuming names based on ratings)
            // TODO: Ideally, fetch distinct insulation *names* and their ratings if DB stores them
            insulationOptions = ratings.mapNotNull { rating ->
                when (rating) {
                    60 -> "TW/UF" to 60
                    75 -> "THW/THWN/XHHW" to 75
                    90 -> "THHN/THWN-2/XHHW-2" to 90
                    else -> null // Ignore unknown ratings
                }
            }.sortedBy { it.second } // Sort by temp rating

            // Set initial defaults
            val initialSize = wireSizes.firstOrNull { it == "12 AWG" } ?: wireSizes.firstOrNull() ?: ""
            val initialInsulationPair = insulationOptions.firstOrNull { it.second == 90 } ?: insulationOptions.firstOrNull()
            val initialMaterial = materials.first()

            uiState = uiState.copy(
                selectedMaterial = initialMaterial,
                selectedSize = initialSize,
                selectedInsulation = initialInsulationPair?.first ?: ""
            )
            // Trigger initial calculation if defaults are valid
            if (initialSize.isNotEmpty() && initialInsulationPair != null) {
                calculateAmpacity()
            }
        }
    }


    fun onMaterialChange(newValue: String) {
        uiState = uiState.copy(selectedMaterial = newValue, errorMessage = null)
        calculateAmpacity()
    }

    fun onSizeChange(newValue: String) {
        uiState = uiState.copy(selectedSize = newValue, errorMessage = null)
        calculateAmpacity()
    }

    fun onInsulationChange(newValue: String) {
        uiState = uiState.copy(selectedInsulation = newValue, errorMessage = null)
        calculateAmpacity()
    }

    fun onAmbientTempChange(newValue: String) {
        uiState = uiState.copy(ambientTempStr = newValue, errorMessage = null)
        calculateAmpacity()
    }

    fun onNumConductorsChange(newValue: String) {
        uiState = uiState.copy(numConductorsStr = newValue, errorMessage = null)
        calculateAmpacity()
    }

    fun clearInputs() {
        // Reset to initial state, which will trigger loadInitialData implicitly if needed
        // or just reset the fields and call calculate
        val initialSize = wireSizes.firstOrNull { it == "12 AWG" } ?: wireSizes.firstOrNull() ?: ""
        val initialInsulationPair = insulationOptions.firstOrNull { it.second == 90 } ?: insulationOptions.firstOrNull()
        val initialMaterial = materials.first()
        uiState = WireAmpacityUiState(
            selectedMaterial = initialMaterial,
            selectedSize = initialSize,
            selectedInsulation = initialInsulationPair?.first ?: ""
        )
        calculateAmpacity() // Recalculate with defaults
    }

    // Make calculate public to be called from Screen's LaunchedEffect or input changes
    fun calculateAmpacity() {
        viewModelScope.launch {
            // Find the temp rating associated with the selected insulation name
            val tempRating = insulationOptions.find { it.first == uiState.selectedInsulation }?.second
            val ambientTempC = uiState.ambientTempStr.toDoubleOrNull()
            val numConductors = uiState.numConductorsStr.toIntOrNull()

            if (tempRating == null || ambientTempC == null || numConductors == null || numConductors <= 0) {
                uiState = uiState.copy(
                    errorMessage = "Invalid input values.",
                    baseAmpacity = null, tempCorrectionFactor = null,
                    conductorAdjustmentFactor = null, adjustedAmpacity = null
                )
                return@launch
            }

            var error: String? = null
            var base: Double? = null
            var tempFactor: Double? = null
            var adjFactor: Double? = null
            var adjustedAmpacityPreTermination: Double? = null
            var finalAmpacity: Double? = null

            try {
                // 1. Fetch base ampacity from repository
                val ampacityEntry = necDataRepository.getAmpacityEntry(uiState.selectedMaterial, uiState.selectedSize, tempRating)
                base = ampacityEntry?.ampacity

                if (base == null) {
                    error = "Base ampacity not found for ${uiState.selectedMaterial} ${uiState.selectedSize} @ ${tempRating}°C."
                } else {
                    // 2. Get Correction/Adjustment Factors from repository
                    val tempCorrectionEntry = necDataRepository.getTempCorrectionFactorEntry(tempRating, ambientTempC)
                    tempFactor = tempCorrectionEntry?.correctionFactor

                    val conductorAdjustmentEntry = necDataRepository.getConductorAdjustmentFactorEntry(numConductors)
                    adjFactor = conductorAdjustmentEntry?.adjustmentFactor

                    if (tempFactor == null) {
                        error = "Ambient temperature correction factor not found in database."
                    } else if (adjFactor == null) {
                        error = "Conductor adjustment factor not found in database for $numConductors conductors."
                    } else {
                        // 3. Calculate Adjusted Ampacity (before termination limits)
                        adjustedAmpacityPreTermination = base * tempFactor * adjFactor

                        // 4. Apply Termination Temperature Limits (NEC 110.14(C))
                        // Simplified logic: Assume 75C limit for > 100A or > 1 AWG, else 60C, unless 90C is allowed.
                        // TODO: Refine this logic based on more specific rules or user input if needed.
                        val terminationLimitRating = when {
                            // Check if 90C is explicitly allowed (e.g., equipment listing) - Needs UI input? Assume no for now.
                            // else -> if (base > 100 || isSizeGreaterThan1AWG(uiState.selectedSize)) 75 else 60 // Simplified check
                             isSizeGreaterThan1AWG(uiState.selectedSize) -> 75 // Simplified: Use 75C for > 1 AWG
                             else -> 60 // Use 60C for 1 AWG and smaller
                        }

                        // Fetch the ampacity value for the termination limit rating
                        val terminationLimitedAmpacityEntry = necDataRepository.getAmpacityEntry(uiState.selectedMaterial, uiState.selectedSize, terminationLimitRating)
                        val terminationLimitedAmpacity = terminationLimitedAmpacityEntry?.ampacity

                        finalAmpacity = if (terminationLimitedAmpacity != null) {
                            minOf(adjustedAmpacityPreTermination, terminationLimitedAmpacity)
                        } else {
                            // If the termination limit ampacity isn't found, maybe default to the adjusted value or show error?
                            // For now, let's use the adjusted value but potentially flag it.
                            // error = error ?: "Termination limit ampacity (${terminationLimitRating}°C) not found." // Optional error
                            adjustedAmpacityPreTermination
                        }
                    }
                }
            } catch (e: Exception) {
                error = "Calculation error: ${e.message}"
                // Clear results on error
                base = null; tempFactor = null; adjFactor = null; finalAmpacity = null;
            }

            uiState = uiState.copy(
                baseAmpacity = base,
                tempCorrectionFactor = tempFactor,
                conductorAdjustmentFactor = adjFactor,
                adjustedAmpacity = finalAmpacity, // Show the final value after termination limits
                errorMessage = error
            )
        }
    }

    // Helper function to check wire size (simplified)
    // TODO: Make this more robust based on actual size string format
    private fun isSizeGreaterThan1AWG(size: String): Boolean {
        return size.contains("kcmil") || size.contains("/") || size.matches(Regex("\\d+ AWG")) && (size.substringBefore(" ").toIntOrNull() ?: 100) < 1
    }

    // Helper to format results nicely (can be shared or moved)
    private fun Double.formatResult(decimals: Int = 2): String {
        return String.format("%.${decimals}f", this).trimEnd('0').trimEnd('.')
    }
}
