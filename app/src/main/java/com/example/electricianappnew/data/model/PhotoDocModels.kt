package com.example.electricianappnew.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.electricianappnew.data.local.DateConverter // Assuming DateConverter exists
import java.util.Date

// Basic data class for Photo Documentation metadata
@Entity(
    tableName = "photo_docs",
    foreignKeys = [
        ForeignKey(entity = Job::class, parentColumns = ["id"], childColumns = ["job_id"], onDelete = ForeignKey.SET_NULL), // Keep photo if job deleted? Or Cascade?
        ForeignKey(entity = Task::class, parentColumns = ["id"], childColumns = ["task_id"], onDelete = ForeignKey.SET_NULL) // Keep photo if task deleted? Or Cascade?
    ],
    indices = [Index(value = ["job_id"]), Index(value = ["task_id"])]
)
@TypeConverters(DateConverter::class)
data class PhotoDoc(
    @PrimaryKey val id: String = "photo_${System.currentTimeMillis()}",
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "caption") val caption: String = "",
    @ColumnInfo(name = "date_taken") val dateTaken: Date = Date(),
    @ColumnInfo(name = "job_id") val jobId: String? = null,
    @ColumnInfo(name = "task_id") val taskId: String? = null
)
