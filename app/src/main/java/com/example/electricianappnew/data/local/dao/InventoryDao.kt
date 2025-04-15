package com.example.electricianappnew.data.local.dao

import androidx.room.*
import androidx.room.*
import com.example.electricianappnew.data.model.InventoryItem
import com.example.electricianappnew.data.model.InventoryTransaction
import com.example.electricianappnew.data.model.Material
import com.example.electricianappnew.data.model.InventoryItemWithMaterial // Added missing import
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // --- Material Operations ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: Material)

    @Update
    suspend fun updateMaterial(material: Material)

    @Delete
    suspend fun deleteMaterial(material: Material) // Note: Cascade will delete InventoryItem

    @Query("SELECT * FROM materials ORDER BY name ASC")
    fun getAllMaterials(): Flow<List<Material>>

    @Query("SELECT * FROM materials WHERE id = :materialId")
    fun getMaterialById(materialId: String): Flow<Material?>

    // --- Inventory Item Operations ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem)

    @Update
    suspend fun updateInventoryItem(item: InventoryItem)

    // Deletion usually happens via Material cascade

    @Query("SELECT * FROM inventory_items ORDER BY location ASC") // Or order by material name? Requires join.
    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :itemId")
    fun getInventoryItemById(itemId: String): Flow<InventoryItem?>

     @Query("SELECT * FROM inventory_items WHERE material_id = :materialId")
    fun getInventoryItemByMaterialId(materialId: String): Flow<InventoryItem?>

    // Query to update quantity directly (use with caution, prefer transactions)
    @Query("UPDATE inventory_items SET quantity_on_hand = :newQuantity WHERE id = :itemId")
    suspend fun updateInventoryItemQuantity(itemId: String, newQuantity: Double)

    // --- Combined Queries ---
    @Transaction // Ensures atomic read of related data
    @Query("SELECT * FROM inventory_items ORDER BY location ASC") // Order by location or material name?
    fun getAllInventoryItemsWithMaterial(): Flow<List<InventoryItemWithMaterial>>


    // --- Inventory Transaction Operations ---

    @Insert(onConflict = OnConflictStrategy.IGNORE) // Ignore if duplicate transaction ID somehow occurs
    suspend fun insertTransaction(transaction: InventoryTransaction)

    // Transactions are typically immutable, so no update/delete needed?

    @Query("SELECT * FROM inventory_transactions WHERE inventory_item_id = :itemId ORDER BY timestamp DESC")
    fun getTransactionsForItem(itemId: String): Flow<List<InventoryTransaction>>

    @Query("SELECT * FROM inventory_transactions WHERE related_job_id = :jobId ORDER BY timestamp DESC")
    fun getTransactionsForJob(jobId: String): Flow<List<InventoryTransaction>>

     @Query("SELECT * FROM inventory_transactions WHERE related_task_id = :taskId ORDER BY timestamp DESC")
    fun getTransactionsForTask(taskId: String): Flow<List<InventoryTransaction>>

    // TODO: Add queries for searching/filtering inventory (might need joins)
}
