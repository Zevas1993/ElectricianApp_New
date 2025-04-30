package com.example.electricianappnew.ui.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Import the AutoMirrored version
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorListScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculators") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") // Use the AutoMirrored version
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Make column scrollable
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp) // Add spacing between buttons
        ) {
            Text("Select a Calculator", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            // Add buttons for each calculator
            CalculatorButton(navController = navController, route = "ohmsLaw", text = "Ohm's Law")
            CalculatorButton(navController = navController, route = "seriesParallelResistance", text = "Series/Parallel Resistance")
            CalculatorButton(navController = navController, route = "voltageDrop", text = "Voltage Drop")
            CalculatorButton(navController = navController, route = "wireAmpacity", text = "Wire Ampacity")
            CalculatorButton(navController = navController, route = "conduitFill", text = "Conduit Fill")
            CalculatorButton(navController = navController, route = "boxFill", text = "Box Fill")
            CalculatorButton(navController = navController, route = "racewaySizing", text = "Raceway Sizing")
            CalculatorButton(navController = navController, route = "dwellingLoad", text = "Dwelling Load")
            CalculatorButton(navController = navController, route = "motorCalculator", text = "Motor Calculator")
            CalculatorButton(navController = navController, route = "transformerSizing", text = "Transformer Sizing")
            CalculatorButton(navController = navController, route = "pipeBending", text = "Pipe Bending")
            CalculatorButton(navController = navController, route = "luminaireLayout", text = "Luminaire Layout")
            CalculatorButton(navController = navController, route = "faultCurrent", text = "Fault Current")
            // Add more buttons as needed
        }
    }
}

@Composable
private fun CalculatorButton(navController: NavController, route: String, text: String) {
    Button(
        onClick = { navController.navigate(route) },
        modifier = Modifier.fillMaxWidth() // Make buttons fill width
    ) {
        Text(text)
    }
}
