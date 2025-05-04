package com.example.electricianappnew.data.local

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.electricianappnew.data.local.dao.NecDataDao
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import com.example.electricianappnew.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import javax.inject.Provider

// Removed NecJsonData helper class as we are now reading from separate files

import com.example.electricianappnew.data.repository.NecDataRepository

class DatabaseCallback(
    private val context: Context,
    private val databaseProvider: Provider<AppDatabase>,
    private val necDataRepositoryProvider: Provider<NecDataRepository>
) : RoomDatabase.Callback() {

    private val tag = "AppStartup"

    override fun onCreate(db: SupportSQLiteDatabase) {
        Log.d(tag, "DatabaseCallback onCreate: Entered.")
        try {
            Log.d(tag, "DatabaseCallback onCreate: Calling super.onCreate...")
            super.onCreate(db)
            Log.d(tag, "DatabaseCallback onCreate: super.onCreate finished.")

            Log.d(tag, "DatabaseCallback onCreate: Launching coroutine for populateDatabase...")
            CoroutineScope(Dispatchers.IO).launch {
                Log.d(tag, "DatabaseCallback onCreate: Coroutine started.")
                populateDatabase()
                Log.d(tag, "DatabaseCallback onCreate: Coroutine finished.")
            }
            Log.d(tag, "DatabaseCallback onCreate: Coroutine launched.")
        } catch (e: Exception) {
            Log.e(tag, "DatabaseCallback onCreate: EXCEPTION during onCreate: ${e.message}", e)
        }
        Log.d(tag, "DatabaseCallback onCreate: Exiting.")
    }

    private suspend fun populateDatabase() {
        Log.d(tag, ">>> DatabaseCallback populateDatabase: COROUTINE ENTERED.")
        try {
            val db: AppDatabase = try {
                 databaseProvider.get().also {
                     Log.d(tag, ">>> DatabaseCallback populateDatabase: Got DB instance: $it")
                 }
            } catch (e: Exception) {
                Log.e(tag, ">>> DatabaseCallback populateDatabase: FAILED to get DB instance", e)
                return
            }

            val necDataDao: NecDataDao = try {
                db.necDataDao().also {
                    Log.d(tag, ">>> DatabaseCallback populateDatabase: Got NecDataDao: $it")
                }
            } catch (e: Exception) {
                Log.e(tag, ">>> DatabaseCallback populateDatabase: FAILED to get NecDataDao", e)
                return
            }

            val gson = Gson()

            // --- Read and Insert Ampacity Data ---
            readAndInsertData<List<NecAmpacityEntry>>(
                context,
                gson,
                "wire_ampacity_data.json", // Updated file name
                "ampacities", // dataDescription
                tag,
                "ampacities" // jsonKey
            ) { data ->
                necDataDao.insertAmpacityEntries(data)
            }

            // --- Read and Insert Conductor Properties Data ---
             readAndInsertData<List<NecConductorEntry>>(
                context,
                gson,
                "wire_ampacity_data.json", // Updated file name
                "conductor_properties", // dataDescription
                tag,
                "conductor_properties" // jsonKey
            ) { data ->
                necDataDao.insertConductorEntries(data)
            }

            // --- Read and Insert Raceway Sizing Data (Conduit Areas) ---
            readAndInsertData<List<NecConduitEntry>>(
                context,
                gson,
                "raceway_sizing_data.json", // Updated file name
                "raceway_areas", // dataDescription
                tag,
                "raceway_areas" // jsonKey
            ) { data ->
                necDataDao.insertConduitEntries(data)
            }

            // --- Read and Insert Box Fill Data ---
            readAndInsertData<List<NecBoxFillEntry>>(
                context,
                gson,
                "box_fill_data.json",
                "box_fill_allowances", // dataDescription
                tag,
                "box_fill_allowances" // jsonKey
            ) { data ->
                 // Filter out invalid entries before inserting
                val validEntries = data.filter { entry ->
                    val itemTypeValid = !entry.item_type.isNullOrBlank() && entry.item_type != "null"
                    val conductorSizeValid = !entry.conductorSize.isNullOrBlank() && entry.conductorSize != "null"
                    itemTypeValid && conductorSizeValid
                }
                val filteredCount = data.size - validEntries.size
                if (filteredCount > 0) {
                    Log.w(tag, "Filtered out $filteredCount invalid Box Fill entries (null/blank/\"null\" item_type or conductor_size).")
                }
                validEntries.forEach { entry ->
                    try {
                        necDataDao.insertOneBoxFillEntry(entry) // Insert one by one to catch specific bad entries
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to insert BoxFill entry: item_type='${entry.item_type}', conductor_size='${entry.conductorSize}'. Error: ${e.message}", e)
                    }
                }
            }

            // --- Read and Insert Temperature Correction Data ---
            readAndInsertData<List<NecTempCorrectionEntry>>(
                context,
                gson,
                "wire_ampacity_data.json",
                "temp_corrections", // dataDescription
                tag,
                "temp_corrections" // jsonKey
            ) { data ->
                necDataDao.insertTempCorrectionEntries(data)
            }

            // --- Read and Insert Conductor Adjustment Data ---
            readAndInsertData<List<NecConductorAdjustmentEntry>>(
                context,
                gson,
                "wire_ampacity_data.json",
                "conductor_adjustments", // dataDescription
                tag,
                "conductor_adjustments" // jsonKey
            ) { data ->
                necDataDao.insertConductorAdjustmentEntries(data)
            }

            // --- Read and Insert Single Phase Motor FLC Data ---
            readAndInsertData<List<SinglePhaseFLCJson>>(
                context,
                gson,
                "motor_calculator_data.json",
                "single_phase_flc", // dataDescription
                tag,
                "single_phase_flc" // jsonKey
            ) { data ->
                val flcEntries = data.flatMap { entry ->
                    listOf(
                        NecMotorFLCEntry(hp = entry.hp, voltage = 115, phase = "Single", flc = entry.volts115),
                        NecMotorFLCEntry(hp = entry.hp, voltage = 230, phase = "Single", flc = entry.volts230)
                    )
                }
                necDataDao.insertMotorFLCEntries(flcEntries)
            }

            // --- Read and Insert Three Phase Motor FLC Data ---
            readAndInsertData<List<ThreePhaseFLCJson>>(
                context,
                gson,
                "motor_calculator_data.json",
                "three_phase_flc", // dataDescription
                tag,
                "three_phase_flc" // jsonKey
            ) { data ->
                val flcEntries = data.flatMap { entry ->
                    listOf(
                        NecMotorFLCEntry(hp = entry.hp, voltage = 200, phase = "Three", flc = entry.volts200),
                        NecMotorFLCEntry(hp = entry.hp, voltage = 208, phase = "Three", flc = entry.volts208),
                        NecMotorFLCEntry(hp = entry.hp, voltage = 230, phase = "Three", flc = entry.volts230),
                        NecMotorFLCEntry(hp = entry.hp, voltage = 460, phase = "Three", flc = entry.volts460),
                        NecMotorFLCEntry(hp = entry.hp, voltage = 575, phase = "Three", flc = entry.volts575)
                    )
                }
                necDataDao.insertMotorFLCEntries(flcEntries)
            }

            // --- Read and Insert Motor Protection Percentage Data ---
            readAndInsertData<List<MotorProtectionPercentageJson>>(
                context,
                gson,
                "motor_calculator_data.json",
                "motor_protection_percentages", // dataDescription
                tag,
                "motor_protection_percentages" // jsonKey - Corrected key
            ) { data ->
                val percentageEntries = data.map { entry ->
                    NecMotorProtectionPercentageEntry(deviceType = entry.type, maxPercentFLC = entry.percentage)
                }
                necDataDao.insertMotorProtectionPercentageEntries(percentageEntries)
            }

            // --- Read and Insert Motor Protection Non-Time Delay Fuse Data ---
            readAndInsertData<List<NecMotorProtectionNonTimeDelayFuseSizeEntry>>(
                context,
                gson,
                "motor_calculator_data.json",
                "motor_protection_nontime_delay_fuse", // dataDescription
                tag,
                "motor_protection_nontime_delay_fuse" // jsonKey
            ) { data ->
                necDataDao.insertMotorProtectionNonTimeDelayFuseSizeEntries(data)
            }

            // --- Read and Insert Motor Protection Time Delay Fuse Data ---
            readAndInsertData<List<NecMotorProtectionTimeDelayFuseSizeEntry>>(
                context,
                gson,
                "motor_calculator_data.json",
                "motor_protection_time_delay_fuse", // dataDescription
                tag,
                "motor_protection_time_delay_fuse" // jsonKey
            ) { data ->
                necDataDao.insertMotorProtectionTimeDelayFuseSizeEntries(data)
            }

            // --- Read and Insert Conductor Impedance Data ---
            readAndInsertData<List<NecConductorImpedanceEntry>>(
                context,
                gson,
                "voltage_drop_data.json",
                "conductor_impedance", // dataDescription
                tag,
                "conductor_impedance" // jsonKey
            ) { data ->
                necDataDao.insertConductorImpedanceEntries(data)
            }

            // --- Read and Insert Wire Area Data (from conduit_fill_data.json) ---
            readAndInsertData<List<NecWireAreaEntry>>(
                context,
                gson,
                "conduit_fill_data.json",
                "wire_areas", // dataDescription
                tag,
                "wire_areas" // jsonKey
            ) { data ->
                necDataDao.insertWireAreaEntries(data)
            }


            Log.d(tag, ">>> DatabaseCallback populateDatabase: All insertions complete.")
            Log.d(tag, ">>> DatabaseCallback populateDatabase: NEC data successfully populated.")
            necDataRepositoryProvider.get().setDataLoaded()

        } catch (e: Exception) {
            Log.e(tag, ">>> DatabaseCallback populateDatabase: !!! UNCAUGHT EXCEPTION during population: ${e.message}", e)
        }
        Log.d(tag, ">>> DatabaseCallback populateDatabase: COROUTINE FINISHED.")
    }

    /**
     * Generic helper function to read and insert data from a JSON asset.
     * @param context Application context.
     * @param gson Gson instance for parsing.
     * @param fileName The name of the JSON asset file.
     * @param dataDescription A description of the data being inserted (for logging).
     * @param tag Log tag.
     * @param jsonKey Optional key if the data is nested within a JSON object.
     * @param insertAction A lambda function that takes the parsed list and performs the database insertion.
     */
    private suspend inline fun <reified T> readAndInsertData(
        context: Context,
        gson: Gson,
        fileName: String,
        dataDescription: String,
        tag: String,
        jsonKey: String? = null, // Add optional jsonKey parameter
        insertAction: (data: T) -> Unit // Keep insertAction as the last parameter
    ) {
        Log.d(tag, "Attempting to read and insert $dataDescription from $fileName...")
        try {
            val data: T = if (jsonKey != null) {
                // If jsonKey is provided, read as JsonObject and extract the array
                context.assets.open(fileName).use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        val jsonObject: JsonObject = gson.fromJson(reader, JsonObject::class.java)
                        val jsonArray: JsonArray = jsonObject.getAsJsonArray(jsonKey)
                        gson.fromJson(jsonArray, object : TypeToken<T>() {}.type)
                    }
                }
            } else {
                // If no jsonKey, read directly as the target type (assuming it's a top-level array)
                context.assets.open(fileName).use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        val dataType = object : TypeToken<T>() {}.type
                        gson.fromJson(reader, dataType)
                    }
                }
            }

            if (data is List<*> && data.isNotEmpty()) {
                Log.d(tag, "Inserting $dataDescription (${data.size} entries)...")
                insertAction(data)
                Log.d(tag, "$dataDescription Inserted.")
            } else if (data is List<*> && data.isEmpty()) {
                 Log.w(tag, "$dataDescription list from $fileName is empty. Skipping insertion.")
            } else {
                 Log.w(tag, "$dataDescription data from $fileName is not a List or is null. Skipping insertion.")
            }
        } catch (e: Exception) {
            Log.e(tag, "FAILED to read or insert $dataDescription from $fileName: ${e.message}", e)
        }
    }
}
