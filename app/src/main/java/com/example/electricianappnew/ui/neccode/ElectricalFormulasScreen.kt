package com.example.electricianappnew.ui.neccode

import androidx.compose.foundation.layout.* // Correct wildcard import
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.* // Correct wildcard import
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

data class FormulaInfo(
    val category: String,
    val formulas: List<Pair<String, String>> // Pair of Formula Name/Description and Formula String
)

// Common Electrical Formulas
val commonFormulas = listOf(
    FormulaInfo(
        category = "Ohm's Law",
        formulas = listOf(
            "Voltage (V)" to "V = I × R",
            "Current (I)" to "I = V / R",
            "Resistance (R)" to "R = V / I"
        )
    ),
    FormulaInfo(
        category = "Power (P)",
        formulas = listOf(
            "Power (Watts)" to "P = V × I",
            "Power (Watts)" to "P = I² × R",
            "Power (Watts)" to "P = V² / R"
        )
    ),
    FormulaInfo(
        category = "Three-Phase Power",
        formulas = listOf(
            "Apparent Power (VA)" to "S = √3 × V_L × I_L",
            "Real Power (Watts)" to "P = √3 × V_L × I_L × PF",
            "Real Power (Watts)" to "P = 3 × V_P × I_P × PF"
            // Add Reactive Power (VAR) if needed
        )
    ),
     FormulaInfo(
        category = "Voltage Drop (Approx.)",
        formulas = listOf(
            "Single-Phase VD" to "VD = (2 × K × I × L) / CM",
            "Three-Phase VD" to "VD = (√3 × K × I × L) / CM",
            "K (Copper, approx)" to "≈ 12.9 Ω·CM/ft",
            "K (Aluminum, approx)" to "≈ 21.2 Ω·CM/ft"
            // Note: CM = Circular Mils, L = Length (ft), I = Current (A)
        )
    ),
    // Add more categories like Series/Parallel Resistance, Capacitance, Inductance etc.
)

@Composable
fun ElectricalFormulasScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        item {
            Text(
                "Common Electrical Formulas",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(commonFormulas.size) { index ->
            val formulaInfo = commonFormulas[index]
            FormulaCategoryCard(formulaInfo = formulaInfo)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FormulaCategoryCard(formulaInfo: FormulaInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(formulaInfo.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            formulaInfo.formulas.forEach { (name, formula) ->
                Text("${name}:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(formula, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp, bottom = 4.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ElectricalFormulasScreenPreview() {
    ElectricianAppNewTheme {
        ElectricalFormulasScreen()
    }
}
