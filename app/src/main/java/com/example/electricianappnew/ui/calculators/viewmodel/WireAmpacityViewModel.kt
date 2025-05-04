package com.example.electricianappnew.ui.calculators.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.repository.NecDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow
import com.example.electricianappnew.data.repository.AmpacityCalculationResult
import kotlinx.coroutines.Dispatchers // Import Dispatchers
import kotlin.math.round // Import round function
// Removed duplicate Log import

// Data class for UI State (now represents the calculation result)
data class WireAmpacityUiState(
    val baseAmpacity: Double? = null,
    val tempCorrectionFactor: Double? = null,
    val conductorAdjustmentFactor: Double? = null,
    val adjustedAmpacity: Double? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class WireAmpacityViewModel @Inject constructor(
    private val necDataRepository: NecDataRepository
) : ViewModel() {

    // --- StateFlows for Dropdown Options (Fetched from repository) ---
    // Assuming getDistinctConductorMaterials exists and returns Flow<List<String>>
    val materials: StateFlow<List<String>> = necDataRepository.getDistinctConductorMaterials()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val wireSizes: StateFlow<List<String>> = necDataRepository.getDistinctAmpacityWireSizes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // Insulation Options (Fetched ratings from repo, mapped to Name/Rating pairs)
    val insulationOptions: StateFlow<List<Pair<String, Int>>> = necDataRepository.getDistinctAmpacityTempRatings()
        .map { ratingsList ->
            ratingsList.mapNotNull { rating ->
                when (rating) {
                    60 -> "TW/UF" to 60
                    75 -> "THW/THWN/XHHW" to 75
                    90 -> "THHN/THWN-2/XHHW-2" to 90
                    else -> null
                }
            }.sortedBy { it.second }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // --- MutableStateFlows for User Inputs ---
    private val _selectedMaterial = MutableStateFlow<String?>(null)
    val selectedMaterial: StateFlow<String?> = _selectedMaterial.asStateFlow()

    private val _selectedSize = MutableStateFlow<String?>(null)
    val selectedSize: StateFlow<String?> = _selectedSize.asStateFlow()

    private val _selectedInsulation = MutableStateFlow<String?>(null)
    val selectedInsulation: StateFlow<String?> = _selectedInsulation.asStateFlow()

    private val _ambientTempStr = MutableStateFlow("75") // Keep as String for TextField
    val ambientTempStr: StateFlow<String> = _ambientTempStr.asStateFlow()

    private val _numConductorsStr = MutableStateFlow("3") // Keep as String for TextField
    val numConductorsStr: StateFlow<String> = _numConductorsStr.asStateFlow()

    // --- Public functions to update the private MutableStateFlows ---
    fun updateSelectedMaterial(material: String) {
        _selectedMaterial.value = material
    }

    fun updateSelectedSize(size: String) {
        _selectedSize.value = size
    }

    fun updateSelectedInsulation(insulationName: String) {
        _selectedInsulation.value = insulationName
    }

    fun updateAmbientTempStr(temp: String) {
        // Basic validation could be added here if needed
        _ambientTempStr.value = temp
    }

    fun updateNumConductorsStr(count: String) {
        // Basic validation could be added here if needed
        _numConductorsStr.value = count
    }

    // Define the calculation logic lambda separately (now with 5 parameters)
    private val calculationLogic: suspend (String?, String?, String?, String, String) -> WireAmpacityUiState =
        { material, size, insulationName, ambientTempStr, numConductorsStr ->
            Log.d("WireAmpacityVM", "Calculation logic triggered: material=$material, size=$size, insulation=$insulationName, temp=$ambientTempStr, conductors=$numConductorsStr")

            // Fetch the options list from the StateFlow's current value
            val insulationOptionsList = insulationOptions.value
            val tempRating = insulationOptionsList.find { it.first == insulationName }?.second
            var ambientTempC = ambientTempStr.toDoubleOrNull() // Use var to allow modification
            val numConductors = numConductorsStr.toIntOrNull()

            Log.d("WireAmpacityVM", "Parsed values: tempRating=$tempRating, ambientTempC=$ambientTempC, numConductors=$numConductors")

            if (material == null || size == null || tempRating == null || ambientTempC == null || numConductors == null || numConductors <= 0) {
                Log.d("WireAmpacityVM", "Inputs invalid or incomplete. Returning error state.")
                WireAmpacityUiState(errorMessage = "Please select material, size, insulation, valid ambient temperature, and number of conductors.")
            } else if (ambientTempC > tempRating) {
                Log.d("WireAmpacityVM", "Ambient temperature exceeds insulation temp rating. Returning error state.")
                WireAmpacityUiState(errorMessage = "Ambient temperature (${ambientTempC}°C) exceeds insulation temperature rating (${tempRating}°C). Please adjust inputs.")
            } else {
                // Round ambient temperature to the nearest 5°C increment
                ambientTempC = roundToNearestFive(ambientTempC)
                Log.d("WireAmpacityVM", "Rounded ambientTempC: $ambientTempC") // Log the rounded value

                Log.d("WireAmpacityVM", "Inputs valid. Calling repository.calculateAmpacity...") // Simplified log message
                try {
                    val result = necDataRepository.calculateAmpacity(
                        material = material,
                        size = size,
                        tempRating = tempRating,
                        ambientTempC = ambientTempC, // Use the rounded value
                        numConductors = numConductors
                    )
                    Log.d("WireAmpacityVM", "Calculation result: $result")
                    WireAmpacityUiState(
                        baseAmpacity = result.baseAmpacity,
                        tempCorrectionFactor = result.tempCorrectionFactor,
                        conductorAdjustmentFactor = result.conductorAdjustmentFactor,
                        adjustedAmpacity = result.adjustedAmpacity,
                        errorMessage = result.errorMessage
                    )
                } catch (e: Exception) {
                    Log.e("WireAmpacityVM", "Calculation error", e)
                    WireAmpacityUiState(errorMessage = "Calculation error: ${e.message}")
                }
            }
        }


    // --- Combined Input Flow and Calculation Result StateFlow ---
    // Combine only the 5 necessary input flows
    val uiState: StateFlow<WireAmpacityUiState> = combine(
        _selectedMaterial,      // Flow<String?>
        _selectedSize,          // Flow<String?>
        _selectedInsulation,    // Flow<String?>
        _ambientTempStr,        // Flow<String>
        _numConductorsStr       // Flow<String>
        // insulationOptions is removed
    ) { material: String?, size: String?, insulationName: String?, ambientTempStr: String, numConductorsStr: String ->
        // Call the separately defined logic function (which now takes 5 args)
        calculationLogic(material, size, insulationName, ambientTempStr, numConductorsStr)
    }
        .distinctUntilChanged() // Only emit if the result changes
        .flowOn(Dispatchers.IO) // Perform calculations off the main thread
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = WireAmpacityUiState(errorMessage = "Select inputs to calculate.") // Changed initial message
        )

    // init block is removed as its logic is now handled by the uiState flow definition.
    // fetchDropdownValues function is removed as data fetching is handled by StateFlow initializations.

    fun clearInputs() {
        // Reset MutableStateFlows to trigger recalculation via the combine flow
        // Set defaults intelligently if possible, otherwise null/empty
        _selectedMaterial.value = null // Or materials.value.firstOrNull() if you want a default
        _selectedSize.value = null // Or a common default like "12 AWG"
        _selectedInsulation.value = null // Or a common default like "THW/THWN/XHHW"
        _ambientTempStr.value = "75" // Reset to default string
        _numConductorsStr.value = "3" // Reset to default string
        // The combined flow 'uiState' will automatically react to these changes.
    }

    // Helper function to round a Double to the nearest 5
    private fun roundToNearestFive(value: Double): Double {
        return round(value / 5.0) * 5.0
    }

    // Removed calculateAmpacity function as it's now part of the combined flow chain
    // Removed helper functions as they are now in the repository
}
