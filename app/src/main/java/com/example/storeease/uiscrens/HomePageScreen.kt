package com.example.storeease.uiscrens

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.storeease.R
import com.example.storeease.db.FileItem
import com.example.storeease.viewmodel.FileViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageScreen(fileViewModel: FileViewModel) {
    val allFiles by fileViewModel.allFiles.observeAsState()
    val trashedFiles by fileViewModel.trashedFiles.observeAsState(emptyList())
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val selectedFileType by remember { mutableStateOf("All") }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()




    val selectedFiles = remember { mutableStateListOf<FileItem>() }
    var isSelectableMode by remember { mutableStateOf(false) }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                val fileName = context.contentResolver.query(it, null, null, null, null)
                    ?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        cursor.moveToFirst()
                        cursor.getString(nameIndex)
                    } ?: "Unknown"

                val fileType = context.contentResolver.getType(it) ?: "unknown"
                val filePath = it.toString()

                val newFile = FileItem(
                    name = fileName,
                    filePath = filePath,
                    type = fileType,
                    isInTrash = false,
                    addedAt = System.currentTimeMillis()
                )

                fileViewModel.insertFile(newFile)
            }
        }
    )

    @SuppressLint("InlinedApi")
    fun storeFilesToDownloads() {
        selectedFiles.forEach { file ->
            try {
                val fileUri = Uri.parse(file.filePath)
                val inputStream = context.contentResolver.openInputStream(fileUri)
                    ?: throw IOException("Failed to open InputStream for the URI")

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                    put(MediaStore.MediaColumns.MIME_TYPE, file.type)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uriForDownloads = context.contentResolver.insert(
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                    contentValues
                ) ?: throw IOException("Failed to insert file into Downloads folder")

                val outputStream = context.contentResolver.openOutputStream(uriForDownloads)
                    ?: throw IOException("Failed to open OutputStream to save file")

                inputStream.use { input -> outputStream.use { output -> input.copyTo(output) } }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save file: ${file.name}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
        Toast.makeText(context, "Files saved to Downloads!", Toast.LENGTH_SHORT).show()
        selectedFiles.clear()
        isSelectableMode = false
    }

    fun deleteSelectedFiles() {
        selectedFiles.forEach { file ->
            fileViewModel.moveToTrash(file.id)
        }
        Toast.makeText(context, "Files moved to Trash!", Toast.LENGTH_SHORT).show()
        selectedFiles.clear()
        isSelectableMode = false
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        scrimColor = DrawerDefaults.scrimColor,
        drawerContent = {
            Box(modifier = Modifier.fillMaxHeight().width(320.dp).background(Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Trashed Files", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(trashedFiles) { file ->
                            TrashedFilesList(file = file, fileViewModel = fileViewModel,context)
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    title = { Text("SmartEase", modifier = Modifier.fillMaxWidth()) },
                    actions = {
                        if (isSelectableMode) {
                            IconButton(onClick = { storeFilesToDownloads() }) {
                                Icon(
                                    painter = painterResource(R.drawable.documents_icon),
                                    contentDescription = "Save Downloads",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .padding(8.dp),
                                    tint = Color.Unspecified
                                )
                            }
                            IconButton(onClick = { deleteSelectedFiles() }) {
                                Icon(
                                    painter = painterResource(R.drawable.delete_icon),
                                    contentDescription = "Delete",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .padding(8.dp),
                                    tint = Color.Unspecified
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Files") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Button(
                    onClick = { pickFileLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Select Files")
                }

                if (allFiles == null) {
                    Text("Loading files...", style = MaterialTheme.typography.bodyMedium)
                } else {
                    val filteredFiles = allFiles!!.filter { file ->
                        (file.name.contains(searchQuery, ignoreCase = true) ||
                                file.filePath.contains(searchQuery, ignoreCase = true)) &&
                                (selectedFileType == "All" || file.type.contains(selectedFileType, ignoreCase = true))
                    }

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(filteredFiles) { file ->
                            FileItemView(
                                file = file,

                                isSelected = selectedFiles.contains(file),
                                onSelect = { selectedFile ->
                                    if (selectedFiles.contains(selectedFile)) {
                                        selectedFiles.remove(selectedFile)
                                    } else {
                                        selectedFiles.add(selectedFile)
                                    }
                                    isSelectableMode = selectedFiles.isNotEmpty()
                                },
                                isSelectableMode = isSelectableMode,
                                setSelectableMode = { isSelectableMode = it }
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun TrashedFilesList(file: FileItem, fileViewModel: FileViewModel, context: Context) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.LightGray.copy(alpha = 0.1f))
            .clip(RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            val fileExtension = file.name.substringAfterLast('.', "").lowercase()
            when {
                fileExtension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp") -> {
                    Image(
                        painter = rememberAsyncImagePainter(file.filePath),
                        contentDescription = "Image Preview",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    IconButton(
                        onClick = { fileViewModel.restoreFile(file.id)
                            Toast.makeText(context, " Restored", Toast.LENGTH_SHORT).show()

                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.trash_icon),
                            contentDescription = "Restore File",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            tint = Color.Unspecified
                        )
                    }

                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

       
        Text(
            text = file.name,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )


    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItemView(
    file: FileItem,

    isSelected: Boolean,
    onSelect: (FileItem) -> Unit,
    isSelectableMode: Boolean,
    setSelectableMode: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.Black else Color.White,
                shape = RoundedCornerShape(8.dp)
            ).background(Color.White)
            .combinedClickable(
                onClick = {
                    if (isSelectableMode) {
                        onSelect(file)
                    }
                },
                onLongClick = {
                    setSelectableMode(true)
                    onSelect(file)
                }
            ),
        elevation = if (isSelected) {
            CardDefaults.cardElevation(defaultElevation = 8.dp)
        } else {
            CardDefaults.cardElevation(defaultElevation = 4.dp)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val fileExtension = file.name.substringAfterLast('.', "").lowercase()
            val iconModifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)

            when (fileExtension) {
                in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp") -> {
                    AsyncImage(
                        model = file.filePath,
                        contentDescription = "Image Preview",
                        modifier = iconModifier
                    )
                }
                "pdf" -> {
                    Icon(
                        painter = painterResource(R.drawable.pdf_icon),
                        contentDescription = "PDF File",
                        modifier = iconModifier.padding(8.dp),
                        tint = Color.Unspecified
                    )
                }
                in listOf("mp4", "avi", "mkv", "mov", "flv", "wmv") -> {
                    Icon(
                        painter = painterResource(R.drawable.video_icon),
                        contentDescription = "Video File",
                        modifier = iconModifier.padding(8.dp),
                        tint = Color.Unspecified
                    )
                }
                in listOf("mp3", "wav", "aac", "flac", "ogg") -> {
                    Icon(
                        painter = painterResource(R.drawable.audio_icon),
                        contentDescription = "Audio File",
                        modifier = iconModifier.padding(8.dp),
                        tint = Color.Unspecified
                    )
                }
                else -> {
                    Icon(
                        painter = painterResource(R.drawable.documents_icon),
                        contentDescription = "Document File",
                        modifier = iconModifier.padding(8.dp),
                        tint = Color.Unspecified
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Type: ${file.type}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Added: ${file.addedAt.formatAsDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

fun Long.formatAsDate(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return formatter.format(Date(this))
}


