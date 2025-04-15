package com.example.electricianappnew.ui.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto // Explicitly import Filled again
import androidx.compose.material.icons.filled.Delete // Import Delete icon
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable // Add clickable import
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // Import Hilt ViewModel
import com.example.electricianappnew.data.model.Job
import com.example.electricianappnew.data.model.Task
import com.example.electricianappnew.ui.jobs.viewmodel.JobDetailViewModel // Import ViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JobDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: JobDetailViewModel = hiltViewModel(), // Inject ViewModel
    onEditJobClick: (String) -> Unit,
    onAddTaskClick: (String) -> Unit,
    onTaskClick: (String, String) -> Unit, // Pass JobId and TaskId
    onViewClientClick: (String) -> Unit, // Add callback for client click
    onAddPhotoForJobClick: (String) -> Unit, // Renamed for clarity
    onAddPhotoForTaskClick: (jobId: String, taskId: String) -> Unit // New callback for task photo
) {
    val uiState by viewModel.uiState.collectAsState() // Observe state
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Scaffold( // Add Scaffold for FABs
        floatingActionButton = {
            Row {
                FloatingActionButton(
                    onClick = { uiState.job?.id?.let { onAddTaskClick(it) } },
                    modifier = Modifier.padding(end = 8.dp) // Add padding between FABs
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
                 FloatingActionButton(onClick = { uiState.job?.id?.let { onAddPhotoForJobClick(it) } }) { // Use renamed callback
                    Icon(Icons.Filled.AddAPhoto, contentDescription = "Add Photo for Job") // Updated description
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = modifier.padding(paddingValues).padding(16.dp)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                uiState.errorMessage != null -> {
                     Text(
                        text = uiState.errorMessage ?: "Error loading job",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                uiState.job != null -> {
                    val job = uiState.job!!
                    val client = uiState.client
                    val tasks = uiState.tasks

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(job.jobName, style = MaterialTheme.typography.headlineMedium) // Use job.jobName
                        IconButton(onClick = { onEditJobClick(job.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Job")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Display Client Name (clickable if client exists)
                    Text(
                        text = "Client: ${client?.name ?: job.clientName}", // Show clientName from Job if client fetch failed
                        style = MaterialTheme.typography.titleMedium,
                        modifier = if (client != null) Modifier.clickable { onViewClientClick(client.id) } else Modifier
                    )
                    Text("Address: ${job.address}", style = MaterialTheme.typography.bodyMedium)
                    Text("Status: ${job.status}", style = MaterialTheme.typography.bodyMedium)
                    Text("Created: ${dateFormatter.format(job.dateCreated)}", style = MaterialTheme.typography.bodySmall)
                    // Removed references to non-existent job.dateCompleted and job.notes


                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    Text("Tasks", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (tasks.isEmpty()) {
                        Text("No tasks added yet.")
                    } else {
                        // Use Column instead of LazyColumn if nested scrolling is an issue,
                        // or set a fixed height for the LazyColumn. For now, keep LazyColumn.
                        LazyColumn {
                            items(tasks, key = { it.id }) { task ->
                                TaskListItem(
                                    task = task,
                                    onClick = { onTaskClick(job.id, task.id) },
                                    onDeleteClick = { viewModel.deleteTask(task) },
                                    onStatusChange = { newStatus -> viewModel.updateTaskStatus(task, newStatus) },
                                    onAddPhotoClick = { onAddPhotoForTaskClick(job.id, task.id) } // Pass callback
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
                else -> {
                     Text("Job not found.", modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}

// Define possible task statuses
private val taskStatuses = listOf("Pending", "In Progress", "Completed")

@Composable
fun TaskListItem(
    task: Task,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStatusChange: (String) -> Unit,
    onAddPhotoClick: () -> Unit // Add callback parameter
) {
     Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(task.description, modifier = Modifier.weight(1f).clickable(onClick = onClick), style = MaterialTheme.typography.bodyLarge) // Make description clickable too
        Spacer(modifier = Modifier.width(8.dp))
        // Make status text clickable to cycle through statuses
        Text(
            text = task.status,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.clickable {
                val currentIndex = taskStatuses.indexOf(task.status)
                val nextIndex = (currentIndex + 1) % taskStatuses.size
                onStatusChange(taskStatuses[nextIndex])
            }
        )
        Spacer(modifier = Modifier.width(4.dp)) // Reduced spacer
         IconButton(onClick = onAddPhotoClick, modifier = Modifier.size(24.dp)) { // Add Photo IconButton
            Icon(Icons.Filled.AddAPhoto, contentDescription = "Add Photo for Task")
        }
        Spacer(modifier = Modifier.width(4.dp)) // Reduced spacer
        IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) { // Delete IconButton
            Icon(Icons.Default.Delete, contentDescription = "Delete Task")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun JobDetailScreenPreview() {
    ElectricianAppNewTheme {
        // Preview won't have real ViewModel interaction
         JobDetailScreen(
             onEditJobClick = {},
             onAddTaskClick = {},
             onTaskClick = { _, _ -> },
             onViewClientClick = {},
             onAddPhotoForJobClick = {},
             onAddPhotoForTaskClick = { _, _ -> } // Add dummy callback
         )
    }
}
