package com.example.storeease.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface FileDao {

    @Query("SELECT * FROM files WHERE isInTrash = 1 AND trashedAt <= :timeLimit")
    suspend fun getFilesInTrashOlderThan(timeLimit: Long): List<FileItem>

    @Insert
    suspend fun insertFile(fileItem: FileItem)

    @Query("SELECT * FROM files WHERE isInTrash = 0")
    suspend fun getAllFiles(): List<FileItem>

    @Update
    suspend fun updateFile(fileItem: FileItem)

    @Delete
    suspend fun deleteFile(fileItem: FileItem)

    @Query("SELECT * FROM files WHERE id = :fileId")
    suspend fun getFileById(fileId: Long): FileItem?

    @Query("SELECT * FROM files WHERE isInTrash = 1")
    suspend fun getTrashedFiles(): List<FileItem>
}
