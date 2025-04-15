package com.example.electricianappnew.ui.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electricianappnew.data.model.Client
import com.example.electricianappnew.ui.clients.viewmodel.AddEditClientViewModel // Import ViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme


@Composable
fun AddEditClientScreen(
    modifier: Modifier = Modifier,
    viewModel: AddEditClientViewModel = hiltViewModel(), // Inject ViewModel
    onSaveComplete: () -> Unit, // Callback when save is successful
    onCancelClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState() // Observe state

    // Navigate back on successful save
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaveComplete()
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (uiState.clientId == null) "Add New Client" else "Edit Client",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
             CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Client Name*") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errorMessage?.contains("Name") == true,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.contactPerson,
                onValueChange = viewModel::onContactPersonChange,
                label = { Text("Contact Person (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text("Phone (Optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email (Optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.address,
                onValueChange = viewModel::onAddressChange,
                label = { Text("Address (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )

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
                    onClick = viewModel::saveClient,
                    enabled = !uiState.isSaving
                ) {
                     if (uiState.isSaving) {
                         CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (uiState.clientId == null) "Add Client" else "Save Changes")
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
fun AddClientScreenPreview() {
    ElectricianAppNewTheme {
        AddEditClientScreen(onSaveComplete = {}, onCancelClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun EditClientScreenPreview() {
     ElectricianAppNewTheme {
        // Preview won't have real ViewModel interaction
        AddEditClientScreen(onSaveComplete = {}, onCancelClick = {})
        // To preview edit state visually, would need to pass dummy data differently
    }
}
// Removed duplicate Preview and extra closing brace
