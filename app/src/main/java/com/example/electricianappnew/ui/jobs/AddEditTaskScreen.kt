package com.example.electricianappnew.ui.jobs // Corrected package declaration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electricianappnew.data.model.Task
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput
import com.example.electricianappnew.ui.jobs.viewmodel.AddEditTaskViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import androidx.compose.foundation.text.KeyboardOptions // Keep this import
import androidx.compose.ui.text.input.KeyboardType // Keep this import
import com.example.electricianappnew.data.model.InventoryItemWithMaterial // Keep this import
// Removed duplicate package declaration and cleaned imports

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    modifier: Modifier = Modifier,
    viewModel: AddEditTaskViewModel = hiltViewModel(),
    onSaveComplete: () -> Unit,
    onCancelClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaveComplete()
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (uiState.taskId == null) "Add New Task" else "Edit Task",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
             CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Task Description*") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                isError = uiState.errorMessage?.contains("Description") == true
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBoxInput(
                label = "Status",
                options = uiState.availableStatuses,
                selectedOption = uiState.status,
                onOptionSelected = viewModel::onStatusChange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
             OutlinedTextField(
                value = uiState.assignedTo,
                onValueChange = viewModel::onAssignedToChange,
                label = { Text("Assigned To (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Materials Used Section ---
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Materials Used", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            uiState.inventoryLoadingError?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
            }

            uiState.usedMaterials.forEach { (inventoryItemId, quantityUsed) ->
                val itemWithMaterial = uiState.availableInventory.find { it.inventoryItem.id == inventoryItemId }
                if (itemWithMaterial != null) {
                    MaterialUsageRow(
                        itemWithMaterial = itemWithMaterial,
                        quantityUsed = quantityUsed,
                        onQuantityChange = { newQuantity ->
                            viewModel.addOrUpdateMaterialUsage(inventoryItemId, newQuantity)
                        },
                        onRemove = { viewModel.removeMaterialUsage(inventoryItemId) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            AddMaterialDropdown(
                availableInventory = uiState.availableInventory,
                alreadyUsedIds = uiState.usedMaterials.keys,
                onMaterialSelected = { inventoryItemId ->
                    viewModel.addOrUpdateMaterialUsage(inventoryItemId, 1.0)
                }
            )
            // --- End Materials Used Section ---


            Spacer(modifier = Modifier.height(24.dp))

             uiState.errorMessage?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = viewModel::saveTask,
                    enabled = !uiState.isSaving
                ) {
                     if (uiState.isSaving) {
                         CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (uiState.taskId == null) "Add Task" else "Save Changes")
                    }
                }
                OutlinedButton(onClick = onCancelClick, enabled = !uiState.isSaving) {
                    Text("Cancel")
                }
            }
        }
    }
}


// --- Composables for Material Usage ---

@Composable
fun MaterialUsageRow(
    itemWithMaterial: InventoryItemWithMaterial, // Use imported model
    quantityUsed: Double,
    onQuantityChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
    var quantityString by remember(quantityUsed) { mutableStateOf(quantityUsed.formatQuantity()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = itemWithMaterial.material?.name ?: "Unknown Material",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = quantityString,
            onValueChange = { newValue ->
                quantityString = newValue
                newValue.toDoubleOrNull()?.let {
                    onQuantityChange(it)
                 }
             },
             modifier = Modifier.width(80.dp),
             keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), // Correct: Use Decimal
             singleLine = true,
             textStyle = LocalTextStyle.current.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize)
        )
         Spacer(modifier = Modifier.width(8.dp))
         IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Remove Material")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaterialDropdown(
    availableInventory: List<InventoryItemWithMaterial>, // Use imported model
    alreadyUsedIds: Set<String>,
    onMaterialSelected: (inventoryItemId: String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectableItems = availableInventory.filter { it.inventoryItem.id !in alreadyUsedIds }

    if (selectableItems.isNotEmpty()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = "Add Material...",
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Material to Add") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                selectableItems.forEach { item ->
                    DropdownMenuItem(
                        text = { Text("${item.material?.name ?: "Unknown"} (Qty: ${item.inventoryItem.quantityOnHand.formatQuantity()})") },
                        onClick = {
                            onMaterialSelected(item.inventoryItem.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    } else {
        Text("No more materials available to add.", style = MaterialTheme.typography.bodySmall)
    }
}

// Helper extension function
private fun Double.formatQuantity(): String {
    return if (this == this.toInt().toDouble()) {
        this.toInt().toString()
    } else {
        String.format("%.1f", this)
    }
}


// --- Previews ---

@Preview(showBackground = true)
@Composable
fun AddTaskScreenPreview() {
    ElectricianAppNewTheme {
        AddEditTaskScreen(onSaveComplete = {}, onCancelClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun EditTaskScreenPreview() {
    ElectricianAppNewTheme {
        AddEditTaskScreen(onSaveComplete = {}, onCancelClick = {})
    }
}
