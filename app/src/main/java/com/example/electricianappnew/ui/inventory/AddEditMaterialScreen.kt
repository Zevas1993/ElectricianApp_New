package com.example.electricianappnew.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electricianappnew.data.model.Material
import com.example.electricianappnew.ui.inventory.viewmodel.AddEditMaterialViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMaterialScreen(
    modifier: Modifier = Modifier,
    viewModel: AddEditMaterialViewModel = hiltViewModel(),
    onSaveComplete: () -> Unit, // Navigate back on success
    onCancelClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Navigate back on successful save
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaveComplete()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = if (uiState.materialId == null) "Add New Material" else "Edit Material",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Material Name*") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errorMessage?.contains("Name") == true,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.category,
                onValueChange = viewModel::onCategoryChange,
                label = { Text("Category*") },
                modifier = Modifier.fillMaxWidth(),
                 isError = uiState.errorMessage?.contains("Category") == true,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.partNumber,
                onValueChange = viewModel::onPartNumberChange,
                label = { Text("Part Number (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Section for initial inventory item details (only show when adding new material)
            if (uiState.materialId == null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Initial Inventory Details (Optional)", style = MaterialTheme.typography.titleMedium)
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Spacer(modifier = Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.initialQuantityStr,
                        onValueChange = viewModel::onInitialQuantityChange,
                        label = { Text("Initial Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = uiState.unitOfMeasure,
                        onValueChange = viewModel::onUnitOfMeasureChange,
                        label = { Text("Unit*") }, // Required only if adding qty
                         isError = uiState.errorMessage?.contains("Unit") == true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.location,
                    onValueChange = viewModel::onLocationChange,
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.lowStockThresholdStr,
                    onValueChange = viewModel::onLowStockThresholdChange,
                    label = { Text("Low Stock Threshold") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }

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
                    onClick = viewModel::saveMaterial,
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                         CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (uiState.materialId == null) "Add Material" else "Save Changes")
                    }
                }
                OutlinedButton(onClick = onCancelClick, enabled = !uiState.isSaving) {
                    Text("Cancel")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AddMaterialScreenPreview() {
    ElectricianAppNewTheme {
        // Preview won't have real ViewModel interaction
        // Remove parameters that rely on ViewModel injection
        AddEditMaterialScreen(onSaveComplete = {}, onCancelClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun EditMaterialScreenPreview() {
     ElectricianAppNewTheme {
        // Preview won't have real ViewModel interaction
        // Remove parameters that rely on ViewModel injection
         AddEditMaterialScreen(onSaveComplete = {}, onCancelClick = {})
         // To preview edit state visually, would need to pass dummy data differently
    }
}
