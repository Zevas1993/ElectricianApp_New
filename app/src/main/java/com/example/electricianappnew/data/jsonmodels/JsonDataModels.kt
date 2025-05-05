package com.example.electricianappnew.data.jsonmodels

import kotlinx.serialization.Serializable

// --- Luminaire Layout - Coefficient of Utilization (CU) Data ---
// Represents a single RCR and its corresponding CU value within a table
@Serializable
data class NecCuValue(
    val rcr: Int,
    val cu: Double
)

// Represents a CU table for specific room surface reflectances
@Serializable
data class NecCuTable(
    val description: String?, // Optional description
    val ceilingReflectance: Int,
    val wallReflectance: Int,
    val floorReflectance: Int,
    val cuValues: List<NecCuValue>
)

// Data class for parsing cu_data.json
@Serializable
data class CuData(
    val cuTables: List<NecCuTable>
)
