package com.example.electricianappnew.ui.jobs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
// import androidx.navigation.NavController // Removed NavController import
import com.example.electricianappnew.data.model.Job
import com.example.electricianappnew.navigation.Screen // Keep Screen import for routes
import com.example.electricianappnew.ui.jobs.viewmodel.JobListViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListScreen(
    // Removed navController: NavController,
    viewModel: JobListViewModel = hiltViewModel(),
    onAddJobClick: () -> Unit, // Added lambda for adding job
    onJobClick: (String) -> Unit, // Added lambda for clicking job (jobId is String)
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Jobs") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddJobClick) { // Use hoisted lambda
                Icon(Icons.Default.Add, contentDescription = "Add Job")
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
                        text = uiState.errorMessage ?: "Error loading jobs",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                uiState.jobs.isEmpty() -> {
                    Text(
                        text = "No jobs found. Tap '+' to add one.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                else -> {
                    JobListContent(
                        jobs = uiState.jobs,
                        onJobClick = onJobClick, // Pass hoisted lambda directly
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun JobListContent(
    jobs: List<Job>,
    onJobClick: (String) -> Unit, // Change Int to String
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(jobs, key = { it.id }) { job ->
            JobListItem(job = job, onClick = { onJobClick(job.id) })
            HorizontalDivider()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Add OptIn for ListItem
@Composable
fun JobListItem(
    job: Job,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(job.jobName) }, // Use jobName
        supportingContent = { Text(job.address ?: "No address") },
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 8.dp)
        // Add more details or actions if needed (e.g., status indicator)
    )
}

// Preview (Optional)
@Preview(showBackground = true)
@Composable
fun JobListScreenPreview() {
    ElectricianAppNewTheme {
        // Provide dummy data for preview with String ID and jobName
        val dummyJobs = List(5) {
            Job(
                id = it.toString(), // Use String ID
                jobName = "Job ${it + 1}", // Use jobName
                clientId = "preview_client_${it}", // Add dummy clientId
                clientName = "Preview Client ${it}", // Add dummy clientName if needed for display
                address = "123 Main St",
                status = "Open"
            )
        }
        Scaffold(
            topBar = { TopAppBar(title = { Text("Jobs Preview") }) },
            floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(Icons.Default.Add, "") } }
        ) { padding ->
            // Ensure onJobClick lambda matches the (String) -> Unit signature
            JobListContent(jobs = dummyJobs, onJobClick = { /* Do nothing in preview */ }, modifier = Modifier.padding(padding))
        }
    }
}
