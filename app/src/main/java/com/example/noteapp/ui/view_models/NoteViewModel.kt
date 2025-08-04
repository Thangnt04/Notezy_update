//
//package com.example.noteapp.ui.view_models
//
//import android.content.Context
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.noteapp.data.models.Note
//import com.example.noteapp.data.repositories.NoteRepository
//import com.example.noteapp.utils.response.EmptyResult
//import com.example.noteapp.utils.response.ResultStatus
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//class NoteViewModel : ViewModel() {
//    private val noteRepository: NoteRepository by lazy { NoteRepository() }
//
//    private val _notesResultStatus = MutableLiveData<ResultStatus<MutableList<Note>>>()
//    val notesResultStatus: LiveData<ResultStatus<MutableList<Note>>> = _notesResultStatus
//
//    private val _searchResultStatus = MutableLiveData<ResultStatus<MutableList<Note>>>()
//    val searchResultStatus: LiveData<ResultStatus<MutableList<Note>>> = _searchResultStatus
//
//    private val _noteAddResultStatus = MutableLiveData<ResultStatus<Note>?>()
//    val noteAddResultStatus: LiveData<ResultStatus<Note>?> = _noteAddResultStatus
//
//    private val _noteUpdateResultStatus = MutableLiveData<ResultStatus<Note>?>()
//    val noteUpdateResultStatus: LiveData<ResultStatus<Note>?> = _noteUpdateResultStatus
//
//    private val _noteDeleteStatus = MutableLiveData<EmptyResult?>()
//    val noteDeleteStatus: LiveData<EmptyResult?> = _noteDeleteStatus
//
//    fun listNotes(isFinished: Boolean = false) {
//        viewModelScope.launch(Dispatchers.IO) {
//            _notesResultStatus.postValue(ResultStatus.Loading)
//            val result = noteRepository.listNotes(isFinished)
//            _notesResultStatus.postValue(result)
//        }
//    }
//
//    fun searchNotes(query: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            if (query.isBlank()) {
//                _searchResultStatus.postValue(ResultStatus.Success(mutableListOf()))
//            } else {
//                _searchResultStatus.postValue(ResultStatus.Loading)
//                val result = noteRepository.searchNotes(query)
//                _searchResultStatus.postValue(result)
//            }
//        }
//    }
//
//    fun addNote(context: Context, note: Note) {
//        viewModelScope.launch(Dispatchers.IO) {
//            _noteAddResultStatus.postValue(ResultStatus.Loading)
//            val result = noteRepository.addNote(context, note)
//            _noteAddResultStatus.postValue(result)
//            if (result is ResultStatus.Success) {
//                delay(500) // Chờ Firestore đồng bộ
//                listNotes(isFinished = false) // Làm mới danh sách ghi chú
//            }
//        }
//    }
//
//    fun updateNote(context: Context, note: Note) {
//        viewModelScope.launch(Dispatchers.IO) {
//            _noteUpdateResultStatus.postValue(ResultStatus.Loading)
//            val result = noteRepository.updateNote(context, note)
//            _noteUpdateResultStatus.postValue(result)
//            if (result is ResultStatus.Success) {
//                delay(500) // Chờ Firestore đồng bộ
//                listNotes(isFinished = false) // Làm mới danh sách ghi chú
//            }
//        }
//    }
//
//    fun deleteNote(noteId: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val result = noteRepository.deleteNote(noteId)
//            _noteDeleteStatus.postValue(result)
//            if (result is EmptyResult.Success) {
//                delay(500) // Chờ Firestore đồng bộ
//                listNotes(isFinished = false) // Làm mới danh sách ghi chú
//            }
//        }
//    }
//
//    fun resetNoteStatus() {
//        _noteAddResultStatus.postValue(null)
//        _noteUpdateResultStatus.postValue(null)
//    }
//
//    fun resetNoteDeleteStatus() {
//        _noteDeleteStatus.postValue(null)
//    }
//}


package com.example.noteapp.ui.view_models

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.models.Note
import com.example.noteapp.data.repositories.NoteRepository
import com.example.noteapp.utils.response.EmptyResult
import com.example.noteapp.utils.response.ResultStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NoteViewModel : ViewModel() {
    private val noteRepository: NoteRepository by lazy { NoteRepository() }

    private val _notesResultStatus = MutableLiveData<ResultStatus<MutableList<Note>>>()
    val notesResultStatus: LiveData<ResultStatus<MutableList<Note>>> = _notesResultStatus

    private val _searchResultStatus = MutableLiveData<ResultStatus<MutableList<Note>>>()
    val searchResultStatus: LiveData<ResultStatus<MutableList<Note>>> = _searchResultStatus

    private val _noteAddResultStatus = MutableLiveData<ResultStatus<Note>?>()
    val noteAddResultStatus: LiveData<ResultStatus<Note>?> = _noteAddResultStatus

    private val _noteUpdateResultStatus = MutableLiveData<ResultStatus<Note>?>()
    val noteUpdateResultStatus: LiveData<ResultStatus<Note>?> = _noteUpdateResultStatus

    private val _noteDeleteStatus = MutableLiveData<EmptyResult?>()
    val noteDeleteStatus: LiveData<EmptyResult?> = _noteDeleteStatus

    fun listNotes(isFinished: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _notesResultStatus.postValue(ResultStatus.Loading)
            val result = noteRepository.listNotes(isFinished)
            _notesResultStatus.postValue(result)
        }
    }

    fun searchNotes(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (query.isBlank()) {
                _searchResultStatus.postValue(ResultStatus.Success(mutableListOf()))
            } else {
                _searchResultStatus.postValue(ResultStatus.Loading)
                val result = noteRepository.searchNotes(query)
                _searchResultStatus.postValue(result)
            }
        }
    }

    fun addNote(context: Context, note: Note, imageUri: Uri? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _noteAddResultStatus.postValue(ResultStatus.Loading)
            val result = noteRepository.addNote(context, note, imageUri)
            _noteAddResultStatus.postValue(result)
            if (result is ResultStatus.Success) {
                delay(500) // Chờ Firestore đồng bộ
                listNotes(isFinished = false) // Làm mới danh sách ghi chú
            }
        }
    }

    fun updateNote(context: Context, note: Note, imageUri: Uri? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _noteUpdateResultStatus.postValue(ResultStatus.Loading)
            val result = noteRepository.updateNote(context, note, imageUri)
            _noteUpdateResultStatus.postValue(result)
            if (result is ResultStatus.Success) {
                delay(500) // Chờ Firestore đồng bộ
                listNotes(isFinished = false) // Làm mới danh sách ghi chú
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = noteRepository.deleteNote(noteId)
            _noteDeleteStatus.postValue(result)
            if (result is EmptyResult.Success) {
                delay(500) // Chờ Firestore đồng bộ
                listNotes(isFinished = false) // Làm mới danh sách ghi chú
            }
        }
    }

    fun resetNoteStatus() {
        _noteAddResultStatus.postValue(null)
        _noteUpdateResultStatus.postValue(null)
    }

    fun resetNoteDeleteStatus() {
        _noteDeleteStatus.postValue(null)
    }
}