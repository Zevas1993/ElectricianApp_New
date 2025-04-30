package com.example.electricianappnew.data.repository

import android.content.Context
import com.example.electricianappnew.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import kotlin.math.abs // Import abs for finding closest temp
import kotlin.math.min // Import min for termination limit

import com.example.electricianappnew.data.local.dao.NecDataDao
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import java.io.InputStreamReader

// Data class to hold the results of the ampacity calculation
data class AmpacityCalculationResult(
    val baseAmpacity: Double? = null,
    val tempCorrectionFactor: Double? = null,
    val conductorAdjustmentFactor: Double? = null,
    val adjustedAmpacity: Double? = null,
    val errorMessage: String? = null
)

// Interface for accessing NEC-related data
interface NecDataRepository {
    suspend fun getAmpacityEntry(material: String, size: String, tempRating: Int): NecAmpacityEntry?
    fun getDistinctAmpacityWireSizes(): Flow<List<String>>
    fun getDistinctAmpacityTempRatings(): Flow<List<Int>>
    suspend fun getTempCorrectionEntriesForRating(tempRating: Int): List<NecTempCorrectionEntry>
    suspend fun getConductorAdjustmentFactorEntry(numConductors: Int): NecConductorAdjustmentEntry?
    suspend fun getConductorProperties(material: String, size: String): NecConductorEntry?
    suspend fun getConduitEntry(type: String, size: String): NecConduitEntry?
    suspend fun getWireAreaEntry(insulationType: String, size: String): NecWireAreaEntry?
    suspend fun getBoxFillEntry(itemType: String, conductorSize: String): NecBoxFillEntry?
    fun getDistinctConduitTypes(): Flow<List<String>>
    fun getConduitEntriesForType(type: String): Flow<List<NecConduitEntry>>
    fun getDistinctConduitSizesForType(type: String): Flow<List<String>>
    suspend fun getAllConduitEntriesForType(type: String): List<NecConduitEntry>
    fun getDistinctWireTypes(): Flow<List<String>>
    fun getDistinctWireSizesForType(insulationType: String): Flow<List<String>>
    suspend fun getDistinctBoxFillConductorSizes(): List<String>
    suspend fun getMotorFLCEntry(hp: Double, voltage: Int, phase: String): NecMotorFLCEntry?
    suspend fun getMotorProtectionPercentageEntry(deviceType: String): NecMotorProtectionPercentageEntry?
    suspend fun getMotorProtectionNonTimeDelayFuseSizeEntry(hp: Double, volts: Int): NecMotorProtectionNonTimeDelayFuseSizeEntry?
    suspend fun getMotorProtectionTimeDelayFuseSizeEntry(hp: Double, volts: Int): NecMotorProtectionTimeDelayFuseSizeEntry?
    suspend fun getConductorImpedanceEntry(material: String, size: String, racewayType: String): NecConductorImpedanceEntry?

    fun getAllNecAmpacityEntries(): Flow<List<NecAmpacityEntry>>
    fun getAllNecConductorEntries(): Flow<List<NecConductorEntry>>
    fun getAllAcImpedanceEntries(): Flow<List<NecAcImpedanceEntry>>
    fun getAllNecConduitEntries(): Flow<List<NecConduitEntry>>
    fun getAllNecWireAreaEntries(): Flow<List<NecWireAreaEntry>>
    fun getAllNecBoxFillEntries(): Flow<List<NecBoxFillEntry>>
    fun getAllNecTempCorrectionEntries(): Flow<List<NecTempCorrectionEntry>>
    fun getAllNecConductorAdjustmentEntries(): Flow<List<NecConductorAdjustmentEntry>>
    fun getAllNecMotorFLCEntries(): Flow<List<NecMotorFLCEntry>>
    fun getAllNecMotorProtectionPercentageEntries(): Flow<List<NecMotorProtectionPercentageEntry>>
    fun getAllNecMotorProtectionNonTimeDelayFuseSizeEntries(): Flow<List<NecMotorProtectionNonTimeDelayFuseSizeEntry>>
    fun getAllNecMotorProtectionTimeDelayFuseSizeEntries(): Flow<List<NecMotorProtectionTimeDelayFuseSizeEntry>>
    fun getAllNecConductorImpedanceEntries(): Flow<List<NecConductorImpedanceEntry>>
    fun getAllCuTables(): Flow<List<NecCuTable>>

    fun searchNecData(query: String): Flow<List<NecSearchResult>>

    fun getDistinctConductorMaterials(): Flow<List<String>>

    suspend fun calculateAmpacity(
        material: String,
        size: String,
        tempRating: Int,
        ambientTempC: Double,
        numConductors: Int
    ): AmpacityCalculationResult

}

@Singleton
class NecDataRepositoryImpl @Inject constructor(
    private val necDataDao: NecDataDao, // Keep DAO for now, might be used elsewhere
    private val context: Context // Inject Context to access assets
) : NecDataRepository {

    // Private properties to cache data read from JSON assets
    private var _ampacityData: List<NecAmpacityEntry>? = null
    private var _tempCorrectionData: List<NecTempCorrectionEntry>? = null
    private var _conductorAdjustmentData: List<NecConductorAdjustmentEntry>? = null
    private var _conductorPropertiesData: List<NecConductorEntry>? = null
    private var _voltageDropData: List<NecConductorImpedanceEntry>? = null // Assuming this is impedance
    private var _boxFillData: List<NecBoxFillEntry>? = null
    private var _conduitFillData: List<NecWireAreaEntry>? = null // Assuming this is wire area
    private var _racewaySizingData: List<NecConduitEntry>? = null // Assuming this is conduit area
    private var _motorCalculatorData: MotorCalculatorData? = null // Data from motor_calculator_data.json
    private var _cuData: CuData? = null // Data from cu_data.json

    private val gson = Gson()

    // --- Helper functions to read and cache data from JSON assets ---

    private suspend fun getCuData(): CuData? {
        if (_cuData == null) {
            _cuData = readJsonAsset("cu_data.json")
        }
        return _cuData
    }

    private suspend fun getAmpacityData(): List<NecAmpacityEntry> {
        if (_ampacityData == null) {
            _ampacityData = readJsonAsset("wire_ampacity_data.json", "ampacities")
        }
        return _ampacityData ?: emptyList()
    }

    private suspend fun getTempCorrectionData(): List<NecTempCorrectionEntry> {
        if (_tempCorrectionData == null) {
            _tempCorrectionData = readJsonAsset("wire_ampacity_data.json", "temp_corrections")
        }
        return _tempCorrectionData ?: emptyList()
    }

    private suspend fun getConductorAdjustmentData(): List<NecConductorAdjustmentEntry> {
        if (_conductorAdjustmentData == null) {
            _conductorAdjustmentData = readJsonAsset("wire_ampacity_data.json", "conductor_adjustments")
        }
        return _conductorAdjustmentData ?: emptyList()
    }

    private suspend fun getConductorPropertiesData(): List<NecConductorEntry> {
        if (_conductorPropertiesData == null) {
            _conductorPropertiesData = readJsonAsset("conductor_properties.json", "conductor_properties")
        }
        return _conductorPropertiesData ?: emptyList()
    }

    private suspend fun getVoltageDropData(): List<NecConductorImpedanceEntry> {
         if (_voltageDropData == null) {
             _voltageDropData = readJsonAsset("voltage_drop_data.json", "conductor_impedance")
         }
         return _voltageDropData ?: emptyList()
    }

    private suspend fun getBoxFillData(): List<NecBoxFillEntry> {
         if (_boxFillData == null) {
             _boxFillData = readJsonAsset("box_fill_data.json", "box_fill_allowances")
         }
         return _boxFillData ?: emptyList()
    }

    private suspend fun getConduitFillData(): List<NecWireAreaEntry> {
         if (_conduitFillData == null) {
             _conduitFillData = readJsonAsset("conduit_fill_data.json", "wire_areas")
         }
         return _conduitFillData ?: emptyList()
    }

    private suspend fun getRacewaySizingData(): List<NecConduitEntry> {
         if (_racewaySizingData == null) {
             _racewaySizingData = readJsonAsset("raceway_sizing_data.json", "raceway_areas") // Assuming key is "raceway_areas"
         }
         return _racewaySizingData ?: emptyList()
    }

    private suspend fun getMotorCalculatorData(): MotorCalculatorData? {
        if (_motorCalculatorData == null) {
            _motorCalculatorData = readJsonAsset("motor_calculator_data.json")
        }
        return _motorCalculatorData
    }


    /**
     * Generic helper function to read data from a JSON asset.
     * @param fileName The name of the JSON asset file.
     * @param jsonKey Optional key if the data is nested within a JSON object.
     * @return The parsed data as type T, or null if an error occurs.
     */
    private suspend inline fun <reified T> readJsonAsset(
        fileName: String,
        jsonKey: String? = null
    ): T? = withContext(Dispatchers.IO) {
        try {
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    if (jsonKey != null) {
                        val jsonObject: JsonObject = gson.fromJson(reader, JsonObject::class.java)
                        val jsonArray: JsonArray = jsonObject.getAsJsonArray(jsonKey)
                        gson.fromJson(jsonArray, object : TypeToken<T>() {}.type)
                    } else {
                        val dataType = object : TypeToken<T>() {}.type
                        gson.fromJson(reader, dataType)
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading JSON asset $fileName: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON asset $fileName: ${e.message}", e)
            null
        }
    }


    // --- Repository methods updated to use JSON data ---

    override suspend fun getAmpacityEntry(material: String, size: String, tempRating: Int): NecAmpacityEntry? {
        return getAmpacityData().find {
            it.material.equals(material, ignoreCase = true) &&
            it.size.equals(size, ignoreCase = true) &&
            it.tempRating == tempRating
        }
    }

    override suspend fun getConductorProperties(material: String, size: String): NecConductorEntry? {
        return getConductorPropertiesData().find {
            it.material.equals(material, ignoreCase = true) &&
            it.size.equals(size, ignoreCase = true)
        }
    }

    override suspend fun getConduitEntry(type: String, size: String): NecConduitEntry? {
        return getRacewaySizingData().find { // Use raceway data for conduit entries
            it.type.equals(type, ignoreCase = true) &&
            it.size.equals(size, ignoreCase = true)
        }
    }

    override suspend fun getWireAreaEntry(insulationType: String, size: String): NecWireAreaEntry? {
        return getConduitFillData().find { // Use conduit fill data for wire area entries
            it.insulationType.equals(insulationType, ignoreCase = true) &&
            it.size.equals(size, ignoreCase = true)
        }
    }

    override suspend fun getBoxFillEntry(itemType: String, conductorSize: String): NecBoxFillEntry? {
        return getBoxFillData().find {
            it.item_type.equals(itemType, ignoreCase = true) &&
            it.conductorSize.equals(conductorSize, ignoreCase = true)
        }
    }

    override fun getDistinctConduitTypes(): Flow<List<String>> = flow {
        emit(getRacewaySizingData().mapNotNull { it.type }.distinct().sorted())
    }

    override fun getConduitEntriesForType(type: String): Flow<List<NecConduitEntry>> = flow {
        emit(getRacewaySizingData().filter { it.type.equals(type, ignoreCase = true) })
    }

    override fun getDistinctConduitSizesForType(type: String): Flow<List<String>> = flow {
        emit(getRacewaySizingData().filter { it.type.equals(type, ignoreCase = true) }.mapNotNull { it.size }.distinct().sorted())
    }

    override suspend fun getAllConduitEntriesForType(type: String): List<NecConduitEntry> {
        return getRacewaySizingData().filter { it.type.equals(type, ignoreCase = true) }
    }

    override fun getDistinctWireTypes(): Flow<List<String>> = flow {
        emit(getConduitFillData().mapNotNull { it.insulationType }.distinct().sorted())
    }

    override fun getDistinctWireSizesForType(insulationType: String): Flow<List<String>> = flow {
        emit(getConduitFillData().filter { it.insulationType.equals(insulationType, ignoreCase = true) }.mapNotNull { it.size }.distinct().sorted())
    }

    override fun getDistinctAmpacityWireSizes(): Flow<List<String>> = flow {
        emit(getAmpacityData().mapNotNull { it.size }.distinct().sortedWith(compareBy { it.replace(" AWG", "").replace(" kcmil", "").toIntOrNull() ?: Int.MAX_VALUE })) // Basic sorting
    }

    override fun getDistinctAmpacityTempRatings(): Flow<List<Int>> = flow {
        emit(getAmpacityData().mapNotNull { it.tempRating }.distinct().sorted())
    }

    override suspend fun getTempCorrectionEntriesForRating(tempRating: Int): List<NecTempCorrectionEntry> {
        return getTempCorrectionData().filter { it.tempRating == tempRating }.sortedBy { it.ambientTempCelsius }
    }

    override suspend fun getConductorAdjustmentFactorEntry(numConductors: Int): NecConductorAdjustmentEntry? {
        return getConductorAdjustmentData().find {
            numConductors >= it.minConductors && numConductors <= it.maxConductors
        }
    }

    override suspend fun getDistinctBoxFillConductorSizes(): List<String> {
        return getBoxFillData().mapNotNull { it.conductorSize }.distinct().sorted()
    }

    override suspend fun getMotorFLCEntry(hp: Double, voltage: Int, phase: String): NecMotorFLCEntry? {
        val motorData = getMotorCalculatorData() ?: return null
        return when (phase.lowercase()) {
            "single" -> {
                motorData.singlePhaseFlc.find { it.hp == hp }?.let { entry ->
                    when (voltage) {
                        115 -> NecMotorFLCEntry(hp, voltage, phase, entry.volts115)
                        230 -> NecMotorFLCEntry(hp, voltage, phase, entry.volts230)
                        else -> null
                    }
                }
            }
            "three" -> {
                motorData.threePhaseFlc.find { it.hp == hp }?.let { entry ->
                     when (voltage) {
                        200 -> NecMotorFLCEntry(hp, voltage, phase, entry.volts200)
                        208 -> NecMotorFLCEntry(hp, voltage, phase, entry.volts208)
                        230 -> NecMotorFLCEntry(hp, voltage, phase, entry.volts230)
                        460 -> NecMotorFLCEntry(hp, voltage, phase, entry.volts460)
                        575 -> NecMotorFLCEntry(hp, voltage, phase, entry.volts575)
                        else -> null
                    }
                }
            }
            else -> null
        }
    }

    override suspend fun getMotorProtectionPercentageEntry(deviceType: String): NecMotorProtectionPercentageEntry? {
        val motorData = getMotorCalculatorData() ?: return null
        return motorData.motorProtectionPercentages.find { it.type.equals(deviceType, ignoreCase = true) }?.let {
            NecMotorProtectionPercentageEntry(it.type, it.percentage)
        }
    }

    override suspend fun getMotorProtectionNonTimeDelayFuseSizeEntry(hp: Double, volts: Int): NecMotorProtectionNonTimeDelayFuseSizeEntry? {
        val motorData = getMotorCalculatorData() ?: return null
        return motorData.motorProtectionNonTimeDelayFuseSizes.find { it.hp == hp && it.volts == volts }?.let {
            NecMotorProtectionNonTimeDelayFuseSizeEntry(it.hp, it.volts, it.amps, it.maxFuse)
        }
    }

    override suspend fun getMotorProtectionTimeDelayFuseSizeEntry(hp: Double, volts: Int): NecMotorProtectionTimeDelayFuseSizeEntry? {
        val motorData = getMotorCalculatorData() ?: return null
        return motorData.motorProtectionTimeDelayFuseSizes.find { it.hp == hp && it.volts == volts }?.let {
            NecMotorProtectionTimeDelayFuseSizeEntry(it.hp, it.volts, it.amps, it.maxFuse)
        }
    }

    override suspend fun getConductorImpedanceEntry(material: String, size: String, racewayType: String): NecConductorImpedanceEntry? {
        return getVoltageDropData().find { // Use voltage drop data for impedance
            it.material.equals(material, ignoreCase = true) &&
            it.size.equals(size, ignoreCase = true) &&
            it.racewayType.equals(racewayType, ignoreCase = true)
        }
    }

    // Update getAll methods to use the new JSON reading functions
    override fun getAllNecAmpacityEntries(): Flow<List<NecAmpacityEntry>> = flow { emit(getAmpacityData()) }
    override fun getAllNecConductorEntries(): Flow<List<NecConductorEntry>> = flow { emit(getConductorPropertiesData()) }
    override fun getAllAcImpedanceEntries(): Flow<List<NecAcImpedanceEntry>> = flow { emit(emptyList()) } // No direct AC Impedance table in new JSONs?
    override fun getAllNecConduitEntries(): Flow<List<NecConduitEntry>> = flow { emit(getRacewaySizingData()) }
    override fun getAllNecWireAreaEntries(): Flow<List<NecWireAreaEntry>> = flow { emit(getConduitFillData()) }
    override fun getAllNecBoxFillEntries(): Flow<List<NecBoxFillEntry>> = flow { emit(getBoxFillData()) }
    override fun getAllNecTempCorrectionEntries(): Flow<List<NecTempCorrectionEntry>> = flow { emit(getTempCorrectionData()) }
    override fun getAllNecConductorAdjustmentEntries(): Flow<List<NecConductorAdjustmentEntry>> = flow { emit(getConductorAdjustmentData()) }
    override fun getAllNecMotorFLCEntries(): Flow<List<NecMotorFLCEntry>> = flow {
        val motorData = getMotorCalculatorData()
        val flcEntries = mutableListOf<NecMotorFLCEntry>()
        motorData?.singlePhaseFlc?.forEach { entry ->
            flcEntries.add(NecMotorFLCEntry(hp = entry.hp, voltage = 115, phase = "Single", flc = entry.volts115))
            flcEntries.add(NecMotorFLCEntry(hp = entry.hp, voltage = 230, phase = "Single", flc = entry.volts230))
        }
        motorData?.threePhaseFlc?.forEach { entry ->
            flcEntries.add(NecMotorFLCEntry(hp = entry.hp, voltage = 200, phase = "Three", flc = entry.volts200))
            flcEntries.add(NecMotorFLCEntry(hp = entry.hp, voltage = 208, phase = "Three", flc = entry.volts208))
            flcEntries.add(NecMotorFLCEntry(hp = entry.hp, voltage = 230, phase = "Three", flc = entry.volts230))
            flcEntries.add(NecMotorFLCEntry(hp = entry.hp, voltage = 460, phase = "Three", flc = entry.volts460))
            flcEntries.add(NecMotorFLCEntry(hp = entry.hp, voltage = 575, phase = "Three", flc = entry.volts575))
        }
        emit(flcEntries)
    }
    override fun getAllNecMotorProtectionPercentageEntries(): Flow<List<NecMotorProtectionPercentageEntry>> = flow {
        val motorData = getMotorCalculatorData()
        val percentageEntries = motorData?.motorProtectionPercentages?.map { NecMotorProtectionPercentageEntry(it.type, it.percentage) } ?: emptyList()
        emit(percentageEntries)
    }
    override fun getAllNecMotorProtectionNonTimeDelayFuseSizeEntries(): Flow<List<NecMotorProtectionNonTimeDelayFuseSizeEntry>> = flow {
        val motorData = getMotorCalculatorData()
        val fuseEntries = motorData?.motorProtectionNonTimeDelayFuseSizes?.map { NecMotorProtectionNonTimeDelayFuseSizeEntry(it.hp, it.volts, it.amps, it.maxFuse) } ?: emptyList()
        emit(fuseEntries)
    }
    override fun getAllNecMotorProtectionTimeDelayFuseSizeEntries(): Flow<List<NecMotorProtectionTimeDelayFuseSizeEntry>> = flow {
        val motorData = getMotorCalculatorData()
        val fuseEntries = motorData?.motorProtectionTimeDelayFuseSizes?.map { NecMotorProtectionTimeDelayFuseSizeEntry(it.hp, it.volts, it.amps, it.maxFuse) } ?: emptyList()
        emit(fuseEntries)
    }
    override fun getAllNecConductorImpedanceEntries(): Flow<List<NecConductorImpedanceEntry>> = flow { emit(getVoltageDropData()) }

    override fun getAllCuTables(): Flow<List<NecCuTable>> = flow {
        val cuData = getCuData()
        emit(cuData?.cuTables ?: emptyList())
    }

    override fun searchNecData(query: String): Flow<List<NecSearchResult>> {
        // Update search to use data from JSON files
        val ampacityResults: Flow<List<NecSearchResult>> = flow {
            emit(getAmpacityData().filter {
                it.material.contains(query, ignoreCase = true) ||
                it.size.contains(query, ignoreCase = true) ||
                it.tempRating.toString().contains(query, ignoreCase = true) ||
                it.ampacity.toString().contains(query, ignoreCase = true)
            }.map { NecSearchResult.AmpacityResult(it) })
        }
        val conduitResults: Flow<List<NecSearchResult>> = flow {
             emit(getRacewaySizingData().filter { // Use raceway data for conduit search
                 it.type.contains(query, ignoreCase = true) ||
                 it.size.contains(query, ignoreCase = true) ||
                 it.internalAreaSqIn.toString().contains(query, ignoreCase = true) ||
                 it.fillAreaOver2WiresSqIn.toString().contains(query, ignoreCase = true) // Use correct field name
             }.map { NecSearchResult.ConduitResult(it) })
        }
        val boxFillResults: Flow<List<NecSearchResult>> = flow {
             emit(getBoxFillData().filter {
                 it.item_type.contains(query, ignoreCase = true) ||
                 it.conductorSize.contains(query, ignoreCase = true) ||
                 it.volumeAllowanceCuIn.toString().contains(query, ignoreCase = true)
             }.map { NecSearchResult.BoxFillResult(it) })
        }
        val conductorResults: Flow<List<NecSearchResult>> = flow {
             emit(getConductorPropertiesData().filter {
                 it.material.contains(query, ignoreCase = true) ||
                 it.size.contains(query, ignoreCase = true) ||
                 it.resistanceDcOhmsPer1000ft.toString().contains(query, ignoreCase = true) // Use correct field name
             }.map { NecSearchResult.ConductorResult(it) })
        }
        val wireAreaResults: Flow<List<NecSearchResult>> = flow {
             emit(getConduitFillData().filter { // Use conduit fill data for wire area search
                 it.insulationType?.contains(query, ignoreCase = true) == true || // Handle nullable insulationType
                 it.size.contains(query, ignoreCase = true) ||
                 it.areaSqIn.toString().contains(query, ignoreCase = true) // Use correct field name
             }.map { NecSearchResult.WireAreaResult(it) })
        }
        val voltageDropResults: Flow<List<NecSearchResult>> = flow {
             emit(getVoltageDropData().filter { // Use voltage drop data for impedance search
                 it.material.contains(query, ignoreCase = true) ||
                 it.size.contains(query, ignoreCase = true) ||
                 it.racewayType.contains(query, ignoreCase = true) ||
                 it.resistanceAcOhmsPer1000ft.toString().contains(query, ignoreCase = true) ||
                 it.reactanceOhmsPer1000ft.toString().contains(query, ignoreCase = true)
             }.map { NecSearchResult.ConductorImpedanceResult(it) })
        }
        // TODO: Add search results for other new tables if needed for the search screen

        return combine(
            ampacityResults,
            conduitResults,
            boxFillResults,
            conductorResults,
            wireAreaResults,
            voltageDropResults
        ) { results: Array<List<NecSearchResult>> ->
            results.flatMap { it }
        }
    }

    override fun getDistinctConductorMaterials(): Flow<List<String>> = flow {
        emit(getAmpacityData().mapNotNull { it.material }.distinct().sorted())
    }

    // Implementation of the full ampacity calculation logic
    override suspend fun calculateAmpacity(
        material: String,
        size: String,
        tempRating: Int,
        ambientTempC: Double,
        numConductors: Int
    ): AmpacityCalculationResult {
        try {
            // 1. Fetch base ampacity
            val ampacityEntry = getAmpacityEntry(material, size, tempRating)
            val baseAmpacity = ampacityEntry?.ampacity

            if (baseAmpacity == null) {
                return AmpacityCalculationResult(errorMessage = "Base ampacity not found for $material $size @ ${tempRating}°C.")
            }

            // Check if ambient temperature exceeds conductor rating
            if (ambientTempC > tempRating) {
                return AmpacityCalculationResult(
                    baseAmpacity = baseAmpacity,
                    errorMessage = "Ambient temperature (${ambientTempC}°C) exceeds conductor temperature rating (${tempRating}°C)."
                )
            }

            // 2. Get Temperature Correction Factor using the new helper function
            val tempCorrectionFactor = getTempCorrectionFactor(tempRating, ambientTempC)

            if (tempCorrectionFactor == null) {
                val errorMessage = "Could not determine temperature correction factor for ${tempRating}°C rated conductors at ${ambientTempC}°C ambient. Check data source."
                return AmpacityCalculationResult(
                    baseAmpacity = baseAmpacity,
                    errorMessage = errorMessage
                )
            }

            // 3. Get Conductor Adjustment Factor
            val conductorAdjustmentEntry = getConductorAdjustmentFactorEntry(numConductors)
            val conductorAdjustmentFactor = conductorAdjustmentEntry?.adjustmentFactor ?: 1.0 // Default to 1.0 if not found

            // 4. Calculate Adjusted Ampacity (before termination limits)
            val adjustedAmpacityPreTermination = baseAmpacity * tempCorrectionFactor * conductorAdjustmentFactor

            // 5. Apply Termination Temperature Limits (NEC 110.14(C))
            val terminationLimitRating = when {
                isSizeGreaterThan1AWG(size) -> 75
                else -> 60
            }
            val terminationLimitedAmpacityEntry = getAmpacityEntry(material, size, terminationLimitRating)
            val terminationLimitedAmpacity = terminationLimitedAmpacityEntry?.ampacity

            val finalAdjustedAmpacity = if (terminationLimitedAmpacity != null) {
                minOf(adjustedAmpacityPreTermination, terminationLimitedAmpacity)
            } else {
                adjustedAmpacityPreTermination
            }

            return AmpacityCalculationResult(
                baseAmpacity = baseAmpacity,
                tempCorrectionFactor = tempCorrectionFactor,
                conductorAdjustmentFactor = conductorAdjustmentFactor,
                adjustedAmpacity = finalAdjustedAmpacity,
                errorMessage = null
            )

        } catch (e: Exception) {
            return AmpacityCalculationResult(errorMessage = "Calculation error: ${e.message}")
        }
    }

    /**
     * Returns the temperature correction factor for a given conductor tempRating (°C)
     * and ambientTemp (°C).
     * - If no entries found for the rating, returns null.
     * - If ambientTemp is below the lowest table entry, returns the lowest factor.
     * - If above the highest entry, returns the highest factor.
     * - Otherwise, linearly interpolates between the two nearest entries.
     */
    private suspend fun getTempCorrectionFactor(
        tempRating: Int,
        ambientTemp: Double
    ): Double? {
        val entries = getTempCorrectionData().filter { it.tempRating == tempRating }.sortedBy { it.ambientTempCelsius }

        if (entries.isEmpty()) {
            Log.w(TAG, "No temperature correction entries found for temp rating $tempRating°C.")
            return null
        }

        // If ambient temp is below the lowest entry, use the lowest factor
        if (ambientTemp <= entries.first().ambientTempCelsius) {
            return entries.first().correctionFactor
        }

        // If ambient temp is above the highest entry, use the highest factor
        if (ambientTemp >= entries.last().ambientTempCelsius) {
            return entries.last().correctionFactor
        }

        // Find the two nearest entries for interpolation
        val upperEntry = entries.first { it.ambientTempCelsius >= ambientTemp }
        val lowerEntry = entries.last { it.ambientTempCelsius <= ambientTemp }

        // Linear interpolation
        val tempRange = upperEntry.ambientTempCelsius - lowerEntry.ambientTempCelsius
        val factorRange = upperEntry.correctionFactor - lowerEntry.correctionFactor
        val tempDiff = ambientTemp - lowerEntry.ambientTempCelsius

        return if (tempRange == 0.0) {
            lowerEntry.correctionFactor // Should not happen if entries are distinct, but handle division by zero
        } else {
            lowerEntry.correctionFactor + (tempDiff / tempRange) * factorRange
        }
    }


    // Helper function to check wire size (simplified) - Defined at class level
    private fun isSizeGreaterThan1AWG(size: String): Boolean {
        // Improved logic: Handle AWG and kcmil separately
        return when {
            size.contains("kcmil", ignoreCase = true) -> true // All kcmil sizes are > 1 AWG
            size.contains("AWG", ignoreCase = true) -> {
                val awgNumber = size.substringBefore(" AWG", "").trim().toIntOrNull()
                awgNumber != null && awgNumber < 1 // Sizes like 1/0, 2/0 etc. are < 1
            }
            // Assume non-standard formats might be larger if not AWG/kcmil? Safer to default false.
            else -> false // Default case if format is unexpected
        }
    }

    companion object {
        private const val TAG = "NecDataRepositoryImpl"
    }
}
