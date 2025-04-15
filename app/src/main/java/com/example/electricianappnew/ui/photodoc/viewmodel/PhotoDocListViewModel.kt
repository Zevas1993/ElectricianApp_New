package com.example.electricianappnew.ui.photodoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.PhotoDoc
import com.example.electricianappnew.data.repository.PhotoDocRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhotoDocListUiState(
    val photos: List<PhotoDoc> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
    // Add filter state later (e.g., jobId, taskId)
)

@HiltViewModel
class PhotoDocListViewModel @Inject constructor(
    private val photoDocRepository: PhotoDocRepository
    // Potentially inject SavedStateHandle if filtering by Job/Task ID passed via navigation
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoDocListUiState(isLoading = true))
    val uiState: StateFlow<PhotoDocListUiState> = _uiState

    // TODO: Add logic to filter photos based on JobId or TaskId if needed

    init {
        loadAllPhotos() // Load all photos initially, filtering can be added
    }

    fun loadAllPhotos() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            photoDocRepository.getAllPhotoDocs() // Corrected method name
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load photos: ${exception.message}"
                    )
                }
                .collect { photoList ->
                    _uiState.value = _uiState.value.copy(
                        photos = photoList,
                        isLoading = false
                    )
                }
        }
    }

    fun deletePhoto(photoDoc: PhotoDoc) {
        viewModelScope.launch {
            try {
                photoDocRepository.deletePhotoDoc(photoDoc) // Corrected method name
                // Flow should update the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete photo: ${e.message}"
                )
            }
        }
    }
}
