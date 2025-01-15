package com.example.storeease.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class FileItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val filePath: String,
    val type: String,
    var isInTrash: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    var trashedAt: Long? = null
)
