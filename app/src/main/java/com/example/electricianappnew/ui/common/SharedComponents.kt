package com.example.electricianappnew.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions // Ensure this is the only KeyboardOptions import
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.MenuAnchorType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.electricianappnew.ui.calculators.viewmodel.ResistanceEntry // Import needed for ResistanceInputRow
import com.example.electricianappnew.data.model.WireEntry // Import WireEntry from central model location
import java.util.Locale

// Reusable Dropdown Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenuBoxInput(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    optionDisplayTransform: (String) -> String = { it }
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = optionDisplayTransform(selectedOption),
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
            enabled = enabled,
            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = enabled).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionDisplayTransform(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                        focusManager.clearFocus()
                    },
                    enabled = enabled
                )
            }
        }
    }
}

// --- Consolidated Helper Functions/Composables ---

/**
 * Formats a Double value to a specific number of decimal places, using US Locale.
 * Removes trailing zeros and decimal point if result is a whole number.
 */
internal fun Double.formatCalculationResult(decimals: Int = 2): String { // Renamed
    return String.format(Locale.US, "%.${decimals}f", this).trimEnd('0').trimEnd('.')
}

/**
 * A standard row for displaying a label, a formatted result value, and a unit.
 */
@Composable
fun CalculationResultRow(label: String, value: Double?, unit: String, isBold: Boolean = false, decimals: Int = 1) { // Renamed
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (isBold) FontWeight.Bold else null)
        Text(
            text = value?.formatCalculationResult(decimals) ?: "N/A", // Use renamed shared formatter
            fontWeight = if (isBold) FontWeight.Bold else null
        )
        Text(unit, fontWeight = if (isBold) FontWeight.Bold else null)
    }
}

/**
 * Input row specifically for Wire entries (Type, Size, Quantity).
 * Requires WireEntry data class to be imported.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WireInputRow(
    entry: WireEntry,
    wireTypeOptions: List<String>,
    wireSizeOptions: List<String>,
    onEntryChange: (WireEntry) -> Unit,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
         ExposedDropdownMenuBoxInput(
             label = "Type",
             options = wireTypeOptions,
             selectedOption = entry.type,
             onOptionSelected = { newType ->
                 onEntryChange(entry.copy(type = newType))
             },
             modifier = Modifier.weight(2f)
         )
         Spacer(Modifier.width(8.dp))
          ExposedDropdownMenuBoxInput(
             label = "Size",
             options = wireSizeOptions,
             selectedOption = entry.size,
             onOptionSelected = { newSize -> onEntryChange(entry.copy(size = newSize)) },
             modifier = Modifier.weight(1.5f)
         )
         Spacer(Modifier.width(8.dp))
         OutlinedTextField(
             value = entry.quantity.toString(),
             onValueChange = { qtyStr ->
                 onEntryChange(entry.copy(quantity = qtyStr.toIntOrNull() ?: 1))
             },
             label = { Text("Qty") },
             keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
             modifier = Modifier.weight(1f)
         )

        IconButton(onClick = onRemoveClick) {
            Icon(Icons.Default.Delete, contentDescription = "Remove Conductor")
        }
    }
}

/**
 * Input row specifically for Resistance entries.
 * Requires ResistanceEntry data class to be imported.
 */
@Composable
fun ResistanceInputRow(
    entry: ResistanceEntry,
    index: Int,
    onValueChange: (String) -> Unit,
    onRemoveClick: () -> Unit,
    canRemove: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = entry.valueStr,
            onValueChange = onValueChange,
            label = { Text("R${index + 1} (Ω)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onRemoveClick, enabled = canRemove) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Remove Resistor R${index + 1}",
                tint = if (canRemove) LocalContentColor.current else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}

/**
 * Input row for simple value entry with a label.
 */
@Composable
fun LoadInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier, // Add modifier
    enabled: Boolean = true // Add enabled flag
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        singleLine = true
    )
}


/**
 * Displays a section header with a divider.
 */
@Composable
fun InputSectionHeader(title: String) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        HorizontalDivider() // Consider replacing with Divider() if Material 3
        Spacer(modifier = Modifier.height(8.dp))
    }
}
