package com.example.electricianappnew.ui.neccode

import androidx.compose.foundation.background
import androidx.compose.foundation.border // Import border
import androidx.compose.foundation.layout.* // Correct wildcard import
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape // Import CircleShape
import androidx.compose.material3.* // Correct wildcard import
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme

// Data class to hold color code information
data class ColorCodeInfo(
    val voltageSystem: String,
    val phaseAColor: Color,
    val phaseBColor: Color,
    val phaseCColor: Color,
    val neutralColor: Color,
    val groundColor: Color,
    val phaseALabel: String = "A",
    val phaseBLabel: String = "B",
    val phaseCLabel: String = "C",
    val neutralLabel: String = "N",
    val groundLabel: String = "G"
)

// Standard Color Codes (adjust based on common US standards)
val standardColorCodes = listOf(
    ColorCodeInfo(
        voltageSystem = "120/240V Single-Phase",
        phaseAColor = Color.Black, phaseALabel = "L1",
        phaseBColor = Color.Red, phaseBLabel = "L2",
        phaseCColor = Color.Transparent, phaseCLabel = "-", // Indicate not applicable
        neutralColor = Color.White,
        groundColor = Color(0xFF008000) // Dark Green
    ),
    ColorCodeInfo(
        voltageSystem = "120/208V Three-Phase",
        phaseAColor = Color.Black,
        phaseBColor = Color.Red,
        phaseCColor = Color.Blue,
        neutralColor = Color.White, // Or Gray
        groundColor = Color(0xFF008000)
    ),
    ColorCodeInfo(
        voltageSystem = "277/480V Three-Phase",
        phaseAColor = Color(0xFFFFA500), // Orange (Brown often used too)
        phaseBColor = Color(0xFF800080), // Purple (Yellow often used too)
        phaseCColor = Color(0xFFFFFF00), // Yellow (Blue often used too - Regional variations exist!)
        neutralColor = Color.Gray, // Or White with stripe
        groundColor = Color(0xFF008000)
    )
    // Add other systems if needed (e.g., High-Leg Delta)
)

@Composable
fun CircuitColorReferenceScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        item {
            Text(
                "Circuit Color Codes (Common US Standards)",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(standardColorCodes.size) { index ->
            val codeInfo = standardColorCodes[index]
            ColorCodeCard(codeInfo = codeInfo)
            Spacer(modifier = Modifier.height(16.dp))
        }

         item {
             Text(
                 "Note: Local codes and specific project requirements may vary. Always verify.",
                 style = MaterialTheme.typography.bodySmall,
                 modifier = Modifier.padding(top = 8.dp)
             )
         }
    }
}

@Composable
fun ColorCodeCard(codeInfo: ColorCodeInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(codeInfo.voltageSystem, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround // Distribute colors evenly
            ) {
                ColorSwatch(label = codeInfo.phaseALabel, color = codeInfo.phaseAColor)
                ColorSwatch(label = codeInfo.phaseBLabel, color = codeInfo.phaseBColor)
                ColorSwatch(label = codeInfo.phaseCLabel, color = codeInfo.phaseCColor)
                ColorSwatch(label = codeInfo.neutralLabel, color = codeInfo.neutralColor)
                ColorSwatch(label = codeInfo.groundLabel, color = codeInfo.groundColor)
            }
        }
    }
}

@Composable
fun ColorSwatch(label: String, color: Color) {
    if (color == Color.Transparent) return // Don't display if color is transparent (e.g., no Phase C)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, shape = CircleShape)
                .border(1.dp, Color.Gray, CircleShape) // Add border for light colors like White/Yellow
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Preview(showBackground = true)
@Composable
fun CircuitColorReferenceScreenPreview() {
    ElectricianAppNewTheme {
        CircuitColorReferenceScreen()
    }
}
