package com.example.electricianappnew.data.repository

import com.example.electricianappnew.data.local.dao.JobTaskDao
import com.example.electricianappnew.data.model.Job
import com.example.electricianappnew.data.model.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// Interface defining the operations for Job and Task data
interface JobTaskRepository {
    fun getAllJobs(): Flow<List<Job>>
    fun getJobById(jobId: String): Flow<Job?>
    suspend fun insertJob(job: Job)
    suspend fun updateJob(job: Job)
    suspend fun deleteJob(job: Job)

    fun getTasksForJob(jobId: String): Flow<List<Task>>
    fun getTaskById(taskId: String): Flow<Task?>
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
}

// Implementation of the repository using the Room DAO
@Singleton // Hilt will provide a single instance of this repository
class JobTaskRepositoryImpl @Inject constructor(
    private val jobTaskDao: JobTaskDao // Hilt provides the DAO instance
) : JobTaskRepository {

    override fun getAllJobs(): Flow<List<Job>> = jobTaskDao.getAllJobs()

    override fun getJobById(jobId: String): Flow<Job?> = jobTaskDao.getJobById(jobId)

    override suspend fun insertJob(job: Job) = jobTaskDao.insertJob(job)

    override suspend fun updateJob(job: Job) = jobTaskDao.updateJob(job)

    override suspend fun deleteJob(job: Job) = jobTaskDao.deleteJob(job)

    override fun getTasksForJob(jobId: String): Flow<List<Task>> = jobTaskDao.getTasksForJob(jobId)

    override fun getTaskById(taskId: String): Flow<Task?> = jobTaskDao.getTaskById(taskId)

    override suspend fun insertTask(task: Task) = jobTaskDao.insertTask(task)

    override suspend fun updateTask(task: Task) = jobTaskDao.updateTask(task)

    override suspend fun deleteTask(task: Task) = jobTaskDao.deleteTask(task)
}
