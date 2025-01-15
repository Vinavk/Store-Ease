package com.example.storeease.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storeease.db.FileItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileViewModel @Inject constructor(
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _allFiles = MutableLiveData<List<FileItem>>()
    val allFiles: LiveData<List<FileItem>> get() = _allFiles


    private val _trashedFiles = MutableLiveData<List<FileItem>>()
    val trashedFiles: LiveData<List<FileItem>> get() = _trashedFiles

    private val _selectedFile = MutableLiveData<FileItem?>()
    val selectedFile: LiveData<FileItem?> = _selectedFile


    private val _trashFile = MutableLiveData<FileItem?>()
    val trashFile: LiveData<FileItem?> = _trashFile

    init {
        loadFiles()
        loadTrashedFiles()
    }


    fun loadFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val files = fileRepository.getAllFiles()
            _allFiles.postValue(files)
        }
    }


    fun loadTrashedFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val trashedFilesList = fileRepository.getTrashedFiles()
            _trashedFiles.postValue(trashedFilesList)
        }
    }


    fun insertFile(fileItem: FileItem) {
        viewModelScope.launch(Dispatchers.IO) {
            fileRepository.insertFile(fileItem)
            loadFiles()
        }
    }



    fun moveToTrash(fileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            fileRepository.moveToTrash(fileId)
            val file = fileRepository.getFileById(fileId)
            file?.let {
                _trashFile.postValue(it)
            }
            loadFiles()
            loadTrashedFiles()
        }
    }


    fun restoreFile(fileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            fileRepository.restoreFile(fileId)
            loadFiles()
            loadTrashedFiles()
        }
    }







}
