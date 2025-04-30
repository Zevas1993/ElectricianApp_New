package com.example.electricianappnew.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth // Add this import
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
// Removed Box import as it's not needed now

@OptIn(ExperimentalMaterial3Api::class) // Restore OptIn for Scaffold/TopAppBar
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    navController: NavController // Keep NavController for potential future use
) {
    // Restore Scaffold and TopAppBar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") }
                // No navigation icon needed for the home screen usually
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues) // Restore padding from Scaffold
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to the Electrician App!", // Restore original text
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp) // Add some space below title
            )

            // Navigation Buttons
            DashboardButton(navController = navController, route = "jobs", text = "Jobs")
            DashboardButton(navController = navController, route = "clients", text = "Clients")
            DashboardButton(navController = navController, route = "inventory", text = "Inventory")
            DashboardButton(navController = navController, route = "calculators", text = "Calculators")
            DashboardButton(navController = navController, route = "nec", text = "NEC Tools")
            DashboardButton(navController = navController, route = "photoDocs", text = "Photo Docs")
            // Add more buttons for Estimating, Invoicing, Reporting later if needed
        }
    }
}

@Composable
private fun DashboardButton(navController: NavController, route: String, text: String) {
    Button(
        onClick = { navController.navigate(route) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp) // Add vertical padding between buttons
    ) {
        Text(text)
    }
}
