package com.example.electricianappnew.ui.estimating

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

// TODO: Implement Estimating features (link to jobs, materials, labor rates)
@Composable
fun EstimatingScreen(
    modifier: Modifier = Modifier,
    // Add navigation callbacks as needed
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Estimating / Quoting", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Estimating features (creating quotes, adding materials/labor) will be implemented here.")
        // Placeholder for future UI elements like:
        // - List of existing estimates
        // - Button to create new estimate
        // - Estimate detail view
    }
}

@Preview(showBackground = true)
@Composable
fun EstimatingScreenPreview() {
    ElectricianAppNewTheme {
        EstimatingScreen()
    }
}
