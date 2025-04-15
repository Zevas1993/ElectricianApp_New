package com.example.electricianappnew.data.repository

import com.example.electricianappnew.data.local.dao.ClientDao
import com.example.electricianappnew.data.model.Client
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface ClientRepository {
    fun getAllClients(): Flow<List<Client>>
    fun getClientById(clientId: String): Flow<Client?>
    suspend fun insertClient(client: Client)
    suspend fun updateClient(client: Client)
    suspend fun deleteClient(client: Client)
}

@Singleton
class ClientRepositoryImpl @Inject constructor(
    private val clientDao: ClientDao
) : ClientRepository {

    override fun getAllClients(): Flow<List<Client>> = clientDao.getAllClients()

    override fun getClientById(clientId: String): Flow<Client?> = clientDao.getClientById(clientId)

    override suspend fun insertClient(client: Client) = clientDao.insertClient(client)

    override suspend fun updateClient(client: Client) = clientDao.updateClient(client)

    override suspend fun deleteClient(client: Client) = clientDao.deleteClient(client)
}
