package com.example.electricianappnew.data.local.dao

import androidx.room.*
import com.example.electricianappnew.data.model.Job
import com.example.electricianappnew.data.model.Task
import kotlinx.coroutines.flow.Flow // For reactive queries

@Dao
interface JobTaskDao {

    // --- Job Operations ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: Job)

    @Update
    suspend fun updateJob(job: Job)

    @Delete
    suspend fun deleteJob(job: Job)

    @Query("SELECT * FROM jobs ORDER BY date_created DESC")
    fun getAllJobs(): Flow<List<Job>> // Use Flow for observing changes

    @Query("SELECT * FROM jobs WHERE id = :jobId")
    fun getJobById(jobId: String): Flow<Job?>

    // --- Task Operations ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE job_id = :jobId ORDER BY date_created ASC")
    fun getTasksForJob(jobId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: String): Flow<Task?>

    // TODO: Add more complex queries as needed (e.g., search, filtering by status)
}
