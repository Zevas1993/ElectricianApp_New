package com.example.electricianappnew.data.repository

import com.example.electricianappnew.data.local.dao.PhotoDocDao
import com.example.electricianappnew.data.model.PhotoDoc
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface PhotoDocRepository {
    fun getAllPhotoDocs(): Flow<List<PhotoDoc>>
    fun getPhotoDocById(photoId: String): Flow<PhotoDoc?>
    fun getPhotoDocsForJob(jobId: String): Flow<List<PhotoDoc>>
    fun getPhotoDocsForTask(taskId: String): Flow<List<PhotoDoc>>
    suspend fun insertPhotoDoc(photoDoc: PhotoDoc)
    suspend fun updatePhotoDoc(photoDoc: PhotoDoc)
    suspend fun deletePhotoDoc(photoDoc: PhotoDoc)
    suspend fun deletePhotosForJob(jobId: String)
    suspend fun deletePhotosForTask(taskId: String)
}

@Singleton
class PhotoDocRepositoryImpl @Inject constructor(
    private val photoDocDao: PhotoDocDao
) : PhotoDocRepository {

    override fun getAllPhotoDocs(): Flow<List<PhotoDoc>> = photoDocDao.getAllPhotoDocs()

    override fun getPhotoDocById(photoId: String): Flow<PhotoDoc?> = photoDocDao.getPhotoDocById(photoId)

    override fun getPhotoDocsForJob(jobId: String): Flow<List<PhotoDoc>> = photoDocDao.getPhotoDocsForJob(jobId)

    override fun getPhotoDocsForTask(taskId: String): Flow<List<PhotoDoc>> = photoDocDao.getPhotoDocsForTask(taskId)

    override suspend fun insertPhotoDoc(photoDoc: PhotoDoc) = photoDocDao.insertPhotoDoc(photoDoc)

    override suspend fun updatePhotoDoc(photoDoc: PhotoDoc) = photoDocDao.updatePhotoDoc(photoDoc)

    override suspend fun deletePhotoDoc(photoDoc: PhotoDoc) = photoDocDao.deletePhotoDoc(photoDoc)

    override suspend fun deletePhotosForJob(jobId: String) = photoDocDao.deletePhotosForJob(jobId)

    override suspend fun deletePhotosForTask(taskId: String) = photoDocDao.deletePhotosForTask(taskId)
}
