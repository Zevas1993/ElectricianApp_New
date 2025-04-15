package com.example.electricianappnew.ui.photodoc.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.PhotoDoc
import com.example.electricianappnew.data.repository.PhotoDocRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class AddEditPhotoDocUiState(
    val imageUri: Uri? = null,
    val description: String = "",
    val jobId: String? = null, // Required for saving
    val taskId: String? = null, // Optional
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class AddEditPhotoDocViewModel @Inject constructor(
    private val photoDocRepository: PhotoDocRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val jobIdArg: String? = savedStateHandle["jobId"]
    private val taskIdArg: String? = savedStateHandle["taskId"] // Optional

    private val _uiState = MutableStateFlow(AddEditPhotoDocUiState(jobId = jobIdArg, taskId = taskIdArg))
    val uiState: StateFlow<AddEditPhotoDocUiState> = _uiState

    fun onImageUriChanged(uri: Uri?) {
        _uiState.value = _uiState.value.copy(imageUri = uri)
    }

    fun onDescriptionChanged(desc: String) {
        _uiState.value = _uiState.value.copy(description = desc)
    }

    fun savePhoto() {
        val currentState = _uiState.value
        val imageUri = currentState.imageUri
        val jobId = currentState.jobId

        if (imageUri == null) {
            _uiState.value = currentState.copy(saveError = "Please select or capture an image.")
            return
        }
        if (jobId == null) {
             _uiState.value = currentState.copy(saveError = "Job ID is missing.")
             return // Should not happen if navigation is set up correctly
        }

        _uiState.value = currentState.copy(isSaving = true, saveError = null)

        viewModelScope.launch {
            try {
                val newPhotoDoc = PhotoDoc(
                    filePath = imageUri.toString(), // Use filePath
                    caption = currentState.description, // Use caption
                    dateTaken = Date(), // Use dateTaken
                    jobId = jobId,
                    taskId = currentState.taskId // Can be null
                )
                photoDocRepository.insertPhotoDoc(newPhotoDoc) // Corrected method name
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveError = "Failed to save photo: ${e.message}"
                )
            }
        }
    }
}
