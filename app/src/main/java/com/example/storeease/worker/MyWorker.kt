package com.example.storeease.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.storeease.viewmodel.FileRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


class MyCoroutineWorker @AssistedInject constructor(
    @Assisted  private val repository: FileRepository,
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {


    override suspend fun doWork(): Result {

        return try {

            val currentTime = System.currentTimeMillis()

            val timeLimit = currentTime - (24 * 60 * 60 * 1000)

            val files = repository.getFilesInTrashOlderThan(timeLimit)

            files.forEach { file ->
                try {
                    repository.deleteFile(file)
                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }

            Result.success()
        } catch (e: Exception) {

            e.printStackTrace()
            Result.failure()
        }
    }
}



