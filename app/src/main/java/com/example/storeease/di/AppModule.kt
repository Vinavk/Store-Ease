package com.example.storeease.di



import android.content.Context
import androidx.room.Room
import com.example.storeease.db.FileDao
import com.example.storeease.db.FileDatabase
import com.example.storeease.viewmodel.FileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FileDatabase {
        return Room.databaseBuilder(
            context,
            FileDatabase::class.java,
            "file_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFileDao(database: FileDatabase): FileDao {
        return database.fileDao()
    }

    @Provides
    @Singleton
    fun provideRepository(fileDao: FileDao): FileRepository {
        return FileRepository(fileDao)
    }


}
