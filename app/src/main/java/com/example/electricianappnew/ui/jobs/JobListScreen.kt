package com.example.electricianappnew.ui.jobs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete // Import Delete icon
import androidx.compose.material.icons.filled.Search // Import Search icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // Import Hilt ViewModel
import com.example.electricianappnew.data.model.Job
import com.example.electricianappnew.ui.jobs.viewmodel.JobListViewModel // Import ViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import java.util.Date


@Composable
fun JobListScreen(
    modifier: Modifier = Modifier,
    viewModel: JobListViewModel = hiltViewModel(), // Inject ViewModel
    onJobClick: (String) -> Unit,
    onAddJobClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState() // Observe state

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddJobClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Job")
            }
        }
    ) { paddingValues ->
        Column(modifier = modifier.padding(paddingValues).padding(16.dp)) {
            Text("Jobs", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))

            // Search Input Field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label = { Text("Search Jobs...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))


            when {
                 uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                 // Check displayedJobs for empty state after filtering
                uiState.displayedJobs.isEmpty() && !uiState.isLoading -> {
                     Text(
                         if (uiState.searchQuery.isBlank()) "No jobs found. Add one!" else "No jobs match your search.",
                         modifier = Modifier.align(Alignment.CenterHorizontally)
                     )
                }
                else -> {
                    LazyColumn {
                         // Iterate over displayedJobs
                        items(uiState.displayedJobs, key = { it.id }) { job ->
                            JobListItem(
                                job = job,
                                onClick = { onJobClick(job.id) },
                                onDeleteClick = { viewModel.deleteJob(job) } // Pass delete handler
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        } // <-- Add missing closing brace for Column
    }
}

@Composable
fun JobListItem(job: Job, onClick: () -> Unit, onDeleteClick: () -> Unit) { // Add onDeleteClick parameter
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(job.jobName, style = MaterialTheme.typography.titleMedium)
            Text("${job.clientName} - ${job.address}", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(job.status, style = MaterialTheme.typography.labelMedium) // Or use a Chip
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onDeleteClick) { // Add Delete IconButton
            Icon(Icons.Default.Delete, contentDescription = "Delete Job")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JobListScreenPreview() {
    ElectricianAppNewTheme {
        JobListScreen(onJobClick = {}, onAddJobClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun JobListItemPreview() {
     ElectricianAppNewTheme {
         // Add clientId to the dummy Job object
         JobListItem(
             job = Job(jobName = "Preview Job", clientId = "dummy_client", clientName = "Preview Client", address = "Preview Address", status = "Preview Status"),
             onClick = {},
             onDeleteClick = {} // Add dummy onDeleteClick for preview
         )
     }
}
