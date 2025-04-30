package com.example.electricianappnew.ui.jobs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Added specific import
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
// import androidx.navigation.NavController // Removed NavController import
import com.example.electricianappnew.data.model.Job
import com.example.electricianappnew.data.model.Task
import com.example.electricianappnew.navigation.Screen // Keep Screen import for routes
import com.example.electricianappnew.ui.jobs.viewmodel.JobDetailViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    // Removed navController: NavController,
    viewModel: JobDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onEditJobClick: (jobId: String) -> Unit,
    onAddTaskClick: (jobId: String) -> Unit,
    onEditTaskClick: (jobId: String, taskId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val job = uiState.job // Convenience variable

    // State for confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(job?.jobName ?: "Job Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { // Use hoisted lambda
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") // Updated Icon
                    }
                },
                actions = {
                    // Edit Job Button
                    IconButton(onClick = {
                        job?.let { onEditJobClick(it.id) } // Use hoisted lambda
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Job")
                    }
                    // Add more actions if needed (e.g., Delete Job)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                job?.let { onAddTaskClick(it.id) } // Use hoisted lambda
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "Error loading job details",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                job == null -> {
                     Text(
                        text = "Job not found.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                else -> {
                    JobDetailContent(
                        job = job,
                        tasks = uiState.tasks,
                        onTaskClick = { taskId ->
                            onEditTaskClick(job.id, taskId) // Use hoisted lambda
                        },
                        onTaskCheckedChange = viewModel::updateTaskStatus,
                        onDeleteTaskClick = { task ->
                            taskToDelete = task
                            showDeleteDialog = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

     // Confirmation Dialog for Deleting Task
    if (showDeleteDialog && taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task?") },
            text = { Text("Are you sure you want to delete the task: \"${taskToDelete?.description}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        taskToDelete?.let { viewModel.deleteTask(it.id) }
                        showDeleteDialog = false
                        taskToDelete = null // Reset
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun JobDetailContent(
    job: Job,
    tasks: List<Task>,
    onTaskClick: (String) -> Unit, // Change Int to String
    onTaskCheckedChange: (Task, Boolean) -> Unit,
    onDeleteTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        JobInfoCard(job = job)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Tasks", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        TaskList(
            tasks = tasks,
            onTaskClick = onTaskClick,
            onTaskCheckedChange = onTaskCheckedChange,
            onDeleteTaskClick = onDeleteTaskClick
        )
    }
}

@Composable
fun JobInfoCard(job: Job, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(job.jobName, style = MaterialTheme.typography.titleLarge) // Use jobName
            Spacer(modifier = Modifier.height(4.dp))
            Text("Address: ${job.address ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
            Text("Status: ${job.status}", style = MaterialTheme.typography.bodyMedium)
            // Add more job details here (client, dates, etc.) if available
        }
    }
}

@Composable
fun TaskList(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit, // Change Int to String
    onTaskCheckedChange: (Task, Boolean) -> Unit,
    onDeleteTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tasks.isEmpty()) {
        Text("No tasks added yet.", modifier = Modifier.padding(vertical = 16.dp))
    } else {
        LazyColumn(modifier = modifier) {
            items(tasks, key = { it.id }) { task ->
                TaskListItem(
                    task = task,
                    onClick = { onTaskClick(task.id) },
                    onCheckedChange = { isChecked -> onTaskCheckedChange(task, isChecked) },
                    onDeleteClick = { onDeleteTaskClick(task) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun TaskListItem(
    task: Task,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.status == "Completed", // Check status string instead of isComplete
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = task.description,
            modifier = Modifier.weight(1f),
            style = if (task.status == "Completed") MaterialTheme.typography.bodyMedium.copy( // Check status string
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.onSurfaceVariant // Dim completed tasks
            ) else MaterialTheme.typography.bodyMedium
        )
         IconButton(onClick = onDeleteClick) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Task")
        }
    }
}


// --- Preview ---
@Preview(showBackground = true)
@Composable
fun JobDetailScreenPreview() {
    ElectricianAppNewTheme {
        // Use jobName and status in preview data, ADD clientId and clientName
        val previewJob = Job(
            id = "preview_job_1",
            jobName = "Kitchen Remodel",
            clientId = "preview_client_1", // Add dummy clientId
            clientName = "Preview Client", // Add dummy clientName
            address = "456 Oak Ave",
            status = "In Progress"
        )
        val previewTasks = listOf(
            Task(id = "preview_task_1", jobId = "preview_job_1", description = "Rough-in wiring", status = "Completed"),
            Task(id = "preview_task_2", jobId = "preview_job_1", description = "Install outlets and switches", status = "In Progress"),
            Task(id = "preview_task_3", jobId = "preview_job_1", description = "Install light fixtures", status = "Pending")
        )

        Scaffold(
            topBar = {
            TopAppBar(
                title = { Text("Job Details Preview") },
                navigationIcon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") }, // Updated Icon
                actions = { IconButton(onClick = {}) { Icon(Icons.Default.Edit, "") } }
            )
        },
            floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(Icons.Default.Add, "") } }
        ) { padding ->
            JobDetailContent(
                job = previewJob,
                tasks = previewTasks,
                onTaskClick = {},
                onTaskCheckedChange = { _, _ -> },
                onDeleteTaskClick = {},
                modifier = Modifier.padding(padding)
            )
        }
    }
}
