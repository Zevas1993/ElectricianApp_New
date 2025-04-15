package com.example.electricianappnew.ui.calculators.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// Enum for connection type
enum class ConnectionType { Series, Parallel }

// Data class for a single resistance entry
data class ResistanceEntry(val id: Int = System.identityHashCode(Any()), var valueStr: String = "")

// UI State
data class SeriesParallelResistanceUiState(
    val resistanceEntries: List<ResistanceEntry> = listOf(ResistanceEntry(), ResistanceEntry()), // Start with two entries
    val connectionType: ConnectionType = ConnectionType.Series,
    val totalResistance: Double? = null,
    val errorMessage: String? = null,
    val connectionTypes: List<String> = ConnectionType.values().map { it.name } // For dropdown
)

@HiltViewModel
class SeriesParallelResistanceViewModel @Inject constructor() : ViewModel() {

    var uiState by mutableStateOf(SeriesParallelResistanceUiState())
        private set

    fun addResistanceEntry() {
        val updatedList = uiState.resistanceEntries + ResistanceEntry()
        uiState = uiState.copy(resistanceEntries = updatedList)
        // No immediate recalculation needed, wait for value input or explicit calculate
    }

    fun removeResistanceEntry(index: Int) {
        if (uiState.resistanceEntries.size > 2) { // Keep at least two entries
            val updatedList = uiState.resistanceEntries.filterIndexed { i, _ -> i != index }
            uiState = uiState.copy(resistanceEntries = updatedList)
            calculateTotalResistance() // Recalculate after removing
        }
    }

    fun updateResistanceEntry(index: Int, newValueStr: String) {
        val currentList = uiState.resistanceEntries.toMutableList()
        if (index >= 0 && index < currentList.size) {
            currentList[index] = currentList[index].copy(valueStr = newValueStr)
            uiState = uiState.copy(resistanceEntries = currentList.toList(), errorMessage = null)
            // Optionally recalculate on change, or require button press
             calculateTotalResistance() // Recalculate on value change
        }
    }

    fun onConnectionTypeChanged(newTypeString: String) {
        val newType = ConnectionType.values().find { it.name == newTypeString } ?: ConnectionType.Series
        uiState = uiState.copy(connectionType = newType, errorMessage = null)
        calculateTotalResistance()
    }

     fun clearInputs() {
        uiState = SeriesParallelResistanceUiState() // Reset to initial state
    }


    fun calculateTotalResistance() {
        viewModelScope.launch {
            val resistances = uiState.resistanceEntries
                .mapNotNull { it.valueStr.toDoubleOrNull() }

            if (resistances.size != uiState.resistanceEntries.size || resistances.isEmpty()) {
                 uiState = uiState.copy(totalResistance = null, errorMessage = "Enter valid numeric values for all resistances.")
                return@launch
            }
             if (resistances.any { it < 0 }) {
                 uiState = uiState.copy(totalResistance = null, errorMessage = "Resistance values cannot be negative.")
                 return@launch
             }


            var total: Double? = null
            var error: String? = null

            try {
                total = when (uiState.connectionType) {
                    ConnectionType.Series -> resistances.sum()
                    ConnectionType.Parallel -> {
                        if (resistances.any { it == 0.0 }) {
                            throw CalculationException("Resistance cannot be zero for parallel calculation.")
                        }
                        1.0 / resistances.sumOf { 1.0 / it }
                    }
                }
            } catch (e: CalculationException) {
                error = e.message
            } catch (e: Exception) {
                error = "Calculation error: ${e.message}"
            }

            uiState = uiState.copy(
                totalResistance = total,
                errorMessage = error
            )
        }
    }

     // Custom exception
    class CalculationException(message: String): Exception(message)

    // Helper to format results nicely
    private fun Double.formatResult(decimals: Int = 2): String {
        return String.format("%.${decimals}f", this).trimEnd('0').trimEnd('.')
    }
}
