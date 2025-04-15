package com.example.electricianappnew.ui.jobs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Removed duplicate import
import com.example.electricianappnew.data.model.Job
import com.example.electricianappnew.data.repository.JobTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // Import combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobListUiState(
    val allJobs: List<Job> = emptyList(), // Store the full list
    val displayedJobs: List<Job> = emptyList(), // Filtered list for UI
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class JobListViewModel @Inject constructor(
    private val jobTaskRepository: JobTaskRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _allJobsFlow = MutableStateFlow(JobListUiState(isLoading = true)) // Holds raw data + loading state

    // Combine the full job list and the search query to produce the final UI state
    val uiState: StateFlow<JobListUiState> = combine(
        _allJobsFlow,
        _searchQuery
    ) { jobsState, query ->
        val filteredJobs = if (query.isBlank()) {
            jobsState.allJobs
        } else {
            jobsState.allJobs.filter { job ->
                job.jobName.contains(query, ignoreCase = true) ||
                job.clientName.contains(query, ignoreCase = true) ||
                job.address.contains(query, ignoreCase = true) ||
                job.status.contains(query, ignoreCase = true)
            }
        }
        jobsState.copy(
            searchQuery = query,
            displayedJobs = filteredJobs // Update the displayed list
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Keep flow active for 5s
        initialValue = JobListUiState() // Initial empty state
    )


    init {
        loadJobs() // Initial load
    }

    private fun loadJobs() {
        viewModelScope.launch {
             _allJobsFlow.update { it.copy(isLoading = true, errorMessage = null) } // Update loading state
            jobTaskRepository.getAllJobs()
                .catch { exception ->
                     _allJobsFlow.update {
                         it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load jobs: ${exception.message}"
                        )
                    }
                }
                .collect { jobList ->
                     _allJobsFlow.update {
                         it.copy(
                            allJobs = jobList, // Store the full list
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteJob(job: Job) {
        viewModelScope.launch {
            try {
                jobTaskRepository.deleteJob(job)
                // Flow collection in loadJobs should handle UI update.
                 _allJobsFlow.update { it.copy(errorMessage = null) } // Clear previous error
            } catch (e: Exception) {
                 // Update the error message in the main state flow
                 _allJobsFlow.update { it.copy(errorMessage = "Failed to delete job: ${e.message}") }
            }
        }
    }
}
