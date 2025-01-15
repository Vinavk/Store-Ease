package com.example.storeease.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [FileItem::class], version = 1, exportSchema = false)
abstract class FileDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
}