package com.mobdev20.nhom09.quicknote.viewmodels

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mobdev20.nhom09.quicknote.datasources.StorageDatasource
import com.mobdev20.nhom09.quicknote.helpers.Uuid
import com.mobdev20.nhom09.quicknote.repositories.BackupNote
import com.mobdev20.nhom09.quicknote.repositories.NoteSave
import com.mobdev20.nhom09.quicknote.repositories.UserSave
import com.mobdev20.nhom09.quicknote.state.Attachment
import com.mobdev20.nhom09.quicknote.state.HistoryType
import com.mobdev20.nhom09.quicknote.state.NoteHistory
import com.mobdev20.nhom09.quicknote.state.NoteOverview
import com.mobdev20.nhom09.quicknote.state.NoteState
import com.mobdev20.nhom09.quicknote.state.UserState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

//@HiltViewModel
class EditorViewModel @Inject constructor() : ViewModel() {
    private val _noteState = MutableStateFlow(NoteState())
    val noteState: StateFlow<NoteState> = _noteState.asStateFlow()

    private val _userState = MutableStateFlow(UserState())
    fun updateUser(auth: FirebaseAuth) {
        _userState.value = userSaveRepository.loadInfo(auth)
    }

    @Inject
    lateinit var noteSaveRepository: NoteSave

    @Inject
    lateinit var userSaveRepository: UserSave

    @Inject
    lateinit var backupNote: BackupNote

    @Inject
    lateinit var storageDatasource: StorageDatasource

    private val stateSave = AtomicBoolean(false)

    val load = mutableStateOf(false)
    val redoHistory = mutableListOf<NoteHistory>()
    val currentReverseHistory: MutableState<NoteHistory?> = mutableStateOf(null)
    private var oldCursorPosition = 0;
    val redoEnabled = mutableStateOf(false)

    private val _noteList = mutableStateListOf<NoteOverview>()
    val noteList: SnapshotStateList<NoteOverview> = _noteList

    val currentAttachment = mutableStateListOf<Attachment>()
    fun loadAttachment() {
        currentAttachment.clear()
        _noteState.value.attachments.forEach { attachment ->
            if (attachment.isNotEmpty()) {
                val flow = storageDatasource.getFileFromInternal(attachment)
                viewModelScope.launch {
                    flow.collect {
                        if (it != null) {
                            currentAttachment.add(
                                Attachment(
                                    attachment, BitmapFactory.decodeFile(it.absolutePath)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun deloadAttachment() {
        currentAttachment.forEach {
            viewModelScope.launch {
                storageDatasource.deloadFile(it.filepath)
            }
        }
    }

    fun deleteAttachment(attachment: Attachment) {
        viewModelScope.launch {
            currentAttachment.remove(attachment)
            _noteState.value.attachments.removeIf {
                it == attachment.filepath
            }
            saveNoteAfterDelay()
            storageDatasource.deleteFile(attachment.filepath)
        }
    }

    fun addAttachment(file: File) {
        if (_noteState.value.id.isEmpty()) createNote()
        val flow = storageDatasource.getFileFromInternal(file.absolutePath)
        viewModelScope.launch {
            flow.collect {
                if (it != null) {
                    currentAttachment.add(
                        Attachment(
                            file.absolutePath, BitmapFactory.decodeFile(it.absolutePath)
                        )
                    )
                }
            }
        }
        _noteState.value.attachments.add(file.absolutePath)
        saveNoteAfterDelay()
    }

    fun deleteNote() {
        viewModelScope.launch {
            _noteList.removeIf {
                it.id == noteState.value.id
            }
            noteSaveRepository.delete(noteState.value.id)
            clearState()
        }
    }

    fun loadNoteList() {
        _noteList.clear()
        val flow = noteSaveRepository.loadListNote()
        viewModelScope.launch {
            flow.collect {
                it.forEach { create ->
                    if (create != null) {
                        _noteList.add(create)
                    }
                }
            }
        }
    }

    fun createNote() {
        if (!stateSave.get()) {
            stateSave.set(true)
            _noteState.update {
                it.copy(
                    id = Uuid.generateType1UUID(),
                    title = it.title.ifEmpty { "Untitled Note" },
                    userId = _userState.value.id
                )
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
            load.value = false
            viewModelScope.launch {
                _noteState.update {
                    it.copy(
                        timeUpdate = Instant.now()
                    )
                }
                noteSaveRepository.update(_noteState.value)
                stateSave.set(false)
            }
        }
    }

    fun backupNote() {
        viewModelScope.launch {
            backupNote.backup(_noteState.value.id)
        }
    }

    fun restoreNote() {
        viewModelScope.launch {
            backupNote.restore(_noteState.value.id)
            selectNoteToLoad(_noteState.value.id)
        }
    }

    fun editTitle(newTitle: String) {
//        if (newTitle.isEmpty() && _noteState.value.title.isNotEmpty()) {
//            _clearState()
//        } else {
//            _noteState.update {
//                it.copy(title = newTitle)
//            }
//            val flow = noteSaveRepository.loadNote(newTitle)
//            viewModelScope.launch {
//                flow.collect { col ->
//                    if (col != null)
//                        if (_noteState.value.title == col.id)
//                            _noteState.update {
//                                load.value = true
//                                col
//                            }
//                }
//            }
//        }
        _noteState.value.history.add(
            NoteHistory(
                line = 0,
                type = HistoryType.EDIT,
                userId = _userState.value.id,
                contentOld = _noteState.value.title,
                contentNew = newTitle
            )
        )
        _noteState.update {
            it.copy(title = newTitle)
        }
        if (_noteState.value.id.isNotEmpty()) saveNoteAfterDelay()
    }

    fun selectNoteToLoad(noteId: String) {
        val flow = noteSaveRepository.loadNote(noteId)
        val scope = viewModelScope.launch {
            flow.cancellable().collect { col ->
                if (col != null) {
                    _noteState.update {
                        load.value = true
                        col
                    }
                    loadAttachment()
                    return@collect
                }
            }
        }
    }

    fun editBody(newBody: String, currentCursorPosition: Int) {
        addHistory(_noteState.value.content, newBody, currentCursorPosition, oldCursorPosition)
        _noteState.update {
            it.copy(content = newBody)
        }
        saveNoteAfterDelay()
        oldCursorPosition = currentCursorPosition
    }

    fun addHistory(oldContent: String, newContent: String, currentPos: Int, oldPos: Int) {
        val oldSplit = oldContent.split("\n")
        val newSplit = newContent.split("\n")
        var oldI = 0
        var newI = 0
        val maxI = maxOf(oldSplit.size, newSplit.size)
        val histories = mutableListOf<NoteHistory>()

        while (oldI < maxI && newI < maxI) {
            if (oldI == oldSplit.size) {
                val history = NoteHistory(
                    contentNew = newSplit[newI],
                    type = HistoryType.ADD,
                    line = newI + 1,
                    userId = _userState.value.id
                )
                histories.add(history)
                newI++
            } else if (newI == newSplit.size) {
                val history = NoteHistory(
                    contentOld = oldSplit[oldI],
                    type = HistoryType.DELETE,
                    line = oldI + 1,
                    userId = _userState.value.id
                )
                histories.add(history)
                oldI++
            } else {
                if (oldSplit[oldI].compareTo(newSplit[newI]) != 0) {
                    if (oldI + 1 < oldSplit.size && oldSplit[oldI + 1].compareTo(newSplit[newI]) == 0) {
                        val history = NoteHistory(
                            contentOld = oldSplit[oldI],
                            type = HistoryType.DELETE,
                            line = oldI + 1,
                            userId = _userState.value.id
                        )
                        histories.add(history)
                        newI++
                        oldI += 2
                    } else if (newI + 1 < newSplit.size && newSplit[newI + 1].compareTo(oldSplit[oldI]) == 0) {
                        val history = NoteHistory(
                            contentNew = newSplit[newI],
                            type = HistoryType.ADD,
                            line = newI + 1,
                            userId = _userState.value.id
                        )
                        histories.add(history)
                        oldI++
                        newI += 2
                    } else {
                        val history = NoteHistory(
                            line = newI + 1,
                            type = HistoryType.EDIT,
                            contentOld = oldSplit[oldI],
                            contentNew = newSplit[newI],
                            userId = _userState.value.id
                        )
                        histories.add(history)
                        oldI++
                        newI++
                    }
                } else {
                    oldI++
                    newI++
                }
            }
        }
        val last = histories.lastOrNull()
        if (last != null && last.type == HistoryType.DELETE) {
            histories.reverse()
        }
        _noteState.value.history.addAll(histories)
    }

    fun reverseHistory() {
        if (_noteState.value.history.isEmpty()) {
            return
        }
        load.value = true
        val history = _noteState.value.history.removeLast()
        currentReverseHistory.value = history
        redoHistory.add(history)
        val newSplit = _noteState.value.content.split("\n").toMutableList()
        if (history.type == HistoryType.ADD) {
            newSplit.removeAt(history.line - 1)
        } else if (history.type == HistoryType.DELETE) {
            newSplit.add(history.line - 1, history.contentOld)
        } else {
            if (history.line == 0) {
                _noteState.update {
                    it.copy(title = history.contentOld)
                }
                redoEnabled.value = true
                return
            }
            newSplit[history.line - 1] = history.contentOld
        }
        redoEnabled.value = true
        _noteState.update {
            it.copy(content = newSplit.joinToString("\n"))
        }
        saveNoteAfterDelay()
    }

    fun replayHistory(history: NoteHistory) {
        load.value = true
        currentReverseHistory.value = history
        _noteState.value.history.add(history)
        val newSplit = _noteState.value.content.split("\n").toMutableList()
        if (history.type == HistoryType.ADD) {
            newSplit.add(history.line - 1, history.contentNew)
        } else if (history.type == HistoryType.DELETE) {
            newSplit.removeAt(history.line - 1)
        } else {
            if (history.line == 0) {
                _noteState.update {
                    it.copy(title = history.contentNew)
                }
                if (redoHistory.isEmpty()) redoEnabled.value = false
                return
            }
            newSplit[history.line - 1] = history.contentNew
        }
        if (redoHistory.isEmpty()) redoEnabled.value = false
        _noteState.update {
            it.copy(content = newSplit.joinToString("\n"))
        }
        saveNoteAfterDelay()
    }

    fun clearState() {
        load.value = true
        _noteState.value = NoteState()
        deloadAttachment()
        currentAttachment.clear()
        currentReverseHistory.value = null
        redoHistory.clear()
    }
}