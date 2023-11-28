package com.mobdev20.nhom09.quicknote.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobdev20.nhom09.quicknote.helpers.Uuid
import com.mobdev20.nhom09.quicknote.repositories.NoteSave
import com.mobdev20.nhom09.quicknote.state.HistoryType
import com.mobdev20.nhom09.quicknote.state.NoteHistory
import com.mobdev20.nhom09.quicknote.state.NoteState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

//@HiltViewModel
class EditorViewModel @Inject constructor() : ViewModel() {
    private val _noteState = MutableStateFlow(NoteState())
    val noteState: StateFlow<NoteState> = _noteState.asStateFlow()

    @Inject
    lateinit var noteSaveRepository: NoteSave
    private val stateSave = AtomicBoolean(false)

    val load = mutableStateOf(false)
    val redoHistory = mutableListOf<HistoryType>()

    fun createNote() {
        if (!stateSave.get()) {
            stateSave.set(true)
            _noteState.update {
                it.copy(id = Uuid.generateType1UUID(), title = it.title.ifEmpty { "Untitled Note" })
            }
            viewModelScope.launch {
                noteSaveRepository.update(_noteState.value)
                Log.d("NOTE_ID_TAG", _noteState.value.toString())
                stateSave.set(false)
            }
        }
    }

    fun saveNoteAfterDelay() {
        if (_noteState.value.id.isEmpty()) {
            createNote()
            return
        }
        if (!stateSave.get()) {
            stateSave.set(true)
            viewModelScope.launch {
                delay(2000)
                noteSaveRepository.update(_noteState.value)
                stateSave.set(false)
            }
        }
    }

    fun editTitle(newTitle: String) {
        if (newTitle.isEmpty() && _noteState.value.title.isNotEmpty()) {
            _clearState()
        } else {
            _noteState.update {
                it.copy(title = newTitle)
            }
            val flow = noteSaveRepository.loadNote(newTitle)
            viewModelScope.launch {
                flow.collect { col ->
                    if (col != null)
                        if (_noteState.value.title == col.id)
                            _noteState.update {
                                load.value = true
                                col
                            }
                }
            }
        }
    }

    fun editBody(newBody: String, currentCursorPosition: Int) {
        addHistory(_noteState.value.content, newBody, currentCursorPosition)
        Log.d("CURRENT_STATE", _noteState.value.history.toString())
        _noteState.update {
            it.copy(content = newBody)
        }
    }

    fun addHistory(oldContent: String, newC: String, currentPos: Int) {
        val newContent = newC.subSequence(0, currentPos)
        val oldSplit = oldContent.split("\n")
        val newSplit = newContent.split("\n")
        var oldI =
            if (oldSplit.lastIndex > newSplit.lastIndex) newSplit.lastIndex else oldSplit.lastIndex
        var newI = newSplit.lastIndex
        while (oldI > -1 && newI > -1 && oldSplit[oldI].compareTo(newSplit[newI]) != 0) {
            if (oldI > newI) {
                val history = NoteHistory(
                    contentOld = oldSplit[oldI],
                    type = HistoryType.DELETE,
                    line = oldI + 1
                )
                _noteState.value.history.add(history)
                oldI--
            } else if (oldI < newI) {
                val history = NoteHistory(
                    contentNew = newSplit[newI],
                    type = HistoryType.ADD,
                    line = newI + 1
                )
                _noteState.value.history.add(history)
                newI--
            } else {
                val history = NoteHistory(
                    line = newI + 1,
                    type = HistoryType.EDIT,
                    contentOld = oldSplit[oldI],
                    contentNew = newSplit[newI]
                )
                _noteState.value.history.add(history)
                oldI--
                newI--
            }
        }
    }

    fun reverseHistory() {
        if (_noteState.value.history.isEmpty()) {return}
        load.value = true
        val history = _noteState.value.history.removeLast()
        var newSplit = _noteState.value.content.split("\n").toMutableList()
        if (history.type == HistoryType.ADD) {
            newSplit.removeAt(history.line - 1)
        } else if (history.type == HistoryType.DELETE) {
            newSplit.add(history.line - 1, history.contentOld)
        } else {
            newSplit[history.line - 1] = history.contentOld
        }
        _noteState.update {
            it.copy(content = newSplit.joinToString("\n"))
        }
    }

    private fun _clearState() {
        _noteState.update {
            load.value = true
            NoteState()
        }
    }
}