package com.example.electricianappnew.ui.neccode

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

data class SymbolInfo(
    val category: String,
    val symbols: List<Pair<String, String>> // Pair of Symbol Name and Description/Image Placeholder
)

// Common Electrical Symbols (Replace placeholders with actual image resources later)
val commonSymbols = listOf(
    SymbolInfo(
        category = "Outlets & Receptacles",
        symbols = listOf(
            "Duplex Receptacle" to "[Image: Duplex Outlet Symbol]",
            "Single Receptacle" to "[Image: Single Outlet Symbol]",
            "GFCI Receptacle" to "[Image: GFCI Outlet Symbol]",
            "Weatherproof Receptacle" to "[Image: Weatherproof Outlet Symbol]",
            "Special Purpose Outlet" to "[Image: Special Purpose Outlet Symbol]"
        )
    ),
    SymbolInfo(
        category = "Switches",
        symbols = listOf(
            "Single-Pole Switch" to "[Image: S Symbol]",
            "Three-Way Switch" to "[Image: S3 Symbol]",
            "Four-Way Switch" to "[Image: S4 Symbol]",
            "Dimmer Switch" to "[Image: SD Symbol]",
            "Time Switch" to "[Image: ST Symbol]"
        )
    ),
    SymbolInfo(
        category = "Lighting",
        symbols = listOf(
            "Incandescent Fixture (Surface)" to "[Image: Circle Symbol]",
            "Incandescent Fixture (Recessed)" to "[Image: Circle with R Symbol]",
            "Fluorescent Fixture (Surface)" to "[Image: Rectangle Symbol]",
            "Exit Light" to "[Image: Circle with X Symbol]"
        )
    ),
    SymbolInfo(
        category = "Wiring & Connections",
        symbols = listOf(
            "Concealed Wiring" to "[Image: Dashed Line Symbol]",
            "Exposed Wiring / Conduit" to "[Image: Solid Line Symbol]",
            "Junction Box" to "[Image: Square/Circle with J Symbol]",
            "Ground Connection" to "[Image: Ground Symbol]"
        )
    ),
    SymbolInfo(
        category = "Panels & Equipment",
        symbols = listOf(
            "Panelboard" to "[Image: Rectangle with diagonal lines Symbol]",
            "Transformer" to "[Image: Transformer Symbol]",
            "Motor" to "[Image: Circle with M Symbol]"
        )
    )
    // Add more categories and symbols as needed
)

@Composable
fun ElectricalSymbolsScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        item {
            Text(
                "Common Electrical Symbols",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(commonSymbols.size) { index ->
            val symbolInfo = commonSymbols[index]
            SymbolCategoryCard(symbolInfo = symbolInfo)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SymbolCategoryCard(symbolInfo: SymbolInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(symbolInfo.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            symbolInfo.symbols.forEach { (name, descriptionOrPlaceholder) ->
                 Row(
                     modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                     verticalAlignment = Alignment.CenterVertically
                 ) {
                    // TODO: Replace Text with Image composable when resources are available
                    Text(
                        descriptionOrPlaceholder,
                        modifier = Modifier.size(32.dp).padding(end = 8.dp), // Placeholder size
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(name, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ElectricalSymbolsScreenPreview() {
    ElectricianAppNewTheme {
        ElectricalSymbolsScreen()
    }
}
