package com.example.electricianappnew.data.local

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.electricianappnew.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import javax.inject.Provider // Use Provider for lazy DAO access during callback

// Helper data class to match the structure of nec_data.json
data class NecJsonData(
    val ampacities: List<NecAmpacityEntry>,
    val conductor_properties: List<NecConductorEntry>,
    val conduit_areas: List<NecConduitEntry>,
    val wire_areas: List<NecWireAreaEntry>,
    val box_fill_allowances: List<NecBoxFillEntry>
)

class DatabaseCallback(
    private val context: Context,
    // Use Provider to avoid direct dependency cycle during Hilt setup
    private val databaseProvider: Provider<AppDatabase>
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Populate the database in a coroutine scope when it's first created
        CoroutineScope(Dispatchers.IO).launch {
            populateDatabase()
        }
    }

    private suspend fun populateDatabase() {
        try {
            val necDataDao = databaseProvider.get().necDataDao() // Get DAO instance via provider

            // Read and parse the JSON file from assets
            context.assets.open("nec_data.json").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val necDataType = object : TypeToken<NecJsonData>() {}.type
                    val necData: NecJsonData = Gson().fromJson(reader, necDataType)

                    // Insert data into respective tables
                    necDataDao.insertAmpacityEntries(necData.ampacities)
                    necDataDao.insertConductorEntries(necData.conductor_properties)
                    necDataDao.insertConduitEntries(necData.conduit_areas)
                    necDataDao.insertWireAreaEntries(necData.wire_areas)
                    necDataDao.insertBoxFillEntries(necData.box_fill_allowances)

                    // Log success or handle completion if needed
                     println("NEC data successfully populated into Room database.")
                }
            }
        } catch (e: Exception) {
            // Log error or handle exception appropriately
            println("Error populating database from JSON: ${e.message}")
            e.printStackTrace()
        }
    }
}
