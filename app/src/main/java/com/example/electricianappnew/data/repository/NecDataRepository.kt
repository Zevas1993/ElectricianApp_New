package com.example.electricianappnew.data.repository

import com.example.electricianappnew.data.local.dao.NecDataDao // Keep only one import
import com.example.electricianappnew.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map // Ensure correct map import
import kotlinx.coroutines.flow.combine // Ensure correct combine import
import javax.inject.Inject
import javax.inject.Singleton

// Interface for accessing NEC-related data
interface NecDataRepository {
    suspend fun getAmpacityEntry(material: String, size: String, tempRating: Int): NecAmpacityEntry?
    suspend fun getDistinctAmpacityWireSizes(): List<String> // Added
    suspend fun getDistinctAmpacityTempRatings(): List<Int> // Added
    suspend fun getTempCorrectionFactorEntry(tempRating: Int, ambientTempC: Double): NecTempCorrectionEntry? // Added
    suspend fun getConductorAdjustmentFactorEntry(numConductors: Int): NecConductorAdjustmentEntry? // Added
    suspend fun getConductorProperties(material: String, size: String): NecConductorEntry?
    suspend fun getConduitEntry(type: String, size: String): NecConduitEntry?
    suspend fun getWireAreaEntry(insulationType: String, size: String): NecWireAreaEntry?
    suspend fun getBoxFillEntry(itemType: String, conductorSize: String): NecBoxFillEntry?
    // Add functions for dropdowns
    suspend fun getDistinctConduitTypes(): List<String>
    suspend fun getDistinctConduitSizesForType(type: String): List<String>
    suspend fun getAllConduitEntriesForType(type: String): List<NecConduitEntry> // Added
    suspend fun getDistinctWireTypes(): List<String>
    suspend fun getDistinctWireSizesForType(insulationType: String): List<String>
    suspend fun getDistinctBoxFillConductorSizes(): List<String> // Added
    // Motor Calcs
    suspend fun getMotorFLCEntry(hp: Double, voltage: Int, phase: Int): NecMotorFLCEntry? // Added
    suspend fun getMotorProtectionEntry(motorType: String, deviceType: String): NecMotorProtectionEntry? // Added
    // Fault Current Calcs
    suspend fun getConductorImpedanceEntry(material: String, size: String, racewayType: String): NecConductorImpedanceEntry? // Added

    // Add functions to get all entries for tables (returning Flow) - Ensure these match DAO
    fun getAllNecAmpacityEntries(): Flow<List<NecAmpacityEntry>>
    fun getAllNecConductorEntries(): Flow<List<NecConductorEntry>>
    fun getAllNecConduitEntries(): Flow<List<NecConduitEntry>>
    fun getAllNecWireAreaEntries(): Flow<List<NecWireAreaEntry>>
    fun getAllNecBoxFillEntries(): Flow<List<NecBoxFillEntry>>
    fun getAllNecTempCorrectionEntries(): Flow<List<NecTempCorrectionEntry>>
    fun getAllNecConductorAdjustmentEntries(): Flow<List<NecConductorAdjustmentEntry>>
    fun getAllNecMotorFLCEntries(): Flow<List<NecMotorFLCEntry>>
    fun getAllNecMotorProtectionEntries(): Flow<List<NecMotorProtectionEntry>>
    fun getAllNecConductorImpedanceEntries(): Flow<List<NecConductorImpedanceEntry>>
    // Removed getAllRaceways and getAllWires as models/DAO methods don't exist

    // --- NEC Search ---
    fun searchNecData(query: String): Flow<List<NecSearchResult>> // Added for search

    // Add functions for other NEC data lookups as needed
}

@Singleton
class NecDataRepositoryImpl @Inject constructor(
    private val necDataDao: NecDataDao
) : NecDataRepository {

    override suspend fun getAmpacityEntry(material: String, size: String, tempRating: Int): NecAmpacityEntry? {
        return necDataDao.getAmpacityEntry(material, size, tempRating)
    }

    override suspend fun getConductorProperties(material: String, size: String): NecConductorEntry? {
        return necDataDao.getConductorProperties(material, size)
    }

    override suspend fun getConduitEntry(type: String, size: String): NecConduitEntry? {
        return necDataDao.getConduitEntry(type, size)
    }

    override suspend fun getWireAreaEntry(insulationType: String, size: String): NecWireAreaEntry? {
        return necDataDao.getWireAreaEntry(insulationType, size)
    }

    override suspend fun getBoxFillEntry(itemType: String, conductorSize: String): NecBoxFillEntry? {
        // Special handling for "N/A" conductor size for items based on largest conductor might be needed here
        // or in the ViewModel that calls this. For now, direct lookup.
        return necDataDao.getBoxFillEntry(itemType, conductorSize)
    }

    // Implement new methods
    override suspend fun getDistinctConduitTypes(): List<String> {
        return necDataDao.getDistinctConduitTypes()
    }

    override suspend fun getDistinctConduitSizesForType(type: String): List<String> {
        return necDataDao.getDistinctConduitSizesForType(type)
    }

    override suspend fun getDistinctWireTypes(): List<String> {
        return necDataDao.getDistinctWireTypes()
    }

    override suspend fun getDistinctWireSizesForType(insulationType: String): List<String> {
        return necDataDao.getDistinctWireSizesForType(insulationType)
    }

    override suspend fun getDistinctAmpacityWireSizes(): List<String> {
        return necDataDao.getDistinctAmpacityWireSizes()
    }

    override suspend fun getDistinctAmpacityTempRatings(): List<Int> {
        return necDataDao.getDistinctAmpacityTempRatings()
    }

     override suspend fun getTempCorrectionFactorEntry(tempRating: Int, ambientTempC: Double): NecTempCorrectionEntry? {
        return necDataDao.getTempCorrectionFactorEntry(tempRating, ambientTempC)
    }

    override suspend fun getConductorAdjustmentFactorEntry(numConductors: Int): NecConductorAdjustmentEntry? {
        return necDataDao.getConductorAdjustmentFactorEntry(numConductors)
    }

    override suspend fun getDistinctBoxFillConductorSizes(): List<String> {
        return necDataDao.getDistinctBoxFillConductorSizes()
    }

    override suspend fun getAllConduitEntriesForType(type: String): List<NecConduitEntry> {
        return necDataDao.getAllConduitEntriesForType(type)
    }

    override suspend fun getMotorFLCEntry(hp: Double, voltage: Int, phase: Int): NecMotorFLCEntry? {
        return necDataDao.getMotorFLCEntry(hp, voltage, phase)
    }

    override suspend fun getMotorProtectionEntry(motorType: String, deviceType: String): NecMotorProtectionEntry? {
        return necDataDao.getMotorProtectionEntry(motorType, deviceType)
    }

    override suspend fun getConductorImpedanceEntry(material: String, size: String, racewayType: String): NecConductorImpedanceEntry? {
        return necDataDao.getConductorImpedanceEntry(material, size, racewayType)
    }

    // Implement new Flow-based methods - Ensure these match DAO methods
    override fun getAllNecAmpacityEntries(): Flow<List<NecAmpacityEntry>> = necDataDao.getAllNecAmpacityEntries()
    override fun getAllNecConductorEntries(): Flow<List<NecConductorEntry>> = necDataDao.getAllNecConductorEntries()
    override fun getAllNecConduitEntries(): Flow<List<NecConduitEntry>> = necDataDao.getAllNecConduitEntries()
    override fun getAllNecWireAreaEntries(): Flow<List<NecWireAreaEntry>> = necDataDao.getAllNecWireAreaEntries()
    override fun getAllNecBoxFillEntries(): Flow<List<NecBoxFillEntry>> = necDataDao.getAllNecBoxFillEntries()
    override fun getAllNecTempCorrectionEntries(): Flow<List<NecTempCorrectionEntry>> = necDataDao.getAllNecTempCorrectionEntries()
    override fun getAllNecConductorAdjustmentEntries(): Flow<List<NecConductorAdjustmentEntry>> = necDataDao.getAllNecConductorAdjustmentEntries()
    override fun getAllNecMotorFLCEntries(): Flow<List<NecMotorFLCEntry>> = necDataDao.getAllNecMotorFLCEntries()
    override fun getAllNecMotorProtectionEntries(): Flow<List<NecMotorProtectionEntry>> = necDataDao.getAllNecMotorProtectionEntries()
    override fun getAllNecConductorImpedanceEntries(): Flow<List<NecConductorImpedanceEntry>> = necDataDao.getAllNecConductorImpedanceEntries()
    // Removed implementations for getAllRaceways and getAllWires

    // --- NEC Search Implementation (Incremental) ---
    override fun searchNecData(query: String): Flow<List<NecSearchResult>> {
        val ampacityResults: Flow<List<NecSearchResult>> = necDataDao.searchAmpacity(query).map { list ->
            list.map { NecSearchResult.AmpacityResult(it) }
        }
        val conduitResults: Flow<List<NecSearchResult>> = necDataDao.searchConduit(query).map { list ->
            list.map { NecSearchResult.ConduitResult(it) }
        }
        val boxFillResults: Flow<List<NecSearchResult>> = necDataDao.searchBoxFill(query).map { list ->
            list.map { NecSearchResult.BoxFillResult(it) }
        }
        val conductorResults: Flow<List<NecSearchResult>> = necDataDao.searchConductorProperties(query).map { list -> // Corrected variable name
            list.map { NecSearchResult.ConductorResult(it) }
        }
        val wireAreaResults: Flow<List<NecSearchResult>> = necDataDao.searchWireArea(query).map { list -> // Corrected variable name
            list.map { NecSearchResult.WireAreaResult(it) }
        }
        // Removed tempCorrectionResults flow

        // Combine flows from different tables
        return combine(
            ampacityResults,
            conduitResults,
            boxFillResults,
            conductorResults,
            wireAreaResults
            // Removed tempCorrectionResults from combine parameters
        ) { ampacity, conduit, boxFill, conductor, wireArea ->
            // Combine results by concatenating the lists from each flow
            ampacity + conduit + boxFill + conductor + wireArea
            // TODO: Add flows from other search DAO methods and combine them here
        }
    }
}
