package com.example.electricianappnew.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// --- NEC Table 310.16 Ampacities ---
@Entity(tableName = "nec_ampacities", primaryKeys = ["material", "size", "temp_rating"])
data class NecAmpacityEntry(
    @ColumnInfo(name = "material") val material: String, // "Copper" or "Aluminum"
    @ColumnInfo(name = "size") val size: String, // e.g., "14 AWG", "250 kcmil"
    @ColumnInfo(name = "temp_rating") val tempRating: Int, // 60, 75, 90
    @ColumnInfo(name = "ampacity") val ampacity: Double
)

// --- NEC Chapter 9, Table 8 Conductor Properties ---
@Entity(tableName = "nec_conductor_properties", primaryKeys = ["material", "size"])
data class NecConductorEntry(
    @ColumnInfo(name = "material") val material: String, // "Copper" or "Aluminum"
    @ColumnInfo(name = "size") val size: String,
    @ColumnInfo(name = "circular_mils") val circularMils: Double,
    @ColumnInfo(name = "resistance_dc_1000ft_75c") val resistanceDcOhmsPer1000ft: Double, // DC resistance at 75C
    // Add AC resistance and reactance if needed for more precise VD calcs later
    @ColumnInfo(name = "area_in2") val areaSqIn: Double? = null // Optional: Area from Table 5 for convenience?
)

// --- NEC Chapter 9, Table 4 Conduit/Tubing Areas ---
@Entity(tableName = "nec_conduit_areas", primaryKeys = ["type", "size"])
data class NecConduitEntry(
    @ColumnInfo(name = "type") val type: String, // e.g., "EMT", "RMC"
    @ColumnInfo(name = "size") val size: String, // e.g., "1/2\"", "1\""
    @ColumnInfo(name = "internal_area_in2") val internalAreaSqIn: Double,
    @ColumnInfo(name = "fill_area_1_wire_in2") val fillArea1WireSqIn: Double, // 53%
    @ColumnInfo(name = "fill_area_2_wires_in2") val fillArea2WiresSqIn: Double, // 31%
    @ColumnInfo(name = "fill_area_over_2_wires_in2") val fillAreaOver2WiresSqIn: Double // 40%
    // Add 60% nipple area if desired
)

// --- NEC Chapter 9, Table 5 Wire Areas ---
@Entity(tableName = "nec_wire_areas", primaryKeys = ["insulation_type", "size"])
data class NecWireAreaEntry(
    @ColumnInfo(name = "insulation_type") val insulationType: String, // e.g., "THHN", "XHHW"
    @ColumnInfo(name = "size") val size: String,
    @ColumnInfo(name = "area_in2") val areaSqIn: Double
)

// --- NEC Table 314.16(B) Box Fill Allowances ---
@Entity(tableName = "nec_box_fill_allowances", primaryKeys = ["item_type", "conductor_size"])
data class NecBoxFillEntry(
    @ColumnInfo(name = "item_type") val itemType: String, // e.g., "Conductor", "Clamp", "Fitting", "Device", "Ground"
    @ColumnInfo(name = "conductor_size") val conductorSize: String, // e.g., "14 AWG", "12 AWG", "N/A" for items based on largest conductor
    @ColumnInfo(name = "volume_allowance_in3") val volumeAllowanceCuIn: Double,
    @ColumnInfo(name = "count_multiplier") val countMultiplier: Int = 1 // e.g., 2 for devices
)

// --- NEC Table 310.15(B) Ambient Temperature Correction Factors ---
@Entity(tableName = "nec_temp_corrections", primaryKeys = ["temp_rating", "ambient_temp_celsius"])
data class NecTempCorrectionEntry(
    @ColumnInfo(name = "temp_rating") val tempRating: Int, // 60, 75, 90
    @ColumnInfo(name = "ambient_temp_celsius") val ambientTempCelsius: Double, // The temperature value from the table row
    @ColumnInfo(name = "correction_factor") val correctionFactor: Double
)

// --- NEC Table 310.15(C) Conductor Adjustment Factors ---
@Entity(tableName = "nec_conductor_adjustments", primaryKeys = ["min_conductors", "max_conductors"])
data class NecConductorAdjustmentEntry(
    @ColumnInfo(name = "min_conductors") val minConductors: Int, // Start of the range (e.g., 4)
    @ColumnInfo(name = "max_conductors") val maxConductors: Int, // End of the range (e.g., 6)
    @ColumnInfo(name = "adjustment_factor") val adjustmentFactor: Double // e.g., 0.80
)

// --- NEC Tables 430.248 & 430.250 Motor Full-Load Current (FLC) ---
@Entity(tableName = "nec_motor_flc", primaryKeys = ["hp", "voltage", "phase"])
data class NecMotorFLCEntry(
    @ColumnInfo(name = "hp") val hp: Double, // Horsepower rating
    @ColumnInfo(name = "voltage") val voltage: Int, // Nominal voltage
    @ColumnInfo(name = "phase") val phase: Int, // 1 or 3
    @ColumnInfo(name = "flc") val flc: Double // Full-Load Current in Amperes
)

// --- NEC Table 430.52 Max Protection Rating/Setting ---
@Entity(tableName = "nec_motor_protection", primaryKeys = ["motor_type", "device_type"]) // Assuming motor_type distinguishes standard motors
data class NecMotorProtectionEntry(
    @ColumnInfo(name = "motor_type") val motorType: String, // e.g., "Single-Phase AC", "Three-Phase AC Squirrel Cage"
    @ColumnInfo(name = "device_type") val deviceType: String, // e.g., "Non-Time Delay Fuse", "Dual Element Fuse", "Inverse Time Breaker"
    @ColumnInfo(name = "max_percent_flc") val maxPercentFLC: Int // Maximum percentage of FLC allowed
    // Could add columns for exceptions if needed
)

// --- NEC Chapter 9, Table 9 Conductor Impedance ---
@Entity(tableName = "nec_conductor_impedance", primaryKeys = ["material", "size", "raceway_type"]) // Composite key
data class NecConductorImpedanceEntry(
    @ColumnInfo(name = "material") val material: String, // "Copper" or "Aluminum"
    @ColumnInfo(name = "size") val size: String, // e.g., "14 AWG", "250 kcmil"
    @ColumnInfo(name = "raceway_type") val racewayType: String, // e.g., "Steel", "Aluminum", "PVC"
    @ColumnInfo(name = "resistance_ac_ohms_1000ft") val resistanceAcOhmsPer1000ft: Double, // Effective Z at 0.85 PF
    @ColumnInfo(name = "reactance_xl_ohms_1000ft") val reactanceOhmsPer1000ft: Double // Reactance (Xl)
)
