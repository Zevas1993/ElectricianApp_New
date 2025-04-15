package com.example.electricianappnew.ui.invoicing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

// TODO: Implement Invoicing features (link to jobs, tasks, materials, time tracking)
@Composable
fun InvoicingScreen(
    modifier: Modifier = Modifier,
    // Add navigation callbacks as needed
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Invoicing", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Invoicing features (generating invoices from jobs, tracking payments) will be implemented here.")
        // Placeholder for future UI elements like:
        // - List of existing invoices (filterable by status: Draft, Sent, Paid, Overdue)
        // - Button to create new invoice (likely from a completed Job)
        // - Invoice detail view / PDF generation
    }
}

@Preview(showBackground = true)
@Composable
fun InvoicingScreenPreview() {
    ElectricianAppNewTheme {
        InvoicingScreen()
    }
}
