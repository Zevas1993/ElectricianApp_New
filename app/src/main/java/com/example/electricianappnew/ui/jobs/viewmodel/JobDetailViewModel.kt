package com.example.electricianappnew.ui.jobs.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgument // Add NavArgument import
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricianappnew.data.model.Job
import com.example.electricianappnew.data.model.Task
import com.example.electricianappnew.data.repository.JobTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobDetailUiState(
    val job: Job? = null,
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    private val jobTaskRepository: JobTaskRepository,
    savedStateHandle: SavedStateHandle // Hilt injects this for navigation arguments
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobDetailUiState())
    val uiState: StateFlow<JobDetailUiState> = _uiState.asStateFlow()

    // Retrieve jobId from navigation arguments as String
    private val jobId: String = checkNotNull(savedStateHandle["jobId"]) // Use string key directly

    init {
        loadJobDetails()
    }

    private fun loadJobDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Fetch job and tasks concurrently or sequentially
                // Using combine for potentially better performance if flows emit independently
                combine(
                    jobTaskRepository.getJobById(jobId),
                    jobTaskRepository.getTasksForJob(jobId)
                ) { jobResult, taskList ->
                    // Assuming getJobById returns Flow<Job?> or similar
                    // Handle potential null job if ID is invalid, though checkNotNull helps
                    JobDetailUiState(
                        job = jobResult, // Or handle null case explicitly
                        tasks = taskList,
                        isLoading = false
                    )
                }.catch { e ->
                    // Catch errors from either flow
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load job details: ${e.message}"
                        )
                    }
                }.collect { combinedState ->
                    _uiState.value = combinedState // Update the state with combined results
                }

            } catch (e: Exception) { // Catch potential errors during initial setup or combine
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error loading details: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                // Fetch the task first to pass the object to the delete method
                val taskToDelete = jobTaskRepository.getTaskById(taskId).first() // Get the task Flow and take the first emission
                if (taskToDelete != null) {
                    jobTaskRepository.deleteTask(taskToDelete) // Pass the Task object
                    // Flow collection should update the UI automatically
                } else {
                    // Handle case where task to delete wasn't found (optional)
                     _uiState.update { it.copy(errorMessage = "Task with ID $taskId not found for deletion.") }
                }
                // If not, trigger a reload: loadJobDetails()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to delete task: ${e.message}") }
            }
        }
    }

    // Function to update task status based on checkbox state
    fun updateTaskStatus(task: Task, isChecked: Boolean) {
        viewModelScope.launch {
            val newStatus = if (isChecked) "Completed" else "Pending" // Determine status string
            try {
                // Update the status field, not isComplete
                jobTaskRepository.updateTask(task.copy(status = newStatus))
                // Flow should update UI automatically
            } catch (e: Exception) {
                 _uiState.update { it.copy(errorMessage = "Failed to update task status: ${e.message}") }
            }
        }
    }
}
