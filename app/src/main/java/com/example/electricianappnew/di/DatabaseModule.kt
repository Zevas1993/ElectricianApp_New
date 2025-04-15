package com.example.electricianappnew.di

import android.content.Context
import androidx.room.Room
import com.example.electricianappnew.data.local.AppDatabase
import com.example.electricianappnew.data.local.dao.*
import com.example.electricianappnew.data.repository.* // Import repositories
import dagger.Binds // Import Binds for interfaces
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.electricianappnew.data.local.DatabaseCallback // Import the callback
import javax.inject.Provider // Import Provider

@Module
@InstallIn(SingletonComponent::class) // Provides dependencies for the entire application lifecycle
object DatabaseModule {

    @Provides
    @Singleton // Ensure only one instance of the database is created
    // Inject Provider<AppDatabase> lazily to break potential dependency cycle with callback
    fun provideAppDatabase(
        @ApplicationContext appContext: Context,
        dbProvider: Provider<AppDatabase> // Use Provider
    ): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .addCallback(DatabaseCallback(appContext, dbProvider)) // Add the callback here
        // TODO: Add migrations here if needed in the future
        // .addMigrations(...)
        .fallbackToDestructiveMigration(dropAllTables = true) // Updated deprecated call
        .build()
    }

    // Provide DAOs - Hilt will know how to create them from the AppDatabase instance

    @Provides
    fun provideJobTaskDao(appDatabase: AppDatabase): JobTaskDao {
        return appDatabase.jobTaskDao()
    }

    @Provides
    fun providePhotoDocDao(appDatabase: AppDatabase): PhotoDocDao {
        return appDatabase.photoDocDao()
    }

    @Provides
    fun provideInventoryDao(appDatabase: AppDatabase): InventoryDao {
        return appDatabase.inventoryDao()
    }

    @Provides
    fun provideClientDao(appDatabase: AppDatabase): ClientDao {
        return appDatabase.clientDao()
    }

     @Provides
    fun provideNecDataDao(appDatabase: AppDatabase): NecDataDao {
        return appDatabase.necDataDao()
    }

    // Add providers for other DAOs as they are created

    // Provide Repositories - Hilt injects the DAOs provided above

    @Provides
    @Singleton
    fun provideJobTaskRepository(dao: JobTaskDao): JobTaskRepository {
        return JobTaskRepositoryImpl(dao)
    }

     @Provides
    @Singleton
    fun providePhotoDocRepository(dao: PhotoDocDao): PhotoDocRepository {
        return PhotoDocRepositoryImpl(dao)
    }

     @Provides
    @Singleton
    fun provideInventoryRepository(dao: InventoryDao): InventoryRepository {
        return InventoryRepositoryImpl(dao)
    }

     @Provides
    @Singleton
    fun provideClientRepository(dao: ClientDao): ClientRepository {
        return ClientRepositoryImpl(dao)
    }

     @Provides
    @Singleton
    fun provideNecDataRepository(dao: NecDataDao): NecDataRepository {
        return NecDataRepositoryImpl(dao)
    }

    // Alternative using @Binds (if repositories were interfaces bound to implementations)
    // @Binds abstract fun bindJobTaskRepository(impl: JobTaskRepositoryImpl): JobTaskRepository
}

// Example of using @Binds in a separate module (Optional alternative structure)
/*
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindJobTaskRepository(impl: JobTaskRepositoryImpl): JobTaskRepository

    @Binds
    @Singleton
    abstract fun bindPhotoDocRepository(impl: PhotoDocRepositoryImpl): PhotoDocRepository

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository

     @Binds
    @Singleton
    abstract fun bindClientRepository(impl: ClientRepositoryImpl): ClientRepository
}
*/
