package com.mobdev20.nhom09.quicknote.viewmodels

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.PeriodicWorkRequest
import com.google.firebase.auth.FirebaseAuth
import com.mobdev20.nhom09.quicknote.datasources.FirebaseNote
import com.mobdev20.nhom09.quicknote.datasources.StorageDatasource
import com.mobdev20.nhom09.quicknote.helpers.Uuid
import com.mobdev20.nhom09.quicknote.repositories.AlarmScheduler
import com.mobdev20.nhom09.quicknote.repositories.BackupNote
import com.mobdev20.nhom09.quicknote.repositories.NoteSave
import com.mobdev20.nhom09.quicknote.repositories.UserSave
import com.mobdev20.nhom09.quicknote.state.Attachment
import com.mobdev20.nhom09.quicknote.state.HistoryType
import com.mobdev20.nhom09.quicknote.state.NoteHistory
import com.mobdev20.nhom09.quicknote.state.NoteOverview
import com.mobdev20.nhom09.quicknote.state.NoteState
import com.mobdev20.nhom09.quicknote.state.UserState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


//@HiltViewModel
class EditorViewModel @Inject constructor(@ApplicationContext private val context: Context) :
    ViewModel() {
    private val _noteState = MutableStateFlow(NoteState())
    val noteState: StateFlow<NoteState> = _noteState.asStateFlow()

    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState> = _userState.asStateFlow()
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

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var firebase: FirebaseNote

    private val stateSave = AtomicBoolean(false)

    val load = mutableStateOf(false)
    val redoHistory = mutableListOf<NoteHistory>()
    val currentReverseHistory: MutableState<NoteHistory?> = mutableStateOf(null)
    private var oldCursorPosition = 0;
    val redoEnabled = mutableStateOf(false)

    private val _noteList = mutableStateListOf<NoteOverview>()
    val noteList: SnapshotStateList<NoteOverview> = _noteList

    val currentAttachment = mutableStateListOf<Attachment>()
    private var saveWorker: PeriodicWorkRequest? = null
    private var scopes: MutableList<Job?> = mutableListOf()

    val instant = mutableStateOf(Instant.now())
    val hourOfDay = mutableStateOf(1)
    val minute = mutableStateOf(1)
    val isId = mutableStateOf(false)

    fun combineInstant(): Instant {
        val formatter = DateTimeFormatter.ofPattern("HH:mm yyyy-MM-dd")
        val instantParse =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
        Log.d("TIME_PARSE", instantParse.format(instant.value))
        val localTime = LocalDateTime.parse(
            "${if (hourOfDay.value >= 10) hourOfDay.value else "0" + hourOfDay.value}:${if (minute.value >= 10) minute.value else "0" + minute.value} ${
                instantParse.format(instant.value)
            }", formatter
        )
        return localTime.atZone(ZoneId.systemDefault()).toInstant()
    }

    fun addNotification() {
        val title = if (_noteState.value.title.length > 60) _noteState.value.title.substring(
            0,
            60
        ) + "..." else _noteState.value.title
        val content = if (_noteState.value.content.length > 224) _noteState.value.content.substring(
            0,
            224
        ) + "..." else _noteState.value.content
        val time = combineInstant()
        val alarmId = alarmScheduler.schedule(time, title, content, _noteState.value.id)
        _noteState.update {
            it.copy(notificationId = alarmId.toString(), notificationTime = time)
        }
    }

    fun deleteNotification() {
        alarmScheduler.cancel(noteState.value.notificationId.toInt())
        _noteState.update {
            it.copy(notificationId = "", notificationTime = Instant.now())
        }
    }

    fun loadAttachment() {
        currentAttachment.clear()
        for (i: Long in 0 until _noteState.value.attachmentCount) {
            val attachment = _noteState.value.attachments[i.toInt()]
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
            _noteState.update {
                it.copy(attachmentCount = it.attachmentCount - 1)
            }
            saveNoteNow()
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
        _noteState.update {
            it.copy(attachmentCount = it.attachmentCount + 1)
        }
        saveNoteNow()
    }

    fun deleteNote() {
        viewModelScope.launch {
            _noteList.removeIf {
                it.id == noteState.value.id
            }
            currentAttachment.forEach {
                deleteAttachment(it)
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

    private fun saveNoteAfterDelay() {
        if (_noteState.value.id.isEmpty()) {
            createNote()
            return
        }
        if (!stateSave.get()) {
            stateSave.set(true)
            load.value = false
            viewModelScope.launch {
                delay(5000)
                _noteState.update {
                    it.copy(
                        timeUpdate = Instant.now()
                    )
                }
                noteSaveRepository.update(_noteState.value)
                stateSave.set(false)
            }
        }
//        if (_noteState.value.id.isEmpty()) {
//            createNote()
//            return
//        }
//        viewModelScope.launch() {
//            var id: UUID? = null
//            if (saveWorker != null) {
//                id = saveWorker!!.id
//            }
//            val file = File.createTempFile("notetemp", _noteState.value.id, context.cacheDir)
//            file.deleteOnExit()
//            try {
//                val outputStreamWriter =
//                    OutputStreamWriter(file.outputStream())
//                outputStreamWriter.write(NoteJson.convertModel(_noteState.value))
//                outputStreamWriter.close()
//            } catch (e: IOException) {
//                Log.e("Exception", "File write failed: $e")
//            }
//            val saveWork =
//                PeriodicWorkRequestBuilder<SaveWorker>(Duration.ofSeconds(5L)).setInputData(
//                    workDataOf(
//                        "note" to file.absolutePath,
//                        "noteId" to _noteState.value.id,
//                    )
//                )
//            if (id != null) {
//                saveWork.setId(id)
//            }
//            saveWorker = saveWork.build()
//            if (id != null) {
//                WorkManager.getInstance(context).updateWork(saveWorker!!)
//            } else {
//                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//                    "save_worker",
//                    ExistingPeriodicWorkPolicy.UPDATE,
//                    saveWorker!!
//                )
//            }
//        }
    }

    fun saveNoteNow() {
        if (_noteState.value.id.isEmpty()) {
            createNote()
            return
        }
//        WorkManager.getInstance(context).cancelUniqueWork("save_worker")
        stateSave.set(true)
        load.value = true
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

    fun backupNote() {
        viewModelScope.launch {
            _noteState.update {
                it.copy(
                    timeUpdate = Instant.now()
                )
            }
            saveNoteNow()
            backupNote.backup(_noteState.value.id)
        }
    }

    fun restoreNote(
    ) {
        _noteState.update {
            it.copy(
                timeRestore = Instant.now()
            )
        }
        if (!isId.value) saveNoteNow()
        else selectNoteToLoad(_noteState.value.id)
        val scopes = viewModelScope.launch {
            var noteState: NoteState? = null
            val job = async {
                noteState = firebase.restore(_noteState.value.id)
            }
            job.await()
            val flow = backupNote.restore(_noteState.value.id, noteState)
            flow.cancellable().collect {
                if (it != null) {
                    if (it.first == -1) {
                        selectNoteToLoad(_noteState.value.id)
                        return@collect
                    }
                    for (i: Int in _noteState.value.history.size - 1 downTo it.first) {
                        reverseHistory()
                    }
                    it.second.forEach { history ->
                        replayHistory(history)
                    }
                    _noteState.update { current ->
                        current.copy(
                            timeRestore = Instant.now()
                        )
                    }
                    saveNoteNow()
                }
            }
        }
        this.scopes.add(scopes)
    }

    fun editTitle(newTitle: String) {
        isId.value = false
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
        try {
            val uuid = UUID.fromString(_noteState.value.title)
            if (uuid.version() == 1) {
                _noteState.update {
                    it.copy(id = uuid.toString())
                }
                isId.value = true
                restoreNote()
            }
        } catch (e: Exception) {
            Log.d("FINDING_NOTE", e.message ?: "Failed to fetch")
        }
        if (_noteState.value.id.isNotEmpty()) saveNoteAfterDelay()
    }

    fun selectNoteToLoad(noteId: String) {
        val flow = noteSaveRepository.loadNote(noteId)
        val scopes = viewModelScope.launch {
            flow.cancellable().collect { col ->
                if (col != null) {
                    _noteState.update {
                        load.value = true
                        col
                    }
                    loadAttachment()
                    return@collect
                } else {

                }
            }
        }
        this.scopes.add(scopes)
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
        saveNoteNow()
    }

    fun clearState() {
        load.value = true
        scopes.forEach {
            it?.cancel()
        }
        scopes.clear()
        saveNoteNow()
        _noteState.update {
            NoteState()
        }
        deloadAttachment()
        currentAttachment.clear()
        currentReverseHistory.value = null
        redoHistory.clear()
        loadNoteList()
    }
}