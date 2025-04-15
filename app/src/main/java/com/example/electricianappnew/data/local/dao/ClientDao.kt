package com.example.electricianappnew.data.local.dao

import androidx.room.*
import com.example.electricianappnew.data.model.Client
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client)

    @Update
    suspend fun updateClient(client: Client)

    @Delete
    suspend fun deleteClient(client: Client)

    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE id = :clientId")
    fun getClientById(clientId: String): Flow<Client?>

    // TODO: Add search queries if needed (e.g., search by name, phone)
}
