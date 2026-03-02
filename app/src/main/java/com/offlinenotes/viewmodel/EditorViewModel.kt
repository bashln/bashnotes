package com.offlinenotes.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.offlinenotes.data.NotesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChecklistLine(
    val index: Int,
    val checked: Boolean,
    val text: String
)

data class EditorUiState(
    val uri: Uri,
    val title: String,
    val text: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val checklistLines: List<ChecklistLine> = emptyList()
)

sealed interface EditorEvent {
    data class ShowMessage(val message: String) : EditorEvent
}

class EditorViewModel(
    application: Application,
    noteUri: Uri
) : AndroidViewModel(application) {
    private val notesRepository = NotesRepository(application)

    private val _uiState = MutableStateFlow(
        EditorUiState(
            uri = noteUri,
            title = noteUri.lastPathSegment?.substringAfterLast('/') ?: "Nota"
        )
    )
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditorEvent>()
    val events: SharedFlow<EditorEvent> = _events.asSharedFlow()

    init {
        load()
    }

    fun onTextChanged(value: String) {
        _uiState.update {
            it.copy(
                text = value,
                checklistLines = extractChecklistLines(value)
            )
        }
    }

    fun toggleChecklistLine(lineIndex: Int) {
        val lines = _uiState.value.text.split("\n").toMutableList()
        if (lineIndex !in lines.indices) {
            return
        }

        val current = lines[lineIndex]
        val toggled = when {
            current.startsWith("- [ ]") -> current.replaceFirst("- [ ]", "- [x]")
            current.startsWith("- [x]") -> current.replaceFirst("- [x]", "- [ ]")
            current.startsWith("- [X]") -> current.replaceFirst("- [X]", "- [ ]")
            else -> current
        }
        lines[lineIndex] = toggled
        onTextChanged(lines.joinToString("\n"))
    }

    fun save(showFeedback: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            notesRepository.writeNote(_uiState.value.uri, _uiState.value.text)
                .onSuccess {
                    if (showFeedback) {
                        _events.emit(EditorEvent.ShowMessage("Nota salva"))
                    }
                }
                .onFailure {
                    _events.emit(EditorEvent.ShowMessage(it.message ?: "Falha ao salvar"))
                }
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    fun saveSilently() {
        save(showFeedback = false)
    }

    private fun load() {
        viewModelScope.launch {
            notesRepository.readNote(_uiState.value.uri)
                .onSuccess { content ->
                    _uiState.update {
                        it.copy(
                            text = content,
                            isLoading = false,
                            checklistLines = extractChecklistLines(content)
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(EditorEvent.ShowMessage(it.message ?: "Erro ao abrir nota"))
                }
        }
    }

    private fun extractChecklistLines(text: String): List<ChecklistLine> {
        return text.lines().mapIndexedNotNull { index, line ->
            when {
                line.startsWith("- [ ]") -> ChecklistLine(index = index, checked = false, text = line.removePrefix("- [ ]").trim())
                line.startsWith("- [x]") -> ChecklistLine(index = index, checked = true, text = line.removePrefix("- [x]").trim())
                line.startsWith("- [X]") -> ChecklistLine(index = index, checked = true, text = line.removePrefix("- [X]").trim())
                else -> null
            }
        }
    }

    companion object {
        fun factory(application: Application, noteUri: Uri): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return EditorViewModel(application, noteUri) as T
                }
            }
        }
    }
}
