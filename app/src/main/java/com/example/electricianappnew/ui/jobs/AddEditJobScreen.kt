package com.example.electricianappnew.ui.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState // Add scroll state import
import androidx.compose.foundation.verticalScroll // Add scroll import
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electricianappnew.data.model.Job
import com.example.electricianappnew.ui.common.ExposedDropdownMenuBoxInput
import com.example.electricianappnew.ui.jobs.viewmodel.AddEditJobViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditJobScreen(
    modifier: Modifier = Modifier,
    viewModel: AddEditJobViewModel = hiltViewModel(), // Inject ViewModel
    onSaveComplete: () -> Unit, // Callback when save is successful
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
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (uiState.jobId == null) "Add New Job" else "Edit Job",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
             CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            OutlinedTextField(
                value = uiState.jobName,
                onValueChange = viewModel::onJobNameChange,
                label = { Text("Job Name / Title*") },
                modifier = Modifier.fillMaxWidth(),
                 isError = uiState.errorMessage?.contains("Job Name") == true,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Client Dropdown
            ExposedDropdownMenuBoxInput(
                label = "Client*",
                options = uiState.clients.map { it.name }, // Display client names
                selectedOption = uiState.clients.find { it.id == uiState.selectedClientId }?.name ?: "",
                onOptionSelected = { selectedName ->
                    val selectedId = uiState.clients.find { it.name == selectedName }?.id ?: ""
                    viewModel.onClientChange(selectedId)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.address,
                onValueChange = viewModel::onAddressChange,
                label = { Text("Job Address") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField( // Add Description field
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description / Scope") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Status Dropdown
             ExposedDropdownMenuBoxInput(
                 label = "Status",
                 options = uiState.availableStatuses,
                 selectedOption = uiState.status,
                 onOptionSelected = viewModel::onStatusChange,
                 modifier = Modifier.fillMaxWidth()
             )
             // Removed Notes field

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
                    onClick = viewModel::saveJob,
                    enabled = !uiState.isSaving
                ) {
                     if (uiState.isSaving) {
                         CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                         Text(if (uiState.jobId == null) "Add Job" else "Save Changes")
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
fun AddJobScreenPreview() {
    ElectricianAppNewTheme {
        AddEditJobScreen(onSaveComplete = {}, onCancelClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun EditJobScreenPreview() {
    ElectricianAppNewTheme {
        // Preview won't have real ViewModel interaction
        AddEditJobScreen(onSaveComplete = {}, onCancelClick = {})
        // To preview edit state visually, would need to pass dummy data differently
    }
}
