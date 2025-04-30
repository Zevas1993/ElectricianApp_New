package com.example.electricianappnew.ui.jobs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.Job
import com.example.electricianappnew.data.repository.JobTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobListUiState(
    val jobs: List<Job> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class JobListViewModel @Inject constructor(
    private val jobTaskRepository: JobTaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobListUiState())
    val uiState: StateFlow<JobListUiState> = _uiState.asStateFlow()

    init {
        loadJobs()
    }

    private fun loadJobs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            jobTaskRepository.getAllJobs()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load jobs: ${e.message}") }
                }
                .collect { jobList ->
                    _uiState.update { it.copy(isLoading = false, jobs = jobList, errorMessage = null) }
                }
        }
    }

    // Add functions for deleting jobs or other actions if needed later
}
