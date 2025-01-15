package com.example.storeease
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.storeease.ui.theme.StoreEaseTheme
import com.example.storeease.uiscrens.HomePageScreen
import com.example.storeease.viewmodel.FileViewModel
import com.example.storeease.worker.MyCoroutineWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val fileViewModel: FileViewModel by viewModels()

    private val REQUEST_CODE_READ_STORAGE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE

            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_CODE_READ_STORAGE
            )
        }

        setContent {
            StoreEaseTheme {
                val windowInsetsController = WindowInsetsControllerCompat(
                    (this).window, LocalView.current
                )
                windowInsetsController.isAppearanceLightStatusBars = true

                Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .systemBarsPadding()
                ) {
                    HomePageScreen(fileViewModel)
                }
            }
        }

        fileViewModel.trashFile.observe(this) {
            scheduleWorker(this, fileViewModel)
        }
    }

    private fun scheduleWorker(context: Context, fileViewModel: FileViewModel) {

        val myWorkerRequest = OneTimeWorkRequestBuilder<MyCoroutineWorker>()
            .setInitialDelay(24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueue(myWorkerRequest)

        WorkManager.getInstance(context).getWorkInfoByIdLiveData(myWorkerRequest.id)
            .observeForever { workInfo ->
                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                    fileViewModel.loadTrashedFiles()
                }
            }
    }
}