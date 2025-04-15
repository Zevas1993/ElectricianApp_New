package com.example.electricianappnew.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.electricianappnew.data.local.dao.*
import com.example.electricianappnew.data.model.*

@Database(
    entities = [
        Job::class,
        Task::class,
        PhotoDoc::class,
        Material::class,
        InventoryItem::class,
        InventoryTransaction::class,
        Client::class,
        // NEC Data Entities
        NecAmpacityEntry::class,
        NecConductorEntry::class,
        NecConduitEntry::class,
        NecWireAreaEntry::class,
        NecBoxFillEntry::class,
        // Added for Ampacity Calc
        NecTempCorrectionEntry::class,
        NecConductorAdjustmentEntry::class,
        // Added for Motor Calc
        NecMotorFLCEntry::class,
        NecMotorProtectionEntry::class,
        // Added for Fault Current Calc
        NecConductorImpedanceEntry::class
        // Add other entities here later (e.g., estimates, invoices)
    ],
    version = 2, // Incremented version due to Job entity change
    exportSchema = true
    // TODO: Add autoMigrations later if schema changes (e.g., @AutoMigration(from = 1, to = 2))
)
@TypeConverters(DateConverter::class) // Register the converter
abstract class AppDatabase : RoomDatabase() {

    abstract fun jobTaskDao(): JobTaskDao
    abstract fun photoDocDao(): PhotoDocDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun clientDao(): ClientDao
    abstract fun necDataDao(): NecDataDao // Add NEC Data DAO
    // Add abstract functions for other DAOs here

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time. Needs to be implemented using dependency injection (e.g., Hilt)
        // or a custom singleton pattern. Placeholder for now.
        const val DATABASE_NAME = "electrician_app_database"
    }
}
