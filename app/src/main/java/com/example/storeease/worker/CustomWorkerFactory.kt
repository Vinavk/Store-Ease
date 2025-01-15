package com.example.storeease.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.storeease.viewmodel.FileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomWorkerFactory @Inject constructor(
    private val repository: FileRepository
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            MyCoroutineWorker::class.java.name -> {
                MyCoroutineWorker( repository,appContext, workerParameters)
            }
            else -> null
        }
    }
}
