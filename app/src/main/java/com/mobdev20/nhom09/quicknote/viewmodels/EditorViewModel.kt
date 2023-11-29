package com.mobdev20.nhom09.quicknote.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobdev20.nhom09.quicknote.MainActivity
import com.mobdev20.nhom09.quicknote.datasources.StorageDatasource
import com.mobdev20.nhom09.quicknote.helpers.Uuid
import com.mobdev20.nhom09.quicknote.repositories.NoteSave
import com.mobdev20.nhom09.quicknote.state.NoteState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

//@HiltViewModel
class EditorViewModel @Inject constructor(
//    private val activityResultContractor: ChooseAttachment
    @ApplicationContext private val context: Context
) : ViewModel(), ActivityResultCallback<Bitmap?> {
    private val _noteState = MutableStateFlow(NoteState())
    val noteState: StateFlow<NoteState> = _noteState.asStateFlow()

    @Inject
    lateinit var noteSaveRepository: NoteSave
    private val stateSave = AtomicBoolean(false)

    lateinit var storageDatasource : StorageDatasource

    var load = mutableStateOf(false)

    fun createNote() {
        if (!stateSave.get()) {
            stateSave.set(true)
            _noteState.update {
                it.copy(id = Uuid.generateType1UUID())
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
                delay(5000)
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
                            _noteState.update {9
                                load.value = true
                                col
                            }
                }
            }
        }
    }

    fun editBody(newBody: String) {
        val body = if (newBody.isEmpty()) newBody else newBody.subSequence(0, newBody.length - 1).toString()
        Log.d("CURRENT_STATE", body)
        _noteState.update {
            it.copy(content = body)
        }
    }

    private fun _clearState() {
        _noteState.update {
            load.value = true
            NoteState()
        }
    }

    override fun onActivityResult(result: Bitmap?) {
        TODO("Not yet implemented")
    }

//    Function to save as bitmap and return the path to zip file
    fun saveAsBitmap(bitmap : Bitmap) : String {
        val myFile: File = File(
            (context.filesDir.absolutePath + "/" + Uuid.generateType1UUID()).toString() + ".jpg"
        )
        val created = myFile.createNewFile()

        val fos = FileOutputStream(myFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()
        return storageDatasource.compressAndSaveFile(myFile);
    }


}