package com.example.quicknotes.ui

import android.app.Application
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Query
import androidx.room.util.query
import com.example.quicknotes.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val query: String = "",
    val loading: Boolean = false
)

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: NoteRepository
    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private var allNotesFlow: Flow<List<Note>>

    init {
        val dao = NoteDatabase.getInstance(application).noteDao()
        repo = NoteRepository(dao)
        allNotesFlow =  repo.getAll()
        subscribeAll()
    }

    private fun subscribeAll() {
        viewModelScope.launch {
            allNotesFlow.collectLatest { list ->
                _uiState.update { it.copy(notes = list, loading = false) }
            }
        }
    }

    fun setQuery(q: String){
        _uiState.update { it.copy(query = q, loading = true) }
        viewModelScope.launch {
            if(q.isBlank()) {
                // Already collecting all notes via flow; no extra action needed
                // But set loading false to reflect UI
                _uiState.update { it.copy(loading = false) }
            } else {
                repo.search(q).collectLatest { list ->
                    _uiState.update { it.copy(notes = list, loading = false) }
                }
            }
        }
    }

    fun addNote(title: String,content: String, onComplete: ((Long) -> UInt)? = null) {
        viewModelScope.launch {
            val id = repo.insert(Note(title = title, content = content))
            onComplete?.invoke(id)
        }
    }

    fun updateNote(note: Note, onComplete: (() -> UInt)? = null){
        viewModelScope.launch {
            repo.update(note)
            onComplete?.invoke()
        }
    }

    fun deleteNote(note: Note, onComplete: (() -> UInt)? = null){
        viewModelScope.launch {
            repo.delete(note)
            onComplete?.invoke()
        }
    }
}