package com.example.electricianappnew.ui.neccode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircuitColorReferenceScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Circuit Color Reference") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Circuit Color Reference Content Goes Here")
            // TODO: Implement actual circuit color reference display
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCircuitColorReferenceScreen() {
    ElectricianAppNewTheme {
        CircuitColorReferenceScreen(navController = rememberNavController())
    }
}
