package com.offlinenotes.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.offlinenotes.data.NotesRepository
import com.offlinenotes.data.SettingsRepository
import com.offlinenotes.domain.NoteKind
import com.offlinenotes.domain.NoteMeta
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotesListUiState(
    val rootUri: Uri? = null,
    val isLoading: Boolean = true,
    val query: String = "",
    val notes: List<NoteMeta> = emptyList(),
    val errorMessage: String? = null
)

sealed interface NotesListEvent {
    data class OpenEditor(val noteUri: Uri) : NotesListEvent
    data class ShowMessage(val message: String) : NotesListEvent
}

class NotesListViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)
    private val notesRepository = NotesRepository(application)

    private val _uiState = MutableStateFlow(NotesListUiState())
    val uiState: StateFlow<NotesListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<NotesListEvent>()
    val events: SharedFlow<NotesListEvent> = _events.asSharedFlow()

    init {
        observeRootFolder()
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
        refreshNotes()
    }

    fun onFolderSelected(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val flags =
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                getApplication<Application>().contentResolver.takePersistableUriPermission(uri, flags)
                settingsRepository.saveRootUri(uri)
            }.onFailure {
                _events.emit(NotesListEvent.ShowMessage("Nao foi possivel salvar a pasta"))
            }
        }
    }

    fun createNote(name: String, kind: NoteKind) {
        val rootUri = _uiState.value.rootUri ?: return
        viewModelScope.launch {
            notesRepository.createNote(rootUri, name, kind)
                .onSuccess { createdUri ->
                    if (kind == NoteKind.MARKDOWN_TASKS) {
                        val content = "- [ ] Item 1\n- [ ] Item 2\n"
                        notesRepository.writeNote(createdUri, content)
                    }
                    refreshNotes()
                    _events.emit(NotesListEvent.OpenEditor(createdUri))
                }
                .onFailure {
                    _events.emit(NotesListEvent.ShowMessage(it.message ?: "Falha ao criar nota"))
                }
        }
    }

    fun openNote(noteMeta: NoteMeta) {
        viewModelScope.launch {
            _events.emit(NotesListEvent.OpenEditor(noteMeta.uri))
        }
    }

    fun renameNote(noteMeta: NoteMeta, newName: String) {
        viewModelScope.launch {
            notesRepository.renameNote(noteMeta.uri, newName.trim())
                .onSuccess {
                    refreshNotes()
                }
                .onFailure {
                    _events.emit(NotesListEvent.ShowMessage(it.message ?: "Falha ao renomear"))
                }
        }
    }

    fun deleteNote(noteMeta: NoteMeta) {
        viewModelScope.launch {
            notesRepository.deleteNote(noteMeta.uri)
                .onSuccess {
                    refreshNotes()
                }
                .onFailure {
                    _events.emit(NotesListEvent.ShowMessage(it.message ?: "Falha ao deletar"))
                }
        }
    }

    fun suggestedName(): String {
        val now = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
        return "nota-$now"
    }

    fun refreshNotes() {
        val rootUri = _uiState.value.rootUri ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                notesRepository.listNotes(rootUri, _uiState.value.query)
            }.onSuccess { list ->
                _uiState.update { it.copy(notes = list, isLoading = false) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao listar notas"
                    )
                }
            }
        }
    }

    private fun observeRootFolder() {
        viewModelScope.launch {
            settingsRepository.rootUriFlow.collectLatest { uri ->
                _uiState.update { it.copy(rootUri = uri) }
                if (uri != null) {
                    refreshNotes()
                } else {
                    _uiState.update { it.copy(isLoading = false, notes = emptyList()) }
                }
            }
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return NotesListViewModel(application) as T
                }
            }
        }
    }
}
