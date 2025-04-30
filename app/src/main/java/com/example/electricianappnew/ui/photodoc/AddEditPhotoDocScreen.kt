package com.example.electricianappnew.ui.photodoc

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.* // Ensure correct wildcard or specific imports
import androidx.compose.material3.* // Ensure correct wildcard or specific imports
import androidx.compose.runtime.* // Ensure correct wildcard or specific imports
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage // Correct import
// import coil3.request.ImageRequest // Removed as unused
// import coil3.compose.LocalPlatformContext // Removed as unused
import com.example.electricianappnew.ui.photodoc.viewmodel.AddEditPhotoDocViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

@Composable
fun AddEditPhotoDocScreen(
    modifier: Modifier = Modifier,
    viewModel: AddEditPhotoDocViewModel = hiltViewModel(),
    onSaveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onImageUriChanged(uri)
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaveSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Add Photo Documentation", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.imageUri != null) {
                // Simplified AsyncImage call (no builder, no crossfade)
                AsyncImage(
                    model = uiState.imageUri, // Pass URI directly
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text("No image selected")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) {
            Text(if (uiState.imageUri == null) "Select Image" else "Change Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChanged,
            label = { Text("Caption (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isSaving) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = viewModel::savePhoto,
                enabled = uiState.imageUri != null && uiState.jobId != null // Ensure jobId is available if needed for saving
            ) {
                Text("Save Photo")
            }
        }

        if (uiState.saveError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.saveError ?: "Unknown save error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddEditPhotoDocScreenPreview() {
    ElectricianAppNewTheme {
        AddEditPhotoDocScreen(onSaveSuccess = {})
    }
}
