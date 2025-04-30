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
    fun getDistinctAmpacityWireSizes(): Flow<List<String>> // For dropdown - Changed to Flow

    @Query("SELECT DISTINCT temp_rating FROM nec_ampacities ORDER BY temp_rating ASC")
    fun getDistinctAmpacityTempRatings(): Flow<List<Int>> // For dropdown/validation - Changed to Flow

    @Query("SELECT * FROM nec_ampacities") // Added: Get all ampacity entries
    fun getAllNecAmpacityEntries(): Flow<List<NecAmpacityEntry>>

    // Search Ampacity Table
    @Query("SELECT * FROM nec_ampacities WHERE material LIKE '%' || :query || '%' OR size LIKE '%' || :query || '%'")
    fun searchAmpacity(query: String): Flow<List<NecAmpacityEntry>>

    // --- Temperature Correction Factors (Table 310.15(B)) ---
    // Assuming a table structure like: temp_rating, ambient_temp_celsius, correction_factor
    // Modified to return all entries for a given temp rating, finding closest ambient temp will be done in the repository
    @Query("SELECT * FROM nec_temp_corrections WHERE temp_rating = :tempRating")
    suspend fun getTempCorrectionEntriesForRating(tempRating: Int): List<NecTempCorrectionEntry>

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

    @Query("SELECT DISTINCT material FROM nec_conductor_properties ORDER BY material ASC")
    fun getDistinctConductorMaterials(): Flow<List<String>> // For Conduit Fill dropdown

    @Query("SELECT DISTINCT size FROM nec_conductor_properties ORDER BY size ASC") // Consider custom sorting
    fun getDistinctConductorSizes(): Flow<List<String>> // For Conduit Fill dropdown

    // Search Conductor Properties Table
    @Query("SELECT * FROM nec_conductor_properties WHERE material LIKE '%' || :query || '%' OR size LIKE '%' || :query || '%'")
    fun searchConductorProperties(query: String): Flow<List<NecConductorEntry>>

    // --- Conduit Areas (Table 4) ---
     @Query("SELECT * FROM nec_conduit_areas WHERE type = :type AND size = :size")
    suspend fun getConduitEntry(type: String, size: String): NecConduitEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConduitEntries(entries: List<NecConduitEntry>)

    @Query("SELECT DISTINCT type FROM nec_conduit_areas ORDER BY type ASC")
    fun getDistinctConduitTypes(): Flow<List<String>> // Changed return type to Flow

    // Get distinct conduit sizes for a given type (Modified for diagnostics - return full entry)
    @Query("SELECT * FROM nec_conduit_areas WHERE type = :type") // Select all columns
    fun getConduitEntriesForType(type: String): Flow<List<NecConduitEntry>> // Return full entries

    @Query("SELECT * FROM nec_conduit_areas WHERE type = :type ORDER BY internal_area_in2 ASC") // Get all sizes for a type, ordered by area
    suspend fun getAllConduitEntriesForType(type: String): List<NecConduitEntry> // Keep this specific one if needed

    @Query("SELECT * FROM nec_conduit_areas") // Added: Get all conduit entries
    fun getAllNecConduitEntries(): Flow<List<NecConduitEntry>>

    // Search Conduit Table
    @Query("SELECT * FROM nec_conduit_areas WHERE type LIKE '%' || :query || '%' OR size LIKE '%' || :query || '%'")
    fun searchConduit(query: String): Flow<List<NecConduitEntry>>

    // --- Wire Areas (Table 5) ---
     @Query("SELECT * FROM nec_wire_areas WHERE insulation_type = :insulationType AND size = :size")
    suspend fun getWireAreaEntry(insulationType: String, size: String): NecWireAreaEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWireAreaEntries(entries: List<NecWireAreaEntry>)

    @Query("SELECT DISTINCT insulation_type FROM nec_wire_areas WHERE insulation_type IS NOT NULL AND insulation_type != '' ORDER BY insulation_type ASC")
    fun getDistinctWireTypes(): Flow<List<String>> // Changed return type to Flow

    @Query("SELECT DISTINCT size FROM nec_wire_areas WHERE insulation_type = :insulationType ORDER BY size ASC") // Consider custom sorting if needed
    fun getDistinctWireSizesForType(insulationType: String): Flow<List<String>> // Changed return type to Flow

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOneBoxFillEntry(entry: NecBoxFillEntry) // Added for single entry insertion

    @Query("SELECT DISTINCT conductor_size FROM nec_box_fill_allowances WHERE item_type = 'Conductor' ORDER BY conductor_size ASC") // Get sizes relevant for conductors
    suspend fun getDistinctBoxFillConductorSizes(): List<String>

    @Query("SELECT * FROM nec_box_fill_allowances") // Added: Get all box fill entries
    fun getAllNecBoxFillEntries(): Flow<List<NecBoxFillEntry>>

    // Search Box Fill Table
    @Query("SELECT * FROM nec_box_fill_allowances WHERE item_type LIKE '%' || :query || '%' OR conductor_size LIKE '%' || :query || '%'")
    fun searchBoxFill(query: String): Flow<List<NecBoxFillEntry>>

    // --- Motor FLC (Tables 430.248 & 430.250) ---
    @Query("SELECT * FROM nec_motor_flc WHERE hp = :hp AND voltage = :voltage AND phase = :phase LIMIT 1")
    suspend fun getMotorFLCEntry(hp: Double, voltage: Int, phase: String): NecMotorFLCEntry? // Corrected phase type

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotorFLCEntries(entries: List<NecMotorFLCEntry>)

    @Query("SELECT * FROM nec_motor_flc") // Added: Get all motor FLC entries
    fun getAllNecMotorFLCEntries(): Flow<List<NecMotorFLCEntry>>

    // --- Motor Protection (Table 430.52) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotorProtectionPercentageEntries(entries: List<NecMotorProtectionPercentageEntry>)

    @Query("SELECT * FROM nec_motor_protection_percentages WHERE device_type = :deviceType LIMIT 1")
    suspend fun getMotorProtectionPercentageEntry(deviceType: String): NecMotorProtectionPercentageEntry?

    @Query("SELECT * FROM nec_motor_protection_percentages")
    fun getAllNecMotorProtectionPercentageEntries(): Flow<List<NecMotorProtectionPercentageEntry>>

    // --- Motor Protection Fuse Sizes (Table 430.52) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotorProtectionNonTimeDelayFuseSizeEntries(entries: List<NecMotorProtectionNonTimeDelayFuseSizeEntry>) // Corrected model name

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotorProtectionTimeDelayFuseSizeEntries(entries: List<NecMotorProtectionTimeDelayFuseSizeEntry>) // Corrected model name

    @Query("SELECT * FROM nec_motor_protection_nontime_delay_fuse_sizes WHERE hp = :hp AND volts = :volts LIMIT 1") // Corrected table name
    suspend fun getMotorProtectionNonTimeDelayFuseSizeEntry(hp: Double, volts: Int): NecMotorProtectionNonTimeDelayFuseSizeEntry? // Corrected model name

    @Query("SELECT * FROM nec_motor_protection_time_delay_fuse_sizes WHERE hp = :hp AND volts = :volts LIMIT 1") // Corrected table name
    suspend fun getMotorProtectionTimeDelayFuseSizeEntry(hp: Double, volts: Int): NecMotorProtectionTimeDelayFuseSizeEntry? // Corrected model name

    @Query("SELECT * FROM nec_motor_protection_nontime_delay_fuse_sizes")
    fun getAllNecMotorProtectionNonTimeDelayFuseSizeEntries(): Flow<List<NecMotorProtectionNonTimeDelayFuseSizeEntry>>

    @Query("SELECT * FROM nec_motor_protection_time_delay_fuse_sizes")
    fun getAllNecMotorProtectionTimeDelayFuseSizeEntries(): Flow<List<NecMotorProtectionTimeDelayFuseSizeEntry>>


    // --- Conductor Impedance (Table 9) ---
    @Query("SELECT * FROM nec_conductor_impedance WHERE material = :material AND size = :size AND raceway_type = :racewayType LIMIT 1")
    suspend fun getConductorImpedanceEntry(material: String, size: String, racewayType: String): NecConductorImpedanceEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConductorImpedanceEntries(entries: List<NecConductorImpedanceEntry>)

    @Query("SELECT * FROM nec_conductor_impedance") // Added: Get all conductor impedance entries
    fun getAllNecConductorImpedanceEntries(): Flow<List<NecConductorImpedanceEntry>>

    // Removed functions related to NecAcImpedanceEntry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcImpedanceEntries(entries: List<NecAcImpedanceEntry>)

    @Query("SELECT * FROM nec_ac_impedance")
    fun getAllAcImpedanceEntries(): Flow<List<NecAcImpedanceEntry>>

    // --- Full NEC Code Inserts ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNecArticles(articles: List<NecArticle>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNecSections(sections: List<NecSection>)

    // --- Full NEC Code Search (using FTS) ---
    // This query searches the FTS table and joins back to get the full section and article title
    @Query("""
        SELECT s.*, a.title as articleTitle
        FROM nec_sections_fts fts
        JOIN nec_sections s ON fts.rowid = s.rowid
        LEFT JOIN nec_articles a ON s.article_number = a.articleNumber
        WHERE fts.nec_sections_fts MATCH :query
    """)
    fun searchFullCode(query: String): Flow<List<NecSearchResult.FullCodeResult>> // Return the specific result type

    // --- Luminaire Layout - Coefficient of Utilization (CU) Data ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuTables(tables: List<NecCuTable>)

    @Query("SELECT * FROM nec_cu_tables")
    fun getAllCuTables(): Flow<List<NecCuTable>>

    // TODO: Add queries for other NEC data as needed (e.g., demand factors)
}
