package com.example.electricianappnew.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.electricianappnew.data.local.DateConverter // Assuming DateConverter exists
import java.util.Date
import androidx.room.ForeignKey // Import ForeignKey
import androidx.room.Index // Import Index

// Basic data class for a Job
@Entity(
    tableName = "jobs",
    foreignKeys = [ForeignKey(
        entity = Client::class, // Reference the Client entity
        parentColumns = ["id"],
        childColumns = ["client_id"],
        onDelete = ForeignKey.SET_NULL // Or CASCADE, depending on desired behavior
    )],
    indices = [Index(value = ["client_id"])] // Index for faster client-based lookups
)
@TypeConverters(DateConverter::class)
data class Job(
    @PrimaryKey val id: String = "job_${System.currentTimeMillis()}",
    @ColumnInfo(name = "job_name") val jobName: String = "",
    @ColumnInfo(name = "client_id") val clientId: String, // Added Foreign Key to Client
    @ColumnInfo(name = "client_name") val clientName: String = "", // Keep for display convenience? Or remove? Keep for now.
    @ColumnInfo(name = "address") val address: String = "",
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "status") val status: String = "Not Started",
    @ColumnInfo(name = "date_created") val dateCreated: Date = Date(),
    @ColumnInfo(name = "date_updated") var dateUpdated: Date = Date() // Make var for updates
    // Consider adding fields like: clientId (foreign key), estimatedHours, actualHours
)

// Basic data class for a Task within a Job
@Entity(
    tableName = "tasks",
    foreignKeys = [androidx.room.ForeignKey(
        entity = Job::class,
        parentColumns = ["id"],
        childColumns = ["job_id"],
        onDelete = androidx.room.ForeignKey.CASCADE // Delete tasks if job is deleted
    )],
    indices = [androidx.room.Index(value = ["job_id"])] // Index for faster job-based lookups
)
@TypeConverters(DateConverter::class)
data class Task(
    @PrimaryKey val id: String = "task_${System.currentTimeMillis()}",
    @ColumnInfo(name = "job_id") val jobId: String,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "status") val status: String = "Pending",
    @ColumnInfo(name = "assigned_to") val assignedTo: String = "",
    @ColumnInfo(name = "date_created") val dateCreated: Date = Date(),
    @ColumnInfo(name = "date_completed") var dateCompleted: Date? = null // Make var
    // Consider adding fields like: materialsUsedJson (String?), timeLoggedMillis (Long?), linkedPhotoIdsJson (String?)
)
