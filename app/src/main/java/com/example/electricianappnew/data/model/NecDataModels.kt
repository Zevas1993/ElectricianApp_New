package com.example.electricianappnew.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Fts4
import com.google.gson.annotations.SerializedName

// --- NEC Full Code Structure ---

@Entity(tableName = "nec_articles")
data class NecArticle(
    @PrimaryKey val articleNumber: String, // e.g., "250"
    @ColumnInfo(name = "title") val title: String // e.g., "Grounding and Bonding"
)

@Entity(
    tableName = "nec_sections",
    indices = [Index(value = ["article_number"])] // Index for faster lookup by article
)
data class NecSection(
    @PrimaryKey val sectionNumber: String, // e.g., "250.52", "110.14(A)"
    @ColumnInfo(name = "article_number") val articleNumber: String, // Foreign key reference (conceptual)
    @ColumnInfo(name = "title") val title: String?, // Optional title for the section
    @ColumnInfo(name = "text_content") val textContent: String // The actual text of the code section
    // `rowid` is implicitly available for FTS mapping
)

// FTS table for searching NecSection text content
@Fts4(contentEntity = NecSection::class)
@Entity(tableName = "nec_sections_fts")
data class NecSectionFts(
    // Columns must match contentEntity: sectionNumber, article_number, title, text_content
    // Room handles mapping these automatically based on contentEntity
    // We primarily search on 'text_content' and potentially 'title'
    @ColumnInfo(name = "text_content") val textContent: String,
    @ColumnInfo(name = "title") val title: String?
    // Add other columns from NecSection if you want to search them via FTS
)


// --- NEC Table 310.16 Ampacities ---
@Entity(tableName = "nec_ampacities", primaryKeys = ["material", "size", "temp_rating"])
data class NecAmpacityEntry(
    @ColumnInfo(name = "material") val material: String, // "Copper" or "Aluminum"
    @ColumnInfo(name = "size") val size: String, // e.g., "14 AWG", "250 kcmil"
    @SerializedName("temp_rating") // Map JSON key
    @ColumnInfo(name = "temp_rating") val tempRating: Int, // 60, 75, 90
    @ColumnInfo(name = "ampacity") val ampacity: Double
)

// --- NEC Chapter 9, Table 8 Conductor Properties ---
@Entity(tableName = "nec_conductor_properties", primaryKeys = ["material", "size"])
data class NecConductorEntry(
    @ColumnInfo(name = "material") val material: String, // "Copper" or "Aluminum"
    @ColumnInfo(name = "size") val size: String,
    @ColumnInfo(name = "circular_mils") val circularMils: Double,
    @SerializedName("resistance_dc_1000ft_75c") // Map JSON key
    @ColumnInfo(name = "resistance_dc_1000ft_75c") val resistanceDcOhmsPer1000ft: Double, // DC resistance at 75C
    // Add AC resistance and reactance if needed for more precise VD calcs later
    @ColumnInfo(name = "area_in2") val areaSqIn: Double? = null // Optional: Area from Table 5 for convenience?
)

// --- NEC Chapter 9, Table 9 Conductor AC Impedance ---
@Entity(tableName = "nec_ac_impedance", primaryKeys = ["material", "size"])
data class NecAcImpedanceEntry(
    @SerializedName("material") // Map JSON key
    @ColumnInfo(name = "material") val material: String, // "Copper" or "Aluminum"
    @SerializedName("size") // Map JSON key
    @ColumnInfo(name = "size") val size: String, // e.g., "14 AWG", "250 kcmil"
    @SerializedName("resistance_ac_ohms_per_1000ft") // Map JSON key
    @ColumnInfo(name = "resistance_ac_ohms_per_1000ft") val resistanceAcOhmsPer1000ft: Double, // AC resistance
    @SerializedName("reactance_ohms_per_1000ft") // Map JSON key
    @ColumnInfo(name = "reactance_ohms_per_1000ft") val reactanceOhmsPer1000ft: Double // Reactance (Xl)
)


// --- NEC Chapter 9, Table 4 Conduit/Tubing Areas ---
@Entity(tableName = "nec_conduit_areas", primaryKeys = ["type", "size"])
data class NecConduitEntry(
    @ColumnInfo(name = "type") val type: String, // e.g., "EMT", "RMC"
    @ColumnInfo(name = "size") val size: String, // e.g., "1/2\"", "1\""
    @SerializedName("internal_area_in2") // Map JSON key
    @ColumnInfo(name = "internal_area_in2") val internalAreaSqIn: Double,
    @SerializedName("fill_area_1_wire_in2") // Map JSON key
    @ColumnInfo(name = "fill_area_1_wire_in2") val fillArea1WireSqIn: Double, // 53%
    @SerializedName("fill_area_2_wires_in2") // Map JSON key
    @ColumnInfo(name = "fill_area_2_wires_in2") val fillArea2WiresSqIn: Double, // 31%
    @SerializedName("fill_area_over_2_wires_in2") // Map JSON key
    @ColumnInfo(name = "fill_area_over_2_wires_in2") val fillAreaOver2WiresSqIn: Double // 40%
    // Add 60% nipple area if desired
)

// --- NEC Chapter 9, Table 5 Wire Areas ---
@Entity(
    tableName = "nec_wire_areas",
    indices = [Index(value = ["insulation_type", "size"], unique = true)]
)
data class NecWireAreaEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @SerializedName("insulationType") // Map JSON key to this field
    @ColumnInfo(name = "insulation_type") val insulationType: String?, // e.g., "THHN", "XHHW", null for bare
    @SerializedName("size") // Map JSON key (even if same, good practice)
    @ColumnInfo(name = "size") val size: String,
    @SerializedName("areaSqIn") // Map JSON key to this field
    @ColumnInfo(name = "area_in2") val areaSqIn: Double
)

// --- NEC Table 314.16(B) Box Fill Allowances ---
@Entity(tableName = "nec_box_fill_allowances", primaryKeys = ["item_type", "conductor_size"])
data class NecBoxFillEntry(
    @SerializedName("item_type") // Map JSON key
    @ColumnInfo(name = "item_type") val item_type: String, // Reverted to non-nullable
    @SerializedName("conductor_size") // Map JSON key
    @ColumnInfo(name = "conductor_size") val conductorSize: String, // e.g., "14 AWG", "12 AWG", "N/A" for items based on largest conductor
    @SerializedName("volume_allowance_in3") // Map JSON key
    @ColumnInfo(name = "volume_allowance_in3") val volumeAllowanceCuIn: Double,
    @SerializedName("count_multiplier") // Map JSON key
    @ColumnInfo(name = "count_multiplier") val countMultiplier: Int = 1 // e.g., 2 for devices
)

// --- NEC Table 310.15(B) Ambient Temperature Correction Factors ---
@Entity(tableName = "nec_temp_corrections", primaryKeys = ["temp_rating", "ambient_temp_celsius"])
data class NecTempCorrectionEntry(
    val id: Long = 0,
    @SerializedName("tempRating") // Map JSON key
    @ColumnInfo(name = "temp_rating") val tempRating: Int, // 60, 75, 90
    @SerializedName("ambientTempCelsius") // Map JSON key
    @ColumnInfo(name = "ambient_temp_celsius") val ambientTempCelsius: Double,
    @SerializedName("correctionFactor") // Map JSON key
    @ColumnInfo(name = "correction_factor") val correctionFactor: Double
)

// --- NEC Table 310.15(C) Conductor Adjustment Factors ---
@Entity(tableName = "nec_conductor_adjustments", primaryKeys = ["min_conductors", "max_conductors"])
data class NecConductorAdjustmentEntry(
    @SerializedName("min_conductors") // Map JSON key
    @ColumnInfo(name = "min_conductors") val minConductors: Int, // Start of the range (e.g., 4)
    @SerializedName("max_conductors") // Map JSON key
    @ColumnInfo(name = "max_conductors") val maxConductors: Int, // End of the range (e.g., 6)
    @SerializedName("factor") // Map JSON key ("factor" in JSON, "adjustment_factor" in DB)
    @ColumnInfo(name = "adjustment_factor") val adjustmentFactor: Double // e.g., 0.80
)

// --- NEC Tables 430.248 & 430.250 Motor Full-Load Current (FLC) ---
@Entity(tableName = "nec_motor_flc", primaryKeys = ["hp", "voltage", "phase"])
data class NecMotorFLCEntry(
    @ColumnInfo(name = "hp") val hp: Double, // Horsepower rating
    @ColumnInfo(name = "voltage") val voltage: Int, // Nominal voltage
    @ColumnInfo(name = "phase") val phase: String, // "Single" or "Three"
    @ColumnInfo(name = "flc") val flc: Double // Full-Load Current in Amperes
)

// --- NEC Table 430.52 Max Protection Rating/Setting (Percentages) ---
@Entity(tableName = "nec_motor_protection_percentages", primaryKeys = ["device_type"])
data class NecMotorProtectionPercentageEntry(
    @ColumnInfo(name = "device_type") val deviceType: String, // e.g., "Non-Time Delay Fuse", "Dual Element Fuse"
    @ColumnInfo(name = "max_percent_flc") val maxPercentFLC: Int // Maximum percentage of FLC allowed
)

// --- NEC Table 430.52 Max Protection Rating/Setting (Non-Time Delay Fuse Sizes) ---
@Entity(tableName = "nec_motor_protection_nontime_delay_fuse_sizes", primaryKeys = ["hp", "volts"])
data class NecMotorProtectionNonTimeDelayFuseSizeEntry(
    @ColumnInfo(name = "hp") val hp: Double,
    @ColumnInfo(name = "volts") val volts: Int,
    @ColumnInfo(name = "amps") val amps: Double,
    @ColumnInfo(name = "max_fuse") val maxFuse: Int
)

// --- NEC Table 430.52 Max Protection Rating/Setting (Time-Delay Fuse Sizes) ---
@Entity(tableName = "nec_motor_protection_time_delay_fuse_sizes", primaryKeys = ["hp", "volts"])
data class NecMotorProtectionTimeDelayFuseSizeEntry(
    @ColumnInfo(name = "hp") val hp: Double,
    @ColumnInfo(name = "volts") val volts: Int,
    @ColumnInfo(name = "amps") val amps: Double,
    @ColumnInfo(name = "max_fuse") val maxFuse: Int
)


// --- NEC Chapter 9, Table 9 Conductor Impedance ---
@Entity(tableName = "nec_conductor_impedance", primaryKeys = ["material", "size", "raceway_type"]) // Composite key
data class NecConductorImpedanceEntry(
    @SerializedName("material") // Map JSON key
    @ColumnInfo(name = "material") val material: String, // "Copper" or "Aluminum"
    @SerializedName("size") // Map JSON key
    @ColumnInfo(name = "size") val size: String, // e.g., "14 AWG", "250 kcmil"
    @SerializedName("racewayType") // Map JSON key
    @ColumnInfo(name = "raceway_type") val racewayType: String, // e.g., "Steel", "Aluminum", "PVC"
    @SerializedName("resistanceAcOhmsPer1000ft") // Map JSON key
    @ColumnInfo(name = "resistance_ac_ohms_1000ft") val resistanceAcOhmsPer1000ft: Double, // Effective Z at 0.85 PF
    @SerializedName("reactanceOhmsPer1000ft") // Map JSON key
    @ColumnInfo(name = "reactance_xl_ohms_1000ft") val reactanceOhmsPer1000ft: Double // Reactance (Xl)
)

// --- Luminaire Layout - Coefficient of Utilization (CU) Data ---
// Represents a single RCR and its corresponding CU value within a table
data class NecCuValue(
    @SerializedName("rcr") val rcr: Int,
    @SerializedName("cu") val cu: Double
)

// Represents a CU table for specific room surface reflectances
@Entity(tableName = "nec_cu_tables")
data class NecCuTable(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @SerializedName("description") val description: String?, // Optional description
    @SerializedName("ceiling_reflectance") @ColumnInfo(name = "ceiling_reflectance") val ceilingReflectance: Int,
    @SerializedName("wall_reflectance") @ColumnInfo(name = "wall_reflectance") val wallReflectance: Int,
    @SerializedName("floor_reflectance") @ColumnInfo(name = "floor_reflectance") val floorReflectance: Int,
    // Room can store a list of complex objects using a TypeConverter
    @SerializedName("cu_values") @ColumnInfo(name = "cu_values") val cuValues: List<NecCuValue> // Remove @Ignore, add @ColumnInfo
)

// Data class for parsing cu_data.json
data class CuData(
    @SerializedName("cu_tables") val cuTables: List<NecCuTable>
)

// Data classes for parsing motor_calculator_data.json
data class SinglePhaseFLCJson(
    @SerializedName("hp") val hp: Double,
    @SerializedName("115v") val volts115: Double,
    @SerializedName("230v") val volts230: Double
)

data class ThreePhaseFLCJson(
    @SerializedName("hp") val hp: Double,
    @SerializedName("200v") val volts200: Double,
    @SerializedName("208v") val volts208: Double,
    @SerializedName("230v") val volts230: Double,
    @SerializedName("460v") val volts460: Double,
    @SerializedName("575v") val volts575: Double
)

data class MotorProtectionPercentageJson(
    @SerializedName("type") val type: String,
    @SerializedName("percentage") val percentage: Int
)

data class MotorProtectionFuseSizeJson(
    @SerializedName("hp") val hp: Double,
    @SerializedName("volts") val volts: Int,
    @SerializedName("amps") val amps: Double,
    @SerializedName("max_fuse") val maxFuse: Int
)


data class MotorCalculatorData(
    @SerializedName("single_phase_flc") val singlePhaseFlc: List<SinglePhaseFLCJson>,
    @SerializedName("three_phase_flc") val threePhaseFlc: List<ThreePhaseFLCJson>,
    @SerializedName("motor_protection_percentages") val motorProtectionPercentages: List<MotorProtectionPercentageJson>,
    @SerializedName("motor_protection_nontime_delay_fuse_sizes") val motorProtectionNonTimeDelayFuseSizes: List<MotorProtectionFuseSizeJson>,
    @SerializedName("motor_protection_time_delay_fuse_sizes") val motorProtectionTimeDelayFuseSizes: List<MotorProtectionFuseSizeJson>
)


// --- Sealed Interface for NEC Search Results ---
sealed interface NecSearchResult {
    val type: String // Common property to identify the result type
    val title: String // Common property for display title
    val details: String // Common property for display details

    data class AmpacityResult(val entry: NecAmpacityEntry) : NecSearchResult {
        override val type: String = "Ampacity (310.16)"
        override val title: String = "${entry.size} ${entry.material} @ ${entry.tempRating}°C"
        override val details: String = "Ampacity: ${entry.ampacity} A"
    }

    data class ConduitResult(val entry: NecConduitEntry) : NecSearchResult {
        override val type: String = "Conduit Area (Ch 9, Tbl 4)"
        override val title: String = "${entry.size} ${entry.type}"
        override val details: String = "Area: ${entry.internalAreaSqIn} in², >2 Wires Fill: ${entry.fillAreaOver2WiresSqIn} in² (40%)"
    }

    data class BoxFillResult(val entry: NecBoxFillEntry) : NecSearchResult {
        override val type: String = "Box Fill (314.16(B))"
        // Reverted: item_type is non-nullable again
        override val title: String = "${entry.item_type} (${entry.conductorSize})"
        override val details: String = "Volume: ${entry.volumeAllowanceCuIn} in³ (Multiplier: ${entry.countMultiplier})"
    }

    data class WireAreaResult(val entry: NecWireAreaEntry) : NecSearchResult {
        override val type: String = "Wire Area (Ch 9, Tbl 5)"
        override val title: String = "${entry.size} ${entry.insulationType ?: "Bare"}" // Handle null insulation
        override val details: String = "Area: ${entry.areaSqIn} in²"
    }

    data class ConductorResult(val entry: NecConductorEntry) : NecSearchResult {
        override val type: String = "Conductor Properties (Ch 9, Tbl 8)"
        override val title: String = "${entry.size} ${entry.material}"
        override val details: String = "DC Res: ${entry.resistanceDcOhmsPer1000ft} Ω/kft, Area: ${entry.areaSqIn ?: "-"} in², CM: ${entry.circularMils}"
    }

    // Removed redundant AcImpedanceResult
    // data class AcImpedanceResult(val entry: NecAcImpedanceEntry) : NecSearchResult {
    //     override val type: String = "AC Impedance (Ch 9, Tbl 9)"
    //     override val title: String = "${entry.size} ${entry.material} in ${entry.racewayType}"
    //     override val details: String = "AC Res: ${entry.resistanceAcOhmsPer1000ft} Ω/kft, Reactance: ${entry.reactanceOhmsPer1000ft} Ω/kft"
    // }

    data class ConductorImpedanceResult(val entry: NecConductorImpedanceEntry) : NecSearchResult {
        override val type: String = "AC Impedance (Ch 9, Tbl 9)"
        override val title: String = "${entry.size} ${entry.material} in ${entry.racewayType}"
        override val details: String = "AC Res: ${entry.resistanceAcOhmsPer1000ft} Ω/kft, Reactance: ${entry.reactanceOhmsPer1000ft} Ω/kft"
    }

    // Add other result types here later

    // Result type for full NEC code search
    // Modified to accept individual fields directly from the DAO query
    data class FullCodeResult(
        // Fields from NecSection table (s.* in the query)
        @ColumnInfo(name = "sectionNumber") val sectionNumber: String,
        @ColumnInfo(name = "article_number") val articleNumber: String,
        @ColumnInfo(name = "title") val sectionTitle: String?, // Renamed from 'title' to avoid conflict
        @ColumnInfo(name = "text_content") val textContent: String,
        // Field from NecArticle table (a.title as articleTitle in the query)
        @ColumnInfo(name = "articleTitle") val articleTitle: String?
    ) : NecSearchResult {
        @Ignore override val type: String = "NEC Code Section"
        // Construct title and details using the direct fields
        @Ignore override val title: String = "Sec $sectionNumber" + (sectionTitle?.let { " - $it" } ?: "")
        @Ignore override val details: String = "Article $articleNumber" + (articleTitle?.let { " ($it)" } ?: "") + "\n" + textContent.take(150) + "..." // Show snippet
    }
}
