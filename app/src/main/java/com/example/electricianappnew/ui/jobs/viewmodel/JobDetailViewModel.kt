package com.example.electricianappnew.ui.jobs.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.Client
import com.example.electricianappnew.data.model.Job
import com.example.electricianappnew.data.model.Task
import com.example.electricianappnew.data.repository.ClientRepository
import com.example.electricianappnew.data.repository.JobTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // Correct wildcard import
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobDetailUiState(
    val job: Job? = null,
    val client: Client? = null, // Associated client details
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    private val jobTaskRepository: JobTaskRepository,
    private val clientRepository: ClientRepository, // Inject ClientRepository
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val jobId: String = checkNotNull(savedStateHandle["jobId"])

    private val _uiState = MutableStateFlow(JobDetailUiState(isLoading = true))
    val uiState: StateFlow<JobDetailUiState> = _uiState

    init {
        loadJobDetails()
    }

    fun loadJobDetails() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                // Fetch job details flow
                val jobFlow: Flow<Job?> = jobTaskRepository.getJobById(jobId)
                // Fetch tasks flow
                val tasksFlow: Flow<List<Task>> = jobTaskRepository.getTasksForJob(jobId)

                // Combine job flow with client flow (derived from job flow) and tasks flow
                jobFlow.flatMapLatest { job ->
                    if (job == null) {
                        // If job is null, emit state with error and empty lists
                        flowOf(JobDetailUiState(isLoading = false, errorMessage = "Job not found."))
                    } else {
                        // If job exists, fetch client and combine with tasks
                        val clientFlow: Flow<Client?> = clientRepository.getClientById(job.clientId)
                        combine(clientFlow, tasksFlow) { client, tasks ->
                            // Create the UI state with job, client, and tasks
                            JobDetailUiState(
                                job = job,
                                client = client, // Client can be null
                                tasks = tasks,
                                isLoading = false // Loading complete
                            )
                        }
                    }
                }.catch { exception ->
                    // Emit error state if any flow fails
                    emit(JobDetailUiState(isLoading = false, errorMessage = "Failed to load job details: ${exception.message}"))
                }.collect { state ->
                    // Update the main UI state flow
                    _uiState.value = state
                }

            } catch (e: Exception) {
                 // Catch any synchronous errors during setup
                 _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading job details: ${e.message}"
                )
            }
        }
    }


    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                jobTaskRepository.deleteTask(task)
                // Flow should update automatically, but add error handling
                 _uiState.update { it.copy(errorMessage = null) } // Clear previous error on success
            } catch (e: Exception) {
                 _uiState.update { it.copy(errorMessage = "Failed to delete task: ${e.message}") }
            }
        }
    }

    fun updateTaskStatus(task: Task, newStatus: String) {
        viewModelScope.launch {
            try {
                val updatedTask = task.copy(status = newStatus) // Create updated task object
                jobTaskRepository.updateTask(updatedTask)
                // Flow should update the list automatically, but handle errors
                 _uiState.update { it.copy(errorMessage = null) } // Clear previous error
            } catch (e: Exception) {
                 _uiState.update { it.copy(errorMessage = "Failed to update task status: ${e.message}") }
            }
        }
    }

    // TODO: Add function for editing job status later
}
