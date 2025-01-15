package com.example.storeease.viewmodel

import com.example.storeease.db.FileDao
import com.example.storeease.db.FileItem

class FileRepository(private val fileDao: FileDao) {

    suspend fun getAllFiles(): List<FileItem> {
        return fileDao.getAllFiles()
    }

    suspend fun getTrashedFiles(): List<FileItem> {
        return fileDao.getTrashedFiles()
    }

    suspend fun insertFile(fileItem: FileItem) {
        fileDao.insertFile(fileItem)
    }

    suspend fun deleteFile(fileItem: FileItem) {
        fileDao.deleteFile(fileItem)
    }

    suspend fun moveToTrash(fileId: Long) {
        val file = fileDao.getFileById(fileId)
        file?.let {
            it.isInTrash = true
            it.trashedAt = System.currentTimeMillis()
            fileDao.updateFile(it)
        }
    }

    suspend fun restoreFile(fileId: Long) {
        val file = fileDao.getFileById(fileId)
        file?.let {
            it.isInTrash = false
            it.trashedAt = 0
            fileDao.updateFile(it)
        }
    }

    suspend fun getFilesInTrashOlderThan(timeLimit: Long): List<FileItem> {
        return fileDao.getFilesInTrashOlderThan(timeLimit)
    }



    suspend fun getFileById(fileId: Long): FileItem? {
        return fileDao.getFileById(fileId)
    }
}
