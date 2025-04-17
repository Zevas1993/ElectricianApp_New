package com.example.electricianappnew.data.local.dao

import androidx.room.*
import com.example.electricianappnew.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NecDataDao {

    // --- Ampacity Queries (Table 310.16) ---
    @Query("SELECT * FROM nec_ampacities WHERE material = :material AND size = :size AND temp_rating = :tempRating")
    suspend fun getAmpacityEntry(material: String, size: String, tempRating: Int): NecAmpacityEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAmpacityEntries(entries: List<NecAmpacityEntry>)

    @Query("SELECT DISTINCT size FROM nec_ampacities ORDER BY size ASC") // Consider custom sorting
    suspend fun getDistinctAmpacityWireSizes(): List<String> // For dropdown

    @Query("SELECT DISTINCT temp_rating FROM nec_ampacities ORDER BY temp_rating ASC")
    suspend fun getDistinctAmpacityTempRatings(): List<Int> // For dropdown/validation

    @Query("SELECT * FROM nec_ampacities") // Added: Get all ampacity entries
    fun getAllNecAmpacityEntries(): Flow<List<NecAmpacityEntry>>

    // Search Ampacity Table
    @Query("SELECT * FROM nec_ampacities WHERE material LIKE '%' || :query || '%' OR size LIKE '%' || :query || '%'")
    fun searchAmpacity(query: String): Flow<List<NecAmpacityEntry>>

    // Search Conduit Table
    @Query("SELECT * FROM nec_conduit_areas WHERE type LIKE '%' || :query || '%' OR size LIKE '%' || :query || '%'")
    fun searchConduit(query: String): Flow<List<NecConduitEntry>>

    // --- Temperature Correction Factors (Table 310.15(B)) ---
    // Assuming a table structure like: temp_rating, ambient_temp_celsius, correction_factor
    @Query("SELECT * FROM nec_temp_corrections WHERE temp_rating = :tempRating ORDER BY ABS(ambient_temp_celsius - :ambientTempC) LIMIT 1")
    suspend fun getTempCorrectionFactorEntry(tempRating: Int, ambientTempC: Double): NecTempCorrectionEntry? // Find closest match

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTempCorrectionEntries(entries: List<NecTempCorrectionEntry>) // Assuming NecTempCorrectionEntry model exists

    @Query("SELECT * FROM nec_temp_corrections") // Added: Get all temp correction entries
    fun getAllNecTempCorrectionEntries(): Flow<List<NecTempCorrectionEntry>>

    // --- Conductor Adjustment Factors (Table 310.15(C)) ---
    // Assuming a table structure like: min_conductors, max_conductors, adjustment_factor
    @Query("SELECT * FROM nec_conductor_adjustments WHERE :numConductors BETWEEN min_conductors AND max_conductors LIMIT 1")
    suspend fun getConductorAdjustmentFactorEntry(numConductors: Int): NecConductorAdjustmentEntry? // Find matching range

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConductorAdjustmentEntries(entries: List<NecConductorAdjustmentEntry>) // Assuming NecConductorAdjustmentEntry model exists

    @Query("SELECT * FROM nec_conductor_adjustments") // Added: Get all conductor adjustment entries
    fun getAllNecConductorAdjustmentEntries(): Flow<List<NecConductorAdjustmentEntry>>


    // --- Conductor Properties (Table 8) ---
    @Query("SELECT * FROM nec_conductor_properties WHERE material = :material AND size = :size")
    suspend fun getConductorProperties(material: String, size: String): NecConductorEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConductorEntries(entries: List<NecConductorEntry>)

    @Query("SELECT * FROM nec_conductor_properties") // Added: Get all conductor entries
    fun getAllNecConductorEntries(): Flow<List<NecConductorEntry>>

    // Search Conductor Properties Table
    @Query("SELECT * FROM nec_conductor_properties WHERE material LIKE '%' || :query || '%' OR size LIKE '%' || :query || '%'")
    fun searchConductorProperties(query: String): Flow<List<NecConductorEntry>>

    // --- Conduit Areas (Table 4) ---
     @Query("SELECT * FROM nec_conduit_areas WHERE type = :type AND size = :size")
    suspend fun getConduitEntry(type: String, size: String): NecConduitEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConduitEntries(entries: List<NecConduitEntry>)

    @Query("SELECT DISTINCT type FROM nec_conduit_areas ORDER BY type ASC")
    suspend fun getDistinctConduitTypes(): List<String>

    @Query("SELECT DISTINCT size FROM nec_conduit_areas WHERE type = :type ORDER BY size ASC") // Consider custom sorting if needed
    suspend fun getDistinctConduitSizesForType(type: String): List<String>

    @Query("SELECT * FROM nec_conduit_areas WHERE type = :type ORDER BY internal_area_in2 ASC") // Get all sizes for a type, ordered by area
    suspend fun getAllConduitEntriesForType(type: String): List<NecConduitEntry> // Keep this specific one if needed

    @Query("SELECT * FROM nec_conduit_areas") // Added: Get all conduit entries
    fun getAllNecConduitEntries(): Flow<List<NecConduitEntry>>

    // --- Wire Areas (Table 5) ---
     @Query("SELECT * FROM nec_wire_areas WHERE insulation_type = :insulationType AND size = :size")
    suspend fun getWireAreaEntry(insulationType: String, size: String): NecWireAreaEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWireAreaEntries(entries: List<NecWireAreaEntry>)

    @Query("SELECT DISTINCT insulation_type FROM nec_wire_areas ORDER BY insulation_type ASC")
    suspend fun getDistinctWireTypes(): List<String>

    @Query("SELECT DISTINCT size FROM nec_wire_areas WHERE insulation_type = :insulationType ORDER BY size ASC") // Consider custom sorting if needed
    suspend fun getDistinctWireSizesForType(insulationType: String): List<String>

    @Query("SELECT * FROM nec_wire_areas") // Added: Get all wire area entries
    fun getAllNecWireAreaEntries(): Flow<List<NecWireAreaEntry>>

    // Search Wire Area Table
    @Query("SELECT * FROM nec_wire_areas WHERE insulation_type LIKE '%' || :query || '%' OR size LIKE '%' || :query || '%'")
    fun searchWireArea(query: String): Flow<List<NecWireAreaEntry>>

    // --- Box Fill Allowances (Table 314.16(B)) ---
     @Query("SELECT * FROM nec_box_fill_allowances WHERE item_type = :itemType AND conductor_size = :conductorSize")
    suspend fun getBoxFillEntry(itemType: String, conductorSize: String): NecBoxFillEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoxFillEntries(entries: List<NecBoxFillEntry>)

    @Query("SELECT DISTINCT conductor_size FROM nec_box_fill_allowances WHERE item_type = 'Conductor' ORDER BY conductor_size ASC") // Get sizes relevant for conductors
    suspend fun getDistinctBoxFillConductorSizes(): List<String>

    @Query("SELECT * FROM nec_box_fill_allowances") // Added: Get all box fill entries
    fun getAllNecBoxFillEntries(): Flow<List<NecBoxFillEntry>>

    // Search Box Fill Table
    @Query("SELECT * FROM nec_box_fill_allowances WHERE item_type LIKE '%' || :query || '%' OR conductor_size LIKE '%' || :query || '%'")
    fun searchBoxFill(query: String): Flow<List<NecBoxFillEntry>>

    // --- Motor FLC (Tables 430.248 & 430.250) ---
    @Query("SELECT * FROM nec_motor_flc WHERE hp = :hp AND voltage = :voltage AND phase = :phase LIMIT 1")
    suspend fun getMotorFLCEntry(hp: Double, voltage: Int, phase: Int): NecMotorFLCEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotorFLCEntries(entries: List<NecMotorFLCEntry>)

    @Query("SELECT * FROM nec_motor_flc") // Added: Get all motor FLC entries
    fun getAllNecMotorFLCEntries(): Flow<List<NecMotorFLCEntry>>

    // --- Motor Protection (Table 430.52) ---
    @Query("SELECT * FROM nec_motor_protection WHERE motor_type = :motorType AND device_type = :deviceType LIMIT 1")
    suspend fun getMotorProtectionEntry(motorType: String, deviceType: String): NecMotorProtectionEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotorProtectionEntries(entries: List<NecMotorProtectionEntry>)

    @Query("SELECT * FROM nec_motor_protection") // Added: Get all motor protection entries
    fun getAllNecMotorProtectionEntries(): Flow<List<NecMotorProtectionEntry>>

    // --- Conductor Impedance (Table 9) ---
    @Query("SELECT * FROM nec_conductor_impedance WHERE material = :material AND size = :size AND raceway_type = :racewayType LIMIT 1")
    suspend fun getConductorImpedanceEntry(material: String, size: String, racewayType: String): NecConductorImpedanceEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConductorImpedanceEntries(entries: List<NecConductorImpedanceEntry>)

    @Query("SELECT * FROM nec_conductor_impedance") // Added: Get all conductor impedance entries
    fun getAllNecConductorImpedanceEntries(): Flow<List<NecConductorImpedanceEntry>>

    // TODO: Add queries for other NEC data as needed (e.g., demand factors)
}
