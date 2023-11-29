package com.mobdev20.nhom09.quicknote.repositories.implementations

import com.mobdev20.nhom09.quicknote.datasources.NoteDataStore
import com.mobdev20.nhom09.quicknote.datasources.StorageDatasource
import com.mobdev20.nhom09.quicknote.datasources.StorageDatasourceImpl
import com.mobdev20.nhom09.quicknote.helpers.Encoder
import com.mobdev20.nhom09.quicknote.repositories.NoteSave
import com.mobdev20.nhom09.quicknote.state.NoteState
import com.mobdev20.nhom09.quicknote.viewmodels.EditorViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import javax.inject.Inject

class NoteSaveImpl @Inject constructor(): NoteSave {
    @Inject lateinit var noteDataStore: NoteDataStore

//    @Inject
//    lateinit var editorViewModel : EditorViewModel
//
//    @Inject
//    lateinit var storageDataSource : StorageDatasource
    override suspend fun update(noteState: NoteState) {
        val model = """
            {
                "id": "${noteState.id}",
                "title": "${Encoder.encode(noteState.title)}",
                "content": "${Encoder.encode(noteState.content)}"
            }
        """.trimIndent()
        noteDataStore.writeTo(noteState.id, model)
    }

//    tra ve cho viewModel
//    override fun createFile(file: File): String {
//        return storageDataSource.compressAndSaveFile(file)
//    }

    override fun loadNote(id: String): Flow<NoteState?> {
        val noteFlow = noteDataStore.readFrom(id)
        return noteFlow.map { value ->
            if (value == null || value.isEmpty()) {
                null
            } else {
                val model = Json.parseToJsonElement(value.toString()).jsonObject
                NoteState(model["id"]?.jsonPrimitive?.content ?: "", Encoder.decode(model["title"].toString()), Encoder.decode(
                    model["content"].toString()))
            }
        }
    }

    override fun loadListNote(vararg criteria: String): StateFlow<List<NoteState>> {
        TODO("Not yet implemented")
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NoteSaveModule {
    @Binds
    abstract fun bindNoteSave(noteSaveImpl: NoteSaveImpl): NoteSave
}