package com.example.electricianappnew.data.local

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.electricianappnew.data.local.dao.NecDataDao
import com.example.electricianappnew.data.model.*
import com.example.electricianappnew.data.jsonmodels.* // Import models from the new package
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import javax.inject.Provider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import kotlinx.serialization.json.Json // Import Kotlinx Serialization Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.builtins.ListSerializer // Import ListSerializer for decoding lists
import kotlinx.serialization.serializer // Import serializer for getting serializer for type T

import com.example.electricianappnew.data.repository.NecDataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DatabaseCallback @Inject constructor(
    @ApplicationContext private val context: Context,
    private val databaseProvider: Provider<AppDatabase>,
    private val necDataRepositoryProvider: Provider<NecDataRepository>
) : RoomDatabase.Callback() {

    private val gson = Gson()
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

            // Use Kotlinx Serialization Json instance
            val json = Json { ignoreUnknownKeys = true } // Configure Json for flexibility

            // --- Read and Insert Ampacity Data ---
            readAndInsertData<NecAmpacityEntry>( // Generic over element type
                context,
                json, // Pass Json instance
                "wire_ampacity_data.json", // Updated file name
                "ampacities", // dataDescription
                tag,
                "ampacities" // jsonKey
            ) { dataList -> // Lambda parameter is List<E>
                necDataDao.insertAmpacityEntries(dataList)
            }

            // --- Read and Insert Conductor Properties Data ---
             readAndInsertData<NecConductorEntry>( // Generic over element type
                context,
                json, // Pass Json instance
                "wire_ampacity_data.json", // Updated file name
                "conductor_properties", // dataDescription
                tag,
                "conductor_properties" // jsonKey
            ) { dataList -> // Lambda parameter is List<E>
                necDataDao.insertConductorEntries(dataList)
            }

            // --- Read and Insert Raceway Sizing Data (Conduit Areas) ---
            readAndInsertData<NecConduitEntry>( // Generic over element type
                context,
                json, // Pass Json instance
                "raceway_sizing_data.json", // Updated file name
                "raceway_areas", // dataDescription
                tag,
                "raceway_areas" // jsonKey
            ) { dataList -> // Lambda parameter is List<E>
                necDataDao.insertConduitEntries(dataList)
            }

            // --- Read and Insert Box Fill Data ---
            readAndInsertData<NecBoxFillEntry>( // Generic over element type
                context,
                json, // Pass Json instance
                "box_fill_data.json",
                "box_fill_allowances", // dataDescription
                tag,
                "box_fill_allowances" // jsonKey
            ) { dataList -> // Lambda parameter is List<E>
                 // Filter out invalid entries before inserting
                val validEntries = dataList.filter { entry ->
                    val itemTypeValid = !entry.item_type.isNullOrBlank() && entry.item_type != "null"
                    val conductorSizeValid = !entry.conductorSize.isNullOrBlank() && entry.conductorSize != "null"
                    itemTypeValid && conductorSizeValid
                }
                val filteredCount = dataList.size - validEntries.size
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
            readAndInsertData<NecTempCorrectionEntry>( // Generic over element type
                context,
                json, // Pass Json instance
                "wire_ampacity_data.json",
                "temp_corrections", // dataDescription
                tag,
                "temp_corrections" // jsonKey
            ) { dataList -> // Lambda parameter is List<E>
                necDataDao.insertTempCorrectionEntries(dataList)
            }

            // --- Read and Insert Conductor Adjustment Data ---
            readAndInsertData<NecConductorAdjustmentEntry>( // Generic over element type
                context,
                json, // Pass Json instance
                "wire_ampacity_data.json",
                "conductor_adjustments", // dataDescription
                tag,
                "conductor_adjustments" // jsonKey
            ) { dataList -> // Lambda parameter is List<E>
                necDataDao.insertConductorAdjustmentEntries(dataList)
            }

            // --- Read and Insert Single Phase Motor FLC Data ---
            readAndInsertData<SinglePhaseFLCJson>( // Generic over element type
                context,
                json, // Pass Json instance
                "motor_calculator_data.json",
                "single_phase_flc", // dataDescription
                tag,
                "single_phase_flc" // jsonKey
            ) { dataList -> // Lambda parameter is List<E>
                val flcEntries = dataList.flatMap { entry ->
                    listOf(
                        NecMotorFLCEntry(hp = entry.hp, voltage = 115, phase = "Single", flc = entry.volts115),
                        NecMotorFLCEntry(hp = entry.hp, voltage = 230, phase = "Single", flc = entry.volts230)
                    )
                }
                necDataDao.insertMotorFLCEntries(flcEntries)
            }

            // --- Read and Insert Three Phase Motor FLC Data ---
            readAndInsertData<ThreePhaseFLCJson>( // Generic over element type
                context,
                json, // Pass Json instance
                "motor_calculator_data.json",
                "three_phase_flc", // dataDescription
                tag,
                "three_phase_flc" // jsonKey
            ) { dataList -> // Lambda parameter is List<E>
                val flcEntries = dataList.flatMap { entry ->
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
            readAndInsertData<MotorProtectionPercentageJson>( // Generic over element type
                context,
                json, // Pass Json instance
                "motor_calculator_data.json",
                "motor_protection_percentages", // dataDescription
                tag,
                "motor_protection_percentages" // jsonKey - Corrected key
            ) { dataList -> // Lambda parameter is List<E>
                val percentageEntries = dataList.map { entry ->
                    NecMotorProtectionPercentageEntry(deviceType = entry.type, maxPercentFLC = entry.percentage)
                }
                necDataDao.insertMotorProtectionPercentageEntries(percentageEntries)
            }

            // --- Read and Insert Motor Protection Non-Time Delay Fuse Data ---
            readAndInsertData<NecMotorProtectionNonTimeDelayFuseSizeEntry>( // Generic over element type
                context,
                json, // Pass Json instance
                "motor_calculator_data.json",
                "motor_protection_nontime_delay_fuse", // dataDescription
                tag,
                "motor_protection_nontime_delay_fuse" // jsonKey
            ) { dataList -> // Lambda parameter is List<E>
                necDataDao.insertMotorProtectionNonTimeDelayFuseSizeEntries(dataList)
            }

            // --- Read and Insert Motor Protection Time Delay Fuse Data ---
            readAndInsertData<NecMotorProtectionTimeDelayFuseSizeEntry>( // Generic over element type
                context,
                json, // Pass Json instance
                "motor_calculator_data.json",
                "motor_protection_time_delay_fuse", // dataDescription
                tag,
                "motor_protection_time_delay_fuse" // jsonKey
            ) { dataList -> // Lambda parameter is List<E>
                necDataDao.insertMotorProtectionTimeDelayFuseSizeEntries(dataList)
            }

            // --- Read and Insert Conductor Impedance Data ---
            readAndInsertData<NecConductorImpedanceEntry>( // Generic over element type
                context,
                json, // Pass Json instance
                "voltage_drop_data.json",
                "conductor_impedance", // dataDescription
                tag,
                "conductor_impedance" // jsonKey
            ) { dataList -> // Lambda parameter is List<E>
                necDataDao.insertConductorImpedanceEntries(dataList)
            }

            // --- Read and Insert Wire Area Data (from conduit_fill_data.json) ---
            readAndInsertData<NecWireAreaEntry>( // Generic over element type
                context,
                json, // Pass Json instance
                "conduit_fill_data.json",
                "wire_areas", // dataDescription
                tag,
                "wire_areas" // jsonKey
            ) { dataList -> // Lambda parameter is List<E>
                necDataDao.insertWireAreaEntries(dataList)
            }

            // --- Read and Insert CU Data (from cu_data.json) ---
            // CU data is not a Room Entity, so we don't insert it into the database directly.
            // It's read by the repository as needed.
            // Remove the old CU insertion logic if it existed here.


            Log.d(tag, ">>> DatabaseCallback populateDatabase: All insertions complete.")
            Log.d(tag, ">>> DatabaseCallback populateDatabase: NEC data successfully populated.")
            necDataRepositoryProvider.get().setDataLoaded()

        } catch (e: Exception) {
            Log.e(tag, ">>> DatabaseCallback populateDatabase: !!! UNCAUGHT EXCEPTION during population: ${e.message}", e)
        }
        Log.d(tag, ">>> DatabaseCallback populateDatabase: COROUTINE FINISHED.")
    }

    /**
     * Generic helper function to read and insert a List of data from a JSON asset using Kotlinx Serialization.
     * @param context Application context.
     * @param json Kotlinx Serialization Json instance for parsing.
     * @param fileName The name of the JSON asset file.
     * @param dataDescription A description of the data being inserted (for logging).
     * @param tag Log tag.
     * @param jsonKey Optional key if the data is nested within a JSON object.
     * @param insertAction A lambda function that takes the parsed list and performs the database insertion.
     */
    private suspend inline fun <reified E> readAndInsertData( // Generic over element type E
        context: Context,
        json: Json,
        fileName: String,
        dataDescription: String,
        tag: String,
        jsonKey: String? = null,
        insertAction: (dataList: List<E>) -> Unit // insertAction takes List<E>
    ) {
        Log.d(tag, "Attempting to read and insert $dataDescription from $fileName...")
        try {
            val jsonString = context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    reader.readText()
                }
            }

            val dataList: List<E> = if (jsonKey != null) { // Variable name changed to dataList
                // If jsonKey is provided, parse as JsonObject and extract the array
                val jsonElement = json.parseToJsonElement(jsonString)
                val jsonObject = jsonElement.jsonObject
                val jsonArray = jsonObject.get(jsonKey)?.jsonArray
                    ?: throw IllegalArgumentException("JSON key '$jsonKey' not found or is not an array in $fileName")

                // Correct deserialization for a list from a JsonArray
                json.decodeFromJsonElement(ListSerializer(serializer<E>()), jsonArray)

            } else {
                // If no jsonKey, read directly as a top-level array
                // Correct deserialization for a list from a JSON string
                json.decodeFromString(ListSerializer(serializer<E>()), jsonString)
            }

            if (dataList.isNotEmpty()) {
                Log.d(tag, "Inserting $dataDescription (${dataList.size} entries)...")
                insertAction(dataList) // Pass dataList to insertAction
                Log.d(tag, "$dataDescription Inserted.")
            } else {
                 Log.w(tag, "$dataDescription list from $fileName is empty. Skipping insertion.")
            }
        } catch (e: Exception) {
            Log.e(tag, "FAILED to read or insert $dataDescription from $fileName: ${e.message}", e)
        }
    }
}
