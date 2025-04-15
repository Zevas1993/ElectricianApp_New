package com.example.electricianappnew.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.MenuAnchorType // Add import for MenuAnchorType

// Reusable Dropdown Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenuBoxInput(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true, // Add enabled parameter
    optionDisplayTransform: (String) -> String = { it } // Optional transform for display text
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    ExposedDropdownMenuBox(
        expanded = expanded && enabled, // Only expand if enabled
        onExpandedChange = { if (enabled) expanded = !expanded }, // Only change if enabled
        modifier = modifier
    ) {
        OutlinedTextField(
            value = optionDisplayTransform(selectedOption),
            onValueChange = { }, // Read-only
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
            enabled = enabled, // Pass enabled state
            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = enabled).fillMaxWidth() // Updated deprecated call
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
                        focusManager.clearFocus() // Clear focus after selection
                    },
                    enabled = enabled // Ensure menu items are also disabled if needed
                )
            }
        }
    }
}

// Add other shared components here later if needed
