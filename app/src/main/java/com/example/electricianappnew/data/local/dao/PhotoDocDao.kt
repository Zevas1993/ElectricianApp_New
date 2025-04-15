package com.example.electricianappnew.data.local.dao

import androidx.room.*
import com.example.electricianappnew.data.model.PhotoDoc
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDocDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotoDoc(photoDoc: PhotoDoc)

    @Update
    suspend fun updatePhotoDoc(photoDoc: PhotoDoc)

    @Delete
    suspend fun deletePhotoDoc(photoDoc: PhotoDoc)

    @Query("SELECT * FROM photo_docs ORDER BY date_taken DESC")
    fun getAllPhotoDocs(): Flow<List<PhotoDoc>>

    @Query("SELECT * FROM photo_docs WHERE id = :photoId")
    fun getPhotoDocById(photoId: String): Flow<PhotoDoc?>

    @Query("SELECT * FROM photo_docs WHERE job_id = :jobId ORDER BY date_taken DESC")
    fun getPhotoDocsForJob(jobId: String): Flow<List<PhotoDoc>>

    @Query("SELECT * FROM photo_docs WHERE task_id = :taskId ORDER BY date_taken DESC")
    fun getPhotoDocsForTask(taskId: String): Flow<List<PhotoDoc>>

    // Query to delete photos associated with a specific job (if needed, e.g., if ForeignKey is SET_NULL)
    @Query("DELETE FROM photo_docs WHERE job_id = :jobId")
    suspend fun deletePhotosForJob(jobId: String)

     // Query to delete photos associated with a specific task
    @Query("DELETE FROM photo_docs WHERE task_id = :taskId")
    suspend fun deletePhotosForTask(taskId: String)
}
