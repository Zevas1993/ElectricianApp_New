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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val isDataLoaded: StateFlow<Boolean>
    fun setDataLoaded() // Add setDataLoaded to the interface
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

    private val _isDataLoaded = MutableStateFlow(false)
    override val isDataLoaded: StateFlow<Boolean> = _isDataLoaded.asStateFlow()

    override fun setDataLoaded() {
        _isDataLoaded.value = true
    }

    init {
        // Proactively load conduit fill data when the repository is created
        // This ensures the data is available when the ViewModel requests it.
        // We still wait for the main data loaded flag in case other data is needed.
        // Note: This requires a CoroutineScope, which is not directly available in a Singleton.
        // We might need to inject a CoroutineScope or use a global scope if appropriate.
        // For now, let's assume a suitable scope is available or we'll address this next.
        // Let's add a placeholder for now and refine if needed.
        // TODO: Inject or provide a suitable CoroutineScope for init block async operations.
        // For now, we'll rely on the first access to trigger loading via the suspend functions.
        // The ViewModel's init block waiting for isDataLoaded should be sufficient if the
        // suspend functions are called within the Flow chain.

        // Re-evaluating: The Flow chains in the ViewModel *do* call the suspend functions
        // which trigger the loading. The issue might be that the ViewModel's init block
        // tries to access .value on the StateFlows *before* the Flow chain has emitted
        // the first value after the data is loaded.

        // Let's try adding a launch block here to ensure the data is loaded early.
        // We'll need a CoroutineScope. Let's assume we can get one via injection or context.
        // For now, let's add the loading call and we'll address the scope if there's a build error.
        // We can use a simple GlobalScope.launch for now, but injecting is better practice.
        // Let's add the loading call within a placeholder launch block.

        // Update: Let's not add a launch block here yet. The current pattern of
        // suspend functions reading data on first access within the Flow chain
        // should work *if* the ViewModel correctly collects from the Flow.
        // The ViewModel's init block *is* waiting for isDataLoaded, but then
        // it accesses `.value` which might still be the `initialValue = emptyList()`.

        // The fix might be in the ViewModel's init block: instead of accessing `.value`
        // directly, it should collect the first non-empty list from `wireTypeNames`
        // and `availableConductorSizesForEntry(0)` before creating the initial WireEntry.

        // Let's go back to the ViewModel after confirming the repository's loading logic is sound.
        // The repository's suspend functions *do* load and cache the data on first call.
        // The Flow functions like `getDistinctWireTypes()` call these suspend functions.
        // The ViewModel collects from these Flows. The issue is likely the timing in the ViewModel's init.

        // Let's confirm the repository's loading logic is called. We can add a log in getConduitFillData.
        // (Already added in previous step).

        // Okay, the repository seems to be set up to load data on demand via suspend functions
        // called within the Flow builders. The problem is likely in the ViewModel's init block
        // accessing the StateFlows' initial empty values.

        // Let's proceed to examine the ViewModel's init block again with this understanding.
        // No changes needed in the repository for now based on this analysis.
    }

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
            Log.d(TAG, "Loaded temp correction data: ${_tempCorrectionData?.size} entries")
            _tempCorrectionData?.forEach { entry ->
                Log.d(TAG, "getTempCorrectionData: Loaded entry tempRating: ${entry.tempRating}")
            }
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
            _conductorPropertiesData = readJsonAsset("wire_ampacity_data.json", "conductor_properties")
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
                        Log.d(TAG, "readJsonAsset: Extracted JsonArray for key '$jsonKey' from $fileName: ${jsonArray.size()} elements")
                        // Log the first few elements to inspect
                        jsonArray.take(5).forEachIndexed { index, jsonElement ->
                            Log.d(TAG, "readJsonAsset: Element $index: ${jsonElement.toString()}")
                        }
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
        Log.d(TAG, "getTempCorrectionFactor: tempRating=$tempRating, ambientTemp=$ambientTemp")
        val allEntries = getTempCorrectionData()
        Log.d(TAG, "getTempCorrectionFactor: All tempRatings loaded: ${allEntries.map { it.tempRating }}")
        val entries = allEntries.filter { it.tempRating == tempRating }.sortedBy { it.ambientTempCelsius }

        Log.d(TAG, "getTempCorrectionFactor: Filtering for $tempRating yielded: ${entries.size} entries")
        entries.forEach { entry ->
            Log.d(TAG, " • rating ${entry.tempRating}°C: ambient=${entry.ambientTempCelsius} factor=${entry.correctionFactor}")
        }

        if (entries.isEmpty()) {
            Log.w(TAG, "No temperature correction entries found for temp rating $tempRating°C. Entries list is empty.")
            return null
        }

        // Sort by ambientTemperature ascending
        val sorted = entries.sortedBy { it.ambientTempCelsius }

        // If below the lowest, return the factor at the lowest point (or 1.0 to imply no derating)
        if (ambientTemp <= sorted.first().ambientTempCelsius) {
            return sorted.first().correctionFactor.takeIf { it > 0 } ?: 1.0
        }
        // If above the highest, return the highest available factor (or null/error)
        if (ambientTemp >= sorted.last().ambientTempCelsius) {
            return sorted.last().correctionFactor.takeIf { it > 0 } ?: null
        }

        // Otherwise, find the two bracket values and linearly interpolate or just pick
        val lower = sorted.last { it.ambientTempCelsius <= ambientTemp }
        val upper = sorted.first { it.ambientTempCelsius >= ambientTemp }
        return if (lower == upper) {
            lower.correctionFactor
        } else {
            // simple linear interpolation
            val range = upper.ambientTempCelsius - lower.ambientTempCelsius
            val weight = (ambientTemp - lower.ambientTempCelsius) / range
            lower.correctionFactor + weight * (upper.correctionFactor - lower.correctionFactor)
        }
    }

    /**
     * Returns the conductor adjustment factor for a given number of current-carrying conductors.
     * Returns 1.0 if the number of conductors is 3 or less, or if no matching entry is found.
     */
    private fun getConductorAdjustmentFactor(numConductors: Int): Double {
        return when {
            numConductors <= 3 -> 1.0
            numConductors in 4..6 -> 0.8
            numConductors in 7..9 -> 0.7
            numConductors >= 10 -> 0.5
            else -> 1.0
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
        Log.d(TAG, "getDistinctWireTypes: Called")
        val wireTypes = getConduitFillData().mapNotNull { it.insulationType }.distinct().sorted()
        Log.d(TAG, "getDistinctWireTypes: Emitting wire types: $wireTypes")
        emit(wireTypes)
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
        return getBoxFillData()
            .filter { it.item_type == "Conductor" } // Filter for conductor entries
            .mapNotNull { it.conductorSize }
            .distinct()
            .sorted()
            .also { Log.d(TAG, "getDistinctBoxFillConductorSizes: Returning sizes: $it") } // Add logging here
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

            // 2. Get Temperature Correction Factor
            val tempCorrectionFactor = getTempCorrectionFactor(tempRating, ambientTempC)

            if (tempCorrectionFactor == null) {
                val errorMessage = "Could not determine temperature correction factor for ${tempRating}°C rated conductors at ${ambientTempC}°C ambient. Check data source."
                return AmpacityCalculationResult(
                    baseAmpacity = baseAmpacity,
                    errorMessage = errorMessage
                )
            }

            // 3. Get Conductor Adjustment Factor
            val conductorAdjustmentFactor = getConductorAdjustmentFactor(numConductors)

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

    companion object {
        private const val TAG = "NecDataRepositoryImpl"
    }
}
