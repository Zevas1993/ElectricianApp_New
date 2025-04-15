package com.example.electricianappnew.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.Date
import java.util.UUID

// Represents a type of material (e.g., 12/2 NM-B Wire, 1/2" EMT Conduit)
@Entity(tableName = "materials")
data class Material(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "category") val category: String, // e.g., Wire, Conduit, Devices, Fasteners
    @ColumnInfo(name = "part_number") val partNumber: String? = null, // Optional manufacturer part number
    // Add other relevant fields like description, supplier, cost later
)

// Represents a specific stock of a material at a location
@Entity(
    tableName = "inventory_items",
    foreignKeys = [ForeignKey(
        entity = Material::class,
        parentColumns = ["id"],
        childColumns = ["material_id"],
        onDelete = ForeignKey.CASCADE // Delete inventory item if material is deleted
    )],
    indices = [androidx.room.Index(value = ["material_id"])] // Index for faster lookups by material
)
data class InventoryItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "material_id") val materialId: String,
    @ColumnInfo(name = "quantity_on_hand") var quantityOnHand: Double,
    @ColumnInfo(name = "unit_of_measure") val unitOfMeasure: String, // e.g., Feet, Each, Box
    @ColumnInfo(name = "location") val location: String, // e.g., Truck, Shop Shelf A
    @ColumnInfo(name = "low_stock_threshold") val lowStockThreshold: Double? = null, // Optional threshold
    // Add last updated timestamp?
)

// Represents a change in inventory quantity (addition, usage, adjustment)
@Entity(
    tableName = "inventory_transactions",
    foreignKeys = [
        ForeignKey(
            entity = InventoryItem::class,
            parentColumns = ["id"],
            childColumns = ["inventory_item_id"],
            onDelete = ForeignKey.CASCADE // Delete transactions if item is deleted
        ),
        ForeignKey(
            entity = Job::class, // Assuming Job entity exists
            parentColumns = ["id"],
            childColumns = ["related_job_id"],
            onDelete = ForeignKey.SET_NULL // Keep transaction even if job is deleted
        ),
         ForeignKey(
            entity = Task::class, // Assuming Task entity exists
            parentColumns = ["id"],
            childColumns = ["related_task_id"],
            onDelete = ForeignKey.SET_NULL // Keep transaction even if task is deleted
        )
    ],
     indices = [
         androidx.room.Index(value = ["inventory_item_id"]),
         androidx.room.Index(value = ["related_job_id"]),
         androidx.room.Index(value = ["related_task_id"])
    ]
)
data class InventoryTransaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "inventory_item_id") val inventoryItemId: String,
    @ColumnInfo(name = "transaction_type") val transactionType: String, // e.g., Received, Used on Job, Adjustment, Initial Stock
    @ColumnInfo(name = "quantity_change") val quantityChange: Double, // Positive for addition, negative for removal
    @ColumnInfo(name = "timestamp") val timestamp: Date = Date(),
    @ColumnInfo(name = "related_job_id") val relatedJobId: String? = null,
    @ColumnInfo(name = "related_task_id") val relatedTaskId: String? = null,
    @ColumnInfo(name = "notes") val notes: String = ""
)

// Data class to combine InventoryItem and its Material for display
data class InventoryItemWithMaterial(
    @Embedded val inventoryItem: InventoryItem,
    @Relation(
        parentColumn = "material_id",
        entityColumn = "id"
    )
    val material: Material
)
