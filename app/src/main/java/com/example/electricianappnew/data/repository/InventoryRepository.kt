package com.example.electricianappnew.data.repository

import com.example.electricianappnew.data.local.dao.InventoryDao // Keep only one import
import com.example.electricianappnew.data.model.InventoryItem
import com.example.electricianappnew.data.model.InventoryTransaction
import com.example.electricianappnew.data.model.Material
import com.example.electricianappnew.data.model.InventoryItemWithMaterial // Added missing import
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface InventoryRepository {
    // Material
    fun getAllMaterials(): Flow<List<Material>>
    fun getMaterialById(materialId: String): Flow<Material?>
    suspend fun insertMaterial(material: Material)
    suspend fun updateMaterial(material: Material)
    suspend fun deleteMaterial(material: Material)

    // Inventory Item
    fun getAllInventoryItems(): Flow<List<InventoryItem>>
    fun getInventoryItemById(itemId: String): Flow<InventoryItem?>
    fun getInventoryItemByMaterialId(materialId: String): Flow<InventoryItem?>
    suspend fun insertInventoryItem(item: InventoryItem)
    suspend fun updateInventoryItem(item: InventoryItem)
    suspend fun updateInventoryItemQuantity(itemId: String, newQuantity: Double)

    // Transaction
    suspend fun insertTransaction(transaction: InventoryTransaction)
    fun getTransactionsForItem(itemId: String): Flow<List<InventoryTransaction>>
    fun getTransactionsForJob(jobId: String): Flow<List<InventoryTransaction>>
    fun getTransactionsForTask(taskId: String): Flow<List<InventoryTransaction>>

    // Combined Query for List Screen
    fun getAllInventoryItemsWithMaterial(): Flow<List<InventoryItemWithMaterial>> // Added
}

@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val inventoryDao: InventoryDao
) : InventoryRepository {

    // Material Impl
    override fun getAllMaterials(): Flow<List<Material>> = inventoryDao.getAllMaterials()
    override fun getMaterialById(materialId: String): Flow<Material?> = inventoryDao.getMaterialById(materialId)
    override suspend fun insertMaterial(material: Material) = inventoryDao.insertMaterial(material)
    override suspend fun updateMaterial(material: Material) = inventoryDao.updateMaterial(material)
    override suspend fun deleteMaterial(material: Material) = inventoryDao.deleteMaterial(material)

    // Inventory Item Impl
    override fun getAllInventoryItems(): Flow<List<InventoryItem>> = inventoryDao.getAllInventoryItems()
    override fun getInventoryItemById(itemId: String): Flow<InventoryItem?> = inventoryDao.getInventoryItemById(itemId)
    override fun getInventoryItemByMaterialId(materialId: String): Flow<InventoryItem?> = inventoryDao.getInventoryItemByMaterialId(materialId)
    override suspend fun insertInventoryItem(item: InventoryItem) = inventoryDao.insertInventoryItem(item)
    override suspend fun updateInventoryItem(item: InventoryItem) = inventoryDao.updateInventoryItem(item)
    override suspend fun updateInventoryItemQuantity(itemId: String, newQuantity: Double) = inventoryDao.updateInventoryItemQuantity(itemId, newQuantity)

    // Transaction Impl
    override suspend fun insertTransaction(transaction: InventoryTransaction) = inventoryDao.insertTransaction(transaction)
    override fun getTransactionsForItem(itemId: String): Flow<List<InventoryTransaction>> = inventoryDao.getTransactionsForItem(itemId)
    override fun getTransactionsForJob(jobId: String): Flow<List<InventoryTransaction>> = inventoryDao.getTransactionsForJob(jobId)
    override fun getTransactionsForTask(taskId: String): Flow<List<InventoryTransaction>> = inventoryDao.getTransactionsForTask(taskId)

    // Combined Query Impl
    override fun getAllInventoryItemsWithMaterial(): Flow<List<InventoryItemWithMaterial>> = inventoryDao.getAllInventoryItemsWithMaterial() // Added
}
